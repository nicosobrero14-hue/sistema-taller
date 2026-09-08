import { useEffect, useState } from "react";
import { pedir } from "../api";
import { fechaHora } from "../fechas";

/**
 * Quien movio la orden y cuando. Contesta "¿quien la paso a ENTREGADO?",
 * que hasta ahora no tenia respuesta en ningun lado.
 */
function LineaTiempo({ id_orden }) {
  const [movimientos, setMovimientos] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [abierto, setAbierto] = useState(false);

  useEffect(() => {
    pedir("/ordenes/movimientos/" + id_orden)
      .then((res) => res.json())
      .then((data) => setMovimientos(data))
      .catch(() => setMovimientos([]));

    pedir("/empleados/traer")
      .then((res) => res.json())
      .then((data) => setEmpleados(data))
      .catch(() => setEmpleados([]));
  }, [id_orden]);

  function quien(id) {
    const e = empleados.find((x) => x.id_empleado === id);
    return e ? e.nombre + " " + e.apellido : "alguien sin identificar";
  }

  if (movimientos.length === 0) {
    return null;
  }

  // se muestran los ultimos y el resto queda plegado: una orden vieja
  // puede tener veinte movimientos y no aportan nada de entrada
  const visibles = abierto ? movimientos : movimientos.slice(-3);

  return (
    <div className="linea-tiempo">
      <h4>
        Movimientos
        {movimientos.length > 3 && (
          <button className="ver-todos" onClick={() => setAbierto(!abierto)}>
            {abierto ? "ver menos" : "ver los " + movimientos.length}
          </button>
        )}
      </h4>

      <ul>
        {visibles.map((m) => (
          <li key={m.id_movimiento}>
            <span className="cuando">{fechaHora(m.fecha)}</span>
            <span className="que">
              {m.detalle ? (
                <b>{m.detalle}</b>
              ) : (
                <>
                  {m.estadoAnterior && (
                    <span className="desde">
                      {m.estadoAnterior.replace("_", " ")} →{" "}
                    </span>
                  )}
                  <b>{m.estadoNuevo.replace("_", " ")}</b>
                </>
              )}
            </span>
            <span className="quien">{quien(m.id_empleado)}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default LineaTiempo;
