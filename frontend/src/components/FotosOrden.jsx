import { useEffect, useRef, useState } from "react";
import { pedir, pedirArchivo, leerRespuesta } from "../api";
import { confirmar } from "../avisos";
import Foto from "./Foto";

const MOMENTOS = [
  { valor: "INGRESO", texto: "Al ingresar", ayuda: "Como llego el vehiculo" },
  { valor: "DIAGNOSTICO", texto: "Diagnostico", ayuda: "El problema encontrado" },
  { valor: "TRABAJO", texto: "Durante el trabajo", ayuda: "La reparacion en curso" },
  { valor: "ENTREGA", texto: "Al entregar", ayuda: "Como se entrega" },
];

function FotosOrden({ orden, onCerrar }) {
  const [fotos, setFotos] = useState([]);
  const [mensaje, setMensaje] = useState("");
  const [subiendo, setSubiendo] = useState(false);
  const [momento, setMomento] = useState("INGRESO");
  const [descripcion, setDescripcion] = useState("");
  const [mirando, setMirando] = useState(null);

  // el input de archivo queda escondido: se abre desde el boton
  const inputArchivo = useRef(null);

  function traerFotos() {
    pedir("/fotos/orden/" + orden.id_orden)
      .then((res) => res.json())
      .then((data) => setFotos(data))
      .catch(() => setFotos([]));
  }

  useEffect(() => {
    traerFotos();
  }, [orden]);

  // El boton abre el explorador de archivos. En el celular, con
  // accept="image/*" el navegador ofrece sacar la foto con la camara.
  function elegirArchivo() {
    inputArchivo.current.click();
  }

  function subir(e) {
    const archivo = e.target.files[0];

    if (!archivo) {
      return;
    }

    const formulario = new FormData();

    formulario.append("id_orden", orden.id_orden);
    formulario.append("archivo", archivo);
    formulario.append("momento", momento);
    formulario.append("descripcion", descripcion);

    setSubiendo(true);

    pedirArchivo("/fotos/subir", formulario)
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        setDescripcion("");
        setSubiendo(false);
        traerFotos();
      })
      .catch(() => {
        setMensaje("No se pudo subir la foto");
        setSubiendo(false);
      });

    // se limpia para poder volver a elegir el mismo archivo
    e.target.value = "";
  }

  async function borrarFoto(id) {
    const seguro = await confirmar({
      titulo: "Borrar foto",
      texto: "La foto se borra del servidor y no se puede recuperar.",
      boton: "Borrar",
      tono: "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/fotos/borrar/" + id, { method: "DELETE" })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        setMirando(null);
        traerFotos();
      });
  }

  // las fotos agrupadas por momento, que es lo que las hace utiles
  function fotosDe(valor) {
    return fotos.filter((f) => f.momento === valor);
  }

  return (
    <div className="panel">
      <h3>
        <span>
          Fotos de la orden {orden.id_orden} ({fotos.length})
        </span>
        <button className="secundario" onClick={onCerrar}>
          Cerrar
        </button>
      </h3>

      <div className="subir-foto">
        <select value={momento} onChange={(e) => setMomento(e.target.value)}>
          {MOMENTOS.map((m) => (
            <option key={m.valor} value={m.valor}>
              {m.texto} — {m.ayuda}
            </option>
          ))}
        </select>
        <input
          placeholder="Que se ve en la foto (opcional)"
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
        />
        <button
          type="button"
          className="secundario"
          onClick={elegirArchivo}
          disabled={subiendo}
        >
          {subiendo ? "Subiendo..." : "Elegir foto"}
        </button>

        <input
          ref={inputArchivo}
          type="file"
          accept="image/*"
          onChange={subir}
          hidden
        />
      </div>

      <p className="ayuda">
        Las fotos <b>Al ingresar</b> dejan registrado como llego el vehiculo:
        son las que sirven si despues hay un reclamo.
      </p>

      {mensaje && <p className="mensaje">{mensaje}</p>}

      {fotos.length === 0 && (
        <p className="vacio">Esta orden todavia no tiene fotos.</p>
      )}

      {MOMENTOS.map((m) =>
        fotosDe(m.valor).length === 0 ? null : (
          <div key={m.valor} className="grupo-fotos">
            <h4>
              {m.texto}
              <span>{fotosDe(m.valor).length}</span>
            </h4>
            <div className="galeria">
              {fotosDe(m.valor).map((f) => (
                <figure key={f.id_foto} onClick={() => setMirando(f)}>
                  <Foto archivo={f.archivo} mini={f.miniatura} alt={f.descripcion || m.texto} />
                  <figcaption>
                    {f.descripcion || "Sin descripcion"}
                    <span>{f.fecha ? f.fecha.substring(0, 10) : ""}</span>
                  </figcaption>
                </figure>
              ))}
            </div>
          </div>
        )
      )}

      {mirando && (
        <div className="visor" onClick={() => setMirando(null)}>
          <div className="visor-caja" onClick={(e) => e.stopPropagation()}>
            <Foto archivo={mirando.archivo} alt={mirando.descripcion || ""} />
            <div className="visor-pie">
              <div>
                <b>{mirando.descripcion || "Sin descripcion"}</b>
                <span>
                  {mirando.momento} · {mirando.fecha ? mirando.fecha.substring(0, 16).replace("T", " ") : ""}
                </span>
              </div>
              <button onClick={() => borrarFoto(mirando.id_foto)}>Borrar</button>
              <button className="secundario" onClick={() => setMirando(null)}>
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default FotosOrden;
