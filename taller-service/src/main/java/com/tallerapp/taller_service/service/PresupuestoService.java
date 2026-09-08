package com.tallerapp.taller_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.taller_service.dto.ItemPresupuestoDTO;
import com.tallerapp.taller_service.entity.EstadoPresupuesto;
import com.tallerapp.taller_service.entity.ItemPresupuesto;
import com.tallerapp.taller_service.entity.Presupuesto;
import com.tallerapp.taller_service.entity.TipoItem;
import com.tallerapp.taller_service.exception.RecursoNoEncontradoException;
import com.tallerapp.taller_service.exception.ReglaNegocioException;
import com.tallerapp.taller_service.repository.IPresupuestoRepository;

/**
 * Un presupuesto puede ofrecer varias opciones del mismo trabajo.
 *
 * Los repuestos sin opcion van en todas (lo que hay que cambiar si o si) y
 * los que tienen numero entran solo en esa: asi el cliente compara
 * "Opcion 1 con pastillas economicas" contra "Opcion 2 con las originales"
 * viendo el total de cada una, no una lista suelta de precios.
 */
@Service
public class PresupuestoService implements IPresupuestoService {

	@Autowired
	private IPresupuestoRepository presupuestoRepository;

	@Autowired
	private IOrdenTrabajoService ordenServ;

	@Autowired
	private IItemOrdenTrabajoService itemServ;


	@Override
	public String savePresupuesto(Long id_vehiculo, Long id_empleado, Long id_sucursal, String detalleTrabajo,
								  Integer validezDias, BigDecimal horasEstimadas, BigDecimal precioHora,
								  List<ItemPresupuestoDTO> items) {

		int dias = (validezDias == null || validezDias <= 0) ? 15 : validezDias;
		LocalDateTime ahora = LocalDateTime.now();

		Presupuesto presupuesto = new Presupuesto();
		presupuesto.setId_vehiculo(id_vehiculo);
		presupuesto.setId_empleado(id_empleado);
		presupuesto.setId_sucursal(id_sucursal);
		presupuesto.setDetalleTrabajo(detalleTrabajo);
		presupuesto.setFechaEmision(ahora);
		presupuesto.setValidezDias(dias);
		presupuesto.setFechaVencimiento(ahora.plusDays(dias));
		presupuesto.setEstado(EstadoPresupuesto.VIGENTE);
		presupuesto.setHorasEstimadas(horasEstimadas == null ? BigDecimal.ZERO : horasEstimadas);
		presupuesto.setPrecioHora(precioHora == null ? BigDecimal.ZERO : precioHora);
		presupuesto.setItems(new ArrayList<>());

		// los precios que llegan quedan congelados: son los del dia de hoy
		if (items != null) {

			for (ItemPresupuestoDTO dto : items) {

				ItemPresupuesto item = new ItemPresupuesto();
				item.setPresupuesto(presupuesto);
				item.setId_repuesto(dto.getId_repuesto());
				item.setDescripcion(dto.getDescripcion());
				item.setCantidad(dto.getCantidad());
				item.setPrecioUnitario(dto.getPrecioUnitario());
				item.setOpcion(dto.getOpcion());

				presupuesto.getItems().add(item);
			}
		}

		// si hay opciones, arranca marcada la primera
		List<Integer> opciones = this.numerosDeOpcion(presupuesto);

		if (!opciones.isEmpty()) {
			presupuesto.setOpcionElegida(opciones.get(0));
		}

		presupuestoRepository.save(presupuesto);

		return "Presupuesto creado correctamente";
	}

	@Override
	public List<Presupuesto> getPresupuestos() {

		List<Presupuesto> presupuestos = presupuestoRepository.findAll();

		for (Presupuesto p : presupuestos) {
			this.calcular(p);
		}

		return presupuestos;
	}

	@Override
	public Presupuesto findPresupuesto(Long id) {

		Presupuesto presupuesto = presupuestoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el presupuesto con id " + id));

		this.calcular(presupuesto);

		return presupuesto;
	}

	@Override
	public List<Presupuesto> getPresupuestosPorVehiculo(Long id_vehiculo) {

		List<Presupuesto> presupuestos = presupuestoRepository.findByVehiculoId(id_vehiculo);

		for (Presupuesto p : presupuestos) {
			this.calcular(p);
		}

		return presupuestos;
	}

	// El cliente se decidio por una de las opciones
	@Override
	public String elegirOpcion(Long id_presupuesto, Integer opcion) {

		Presupuesto presupuesto = presupuestoRepository.findById(id_presupuesto)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el presupuesto con id " + id_presupuesto));

		if (presupuesto.getEstado() == EstadoPresupuesto.ACEPTADO) {
			throw new ReglaNegocioException("El presupuesto ya se acepto, no se puede cambiar la opcion");
		}

		if (!this.numerosDeOpcion(presupuesto).contains(opcion)) {
			throw new ReglaNegocioException("Este presupuesto no tiene una opcion " + opcion);
		}

		presupuesto.setOpcionElegida(opcion);
		presupuestoRepository.save(presupuesto);

		return "Quedo elegida la opcion " + opcion;
	}

	/**
	 * El cliente acepto: se genera la orden de trabajo con lo presupuestado.
	 * Recien aca se descuenta el stock, porque recien aca el vehiculo
	 * entra al taller.
	 */
	@Override
	public String aceptarPresupuesto(Long id, Long id_mecanico, Long id_empleado) {

		Presupuesto presupuesto = this.findPresupuesto(id);

		if (presupuesto.getEstado() == EstadoPresupuesto.ACEPTADO) {
			throw new ReglaNegocioException("Este presupuesto ya se acepto y genero la orden " + presupuesto.getId_orden());
		}

		if (Boolean.TRUE.equals(presupuesto.getVencido())) {
			throw new ReglaNegocioException("El presupuesto vencio el " + presupuesto.getFechaVencimiento().toLocalDate()
					+ ". Actualiza los precios antes de aceptarlo.");
		}

		// Quien emitio el presupuesto no es necesariamente quien va a hacer
		// el trabajo: un vendedor puede presupuestar algo que despues arregla
		// un mecanico. Si no se indica ninguno, queda el que lo emitio.
		Long mecanico = id_mecanico != null ? id_mecanico : presupuesto.getId_empleado();

		if (mecanico == null) {
			throw new ReglaNegocioException("Indica que mecanico va a hacer el trabajo para poder generar la orden");
		}

		String resultado = ordenServ.saveOrden(presupuesto.getId_vehiculo(),
											   mecanico,
											   presupuesto.getId_sucursal(),
											   presupuesto.getDetalleTrabajo(),
											   null,
											   null,
											   id_empleado);

		if (!resultado.startsWith("Orden de trabajo creada")) {
			return resultado;
		}

		Long id_orden = ordenServ.getOrdenes().stream()
				.map(o -> o.getId_orden())
				.max(Long::compareTo)
				.orElse(null);

		// solo entran los repuestos de la opcion elegida, y ahi si salen
		// del deposito
		StringBuilder avisos = new StringBuilder();

		for (ItemPresupuesto item : this.itemsDeLaOpcion(presupuesto, presupuesto.getOpcionElegida())) {

			String r = itemServ.saveItem(id_orden,
										 item.getId_repuesto() != null ? TipoItem.REPUESTO : TipoItem.MANO_DE_OBRA,
										 item.getId_repuesto(),
										 item.getDescripcion(),
										 item.getCantidad(),
										 item.getPrecioUnitario());

			if (!"Item creado correctamente".equals(r)) {
				avisos.append(" ").append(r);
			}
		}

		// la mano de obra estimada entra como un item mas
		if (presupuesto.getTotalManoObra().compareTo(BigDecimal.ZERO) > 0) {

			itemServ.saveItem(id_orden, TipoItem.MANO_DE_OBRA, null,
					"Mano de obra (" + presupuesto.getHorasEstimadas() + " hs estimadas)",
					1, presupuesto.getTotalManoObra());
		}

		presupuesto.setEstado(EstadoPresupuesto.ACEPTADO);
		presupuesto.setId_orden(id_orden);
		presupuestoRepository.save(presupuesto);

		if (avisos.length() > 0) {
			return "Se genero la orden " + id_orden + ", pero hubo problemas:" + avisos;
		}

		return "Presupuesto aceptado. Se genero la orden de trabajo " + id_orden;
	}

	@Override
	public String rechazarPresupuesto(Long id) {

		Presupuesto presupuesto = presupuestoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el presupuesto con id " + id));

		if (presupuesto.getEstado() == EstadoPresupuesto.ACEPTADO) {
			throw new ReglaNegocioException("No se puede rechazar: ya genero la orden " + presupuesto.getId_orden());
		}

		presupuesto.setEstado(EstadoPresupuesto.RECHAZADO);
		presupuestoRepository.save(presupuesto);

		return "Presupuesto marcado como rechazado";
	}

	/**
	 * Vencido: se estira la validez desde hoy. Los precios de los repuestos
	 * los tiene que revisar quien lo emite, porque el deposito pudo cambiar.
	 */
	@Override
	public String renovarPresupuesto(Long id) {

		Presupuesto presupuesto = presupuestoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el presupuesto con id " + id));

		if (presupuesto.getEstado() == EstadoPresupuesto.ACEPTADO) {
			throw new ReglaNegocioException("Este presupuesto ya se acepto, no se renueva");
		}

		LocalDateTime ahora = LocalDateTime.now();
		int dias = presupuesto.getValidezDias() == null ? 15 : presupuesto.getValidezDias();

		presupuesto.setFechaEmision(ahora);
		presupuesto.setFechaVencimiento(ahora.plusDays(dias));
		presupuesto.setEstado(EstadoPresupuesto.VIGENTE);

		presupuestoRepository.save(presupuesto);

		return "Presupuesto renovado por " + dias + " dias. Revisa que los precios sigan siendo los del deposito.";
	}

	@Override
	public String deletePresupuesto(Long id) {

		Presupuesto presupuesto = presupuestoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el presupuesto con id " + id));

		if (presupuesto.getEstado() == EstadoPresupuesto.ACEPTADO) {
			throw new ReglaNegocioException("No se puede borrar: este presupuesto genero la orden " + presupuesto.getId_orden());
		}

		presupuestoRepository.deleteById(id);

		return "Presupuesto eliminado correctamente";
	}

	// ------------------------------------------------------------------
	// calculos
	// ------------------------------------------------------------------

	// Los numeros de opcion que tiene el presupuesto, ordenados: [1, 2, 3]
	private List<Integer> numerosDeOpcion(Presupuesto presupuesto) {

		if (presupuesto.getItems() == null) {
			return new ArrayList<>();
		}

		return presupuesto.getItems().stream()
				.map(i -> i.getOpcion())
				.filter(o -> o != null)
				.distinct()
				.sorted()
				.toList();
	}

	// Lo que se cobra en una opcion: los repuestos que van siempre
	// mas los propios de esa opcion
	private List<ItemPresupuesto> itemsDeLaOpcion(Presupuesto presupuesto, Integer opcion) {

		List<ItemPresupuesto> van = new ArrayList<>();

		if (presupuesto.getItems() == null) {
			return van;
		}

		for (ItemPresupuesto item : presupuesto.getItems()) {

			if (item.getOpcion() == null || item.getOpcion().equals(opcion)) {
				van.add(item);
			}
		}

		return van;
	}

	private BigDecimal sumar(List<ItemPresupuesto> items) {

		BigDecimal suma = BigDecimal.ZERO;

		for (ItemPresupuesto item : items) {

			if (item.getCantidad() != null && item.getPrecioUnitario() != null) {
				suma = suma.add(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())));
			}
		}

		return suma;
	}

	// Arma el total de cada opcion y marca si el presupuesto vencio.
	// Nada de esto se guarda: se recalcula cada vez que se pide.
	private void calcular(Presupuesto presupuesto) {

		BigDecimal horas = presupuesto.getHorasEstimadas() == null ? BigDecimal.ZERO : presupuesto.getHorasEstimadas();
		BigDecimal precioHora = presupuesto.getPrecioHora() == null ? BigDecimal.ZERO : presupuesto.getPrecioHora();
		BigDecimal manoObra = horas.multiply(precioHora);

		List<Integer> opciones = this.numerosDeOpcion(presupuesto);

		// si la opcion elegida ya no existe (o nunca hubo), se toma la primera
		Integer elegida = presupuesto.getOpcionElegida();

		if (!opciones.isEmpty() && (elegida == null || !opciones.contains(elegida))) {
			elegida = opciones.get(0);
			presupuesto.setOpcionElegida(elegida);
		}

		// cuanto sale cada opcion, ya con los repuestos comunes y la mano de obra
		Map<Integer, BigDecimal> totales = new LinkedHashMap<>();

		for (Integer opcion : opciones) {
			totales.put(opcion, this.sumar(this.itemsDeLaOpcion(presupuesto, opcion)).add(manoObra));
		}

		presupuesto.setTotalesOpciones(totales);

		BigDecimal repuestos = this.sumar(this.itemsDeLaOpcion(presupuesto, elegida));

		presupuesto.setTotalRepuestos(repuestos);
		presupuesto.setTotalManoObra(manoObra);
		presupuesto.setTotal(repuestos.add(manoObra));

		// el vencimiento se calcula al leer, no hace falta una tarea
		// programada que salga a marcarlos todas las noches
		boolean vencio = presupuesto.getFechaVencimiento() != null
				&& presupuesto.getFechaVencimiento().isBefore(LocalDateTime.now());

		presupuesto.setVencido(vencio);

		if (vencio && presupuesto.getEstado() == EstadoPresupuesto.VIGENTE) {
			presupuesto.setEstado(EstadoPresupuesto.VENCIDO);
		}
	}

}
