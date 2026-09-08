import { useEffect, useState } from "react";
import { pedir, leerRespuesta, esAdmin } from "../api";
import { confirmar, pedirTexto } from "../avisos";
import { fecha, fechaHora, hoy } from "../fechas";

const MEDIOS = [
  "EFECTIVO",
  "TRANSFERENCIA",
  "TARJETA_DEBITO",
  "TARJETA_CREDITO",
  "QR",
];

function CobrarOrden({ orden, vehiculos, onCerrar, onCambio }) {
  const [pago, setPago] = useState(null);
  // todos los cobros de la orden, anulados incluidos
  const [historial, setHistorial] = useState([]);
  const [taller, setTaller] = useState(null);
  const [detalle, setDetalle] = useState(null);
  const [mensaje, setMensaje] = useState("");

  const [medio, setMedio] = useState("EFECTIVO");
  const [observaciones, setObservaciones] = useState("");

  function traerTodo() {
    // el cobro, si es que ya se hizo
    pedir("/pagos/orden/" + orden.id_orden)
      .then((res) => (res.ok ? res.text() : ""))
      .then((texto) => setPago(texto ? JSON.parse(texto) : null))
      .catch(() => setPago(null));

    // lo que se anulo tambien se muestra: no desaparece
    pedir("/pagos/orden/" + orden.id_orden + "/historial")
      .then((res) => (res.ok ? res.json() : []))
      .then((data) => setHistorial(data))
      .catch(() => setHistorial([]));

    // la orden con sus items, para el detalle del recibo
    pedir("/ordenes/traer/" + orden.id_orden)
      .then((res) => res.json())
      .then((data) => setDetalle(data))
      .catch(() => setDetalle(null));

    // El membrete del recibo y adonde paga el cliente. Va por /cobro y no
    // por /traer: esa ruta es solo de ADMIN y dejaba al mecanico imprimiendo
    // recibos sin encabezado.
    pedir("/configuracion/cobro")
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => setTaller(data))
      .catch(() => setTaller(null));
  }

  useEffect(() => {
    traerTodo();
  }, [orden]);

  async function cobrar(e) {
    e.preventDefault();

    const seguro = await confirmar({
      titulo: "Registrar el cobro",
      texto: "$" + orden.total + " en " + medio.replace("_", " ") + ".",
      detalle: "Queda pendiente hasta que confirmes que la plata entro.",
      boton: "Registrar",
    });

    if (!seguro) {
      return;
    }

    pedir("/pagos/crear", {
      method: "POST",
      body: JSON.stringify({
        id_orden: orden.id_orden,
        monto: Number(orden.total),
        medio: medio,
        observaciones: observaciones,
      }),
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerTodo();
        onCambio();
      });
  }

  async function confirmarCobro() {
    const seguro = await confirmar({
      titulo: "Confirmar que entro la plata",
      texto: "$" + pago.monto + " de la orden " + orden.id_orden + ".",
      detalle:
        "Recien ahi se emite el recibo y el vehiculo se puede entregar. " +
        "Confirmalo cuando lo veas en la cuenta.",
      boton: "Confirmar",
      tono: "verde",
    });

    if (!seguro) {
      return;
    }

    pedir("/pagos/confirmar/" + pago.id_pago, { method: "PUT" })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerTodo();
        onCambio();
      });
  }

  // Anular no borra: el cobro queda con su motivo a la vista y el numero
  // de recibo quemado. Por eso el motivo es obligatorio.
  async function anular() {
    const motivo = await pedirTexto({
      titulo: "Anular el cobro",
      texto:
        "El cobro no se borra: queda con el motivo a la vista y el numero de " +
        "recibo sigue usado. La orden vuelve a quedar impaga.",
      etiqueta: "Por que se anula",
      placeholder: "Ej: el banco rechazo la transferencia",
      boton: "Anular cobro",
      tono: "peligro",
    });

    if (motivo === null || !motivo.trim()) {
      return;
    }

    pedir("/pagos/anular/" + pago.id_pago + "?motivo=" + encodeURIComponent(motivo), {
      method: "PUT",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerTodo();
        onCambio();
      });
  }

  // Imprime solo el recibo: el CSS de impresion esconde todo lo demas.
  // Desde el dialogo del navegador se puede guardar como PDF.
  function imprimir() {
    window.print();
  }

  function patente() {
    const v = vehiculos.find((x) => x.id_vehiculo === orden.id_vehiculo);
    return v ? v.patente : "Vehiculo " + orden.id_vehiculo;
  }

  // el recibo existe solo si alguien confirmo que la plata entro
  const confirmado = pago && pago.estado === "CONFIRMADO";

  // el ultimo cobro anulado de esta orden, para dejarlo a la vista
  const anulado = historial.find((p) => p.estado === "ANULADO");

  // Los precios ya llevan el IVA adentro: esto no suma nada, solo dice
  // cuanto de lo que se cobro es impuesto. Sale de los datos del taller.
  const iva = taller && taller.porcentajeIva ? Number(taller.porcentajeIva) : 0;

  function ivaIncluido(monto) {
    return Math.round((Number(monto) * iva) / (100 + iva));
  }

  return (
    <div className="panel">
      <h3>
        <span>Cobro de la orden {orden.id_orden}</span>
        <button className="secundario no-imprimir" onClick={onCerrar}>
          Cerrar
        </button>
      </h3>

      {!pago && (
        <>
          <form onSubmit={cobrar} className="no-imprimir">
            <select value={medio} onChange={(e) => setMedio(e.target.value)}>
              {MEDIOS.map((m) => (
                <option key={m} value={m}>
                  {m.replace("_", " ")}
                </option>
              ))}
            </select>
            <input
              placeholder="Observaciones (opcional)"
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
            />
            <button type="submit">Registrar cobro de ${orden.total}</button>
          </form>

          <DatosDelMedio medio={medio} taller={taller} />

          <p className="ayuda no-imprimir">
            Se cobra el total de una sola vez. El cobro queda pendiente hasta
            que alguien confirme que la plata entro: recien ahi se emite el
            recibo y el vehiculo se puede entregar.
          </p>
        </>
      )}

      {mensaje && <p className="mensaje no-imprimir">{mensaje}</p>}

      {/* Anulado: queda a la vista con el motivo, no se borra */}
      {anulado && (
        <div className="cobrado anulado no-imprimir">
          <div>
            <b>
              Cobro anulado
              {anulado.numeroRecibo
                ? " · recibo N° " + String(anulado.numeroRecibo).padStart(8, "0")
                : ""}
            </b>
            <span>
              {anulado.motivoAnulacion} ·{" "}
              {fechaHora(anulado.fechaAnulacion)}
            </span>
          </div>
          <span className="monto-cobrado">${anulado.monto}</span>
        </div>
      )}

      {/* Cargado pero sin confirmar: no hay recibo todavia */}
      {pago && !confirmado && (
        <>
          <div className="cobrado pendiente no-imprimir">
            <div>
              <b>Cobro pendiente de confirmacion</b>
              <span>
                {pago.medio.replace("_", " ")} · cargado el{" "}
                {fechaHora(pago.fecha)}
                {pago.observaciones ? " · " + pago.observaciones : ""}
              </span>
            </div>
            <span className="monto-cobrado">${pago.monto}</span>
          </div>

          <div className="acciones-recibo no-imprimir">
            <button className="confirmar-cobro" onClick={confirmarCobro}>
              Confirmar que entro la plata
            </button>
            {esAdmin() && <button onClick={anular}>Anular cobro</button>}
          </div>

          <p className="ayuda no-imprimir">
            Confirmalo recien cuando lo veas en la cuenta o el cupon este
            aprobado. Hasta entonces la orden no se puede pasar a ENTREGADO.
          </p>
        </>
      )}

      {pago && confirmado && (
        <>
          <div className="cobrado no-imprimir">
            <div>
              <b>Orden cobrada</b>
              <span>
                Recibo N° {String(pago.numeroRecibo).padStart(8, "0")} ·{" "}
                {pago.medio.replace("_", " ")} · confirmado el{" "}
                {fechaHora(pago.fechaConfirmacion)}
              </span>
            </div>
            <span className="monto-cobrado">${pago.monto}</span>
          </div>

          <div className="acciones-recibo no-imprimir">
            <button className="secundario" onClick={imprimir}>
              Imprimir recibo
            </button>
            {esAdmin() && <button onClick={anular}>Anular cobro</button>}
          </div>

          {/* Esto es lo unico que sale en el papel */}
          <div className="recibo">
            <div className="recibo-cabecera">
              <div>
                <b>{taller && taller.nombreFantasia ? taller.nombreFantasia : "Taller Mecanico"}</b>
                {taller && taller.razonSocial && <span>{taller.razonSocial}</span>}
                {taller && taller.cuit && <span>CUIT {taller.cuit}</span>}
                {taller && taller.domicilio && <span>{taller.domicilio}</span>}
              </div>
              <div className="recibo-numero">
                <span>RECIBO</span>
                <b>N° {String(pago.numeroRecibo).padStart(8, "0")}</b>
                <span>{fecha(hoy())}</span>
              </div>
            </div>

            <div className="recibo-datos">
              <span>
                <b>Orden</b> {orden.id_orden}
              </span>
              <span>
                <b>Vehiculo</b> {patente()}
              </span>
              <span>
                <b>Trabajo</b> {orden.diagnostico || "-"}
              </span>
            </div>

            <table className="recibo-tabla">
              <thead>
                <tr>
                  <th>Detalle</th>
                  <th className="numero">Cant.</th>
                  <th className="importe">Precio</th>
                  <th className="importe">Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {((detalle && detalle.items) || []).map((i) => (
                  <tr key={i.id_item}>
                    <td>{i.descripcion}</td>
                    <td className="numero">{i.cantidad}</td>
                    <td className="importe">${i.precioUnitario}</td>
                    <td className="importe">${i.cantidad * i.precioUnitario}</td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                {detalle && Number(detalle.descuento) > 0 && (
                  <>
                    <tr>
                      <th colSpan="3" className="sin-relieve">
                        Subtotal
                      </th>
                      <th className="importe sin-relieve">
                        ${detalle.subtotal}
                      </th>
                    </tr>
                    <tr>
                      <th colSpan="3" className="sin-relieve">
                        Descuento
                      </th>
                      <th className="importe sin-relieve">
                        -${detalle.descuento}
                      </th>
                    </tr>
                  </>
                )}
                <tr>
                  <th colSpan="3">TOTAL</th>
                  <th className="importe">${pago.monto}</th>
                </tr>
                {iva > 0 && (
                  <tr>
                    <th colSpan="3" className="sin-relieve">
                      IVA {iva}% incluido
                    </th>
                    <th className="importe sin-relieve">
                      ${ivaIncluido(pago.monto)}
                    </th>
                  </tr>
                )}
              </tfoot>
            </table>

            <div className="recibo-pie">
              <span>
                Abonado en <b>{pago.medio.replace("_", " ")}</b>
                {pago.observaciones ? " — " + pago.observaciones : ""}
              </span>
              <span className="recibo-aclaracion">
                Comprobante interno sin valor fiscal. No valido como factura.
              </span>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

/**
 * Lo que hay que decirle al cliente segun como quiera pagar. Sale de los
 * datos del taller: si estan vacios, no se muestra nada en vez de mostrar
 * un cuadro con renglones en blanco.
 */
function DatosDelMedio({ medio, taller }) {
  const [copiado, setCopiado] = useState("");

  if (!taller) {
    return null;
  }

  function copiar(texto, que) {
    navigator.clipboard.writeText(texto).then(() => {
      setCopiado(que);
      setTimeout(() => setCopiado(""), 2000);
    });
  }

  if (medio === "EFECTIVO") {
    return null;
  }

  if (medio === "TRANSFERENCIA") {
    if (!taller.cbu && !taller.aliasCbu) {
      return (
        <p className="ayuda">
          Todavia no hay ningun CBU cargado. Se carga en Datos del taller.
        </p>
      );
    }

    return (
      <div className="datos-pago">
        <h4>Para transferir</h4>

        {taller.titularCuenta && (
          <div className="dato-pago">
            <span>Titular</span>
            <b>{taller.titularCuenta}</b>
          </div>
        )}

        {taller.banco && (
          <div className="dato-pago">
            <span>Banco</span>
            <b>{taller.banco}</b>
          </div>
        )}

        {taller.cbu && (
          <div className="dato-pago">
            <span>CBU / CVU</span>
            <b className="clave">{taller.cbu}</b>
            <button type="button" onClick={() => copiar(taller.cbu, "cbu")}>
              {copiado === "cbu" ? "Copiado" : "Copiar"}
            </button>
          </div>
        )}

        {taller.aliasCbu && (
          <div className="dato-pago">
            <span>Alias</span>
            <b className="clave">{taller.aliasCbu}</b>
            <button type="button" onClick={() => copiar(taller.aliasCbu, "alias")}>
              {copiado === "alias" ? "Copiado" : "Copiar"}
            </button>
          </div>
        )}

        <p className="ayuda">
          Registra el cobro igual, pero confirmalo recien cuando veas la
          transferencia acreditada.
        </p>
      </div>
    );
  }

  if (medio === "QR") {
    return taller.datosQr ? (
      <div className="datos-pago">
        <h4>Para pagar con QR</h4>
        <p className="texto-pago">{taller.datosQr}</p>
      </div>
    ) : null;
  }

  // las dos tarjetas comparten la misma aclaracion
  return taller.datosTarjeta ? (
    <div className="datos-pago">
      <h4>Para pagar con tarjeta</h4>
      <p className="texto-pago">{taller.datosTarjeta}</p>
      <p className="ayuda">
        Confirma el cobro cuando el cupon salga aprobado.
      </p>
    </div>
  ) : null;
}

export default CobrarOrden;
