import { useEffect, useState } from "react";
import { pedir, leerRespuesta } from "../api";
import { confirmar } from "../avisos";
import Acciones from "./Acciones";
import { useTabla, Th } from "./Tabla";

function Sucursales() {
  const [sucursales, setSucursales] = useState([]);
  const [editandoId, setEditandoId] = useState(null);
  const [mensaje, setMensaje] = useState("");

  const [nombre, setNombre] = useState("");
  const [direccion, setDireccion] = useState("");

  //1- traer todas las sucursales
  function traerSucursales() {
    pedir("/sucursales/traer")
      .then((res) => res.json())
      .then((data) => setSucursales(data))
      .catch(() => setSucursales([]));
  }

  useEffect(() => {
    traerSucursales();
  }, []);

  //2- el mismo formulario crea o edita
  async function guardarSucursal(e) {
    e.preventDefault();

    if (
      editandoId &&
      !(await confirmar({
        titulo: "Guardar los cambios",
        texto: "Se van a actualizar los datos de la sucursal.",
        boton: "Guardar",
      }))
    ) {
      return;
    }

    const sucursal = { nombre: nombre, direccion: direccion };

    const ruta = editandoId
      ? "/sucursales/editar/" + editandoId
      : "/sucursales/crear";

    pedir(ruta, {
      method: editandoId ? "PUT" : "POST",
      body: JSON.stringify(sucursal),
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);

        // si el backend rechazo los datos, el formulario queda como estaba
        if (r.ok) {
          limpiarFormulario();
        }

        traerSucursales();
      });
  }

  function editarSucursal(s) {
    setEditandoId(s.id_sucursal);
    setNombre(s.nombre || "");
    setDireccion(s.direccion || "");
  }

  function limpiarFormulario() {
    setEditandoId(null);
    setNombre("");
    setDireccion("");
  }

  //3- borrar una sucursal
  async function borrarSucursal(id) {
    const seguro = await confirmar({
      titulo: "Borrar sucursal",
      texto: "La sucursal se borra del sistema y no se puede recuperar.",
      detalle: "Si tiene empleados u ordenes, el sistema no va a dejar borrarla.",
      boton: "Borrar",
      tono: "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/sucursales/borrar/" + id, {
      method: "DELETE",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerSucursales();
      });
  }

  const tabla = useTabla(sucursales);

  return (
    <div>
      <h2>{editandoId ? "Editando sucursal" : "Nueva sucursal"}</h2>

      <form onSubmit={guardarSucursal}>
        <input
          placeholder="Nombre"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          required
        />
        <input
          placeholder="Direccion"
          value={direccion}
          onChange={(e) => setDireccion(e.target.value)}
        />
        <button type="submit">{editandoId ? "Guardar" : "Crear"}</button>
        {editandoId && (
          <button type="button" onClick={limpiarFormulario}>
            Cancelar
          </button>
        )}
      </form>

      {mensaje && <p className="mensaje">{mensaje}</p>}

      <table onContextMenu={tabla.alClickDerecho}>
        <thead>
          <tr>
            <Th tabla={tabla} campo="id_sucursal">
              Id
            </Th>
            <Th tabla={tabla} campo="nombre">
              Nombre
            </Th>
            <Th tabla={tabla} campo="direccion">
              Direccion
            </Th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {tabla.filas.map((s) => (
            <tr key={s.id_sucursal}>
              <td>{s.id_sucursal}</td>
              <td>{s.nombre}</td>
              <td>{s.direccion}</td>
              <td className="col-acciones">
                <Acciones
                  principal={{
                    texto: "Editar",
                    icono: "editar",
                    alHacer: () => editarSucursal(s),
                  }}
                  opciones={[
                    {
                      texto: "Borrar",
                      icono: "borrar",
                      tono: "peligro",
                      alHacer: () => borrarSucursal(s.id_sucursal),
                    },
                  ]}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {tabla.buscador}

      {sucursales.length === 0 && <p className="vacio">Todavia no hay sucursales cargadas.</p>}
    </div>
  );
}

export default Sucursales;
