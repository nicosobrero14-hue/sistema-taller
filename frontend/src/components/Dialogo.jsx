import { useEffect, useRef, useState } from "react";
import { registrarDialogo } from "../avisos";

/**
 * El cartel de confirmacion de la casa, en vez del del navegador.
 *
 * Se monta una sola vez en App y queda esperando. Las funciones de
 * avisos.js son las que lo abren.
 */
function Dialogo() {
  const [pedido, setPedido] = useState(null);
  const [texto, setTexto] = useState("");
  const campo = useRef(null);
  const principal = useRef(null);

  useEffect(() => {
    registrarDialogo((opciones) => {
      // si es una lista, arranca con el primero elegido
      setTexto(
        opciones.pideOpcion && opciones.lista && opciones.lista.length
          ? String(opciones.lista[0].valor)
          : ""
      );
      setPedido(opciones);
    });
  }, []);

  // al abrirse, el cursor va donde hay que escribir o al boton principal
  useEffect(() => {
    if (!pedido) {
      return;
    }

    if ((pedido.pideTexto || pedido.pideOpcion) && campo.current) {
      campo.current.focus();
    } else if (principal.current) {
      principal.current.focus();
    }
  }, [pedido]);

  function cerrar(respuesta) {
    pedido.resolver(respuesta);
    setPedido(null);
    setTexto("");
  }

  function aceptar() {
    if (pedido.pideTexto || pedido.pideOpcion) {
      cerrar(texto);
    } else {
      cerrar(true);
    }
  }

  function cancelar() {
    cerrar(pedido.pideTexto || pedido.pideOpcion ? null : false);
  }

  function tecla(e) {
    if (e.key === "Escape") {
      cancelar();
    }

    // Enter confirma, salvo que estemos escribiendo en un texto largo
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      aceptar();
    }
  }

  if (!pedido) {
    return null;
  }

  const tono = pedido.tono || "";
  const vacio = (pedido.pideTexto || pedido.pideOpcion) && !texto.trim();

  return (
    <div className="fondo-dialogo" onMouseDown={cancelar}>
      <div
        className={"dialogo " + tono}
        onMouseDown={(e) => e.stopPropagation()}
        onKeyDown={tecla}
        role="dialog"
        aria-modal="true"
      >
        <h3>{pedido.titulo || "Confirmar"}</h3>

        {pedido.texto && <p>{pedido.texto}</p>}
        {pedido.detalle && <p className="detalle-dialogo">{pedido.detalle}</p>}

        {pedido.pideTexto && (
          <label>
            {pedido.etiqueta || "Motivo"}
            <input
              ref={campo}
              value={texto}
              onChange={(e) => setTexto(e.target.value)}
              placeholder={pedido.placeholder || ""}
            />
          </label>
        )}

        {pedido.pideOpcion && (
          <label>
            {pedido.etiqueta || "Elegi una opcion"}
            <select
              ref={campo}
              value={texto}
              onChange={(e) => setTexto(e.target.value)}
            >
              {(pedido.lista || []).map((o) => (
                <option key={o.valor} value={o.valor}>
                  {o.texto}
                </option>
              ))}
            </select>
          </label>
        )}

        <div className="botones-dialogo">
          {!pedido.soloAviso && (
            <button className="cancelar" onClick={cancelar}>
              Cancelar
            </button>
          )}
          <button
            ref={principal}
            className={"aceptar " + tono}
            onClick={aceptar}
            disabled={vacio}
          >
            {pedido.boton || (pedido.soloAviso ? "Entendido" : "Confirmar")}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Dialogo;
