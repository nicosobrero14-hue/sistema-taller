import { useEffect, useState } from "react";
import { pedir, leerRespuesta, esAdmin } from "../api";
import { confirmar, avisar } from "../avisos";
import { filtrar, POR_PAGINA } from "../listas";
import { fechaHora, hoy } from "../fechas";
import { CampoFiltro, VerMas } from "./Filtro";
import DetalleOrden from "./DetalleOrden";
import Acciones from "./Acciones";
import { useTabla, Th } from "./Tabla";
import FotosOrden from "./FotosOrden";
import CobrarOrden from "./CobrarOrden";

const ESTADOS = [
  "RECIBIDO",
  "DIAGNOSTICO",
  "EN_REPARACION",
  "ESPERANDO_REPUESTOS",
  "LISTO",
  "ENTREGADO",
];

function Ordenes() {
  const [ordenes, setOrdenes] = useState([]);
  const [vehiculos, setVehiculos] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [sucursales, setSucursales] = useState([]);

  const [verDetalleDe, setVerDetalleDe] = useState(null);
  const [verFotosDe, setVerFotosDe] = useState(null);
  const [verCobroDe, setVerCobroDe] = useState(null);
  const [pagos, setPagos] = useState([]);
  const [editandoId, setEditandoId] = useState(null);

  // estados que el usuario eligio en los select pero todavia no guardo
  const [estadosElegidos, setEstadosElegidos] = useState({});
  const [mensaje, setMensaje] = useState("");
  const [filtro, setFiltro] = useState("");
  const [cuantas, setCuantas] = useState(POR_PAGINA);
  // ver solo lo que hay que cobrar
  const [soloPorCobrar, setSoloPorCobrar] = useState(false);

  const [idVehiculo, setIdVehiculo] = useState("");
  const [idMecanico, setIdMecanico] = useState("");
  const [idSucursal, setIdSucursal] = useState("");
  const [diagnostico, setDiagnostico] = useState("");
  const [fechaEntrega, setFechaEntrega] = useState("");
  const [kilometraje, setKilometraje] = useState("");
  const [descuento, setDescuento] = useState("");
  // filtro del select de vehiculos: con 200 autos el desplegable no sirve
  const [buscaVehiculo, setBuscaVehiculo] = useState("");
  // a quien avisarle que el auto esta listo
  const [avisarA, setAvisarA] = useState(null);

  // "taller" son las que estan adentro, "archivo" las ya entregadas.
  // Se traen por separado: en un par de años las entregadas son miles y
  // no tiene sentido bajarlas para mirar las ocho de hoy.
  const [vista, setVista] = useState("taller");
  const [desde, setDesde] = useState(haceUnMes());
  const [hasta, setHasta] = useState(hoy());

  //1- traer todas las ordenes
  function traerOrdenes() {
    const ruta =
      vista === "archivo"
        ? "/ordenes/entregadas?desde=" + desde + "&hasta=" + hasta
        : "/ordenes/abiertas";

    pedir(ruta)
      .then((res) => res.json())
      .then((data) => setOrdenes(data))
      .catch(() => setOrdenes([]));
  }

  //2- que ordenes ya estan cobradas
  function traerPagos() {
    pedir("/pagos/traer")
      .then((res) => (res.ok ? res.json() : []))
      .then((data) => setPagos(data))
      .catch(() => setPagos([]));
  }

  // el cobro de una orden, o null si todavia no se cobro
  function pagoDe(id_orden) {
    return pagos.find((p) => p.id_orden === id_orden);
  }

  // Paso la fecha que se le prometio al cliente y todavia no esta lista.
  // Es la unica fecha que el cliente se acuerda.
  function atrasada(orden) {
    return (
      orden.fechaEntregaEstimada &&
      new Date(orden.fechaEntregaEstimada) < new Date() &&
      orden.estado !== "LISTO" &&
      orden.estado !== "ENTREGADO"
    );
  }

  // Un cobro cargado no es un cobro hecho: vale recien cuando alguien
  // confirmo que la plata entro.
  function cobrada(id_orden) {
    const pago = pagoDe(id_orden);

    return pago && pago.estado === "CONFIRMADO";
  }

  //3- traer vehiculos, mecanicos y sucursales para los selects
  function traerDatos() {
    pedir("/vehiculos/traer")
      .then((res) => res.json())
      .then((data) => setVehiculos(data))
      .catch(() => setVehiculos([]));

    pedir("/empleados/traer")
      .then((res) => res.json())
      .then((data) => setEmpleados(data))
      .catch(() => setEmpleados([]));

    pedir("/sucursales/traer")
      .then((res) => res.json())
      .then((data) => setSucursales(data))
      .catch(() => setSucursales([]));
  }

  useEffect(() => {
    traerPagos();
    traerDatos();
  }, []);

  // cambiar de pestaña o de fechas vuelve a pedir la lista
  useEffect(() => {
    traerOrdenes();
    setCuantas(POR_PAGINA);
  }, [vista, desde, hasta]);

  //3- el mismo formulario crea o edita la orden
  async function guardarOrden(e) {
    e.preventDefault();

    if (
      editandoId &&
      !(await confirmar({
        titulo: "Guardar los cambios",
        texto: "Se van a actualizar los datos de la orden " + editandoId + ".",
        boton: "Guardar",
      }))
    ) {
      return;
    }

    const orden = {
      id_vehiculo: Number(idVehiculo),
      id_mecanico: Number(idMecanico),
      id_sucursal: Number(idSucursal),
      diagnostico: diagnostico,
      kilometrajeIngreso: kilometraje === "" ? null : Number(kilometraje),
      descuento: descuento === "" ? null : Number(descuento),
      fechaEntregaEstimada: fechaEntrega,
    };

    const ruta = editandoId
      ? "/ordenes/editar/" + editandoId
      : "/ordenes/crear";

    pedir(ruta, {
      method: editandoId ? "PUT" : "POST",
      body: JSON.stringify(orden),
    })
      .then(leerRespuesta)
      .then((r) => {
        // taller-service responde si pudo validar el vehiculo,
        // el mecanico y la sucursal contra los otros servicios
        setMensaje(r.mensaje);

        // si rechazo los datos, el formulario queda como estaba
        if (r.ok) {
          limpiarFormulario();
        }

        traerOrdenes();
      });
  }

  //5- carga la orden en el formulario. El estado no se edita aca,
  // se cambia desde el select de la tabla.
  function editarOrden(o) {
    setEditandoId(o.id_orden);
    setIdVehiculo(o.id_vehiculo);
    setIdMecanico(o.id_mecanico);
    setIdSucursal(o.id_sucursal);
    setDiagnostico(o.diagnostico || "");
    setKilometraje(o.kilometrajeIngreso || "");
    setDescuento(o.descuento || "");
    setFechaEntrega(
      o.fechaEntregaEstimada ? o.fechaEntregaEstimada.substring(0, 16) : ""
    );
  }

  function limpiarFormulario() {
    setEditandoId(null);
    setIdVehiculo("");
    setIdMecanico("");
    setIdSucursal("");
    setDiagnostico("");
    setFechaEntrega("");
    setKilometraje("");
    setDescuento("");
    setBuscaVehiculo("");
  }

  //6- elegir un estado en el select no lo guarda todavia, solo lo anota
  function elegirEstado(id, estado) {
    setEstadosElegidos({ ...estadosElegidos, [id]: estado });
  }

  // el estado que se esta mostrando en el select de esa fila
  function estadoMostrado(orden) {
    return estadosElegidos[orden.id_orden] || orden.estado;
  }

  // hay algo para guardar solo si el elegido es distinto al que ya tiene
  function hayCambioDeEstado(orden) {
    return estadoMostrado(orden) !== orden.estado;
  }

  //7- recien aca se guarda el estado. Los estados son libres: se puede
  // reabrir una orden ya entregada si el vehiculo vuelve por lo mismo.
  async function guardarEstado(orden) {
    const estado = estadosElegidos[orden.id_orden];

    // El vehiculo no sale del taller sin estar cobrado. Se puede dejar
    // LISTO todo lo que haga falta, pero ENTREGADO no. Taller-service
    // rechaza igual el cambio; esto es para no hacer el viaje al pedo y
    // explicar por que.
    if (estado === "ENTREGADO" && !cobrada(orden.id_orden)) {
      const pago = pagoDe(orden.id_orden);

      avisar({
        titulo: "Todavia no se puede entregar",
        texto: "La orden " + orden.id_orden + " no esta cobrada.",
        detalle: pago
          ? "El cobro de $" +
            pago.monto +
            " esta cargado pero sin confirmar. Confirmalo desde Cobrar cuando la plata haya entrado."
          : "Son $" +
            orden.total +
            ". Cargalos desde Cobrar y confirmalos cuando entren.",
        tono: "peligro",
      });

      // la pastilla vuelve al estado de verdad: si queda en ENTREGADO
      // parece que el cambio se hizo
      const vuelta = { ...estadosElegidos };
      delete vuelta[orden.id_orden];
      setEstadosElegidos(vuelta);

      return;
    }

    const seguro = await confirmar({
      titulo: "Cambiar el estado",
      texto:
        "La orden " + orden.id_orden + " pasa a " + estado.replace("_", " ") + ".",
      detalle:
        estado === "LISTO"
          ? "Despues vas a poder avisarle al cliente que la puede retirar."
          : "",
      boton: "Cambiar",
    });

    if (!seguro) {
      return;
    }

    pedir("/ordenes/estado/" + orden.id_orden + "?estado=" + estado, {
      method: "PUT",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);

        // ya se guardo, se saca de los pendientes
        const pendientes = { ...estadosElegidos };
        delete pendientes[orden.id_orden];
        setEstadosElegidos(pendientes);

        // el auto quedo listo: el cliente quiere enterarse hoy, no mañana
        if (r.ok && estado === "LISTO") {
          setAvisarA(orden);
        }

        traerOrdenes();
      });
  }

  //8- borrar una orden
  async function borrarOrden(id) {
    const seguro = await confirmar({
      titulo: "Borrar orden",
      texto: "Se borra la orden " + id + " con todos sus items y sus fotos.",
      detalle: "Los repuestos que se hayan usado NO vuelven al deposito.",
      boton: "Borrar",
      tono: "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/ordenes/borrar/" + id, {
      method: "DELETE",
    }).then(() => {
      setVerDetalleDe(null);
      limpiarFormulario();
      traerOrdenes();
    });
  }

  //9- avisarle al cliente por whatsapp que el auto esta listo. El telefono
  // sale del cliente del vehiculo; si no lo tiene cargado, wa.me abre igual
  // y se elige el contacto a mano.
  function avisarPorWhatsapp(orden) {
    const vehiculo = vehiculos.find((v) => v.id_vehiculo === orden.id_vehiculo);
    const cliente = vehiculo ? vehiculo.cliente : null;

    const texto =
      "Hola" +
      (cliente && cliente.nombre ? " " + cliente.nombre : "") +
      "! Tu " +
      (vehiculo ? vehiculo.marca + " " + vehiculo.modelo : "vehiculo") +
      (vehiculo ? " (" + vehiculo.patente + ")" : "") +
      " ya esta listo para retirar." +
      (orden.total ? " El total es $" + orden.total + "." : "") +
      " Te esperamos!";

    const telefono = cliente && cliente.telefono ? limpiarTelefono(cliente.telefono) : "";

    window.open(
      "https://wa.me/" + telefono + "?text=" + encodeURIComponent(texto),
      "_blank"
    );

    setAvisarA(null);
  }

  //10- buscar la patente en la lista que trajimos de clientes-service
  function buscarPatente(id) {
    const vehiculo = vehiculos.find((v) => v.id_vehiculo === id);
    return vehiculo ? vehiculo.patente : id;
  }

  //11- buscar el mecanico en la lista que trajimos de usuarios-service
  function buscarMecanico(id) {
    const empleado = empleados.find((e) => e.id_empleado === id);
    return empleado ? empleado.nombre + " " + empleado.apellido : id;
  }

  const vehiculosHallados = filtrar(vehiculos, buscaVehiculo, [
    "patente",
    "marca",
    "modelo",
    "cliente.nombre",
  ]);

  // las que ya se pueden cobrar y siguen impagas
  const porCobrar = ordenes.filter(
    (o) => !cobrada(o.id_orden) && (o.estado === "LISTO" || o.estado === "ENTREGADO")
  );

  const base = soloPorCobrar ? porCobrar : ordenes;

  // lo que se ve: filtrado por el buscador y de a tandas
  // A cada orden se le pega el texto por el que se la busca. La patente y
  // el mecanico viven en otros servicios, asi que no vienen en la orden y
  // hay que armarlos aca.
  const buscables = base.map((o) => ({
    ...o,
    // la patente y el mecanico viven en otros servicios: se pegan aca
    // para poder ordenar y buscar por ellos
    patente: buscarPatente(o.id_vehiculo),
    mecanico: buscarMecanico(o.id_mecanico),
    texto: [
      o.id_orden,
      buscarPatente(o.id_vehiculo),
      buscarMecanico(o.id_mecanico),
      o.estado,
      o.diagnostico,
    ].join(" "),
  }));

  const filtrados = filtrar(buscables, filtro, ["texto"]);

  const tabla = useTabla(filtrados);
  const visibles = tabla.filas.slice(0, cuantas);

  // resumen de arriba, calculado sobre lo que ya trajimos
  const enTaller = ordenes.filter((o) => o.estado !== "ENTREGADO");
  const enReparacion = ordenes.filter((o) => o.estado === "EN_REPARACION");
  const listas = ordenes.filter((o) => o.estado === "LISTO");
  const atrasadas = ordenes.filter((o) => atrasada(o));
  const aCobrar = enTaller.reduce((suma, o) => suma + Number(o.total || 0), 0);

  return (
    <div>
      <div className="metricas" hidden={vista === "archivo"}>
        <div className="metrica">
          <span className="valor">{enTaller.length}</span>
          <span className="rotulo">En el taller</span>
        </div>
        <div className="metrica">
          <span className="valor">{enReparacion.length}</span>
          <span className="rotulo">En reparacion</span>
        </div>
        <div className="metrica destacada">
          <span className="valor">{listas.length}</span>
          <span className="rotulo">Listas para entregar</span>
        </div>
        <div className={"metrica" + (atrasadas.length > 0 ? " alarma" : "")}>
          <span className="valor">{atrasadas.length}</span>
          <span className="rotulo">Atrasadas</span>
        </div>
        <div
          className={"metrica clickeable" + (soloPorCobrar ? " activa" : "")}
          onClick={() => {
            setSoloPorCobrar(!soloPorCobrar);
            setCuantas(POR_PAGINA);
          }}
          title="Ver solo las ordenes listas o entregadas que siguen impagas"
        >
          <span className="valor">
            ${porCobrar.reduce((s, o) => s + Number(o.total || 0), 0)}
          </span>
          <span className="rotulo">
            Por cobrar ({porCobrar.length}) {soloPorCobrar ? "· viendo" : ""}
          </span>
        </div>
      </div>

      {vista === "taller" && (
        <h2>{editandoId ? "Editando orden" : "Nueva orden de trabajo"}</h2>
      )}

      <form onSubmit={guardarOrden} hidden={vista === "archivo"}>
        <input
          placeholder="Buscar patente, marca o cliente..."
          value={buscaVehiculo}
          onChange={(e) => {
            setBuscaVehiculo(e.target.value);
            setIdVehiculo("");
          }}
        />
        <select
          value={idVehiculo}
          onChange={(e) => setIdVehiculo(e.target.value)}
          required
        >
          <option value="">
            {buscaVehiculo
              ? "-- " + vehiculosHallados.length + " coinciden --"
              : "-- Vehiculo (" + vehiculos.length + ") --"}
          </option>
          {vehiculosHallados.map((v) => (
            <option key={v.id_vehiculo} value={v.id_vehiculo}>
              {v.patente} - {v.marca} {v.modelo}
              {v.cliente ? " (" + v.cliente.nombre + ")" : ""}
            </option>
          ))}
        </select>
        <select
          value={idMecanico}
          onChange={(e) => setIdMecanico(e.target.value)}
          required
        >
          <option value="">-- Mecanico --</option>
          {empleados.map((e) => (
            <option key={e.id_empleado} value={e.id_empleado}>
              {e.nombre} {e.apellido}
            </option>
          ))}
        </select>
        <select
          value={idSucursal}
          onChange={(e) => setIdSucursal(e.target.value)}
          required
        >
          <option value="">-- Sucursal --</option>
          {sucursales.map((s) => (
            <option key={s.id_sucursal} value={s.id_sucursal}>
              {s.nombre}
            </option>
          ))}
        </select>
        <input
          placeholder="Diagnostico"
          value={diagnostico}
          onChange={(e) => setDiagnostico(e.target.value)}
        />
        <input
          type="number"
          placeholder="Km al ingresar"
          value={kilometraje}
          onChange={(e) => setKilometraje(e.target.value)}
        />
        <input
          type="number"
          placeholder="Descuento $"
          value={descuento}
          onChange={(e) => setDescuento(e.target.value)}
        />
        <input
          type="datetime-local"
          value={fechaEntrega}
          onChange={(e) => setFechaEntrega(e.target.value)}
        />
        <button type="submit">{editandoId ? "Guardar" : "Crear"}</button>
        {editandoId && (
          <button type="button" onClick={limpiarFormulario}>
            Cancelar
          </button>
        )}
      </form>

      {mensaje && <p className="mensaje">{mensaje}</p>}

      {avisarA && (
        <div className="avisar">
          <span>
            La orden {avisarA.id_orden} quedo lista.{" "}
            <b>Le avisamos al cliente?</b>
            <em>
              Si ahora no podes, queda en los tres puntitos de la fila como
              "Avisar que esta listo".
            </em>
          </span>
          <button className="wp" onClick={() => avisarPorWhatsapp(avisarA)}>
            Avisar por WhatsApp
          </button>
          <button onClick={() => setAvisarA(null)}>Ahora no</button>
        </div>
      )}

      <div className="pestanas">
        <button
          className={vista === "taller" ? "activa" : ""}
          onClick={() => setVista("taller")}
        >
          En el taller
        </button>
        <button
          className={vista === "archivo" ? "activa" : ""}
          onClick={() => setVista("archivo")}
        >
          Entregadas
        </button>

        {vista === "archivo" && (
          <div className="rango">
            <label>
              Desde
              <input
                type="date"
                value={desde}
                onChange={(e) => setDesde(e.target.value)}
              />
            </label>
            <label>
              Hasta
              <input
                type="date"
                value={hasta}
                onChange={(e) => setHasta(e.target.value)}
              />
            </label>
          </div>
        )}
      </div>

      <CampoFiltro
        valor={filtro}
        onCambio={(v) => {
          setFiltro(v);
          setCuantas(POR_PAGINA);
        }}
        placeholder="Buscar por patente, mecanico o trabajo..."
        cantidad={filtrados.length}
        total={base.length}
      />

      <table onContextMenu={tabla.alClickDerecho}>
        <thead>
          <tr>
            <Th tabla={tabla} campo="id_orden">
              Id
            </Th>
            <Th tabla={tabla} campo="patente">
              Vehiculo
            </Th>
            <Th tabla={tabla} campo="mecanico">
              Mecanico
            </Th>
            <Th tabla={tabla} campo="estado">
              Estado
            </Th>
            <Th tabla={tabla} campo="diagnostico">
              Diagnostico
            </Th>
            <Th tabla={tabla} campo="fechaEntregaReal">
              Entregado
            </Th>
            <Th tabla={tabla} campo="total" className="importe">
              Total
            </Th>
            <th>Cobro</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {visibles.map((o) => (
            <tr key={o.id_orden} className={atrasada(o) ? "atrasada" : ""}>
              <td>
                {o.id_orden}
                {atrasada(o) && <span className="alerta">atrasada</span>}
              </td>
              <td className="clave">{buscarPatente(o.id_vehiculo)}</td>
              <td className="nombre">{buscarMecanico(o.id_mecanico)}</td>
              <td>
                <select
                  className={"estado-select estado-" + estadoMostrado(o)}
                  value={estadoMostrado(o)}
                  onChange={(e) => elegirEstado(o.id_orden, e.target.value)}
                >
                  {ESTADOS.map((es) => (
                    <option key={es} value={es}>
                      {es.replace("_", " ")}
                    </option>
                  ))}
                </select>
                {hayCambioDeEstado(o) && (
                  <button
                    className="secundario"
                    onClick={() => guardarEstado(o)}
                  >
                    Guardar
                  </button>
                )}
              </td>
              <td className="recorte" title={o.diagnostico}>
                {o.diagnostico}
              </td>
              <td className="nombre">{fechaHora(o.fechaEntregaReal)}</td>
              <td className="importe">
                ${o.total}
                {Number(o.descuento) > 0 && (
                  <span className="ayuda"> -${o.descuento}</span>
                )}
              </td>
              <td>
                {cobrada(o.id_orden) && (
                  <span className="etiqueta pago-COBRADO">
                    Recibo {pagoDe(o.id_orden).numeroRecibo}
                  </span>
                )}
                {pagoDe(o.id_orden) && !cobrada(o.id_orden) && (
                  <span className="etiqueta pago-PENDIENTE">Sin confirmar</span>
                )}
                {!pagoDe(o.id_orden) && (
                  <span className="etiqueta pago-IMPAGO">Impago</span>
                )}
              </td>
              <td className="col-acciones">
                <Acciones
                  principal={
                    cobrada(o.id_orden)
                      ? {
                          texto: "Recibo",
                          icono: "recibo",
                          alHacer: () => setVerCobroDe(o),
                        }
                      : pagoDe(o.id_orden)
                      ? {
                          texto: "Confirmar",
                          icono: "aceptar",
                          tono: "ambar",
                          ayuda: "El cobro esta cargado y falta confirmarlo",
                          alHacer: () => setVerCobroDe(o),
                        }
                      : {
                          texto: "Cobrar",
                          icono: "cobrar",
                          tono: "verde",
                          alHacer: () => setVerCobroDe(o),
                        }
                  }
                  opciones={[
                    {
                      texto: "Trabajos y repuestos",
                      icono: "items",
                      detalle: (o.items || []).length,
                      alHacer: () => setVerDetalleDe(o),
                    },
                    {
                      texto: "Fotos",
                      icono: "foto",
                      alHacer: () => setVerFotosDe(o),
                    },
                    o.estado === "LISTO" && {
                      texto: "Avisar que esta listo",
                      icono: "compartir",
                      alHacer: () => avisarPorWhatsapp(o),
                    },
                    {
                      texto: "Editar orden",
                      icono: "editar",
                      alHacer: () => editarOrden(o),
                    },
                    esAdmin() && {
                      texto: "Borrar",
                      icono: "borrar",
                      tono: "peligro",
                      alHacer: () => borrarOrden(o.id_orden),
                    },
                  ]}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {tabla.buscador}

      <VerMas
        mostrando={visibles.length}
        total={tabla.filas.length}
        onMas={() => setCuantas(cuantas + POR_PAGINA)}
      />

      {ordenes.length === 0 && (
        <p className="vacio">
          {vista === "archivo"
            ? "No se entrego ningun vehiculo entre esas fechas. Para buscar una orden vieja de un auto, mira el historial del vehiculo."
            : "No hay ningun vehiculo en el taller ahora mismo."}
        </p>
      )}

      {verDetalleDe && (
        <DetalleOrden
          orden={verDetalleDe}
          onCerrar={() => setVerDetalleDe(null)}
          onCambio={traerOrdenes}
        />
      )}

      {verFotosDe && (
        <FotosOrden orden={verFotosDe} onCerrar={() => setVerFotosDe(null)} />
      )}

      {verCobroDe && (
        <CobrarOrden
          orden={verCobroDe}
          vehiculos={vehiculos}
          onCerrar={() => setVerCobroDe(null)}
          onCambio={() => {
            traerOrdenes();
            traerPagos();
          }}
        />
      )}
    </div>
  );
}

// hace un mes, que es lo que se mira casi siempre en el archivo
function haceUnMes() {
  const f = new Date();
  f.setMonth(f.getMonth() - 1);

  return (
    f.getFullYear() +
    "-" +
    String(f.getMonth() + 1).padStart(2, "0") +
    "-" +
    String(f.getDate()).padStart(2, "0")
  );
}

// wa.me quiere el numero pelado y con el codigo de pais. Los telefonos
// se cargan como cada uno quiere, asi que se limpia y se asume Argentina.
function limpiarTelefono(telefono) {
  const numero = String(telefono).replace(/[^0-9]/g, "");

  if (numero.length === 0) {
    return "";
  }

  return numero.startsWith("54") ? numero : "54" + numero;
}

export default Ordenes;
