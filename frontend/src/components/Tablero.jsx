import { useEffect, useState } from "react";
import { pedir, leerRespuesta } from "../api";
import { hoy, esDelDia } from "../fechas";

const ESTADOS = [
  "RECIBIDO",
  "DIAGNOSTICO",
  "EN_REPARACION",
  "ESPERANDO_REPUESTOS",
  "LISTO",
  "ENTREGADO",
];

/**
 * El taller de un vistazo: cada estado es una columna y cada orden una
 * tarjeta que se arrastra de una a otra.
 *
 * Es la vista para dejar puesta en una pantalla colgada en el taller. De
 * las entregadas solo se ven las de hoy: si no, la ultima columna crece
 * para siempre y deja de servir.
 *
 * Soltar una orden en ENTREGADO sin haberla cobrado lo rechaza el backend,
 * y el motivo aparece arriba.
 */
function Tablero() {
  const [ordenes, setOrdenes] = useState([]);
  const [vehiculos, setVehiculos] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [pagos, setPagos] = useState([]);
  const [mensaje, setMensaje] = useState("");

  // la orden que se esta arrastrando y la columna donde esta parada
  const [arrastrando, setArrastrando] = useState(null);
  const [encima, setEncima] = useState("");

  function traerTodo() {
    pedir("/ordenes/traer")
      .then((res) => res.json())
      .then((data) => setOrdenes(data))
      .catch(() => setOrdenes([]));

    pedir("/pagos/traer")
      .then((res) => res.json())
      .then((data) => setPagos(data))
      .catch(() => setPagos([]));
  }

  useEffect(() => {
    traerTodo();

    pedir("/vehiculos/traer")
      .then((res) => res.json())
      .then((data) => setVehiculos(data))
      .catch(() => setVehiculos([]));

    pedir("/empleados/traer")
      .then((res) => res.json())
      .then((data) => setEmpleados(data))
      .catch(() => setEmpleados([]));
  }, []);

  function patente(id) {
    const v = vehiculos.find((x) => x.id_vehiculo === id);
    return v ? v.patente : "?";
  }

  function mecanico(id) {
    const e = empleados.find((x) => x.id_empleado === id);
    return e ? e.nombre + " " + e.apellido : "";
  }

  function cobrada(id_orden) {
    const pago = pagos.find(
      (p) => p.id_orden === id_orden && p.estado !== "ANULADO"
    );

    return pago && pago.estado === "CONFIRMADO";
  }

  // se pasa de la fecha prometida y todavia no esta lista
  function atrasada(orden) {
    return (
      orden.fechaEntregaEstimada &&
      new Date(orden.fechaEntregaEstimada) < new Date() &&
      orden.estado !== "LISTO" &&
      orden.estado !== "ENTREGADO"
    );
  }

  function soltar(estado) {
    setEncima("");

    const orden = arrastrando;
    setArrastrando(null);

    if (!orden || orden.estado === estado) {
      return;
    }

    pedir("/ordenes/estado/" + orden.id_orden + "?estado=" + estado, {
      method: "PUT",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerTodo();
      });
  }

  // las entregadas de hoy siguen a la vista; las de antes ya no
  const enTaller = ordenes.filter(
    (o) =>
      o.estado !== "ENTREGADO" ||
      esDelDia(o.fechaEntregaReal, hoy())
  );

  return (
    <div>
      {mensaje && <p className="mensaje">{mensaje}</p>}

      <p className="ayuda">
        Arrastra una orden de una columna a otra para cambiarle el estado. En
        ENTREGADO quedan solo las que salieron hoy.
      </p>

      <div className="tablero">
        {ESTADOS.map((estado) => {
          const columna = enTaller.filter((o) => o.estado === estado);
          const plata = columna.reduce((s, o) => s + Number(o.total || 0), 0);

          return (
            <div
              key={estado}
              className={
                "columna" +
                (encima === estado ? " encima" : "") +
                (columna.length === 0 ? " vacia" : "")
              }
              onDragOver={(e) => {
                e.preventDefault();
                setEncima(estado);
              }}
              onDragLeave={() => setEncima("")}
              onDrop={() => soltar(estado)}
            >
              <div className={"cabecera-columna estado-" + estado}>
                <b>{estado.replace("_", " ")}</b>
                <span>{columna.length}</span>
              </div>

              {columna.map((o) => (
                <div
                  key={o.id_orden}
                  className={
                    "tarjeta-orden" +
                    (atrasada(o) ? " atrasada" : "") +
                    (arrastrando && arrastrando.id_orden === o.id_orden
                      ? " levantada"
                      : "")
                  }
                  draggable
                  onDragStart={() => setArrastrando(o)}
                  onDragEnd={() => {
                    setArrastrando(null);
                    setEncima("");
                  }}
                >
                  <div className="cabecera-tarjeta">
                    <b className="clave">{patente(o.id_vehiculo)}</b>
                    <span className="importe">${o.total}</span>
                  </div>

                  <p className="trabajo">{o.diagnostico || "Sin diagnostico"}</p>

                  <div className="pie-tarjeta">
                    <span>{mecanico(o.id_mecanico)}</span>
                    {atrasada(o) && <span className="atraso">atrasada</span>}
                    {o.estado === "LISTO" && !cobrada(o.id_orden) && (
                      <span className="falta-cobrar">sin cobrar</span>
                    )}
                  </div>
                </div>
              ))}

              {columna.length > 0 && (
                <div className="pie-columna">${plata}</div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default Tablero;
