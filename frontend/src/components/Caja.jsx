import { useEffect, useState } from "react";
import { pedir } from "../api";
import { hoy, esDelDia, fechaHora } from "../fechas";

const MEDIOS = [
  "EFECTIVO",
  "TRANSFERENCIA",
  "TARJETA_DEBITO",
  "TARJETA_CREDITO",
  "QR",
];

/**
 * El arqueo del dia: cuanto entro, en que, y que quedo colgado.
 *
 * Es la pantalla que se abre a la mañana y a la noche. No pide nada nuevo
 * al backend: los cobros ya vienen todos, aca se ordenan.
 */
function Caja() {
  const [pagos, setPagos] = useState([]);
  const [ordenes, setOrdenes] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [vehiculos, setVehiculos] = useState([]);
  const [ventas, setVentas] = useState([]);
  const [dia, setDia] = useState(hoy());

  function traerTodo() {
    pedir("/pagos/traer")
      .then((res) => res.json())
      .then((data) => setPagos(data))
      .catch(() => setPagos([]));

    pedir("/ordenes/traer")
      .then((res) => res.json())
      .then((data) => setOrdenes(data))
      .catch(() => setOrdenes([]));

    pedir("/empleados/traer")
      .then((res) => res.json())
      .then((data) => setEmpleados(data))
      .catch(() => setEmpleados([]));

    pedir("/vehiculos/traer")
      .then((res) => res.json())
      .then((data) => setVehiculos(data))
      .catch(() => setVehiculos([]));

    // por la caja pasan las dos cosas: lo que se cobra de las ordenes y
    // lo que se vende por mostrador
    pedir("/ventas/traer")
      .then((res) => res.json())
      .then((data) => setVentas(data))
      .catch(() => setVentas([]));
  }

  useEffect(() => {
    traerTodo();
  }, []);

  function empleado(id) {
    const e = empleados.find((x) => x.id_empleado === id);
    return e ? e.nombre + " " + e.apellido : "-";
  }

  // en la caja se busca por patente, no por numero de orden
  function patenteDe(id_orden) {
    const orden = ordenes.find((x) => x.id_orden === id_orden);

    if (!orden) {
      return "orden " + id_orden;
    }

    const vehiculo = vehiculos.find((v) => v.id_vehiculo === orden.id_vehiculo);

    return vehiculo ? vehiculo.patente : "orden " + id_orden;
  }

  // la fecha que cuenta para la caja es la de confirmacion: es cuando la
  // plata entro de verdad, no cuando alguien cargo el cobro
  function entroHoy(pago) {
    return esDelDia(pago.fechaConfirmacion || pago.fecha, dia);
  }

  const confirmados = pagos.filter((p) => p.estado === "CONFIRMADO" && entroHoy(p));
  const anulados = pagos.filter((p) => p.estado === "ANULADO" && entroHoy(p));

  // los pendientes son de cualquier fecha: justamente lo que hay que revisar
  const pendientes = pagos.filter((p) => p.estado === "PENDIENTE");

  // las ventas de mostrador del dia que no se anularon
  const ventasDelDia = ventas.filter(
    (v) => v.estado !== "ANULADA" && esDelDia(v.fecha, dia)
  );

  const deOrdenes = confirmados.reduce((s, p) => s + Number(p.monto || 0), 0);
  const deMostrador = ventasDelDia.reduce((s, v) => s + Number(v.total || 0), 0);
  const total = deOrdenes + deMostrador;

  // el desglose por medio mezcla las dos cosas: al final del dia lo que
  // importa es cuanto efectivo hay en el cajon, venga de donde venga
  function totalDe(medio) {
    return (
      confirmados
        .filter((p) => p.medio === medio)
        .reduce((s, p) => s + Number(p.monto || 0), 0) +
      ventasDelDia
        .filter((v) => v.medio === medio)
        .reduce((s, v) => s + Number(v.total || 0), 0)
    );
  }

  function cuantosDe(medio) {
    return (
      confirmados.filter((p) => p.medio === medio).length +
      ventasDelDia.filter((v) => v.medio === medio).length
    );
  }

  const usados = MEDIOS.filter((m) => totalDe(m) > 0);

  return (
    <div>
      <div className="metricas">
        <div className="metrica destacada">
          <span className="valor">${total}</span>
          <span className="rotulo">Entro este dia</span>
        </div>
        <div className="metrica">
          <span className="valor">${deOrdenes}</span>
          <span className="rotulo">De ordenes ({confirmados.length})</span>
        </div>
        <div className="metrica">
          <span className="valor">${deMostrador}</span>
          <span className="rotulo">De mostrador ({ventasDelDia.length})</span>
        </div>
        <div className="metrica">
          <span className="valor">{pendientes.length}</span>
          <span className="rotulo">Sin confirmar</span>
        </div>
        <div className="metrica">
          <span className="valor">{anulados.length}</span>
          <span className="rotulo">Anulados</span>
        </div>
      </div>

      <div className="dia-caja">
        <label>
          Dia
          <input
            type="date"
            value={dia}
            onChange={(e) => setDia(e.target.value)}
          />
        </label>
        <button className="secundario" onClick={() => setDia(hoy())}>
          Hoy
        </button>
        <button className="secundario" onClick={traerTodo}>
          Actualizar
        </button>
      </div>

      <h2>Como entro</h2>

      {usados.length === 0 && <p className="vacio">Este dia no entro nada.</p>}

      {usados.length > 0 && (
        <div className="medios">
          {usados.map((m) => (
            <div key={m} className="medio">
              <span className="rotulo">{m.replace("_", " ")}</span>
              <b>${totalDe(m)}</b>
              <span className="cuantos">
                {cuantosDe(m)} {cuantosDe(m) === 1 ? "cobro" : "cobros"}
              </span>
            </div>
          ))}
          <div className="medio total">
            <span className="rotulo">Total del dia</span>
            <b>${total}</b>
            <span className="cuantos">
              {confirmados.length + ventasDelDia.length} comprobantes
            </span>
          </div>
        </div>
      )}

      <h2>Cobros de ordenes</h2>

      {confirmados.length === 0 ? (
        <p className="vacio">Todavia no se confirmo ningun cobro este dia.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Recibo</th>
              <th>Orden</th>
              <th>Medio</th>
              <th>Confirmo</th>
              <th>Hora</th>
              <th className="importe">Monto</th>
            </tr>
          </thead>
          <tbody>
            {confirmados.map((p) => (
              <tr key={p.id_pago}>
                <td className="clave">
                  {String(p.numeroRecibo).padStart(8, "0")}
                </td>
                <td>
                  <b className="clave">{patenteDe(p.id_orden)}</b>
                  <span className="ayuda"> orden {p.id_orden}</span>
                </td>
                <td>{p.medio.replace("_", " ")}</td>
                <td className="nombre">{empleado(p.id_confirmo)}</td>
                <td className="nombre">
                  {(p.fechaConfirmacion || "").substring(11, 16)}
                </td>
                <td className="importe">${p.monto}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <th colSpan="5">Total de ordenes</th>
              <th className="importe">${deOrdenes}</th>
            </tr>
          </tfoot>
        </table>
      )}

      {ventasDelDia.length > 0 && (
        <>
          <h2>Ventas de mostrador</h2>

          <table>
            <thead>
              <tr>
                <th>Comprobante</th>
                <th>Cliente</th>
                <th>Medio</th>
                <th>Hora</th>
                <th className="importe">Monto</th>
              </tr>
            </thead>
            <tbody>
              {ventasDelDia.map((v) => (
                <tr key={v.id_venta}>
                  <td className="clave">
                    {String(v.numeroComprobante).padStart(6, "0")}
                  </td>
                  <td>{v.nombreCliente || "Consumidor final"}</td>
                  <td>{v.medio.replace("_", " ")}</td>
                  <td className="nombre">{(v.fecha || "").substring(11, 16)}</td>
                  <td className="importe">${v.total}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <th colSpan="4">Total de mostrador</th>
                <th className="importe">${deMostrador}</th>
              </tr>
            </tfoot>
          </table>
        </>
      )}

      {pendientes.length > 0 && (
        <>
          <h2>Esperando confirmacion</h2>
          <p className="ayuda">
            Cobros cargados que todavia nadie corroboro. Mientras esten aca la
            orden sigue impaga y el vehiculo no se puede entregar.
          </p>

          <table>
            <thead>
              <tr>
                <th>Orden</th>
                <th>Medio</th>
                <th>Cargo</th>
                <th>Cuando</th>
                <th className="importe">Monto</th>
              </tr>
            </thead>
            <tbody>
              {pendientes.map((p) => (
                <tr key={p.id_pago}>
                  <td>
                    <b className="clave">{patenteDe(p.id_orden)}</b>
                    <span className="ayuda"> orden {p.id_orden}</span>
                  </td>
                  <td>{p.medio.replace("_", " ")}</td>
                  <td className="nombre">{empleado(p.id_empleado)}</td>
                  <td className="nombre">
                    {fechaHora(p.fecha)}
                  </td>
                  <td className="importe">${p.monto}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      {anulados.length > 0 && (
        <>
          <h2>Anulados este dia</h2>

          <table>
            <thead>
              <tr>
                <th>Recibo</th>
                <th>Orden</th>
                <th>Anulo</th>
                <th>Motivo</th>
                <th className="importe">Monto</th>
              </tr>
            </thead>
            <tbody>
              {anulados.map((p) => (
                <tr key={p.id_pago} className="anulada">
                  <td className="clave">
                    {p.numeroRecibo
                      ? String(p.numeroRecibo).padStart(8, "0")
                      : "sin emitir"}
                  </td>
                  <td>
                    <b className="clave">{patenteDe(p.id_orden)}</b>
                    <span className="ayuda"> orden {p.id_orden}</span>
                  </td>
                  <td className="nombre">{empleado(p.id_anulo)}</td>
                  <td>{p.motivoAnulacion}</td>
                  <td className="importe">${p.monto}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
}

export default Caja;
