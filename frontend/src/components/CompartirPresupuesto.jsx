import { useEffect, useState } from "react";
import { pedir } from "../api";
import { fecha } from "../fechas";

/**
 * Lo que se le manda al cliente.
 *
 * Va con TODAS las opciones y el total de cada una: el cliente lo recibe,
 * decide cual quiere y avisa. Recien ahi se marca esa opcion en el sistema
 * y se acepta el presupuesto, que es lo que genera la orden de trabajo.
 */
function CompartirPresupuesto({ presupuesto, patente, onCerrar }) {
  const [taller, setTaller] = useState(null);
  const [copiado, setCopiado] = useState(false);

  useEffect(() => {
    // los datos del taller solo los ve un ADMIN; si no vienen,
    // el presupuesto sale con un encabezado generico
    pedir("/configuracion/traer")
      .then((res) => (res.ok ? res.json() : null))
      .then((c) => setTaller(c))
      .catch(() => setTaller(null));
  }, []);

  const totales = presupuesto.totalesOpciones || {};
  const numeros = Object.keys(totales).map(Number).sort();

  // los repuestos que van si o si, sin importar que opcion elija
  const comunes = (presupuesto.items || []).filter((i) => !i.opcion);

  function itemsDe(n) {
    return (presupuesto.items || []).filter((i) => i.opcion === n);
  }

  function nombreTaller() {
    return taller && taller.nombreFantasia ? taller.nombreFantasia : "Taller Mecanico";
  }

  // El mismo texto sirve para WhatsApp y para el mail. Se arma en plano
  // porque ninguno de los dos acepta formato.
  function textoPlano() {
    const lineas = [];

    lineas.push("*" + nombreTaller() + "*");
    lineas.push("Presupuesto N° " + presupuesto.id_presupuesto + " - " + patente);
    lineas.push("");

    if (presupuesto.detalleTrabajo) {
      lineas.push(presupuesto.detalleTrabajo);
      lineas.push("");
    }

    if (comunes.length > 0 || Number(presupuesto.totalManoObra) > 0) {
      lineas.push(numeros.length > 1 ? "*Incluye en todas las opciones:*" : "*Detalle:*");

      comunes.forEach((i) => {
        lineas.push("- " + i.descripcion + ": " + i.cantidad + " x $" + i.precioUnitario);
      });

      if (Number(presupuesto.totalManoObra) > 0) {
        lineas.push(
          "- Mano de obra (" + presupuesto.horasEstimadas + " hs estimadas): $" + presupuesto.totalManoObra
        );
      }

      lineas.push("");
    }

    if (numeros.length === 0) {
      lineas.push("*TOTAL: $" + presupuesto.total + "*");
    } else {
      numeros.forEach((n) => {
        lineas.push("*OPCION " + n + "*");

        itemsDe(n).forEach((i) => {
          lineas.push("- " + i.descripcion + ": " + i.cantidad + " x $" + i.precioUnitario);
        });

        lineas.push("*Total opcion " + n + ": $" + totales[n] + "*");
        lineas.push("");
      });

      lineas.push("Avisanos cual opcion preferis y coordinamos el turno.");
    }

    lineas.push("");
    lineas.push("Precio valido hasta el " + fecha(presupuesto.fechaVencimiento) + ".");
    lineas.push("Las horas son estimadas: si el trabajo lleva mas, se informa antes de seguir.");

    if (taller && taller.telefono) {
      lineas.push("");
      lineas.push("Consultas: " + taller.telefono);
    }

    return lineas.join("\n");
  }

  function porWhatsapp() {
    window.open("https://wa.me/?text=" + encodeURIComponent(textoPlano()), "_blank");
  }

  function porEmail() {
    const asunto = "Presupuesto N° " + presupuesto.id_presupuesto + " - " + patente;

    window.location.href =
      "mailto:?subject=" + encodeURIComponent(asunto) + "&body=" + encodeURIComponent(textoPlano());
  }

  function copiar() {
    navigator.clipboard.writeText(textoPlano()).then(() => {
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2500);
    });
  }

  // Imprimir y "generar PDF" son lo mismo: en el dialogo del navegador
  // se elige la impresora o "Guardar como PDF".
  function imprimir() {
    window.print();
  }

  return (
    <div className="panel">
      <h3 className="no-imprimir">
        <span>Compartir presupuesto {presupuesto.id_presupuesto}</span>
        <button className="secundario" onClick={onCerrar}>
          Cerrar
        </button>
      </h3>

      <div className="acciones-compartir no-imprimir">
        <button className="wp" onClick={porWhatsapp}>
          WhatsApp
        </button>
        <button className="secundario" onClick={porEmail}>
          Email
        </button>
        <button className="secundario" onClick={imprimir}>
          Imprimir o guardar PDF
        </button>
        <button className="secundario" onClick={copiar}>
          {copiado ? "Copiado!" : "Copiar texto"}
        </button>
      </div>

      <p className="ayuda no-imprimir">
        {numeros.length > 1
          ? "Se manda con las " + numeros.length + " opciones para que el cliente elija. Cuando avise cual quiere, marcala en Ver y ahi acepta el presupuesto."
          : "Para el PDF, elegi Guardar como PDF en el destino del dialogo de impresion."}
      </p>

      {/* Esto es lo unico que sale al imprimir */}
      <div className="recibo">
        <div className="recibo-cabecera">
          <div>
            <b>{nombreTaller()}</b>
            {taller && taller.razonSocial && <span>{taller.razonSocial}</span>}
            {taller && taller.cuit && <span>CUIT {taller.cuit}</span>}
            {taller && taller.domicilio && <span>{taller.domicilio}</span>}
            {taller && taller.telefono && <span>Tel {taller.telefono}</span>}
          </div>
          <div className="recibo-numero">
            <span>PRESUPUESTO</span>
            <b>N° {String(presupuesto.id_presupuesto).padStart(6, "0")}</b>
            <span>{fecha(presupuesto.fechaEmision)}</span>
          </div>
        </div>

        <div className="recibo-datos">
          <span>
            <b>Vehiculo</b> {patente}
          </span>
          <span>
            <b>Valido hasta</b> {fecha(presupuesto.fechaVencimiento)}
          </span>
        </div>

        {presupuesto.detalleTrabajo && (
          <p className="recibo-detalle">{presupuesto.detalleTrabajo}</p>
        )}

        {/* lo que va si o si */}
        {(comunes.length > 0 || Number(presupuesto.totalManoObra) > 0) && (
          <>
            {numeros.length > 1 && <h4 className="titulo-bloque">Incluye en todas las opciones</h4>}

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
                {comunes.map((i) => (
                  <tr key={i.id_item}>
                    <td>{i.descripcion}</td>
                    <td className="numero">{i.cantidad}</td>
                    <td className="importe">${i.precioUnitario}</td>
                    <td className="importe">${i.cantidad * i.precioUnitario}</td>
                  </tr>
                ))}
                {Number(presupuesto.totalManoObra) > 0 && (
                  <tr>
                    <td>
                      Mano de obra ({presupuesto.horasEstimadas} hs estimadas x $
                      {presupuesto.precioHora})
                    </td>
                    <td colSpan="2"></td>
                    <td className="importe">${presupuesto.totalManoObra}</td>
                  </tr>
                )}
              </tbody>
              {numeros.length === 0 && (
                <tfoot>
                  <tr>
                    <th colSpan="3">TOTAL</th>
                    <th className="importe">${presupuesto.total}</th>
                  </tr>
                </tfoot>
              )}
            </table>
          </>
        )}

        {/* cada opcion con su total, para que el cliente compare */}
        {numeros.map((n) => (
          <div key={n} className="opcion-impresa">
            <h4 className="titulo-bloque">Opcion {n}</h4>

            <table className="recibo-tabla">
              <tbody>
                {itemsDe(n).map((i) => (
                  <tr key={i.id_item}>
                    <td>{i.descripcion}</td>
                    <td className="numero">{i.cantidad}</td>
                    <td className="importe">${i.precioUnitario}</td>
                    <td className="importe">${i.cantidad * i.precioUnitario}</td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr>
                  <th colSpan="3">TOTAL OPCION {n}</th>
                  <th className="importe">${totales[n]}</th>
                </tr>
              </tfoot>
            </table>
          </div>
        ))}

        <div className="recibo-pie">
          {numeros.length > 1 && (
            <span className="elegir-opcion">
              Elegi una de las {numeros.length} opciones y avisanos para coordinar
              el turno.
            </span>
          )}
          <span>
            Precio valido hasta el <b>{fecha(presupuesto.fechaVencimiento)}</b>.
            Pasada esa fecha hay que actualizarlo.
          </span>
          <span className="recibo-aclaracion">
            Las horas de mano de obra son estimadas. Si el trabajo lleva mas
            tiempo del previsto, se informa antes de continuar. Comprobante
            interno sin valor fiscal.
          </span>
        </div>
      </div>
    </div>
  );
}

export default CompartirPresupuesto;
