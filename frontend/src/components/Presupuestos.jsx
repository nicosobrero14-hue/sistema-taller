import { useEffect, useState } from "react";
import { pedir, leerRespuesta, esAdmin } from "../api";
import { confirmar, pedirOpcion } from "../avisos";
import { filtrar } from "../listas";
import { fecha } from "../fechas";
import CompartirPresupuesto from "./CompartirPresupuesto";
import Acciones from "./Acciones";
import { useTabla, Th } from "./Tabla";

function Presupuestos() {
  const [presupuestos, setPresupuestos] = useState([]);
  const [vehiculos, setVehiculos] = useState([]);
  const [repuestos, setRepuestos] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [precioHoraTaller, setPrecioHoraTaller] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [verDetalleDe, setVerDetalleDe] = useState(null);
  const [compartiendo, setCompartiendo] = useState(null);

  // 1) datos del trabajo
  const [idVehiculo, setIdVehiculo] = useState("");
  const [validezDias, setValidezDias] = useState("15");
  const [horas, setHoras] = useState("");
  const [precioHora, setPrecioHora] = useState("");

  // 2) repuestos. opcion null = va en todas; 1, 2, 3... = solo en esa
  const [items, setItems] = useState([]);
  const [cantidadOpciones, setCantidadOpciones] = useState(1);

  // 3) observaciones
  const [detalleTrabajo, setDetalleTrabajo] = useState("");

  function traerPresupuestos() {
    pedir("/presupuestos/traer")
      .then((res) => res.json())
      .then((data) => setPresupuestos(data))
      .catch(() => setPresupuestos([]));
  }

  function traerDatos() {
    pedir("/vehiculos/traer")
      .then((res) => res.json())
      .then((data) => setVehiculos(data))
      .catch(() => setVehiculos([]));

    pedir("/repuestos/traer")
      .then((res) => res.json())
      .then((data) => setRepuestos(data))
      .catch(() => setRepuestos([]));

    pedir("/empleados/traer")
      .then((res) => res.json())
      .then((data) => setEmpleados(data))
      .catch(() => setEmpleados([]));

    // el precio de la hora sale de los datos del taller. Solo lo ve un
    // ADMIN, asi que si no viene se escribe a mano.
    pedir("/configuracion/traer")
      .then((res) => (res.ok ? res.json() : null))
      .then((c) => {
        if (c && c.precioHoraManoObra) {
          setPrecioHoraTaller(c.precioHoraManoObra);
          setPrecioHora(c.precioHoraManoObra);
        }
      })
      .catch(() => setPrecioHoraTaller(""));
  }

  useEffect(() => {
    traerPresupuestos();
    traerDatos();
  }, []);

  // ---------- armado ----------

  // Agrega un repuesto. El precio es el del deposito hoy y queda
  // congelado cuando se guarda el presupuesto.
  function agregarRepuesto(id_repuesto, cantidad, opcion) {
    const repuesto = repuestos.find(
      (r) => String(r.id_repuesto) === String(id_repuesto)
    );

    if (!repuesto) {
      setMensaje("Elegi un repuesto del deposito");
      return;
    }

    setItems([
      ...items,
      {
        id_repuesto: repuesto.id_repuesto,
        descripcion: repuesto.nombre + (repuesto.marca ? " - " + repuesto.marca : ""),
        cantidad: Number(cantidad) || 1,
        precioUnitario: Number(repuesto.precioVenta || 0),
        opcion: opcion,
      },
    ]);
  }

  function sacarItem(indice) {
    setItems(items.filter((x, i) => i !== indice));
  }

  function agregarOpcion() {
    setCantidadOpciones(cantidadOpciones + 1);
  }

  function sacarOpcion(numero) {
    setItems(items.filter((i) => i.opcion !== numero));
    setCantidadOpciones(cantidadOpciones - 1);
  }

  function itemsDe(opcion) {
    return items
      .map((i, indice) => ({ ...i, indice }))
      .filter((i) => i.opcion === opcion);
  }

  function totalManoObra() {
    return Number(horas || 0) * Number(precioHora || 0);
  }

  function totalBase() {
    return itemsDe(null).reduce((s, i) => s + i.cantidad * i.precioUnitario, 0);
  }

  // lo que sale cada opcion: repuestos comunes + los de esa opcion + mano de obra
  function totalOpcion(numero) {
    const propios = itemsDe(numero).reduce(
      (s, i) => s + i.cantidad * i.precioUnitario,
      0
    );

    return totalBase() + propios + totalManoObra();
  }

  const numeros = Array.from({ length: cantidadOpciones }, (v, i) => i + 1);
  const hayOpciones = numeros.some((n) => itemsDe(n).length > 0);

  function crearPresupuesto() {
    if (!idVehiculo) {
      setMensaje("Elegi el vehiculo");
      return;
    }

    if (!detalleTrabajo.trim()) {
      setMensaje("Escribi el trabajo a realizar");
      return;
    }

    if (items.length === 0 && totalManoObra() === 0) {
      setMensaje("Cargale al menos un repuesto o las horas de mano de obra");
      return;
    }

    pedir("/presupuestos/crear", {
      method: "POST",
      body: JSON.stringify({
        id_vehiculo: Number(idVehiculo),
        id_sucursal: 1,
        detalleTrabajo: detalleTrabajo,
        validezDias: Number(validezDias),
        horasEstimadas: Number(horas || 0),
        precioHora: Number(precioHora || 0),
        items: items,
      }),
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);

        if (r.ok) {
          setDetalleTrabajo("");
          setHoras("");
          setItems([]);
          setCantidadOpciones(1);
        }

        traerPresupuestos();
      });
  }

  // ---------- acciones sobre los emitidos ----------

  function elegirOpcion(id_presupuesto, opcion) {
    pedir("/presupuestos/elegir/" + id_presupuesto + "?opcion=" + opcion, {
      method: "PUT",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerPresupuestos();

        pedir("/presupuestos/traer/" + id_presupuesto)
          .then((res) => res.json())
          .then((data) => setVerDetalleDe(data))
          .catch(() => setVerDetalleDe(null));
      });
  }

  // Aceptar genera la orden, asi que hay que decir que mecanico la hace:
  // el que presupuesto no siempre es el que arregla.
  async function aceptar(p) {
    const lista = empleados
      .filter((e) => e.rol === "MECANICO" || e.rol === "ADMIN")
      .map((e) => ({
        valor: e.id_empleado,
        texto: e.nombre + " " + (e.apellido || ""),
      }));

    const elegido = await pedirOpcion({
      titulo: "Aceptar el presupuesto",
      texto:
        "Se genera la orden de trabajo y los repuestos salen del deposito.",
      etiqueta: "Que mecanico va a hacer el trabajo",
      lista: lista,
      boton: "Aceptar y abrir orden",
      tono: "verde",
    });

    if (!elegido) {
      return;
    }

    pedir("/presupuestos/aceptar/" + p.id_presupuesto + "?id_mecanico=" + elegido, {
      method: "PUT",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerPresupuestos();
      });
  }

  async function accion(ruta, texto, id) {
    const seguro = await confirmar({
      titulo: "Confirmar",
      texto: texto,
      boton: "Confirmar",
    });

    if (!seguro) {
      return;
    }

    pedir("/presupuestos/" + ruta + "/" + id, { method: "PUT" })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerPresupuestos();
      });
  }

  async function borrar(id) {
    const seguro = await confirmar({
      titulo: "Borrar presupuesto",
      texto: "El presupuesto se borra con todos sus items.",
      detalle: "Si ya genero una orden, la orden no se toca.",
      boton: "Borrar",
      tono: "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/presupuestos/borrar/" + id, { method: "DELETE" })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        setVerDetalleDe(null);
        traerPresupuestos();
      });
  }

  function patente(id) {
    const v = vehiculos.find((x) => x.id_vehiculo === id);
    return v ? v.patente : id;
  }

  const vigentes = presupuestos.filter((p) => p.estado === "VIGENTE");
  const vencidos = presupuestos.filter((p) => p.estado === "VENCIDO");
  const aceptados = presupuestos.filter((p) => p.estado === "ACEPTADO");

  const tabla = useTabla(presupuestos);

  return (
    <div>
      <div className="metricas">
        <div className="metrica">
          <span className="valor">{vigentes.length}</span>
          <span className="rotulo">Vigentes</span>
        </div>
        <div className="metrica destacada">
          <span className="valor">{vencidos.length}</span>
          <span className="rotulo">Vencidos</span>
        </div>
        <div className="metrica">
          <span className="valor">{aceptados.length}</span>
          <span className="rotulo">Aceptados</span>
        </div>
      </div>

      {/* ---------- paso 1 ---------- */}
      <div className="paso">
        <h2>
          <span className="numero-paso">1</span> El trabajo
        </h2>

        <div className="campos">
          <label>
            Vehiculo
            <select
              value={idVehiculo}
              onChange={(e) => setIdVehiculo(e.target.value)}
            >
              <option value="">-- Elegir --</option>
              {vehiculos.map((v) => (
                <option key={v.id_vehiculo} value={v.id_vehiculo}>
                  {v.patente} — {v.marca} {v.modelo}
                </option>
              ))}
            </select>
          </label>
          <label>
            Horas estimadas
            <input
              type="number"
              value={horas}
              onChange={(e) => setHoras(e.target.value)}
              placeholder="0"
            />
          </label>
          <label>
            Precio por hora
            <input
              type="number"
              value={precioHora}
              onChange={(e) => setPrecioHora(e.target.value)}
              placeholder="0"
            />
          </label>
          <label>
            Dias de validez
            <input
              type="number"
              value={validezDias}
              onChange={(e) => setValidezDias(e.target.value)}
            />
          </label>
        </div>

        <p className="ayuda">
          Las horas son una <b>estimacion</b>: si el trabajo termina llevando
          mas, esas horas se cargan despues en la orden.
          {precioHoraTaller
            ? ""
            : " Cargá el precio de la hora en Datos del taller para que venga solo."}
        </p>
      </div>

      {/* ---------- paso 2 ---------- */}
      <div className="paso">
        <h2>
          <span className="numero-paso">2</span> Repuestos{" "}
          <span className="opcional">opcional</span>
        </h2>

        <div className="bloque-repuestos">
          <h4>Van en todas las opciones</h4>
          <p className="ayuda">Lo que hay que cambiar si o si.</p>

          <SelectorRepuesto
            repuestos={repuestos}
            onAgregar={(id, cant) => agregarRepuesto(id, cant, null)}
          />

          <ListaItems items={itemsDe(null)} onSacar={sacarItem} />
        </div>

        {numeros.map((n) => (
          <div key={n} className="bloque-repuestos alternativo">
            <h4>
              Opcion {n}
              {cantidadOpciones > 1 && (
                <button className="quitar-opcion" onClick={() => sacarOpcion(n)}>
                  Quitar opcion
                </button>
              )}
            </h4>
            <p className="ayuda">
              Los repuestos que van solo en esta opcion. El cliente elige una.
            </p>

            <SelectorRepuesto
              repuestos={repuestos}
              onAgregar={(id, cant) => agregarRepuesto(id, cant, n)}
            />

            <ListaItems items={itemsDe(n)} onSacar={sacarItem} />
          </div>
        ))}

        <button className="agregar-opcion" onClick={agregarOpcion}>
          + Agregar repuesto alternativo (opcion {cantidadOpciones + 1})
        </button>

        {(items.length > 0 || totalManoObra() > 0) && (
          <div className="resumen-totales">
            <div className="linea">
              <span>Repuestos que van siempre</span>
              <b>${totalBase()}</b>
            </div>
            <div className="linea">
              <span>
                Mano de obra ({horas || 0} hs x ${precioHora || 0})
              </span>
              <b>${totalManoObra()}</b>
            </div>

            {hayOpciones ? (
              numeros
                .filter((n) => itemsDe(n).length > 0)
                .map((n) => (
                  <div key={n} className="linea total-opcion">
                    <span>Total opcion {n}</span>
                    <b>${totalOpcion(n)}</b>
                  </div>
                ))
            ) : (
              <div className="linea total-opcion">
                <span>Total</span>
                <b>${totalBase() + totalManoObra()}</b>
              </div>
            )}
          </div>
        )}
      </div>

      {/* ---------- paso 3 ---------- */}
      <div className="paso">
        <h2>
          <span className="numero-paso">3</span> Trabajo a realizar y
          observaciones
        </h2>

        <textarea
          className="detalle-trabajo"
          placeholder="Que se detecto, que hay que cambiar, aclaraciones para el cliente..."
          value={detalleTrabajo}
          onChange={(e) => setDetalleTrabajo(e.target.value)}
          rows="5"
        />

        <button className="crear-presu" onClick={crearPresupuesto}>
          Crear presupuesto
        </button>
      </div>

      {mensaje && <p className="mensaje">{mensaje}</p>}

      {/* ---------- emitidos ---------- */}
      <h2>Presupuestos emitidos</h2>

      <table onContextMenu={tabla.alClickDerecho}>
        <thead>
          <tr>
            <Th tabla={tabla} campo="id_presupuesto">
              Id
            </Th>
            <Th tabla={tabla} campo="id_vehiculo">
              Vehiculo
            </Th>
            <Th tabla={tabla} campo="detalleTrabajo">
              Trabajo
            </Th>
            <Th tabla={tabla} campo="fechaVencimiento">
              Vence
            </Th>
            <Th tabla={tabla} campo="estado">
              Estado
            </Th>
            <Th tabla={tabla} campo="total" className="importe">
              Total
            </Th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {tabla.filas.map((p) => (
            <tr key={p.id_presupuesto}>
              <td>{p.id_presupuesto}</td>
              <td className="clave">{patente(p.id_vehiculo)}</td>
              <td className="recorte">{p.detalleTrabajo}</td>
              <td>{fecha(p.fechaVencimiento)}</td>
              <td>
                <span className={"etiqueta presu-" + p.estado}>{p.estado}</span>
              </td>
              <td className="importe">
                ${p.total}
                {p.totalesOpciones && Object.keys(p.totalesOpciones).length > 1 && (
                  <span className="ayuda"> op. {p.opcionElegida}</span>
                )}
              </td>
              <td className="col-acciones">
                <Acciones
                  principal={{
                    texto: "Ver",
                    icono: "ver",
                    alHacer: () => setVerDetalleDe(p),
                  }}
                  opciones={[
                    {
                      texto: "Compartir con el cliente",
                      icono: "compartir",
                      alHacer: () => setCompartiendo(p),
                    },
                    p.estado === "VIGENTE" && {
                      texto: "Aceptar y abrir orden",
                      icono: "aceptar",
                      alHacer: () => aceptar(p),
                    },
                    p.estado === "VENCIDO" && {
                      texto: "Renovar " + p.validezDias + " dias",
                      icono: "historial",
                      alHacer: () =>
                        accion(
                          "renovar",
                          "Renovar por " + p.validezDias + " dias mas?",
                          p.id_presupuesto
                        ),
                    },
                    esAdmin() && {
                      texto: "Borrar",
                      icono: "borrar",
                      tono: "peligro",
                      alHacer: () => borrar(p.id_presupuesto),
                    },
                  ]}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {tabla.buscador}

      {presupuestos.length === 0 && (
        <p className="vacio">Todavia no hay presupuestos emitidos.</p>
      )}

      {compartiendo && (
        <CompartirPresupuesto
          presupuesto={compartiendo}
          patente={patente(compartiendo.id_vehiculo)}
          onCerrar={() => setCompartiendo(null)}
        />
      )}

      {verDetalleDe && (
        <DetallePresupuesto
          presupuesto={verDetalleDe}
          patente={patente(verDetalleDe.id_vehiculo)}
          onElegir={elegirOpcion}
          onRechazar={() =>
            accion("rechazar", "Marcar como rechazado?", verDetalleDe.id_presupuesto)
          }
          onCerrar={() => setVerDetalleDe(null)}
        />
      )}
    </div>
  );
}

// ---------------------------------------------------------------
// Elegir un repuesto y agregarlo. Se usa igual para los que van
// siempre y para cada opcion.
// ---------------------------------------------------------------
function SelectorRepuesto({ repuestos, onAgregar }) {
  const [busca, setBusca] = useState("");
  const [id, setId] = useState("");
  const [cantidad, setCantidad] = useState("1");

  // Con 40 repuestos cargados, desplegar la lista entera es incomodo:
  // se escribe parte del nombre o la marca y quedan solo esos.
  const hallados = filtrar(repuestos, busca, [
    "codigo",
    "codigoBarra",
    "nombre",
    "marca",
  ]);

  function agregar() {
    onAgregar(id, cantidad);
    setId("");
    setCantidad("1");
    setBusca("");
  }

  return (
    <div className="selector-repuesto">
      <input
        className="buscar-repuesto"
        value={busca}
        onChange={(e) => {
          setBusca(e.target.value);
          setId("");
        }}
        placeholder="Filtrar por nombre, marca o codigo..."
      />
      <select value={id} onChange={(e) => setId(e.target.value)}>
        <option value="">
          {busca
            ? "-- " + hallados.length + " coinciden --"
            : "-- Repuesto del deposito (" + repuestos.length + ") --"}
        </option>
        {hallados.map((r) => (
          <option key={r.id_repuesto} value={r.id_repuesto}>
            {r.codigo} - {r.nombre}
            {r.marca ? " (" + r.marca + ")" : ""} - ${r.precioVenta}
          </option>
        ))}
      </select>
      <input
        type="number"
        value={cantidad}
        onChange={(e) => setCantidad(e.target.value)}
        placeholder="Cant."
      />
      <button className="secundario" onClick={agregar} disabled={!id}>
        Agregar
      </button>
    </div>
  );
}

function ListaItems({ items, onSacar }) {
  if (items.length === 0) {
    return <p className="sin-items">Todavia no cargaste ninguno.</p>;
  }

  return (
    <ul className="lista-items">
      {items.map((i) => (
        <li key={i.indice}>
          <span>{i.descripcion}</span>
          <span className="cant">
            {i.cantidad} x ${i.precioUnitario}
          </span>
          <b>${i.cantidad * i.precioUnitario}</b>
          <button onClick={() => onSacar(i.indice)}>Sacar</button>
        </li>
      ))}
    </ul>
  );
}

// ---------------------------------------------------------------
// Detalle de un presupuesto ya emitido, con sus opciones
// ---------------------------------------------------------------
function DetallePresupuesto({ presupuesto, patente, onElegir, onRechazar, onCerrar }) {
  const opciones = presupuesto.totalesOpciones || {};
  const numeros = Object.keys(opciones).map(Number).sort();

  const comunes = (presupuesto.items || []).filter((i) => !i.opcion);

  function itemsDe(n) {
    return (presupuesto.items || []).filter((i) => i.opcion === n);
  }

  return (
    <div className="panel">
      <h3>
        <span>
          Presupuesto {presupuesto.id_presupuesto} — {patente}
        </span>
        <button className="secundario" onClick={onCerrar}>
          Cerrar
        </button>
      </h3>

      <p className="tarjeta-texto">{presupuesto.detalleTrabajo}</p>

      {comunes.length > 0 && (
        <>
          <h4>En todas las opciones</h4>
          <ul className="lista-items">
            {comunes.map((i) => (
              <li key={i.id_item}>
                <span>{i.descripcion}</span>
                <span className="cant">
                  {i.cantidad} x ${i.precioUnitario}
                </span>
                <b>${i.cantidad * i.precioUnitario}</b>
              </li>
            ))}
          </ul>
        </>
      )}

      {Number(presupuesto.totalManoObra) > 0 && (
        <ul className="lista-items">
          <li>
            <span>
              Mano de obra ({presupuesto.horasEstimadas} hs x $
              {presupuesto.precioHora})
            </span>
            <span className="cant"></span>
            <b>${presupuesto.totalManoObra}</b>
          </li>
        </ul>
      )}

      {numeros.length === 0 ? (
        <div className="resumen-totales">
          <div className="linea total-opcion">
            <span>Total</span>
            <b>${presupuesto.total}</b>
          </div>
        </div>
      ) : (
        <div className="opciones-cliente">
          {numeros.map((n) => (
            <div
              key={n}
              className={
                "opcion-tarjeta" + (presupuesto.opcionElegida === n ? " elegida" : "")
              }
            >
              <label>
                <input
                  type="radio"
                  className="radio-opcion"
                  checked={presupuesto.opcionElegida === n}
                  disabled={presupuesto.estado === "ACEPTADO"}
                  onChange={() => onElegir(presupuesto.id_presupuesto, n)}
                />
                <b>Opcion {n}</b>
              </label>

              <ul className="lista-items">
                {itemsDe(n).map((i) => (
                  <li key={i.id_item}>
                    <span>{i.descripcion}</span>
                    <span className="cant">
                      {i.cantidad} x ${i.precioUnitario}
                    </span>
                    <b>${i.cantidad * i.precioUnitario}</b>
                  </li>
                ))}
              </ul>

              <div className="total-tarjeta">
                Total opcion {n} <b>${opciones[n]}</b>
              </div>
            </div>
          ))}
        </div>
      )}

      <p className="ayuda">
        Las horas son estimadas. Si el trabajo lleva mas tiempo, la diferencia se
        carga en la orden.
        {presupuesto.id_orden
          ? " Este presupuesto genero la orden " + presupuesto.id_orden + "."
          : ""}
      </p>

      {presupuesto.estado === "VIGENTE" && (
        <button className="rechazar" onClick={onRechazar}>
          El cliente lo rechazo
        </button>
      )}
    </div>
  );
}

export default Presupuestos;
