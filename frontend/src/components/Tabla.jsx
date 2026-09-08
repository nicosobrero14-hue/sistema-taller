import { useEffect, useRef, useState } from "react";
import { ordenar, coincide } from "../tablas";

/**
 * Lo que le da a una tabla el orden por columna y el buscador del click
 * derecho. Se usa asi:
 *
 *     const tabla = useTabla(filtrados);
 *     ...
 *     <table onContextMenu={tabla.alClickDerecho}>
 *       <thead><tr><Th tabla={tabla} campo="codigo">Codigo</Th> ... </tr></thead>
 *       <tbody>{tabla.filas.map(...)}</tbody>
 *     </table>
 *     {tabla.buscador}
 */
export function useTabla(datos) {
  const [campo, setCampo] = useState("");
  const [sentido, setSentido] = useState("asc");

  // el buscador que sale con el click derecho
  const [busca, setBusca] = useState("");
  const [donde, setDonde] = useState(null);

  function ordenarPor(nuevo) {
    if (campo === nuevo) {
      // segundo click: al reves. Tercero: se saca el orden y vuelve como venia.
      if (sentido === "asc") {
        setSentido("desc");
      } else {
        setCampo("");
        setSentido("asc");
      }
      return;
    }

    setCampo(nuevo);
    setSentido("asc");
  }

  function alClickDerecho(e) {
    e.preventDefault();
    setDonde({ x: e.clientX, y: e.clientY });
  }

  function cerrar() {
    setDonde(null);
    setBusca("");
  }

  const filas = ordenar(
    (datos || []).filter((fila) => coincide(fila, busca)),
    campo,
    sentido
  );

  return {
    filas: filas,
    campo: campo,
    sentido: sentido,
    ordenarPor: ordenarPor,
    alClickDerecho: alClickDerecho,
    buscador: (
      <BuscadorDeTabla
        donde={donde}
        busca={busca}
        setBusca={setBusca}
        cuantas={filas.length}
        total={(datos || []).length}
        onCerrar={cerrar}
      />
    ),
  };
}

/**
 * El encabezado de una columna que se puede ordenar. Un click ordena de
 * la A a la Z, otro al reves, y el tercero saca el orden.
 */
export function Th({ tabla, campo, className, children }) {
  const activa = tabla.campo === campo;

  return (
    <th
      className={
        "ordenable" + (activa ? " ordenando " + tabla.sentido : "") +
        (className ? " " + className : "")
      }
      onClick={() => tabla.ordenarPor(campo)}
      title="Ordenar por esta columna"
    >
      {children}
      <span className="flecha" aria-hidden="true" />
    </th>
  );
}

/**
 * El buscador flotante. Aparece donde se hizo click derecho y filtra la
 * tabla mientras se escribe. Sirve tambien en las tablas que no tienen
 * un campo de busqueda propio.
 */
function BuscadorDeTabla({ donde, busca, setBusca, cuantas, total, onCerrar }) {
  const caja = useRef(null);
  const campo = useRef(null);

  useEffect(() => {
    if (donde && campo.current) {
      campo.current.focus();
    }
  }, [donde]);

  useEffect(() => {
    if (!donde) {
      return;
    }

    function afuera(e) {
      if (caja.current && !caja.current.contains(e.target)) {
        onCerrar();
      }
    }

    function tecla(e) {
      if (e.key === "Escape") {
        onCerrar();
      }
    }

    document.addEventListener("mousedown", afuera);
    document.addEventListener("keydown", tecla);

    return () => {
      document.removeEventListener("mousedown", afuera);
      document.removeEventListener("keydown", tecla);
    };
  }, [donde, onCerrar]);

  if (!donde) {
    return null;
  }

  // que no se salga de la pantalla si el click fue contra el borde
  const x = Math.min(donde.x, window.innerWidth - 280);
  const y = Math.min(donde.y, window.innerHeight - 110);

  return (
    <div
      className="buscador-tabla"
      ref={caja}
      style={{ left: x + "px", top: y + "px" }}
    >
      <input
        ref={campo}
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
        placeholder="Buscar en la tabla..."
      />
      <div className="pie-buscador">
        <span>
          {busca ? cuantas + " de " + total : total}{" "}
          {total === 1 ? "fila" : "filas"}
        </span>
        <button onClick={onCerrar}>Cerrar</button>
      </div>
    </div>
  );
}
