import { useEffect, useState } from "react";
import { pedir, leerRespuesta, traerSesion } from "../api";
import { confirmar } from "../avisos";
import Acciones from "./Acciones";
import { useTabla, Th } from "./Tabla";

function Empleados() {
  const [empleados, setEmpleados] = useState([]);
  const [sucursales, setSucursales] = useState([]);
  const [editandoId, setEditandoId] = useState(null);
  const [mensaje, setMensaje] = useState("");

  // para no dejar que alguien se bloquee a si mismo desde la lista
  const sesion = traerSesion() || {};

  const [nombre, setNombre] = useState("");
  const [apellido, setApellido] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rol, setRol] = useState("MECANICO");
  const [idSucursal, setIdSucursal] = useState("");

  //1- traer todos los empleados
  function traerEmpleados() {
    pedir("/empleados/traer")
      .then((res) => res.json())
      .then((data) => setEmpleados(data))
      .catch(() => setEmpleados([]));
  }

  //2- traer las sucursales para el select
  function traerSucursales() {
    pedir("/sucursales/traer")
      .then((res) => res.json())
      .then((data) => setSucursales(data))
      .catch(() => setSucursales([]));
  }

  useEffect(() => {
    traerEmpleados();
    traerSucursales();
  }, []);

  //3- el mismo formulario crea o edita
  async function guardarEmpleado(e) {
    e.preventDefault();

    if (
      editandoId &&
      !(await confirmar({
        titulo: "Guardar los cambios",
        texto: "Se van a actualizar los datos del empleado.",
        boton: "Guardar",
      }))
    ) {
      return;
    }

    if (editandoId) {
      // al editar viaja la entidad: la sucursal va como objeto con su id
      const empleado = {
        nombre: nombre,
        apellido: apellido,
        email: email,
        rol: rol,
        sucursal: { id_sucursal: Number(idSucursal) },
      };

      pedir("/empleados/editar/" + editandoId, {
        method: "PUT",
          body: JSON.stringify(empleado),
      })
        .then(leerRespuesta)
        .then((r) => {
          setMensaje(r.mensaje);

          // si el backend rechazo los datos, el formulario queda como estaba
          if (r.ok) {
            limpiarFormulario();
          }

          traerEmpleados();
        });
    } else {
      pedir("/empleados/crear", {
        method: "POST",
          body: JSON.stringify({
          nombre: nombre,
          apellido: apellido,
          email: email,
          password: password,
          rol: rol,
          id_sucursal: Number(idSucursal),
        }),
      })
        .then(leerRespuesta)
        .then((r) => {
          setMensaje(r.mensaje);

          // si el backend rechazo los datos, el formulario queda como estaba
          if (r.ok) {
            limpiarFormulario();
          }

          traerEmpleados();
        });
    }
  }

  function editarEmpleado(e) {
    setEditandoId(e.id_empleado);
    setNombre(e.nombre || "");
    setApellido(e.apellido || "");
    setEmail(e.email || "");
    setRol(e.rol || "MECANICO");
    setIdSucursal(e.sucursal ? e.sucursal.id_sucursal : "");
  }

  function limpiarFormulario() {
    setEditandoId(null);
    setNombre("");
    setApellido("");
    setEmail("");
    setPassword("");
    setRol("MECANICO");
    setIdSucursal("");
  }

  //4- borrar un empleado
  // Un empleado que se va no se borra: tiene ordenes hechas a su nombre.
  // Se bloquea y deja de poder entrar.
  async function bloquear(e) {
    const activa = e.activo === false;

    const seguro = await confirmar({
      titulo: activa ? "Desbloquear a " + e.nombre : "Bloquear a " + e.nombre,
      texto: activa
        ? "Va a poder volver a entrar al sistema."
        : "No va a poder entrar mas al sistema.",
      detalle: activa
        ? ""
        : "Sus ordenes, cobros y ventas siguen igual: lo unico que pierde es el acceso. Si ahora mismo esta usando el sistema, su sesion sigue viva hasta que venza (12 horas).",
      boton: activa ? "Desbloquear" : "Bloquear",
      tono: activa ? "" : "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/empleados/bloquear/" + e.id_empleado + "?activo=" + activa, {
      method: "PUT",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerEmpleados();
      });
  }

  async function borrarEmpleado(id) {
    const seguro = await confirmar({
      titulo: "Borrar empleado",
      texto: "El empleado pierde el acceso al sistema y se borra de la lista.",
      detalle: "Si tiene ordenes asignadas, el sistema no va a dejar borrarlo.",
      boton: "Borrar",
      tono: "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/empleados/borrar/" + id, {
      method: "DELETE",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerEmpleados();
      });
  }

  const tabla = useTabla(empleados);

  return (
    <div>
      <h2>{editandoId ? "Editando empleado" : "Nuevo empleado"}</h2>

      <form onSubmit={guardarEmpleado}>
        <input
          placeholder="Nombre"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          required
        />
        <input
          placeholder="Apellido"
          value={apellido}
          onChange={(e) => setApellido(e.target.value)}
        />
        <input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        {!editandoId && (
          <input
            type="password"
            placeholder="Contraseña (min 6)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        )}
        <select value={rol} onChange={(e) => setRol(e.target.value)}>
          <option value="ADMIN">ADMIN</option>
          <option value="MECANICO">MECANICO</option>
          <option value="VENDEDOR">VENDEDOR</option>
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
            <Th tabla={tabla} campo="id_empleado">
              Id
            </Th>
            <Th tabla={tabla} campo="nombre">
              Nombre
            </Th>
            <Th tabla={tabla} campo="apellido">
              Apellido
            </Th>
            <Th tabla={tabla} campo="email">
              Email
            </Th>
            <Th tabla={tabla} campo="rol">
              Rol
            </Th>
            <Th tabla={tabla} campo="sucursal.nombre">
              Sucursal
            </Th>
            <Th tabla={tabla} campo="activo">
              Acceso
            </Th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {tabla.filas.map((e) => (
            <tr key={e.id_empleado}>
              <td>{e.id_empleado}</td>
              <td>{e.nombre}</td>
              <td>{e.apellido}</td>
              <td>{e.email}</td>
              <td>{e.rol}</td>
              <td>{e.sucursal ? e.sucursal.nombre : ""}</td>
              <td>
                {e.activo === false ? (
                  <span className="etiqueta bloqueado">Bloqueado</span>
                ) : (
                  <span className="etiqueta activo">Entra</span>
                )}
              </td>
              <td className="col-acciones">
                <Acciones
                  principal={{
                    texto: "Editar",
                    icono: "editar",
                    alHacer: () => editarEmpleado(e),
                  }}
                  opciones={[
                    e.id_empleado !== sesion.id_empleado && {
                      texto: e.activo === false ? "Desbloquear" : "Bloquear",
                      icono: e.activo === false ? "aceptar" : "salir",
                      tono: e.activo === false ? "" : "peligro",
                      alHacer: () => bloquear(e),
                    },
                    {
                      texto: "Borrar",
                      icono: "borrar",
                      tono: "peligro",
                      alHacer: () => borrarEmpleado(e.id_empleado),
                    },
                  ]}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {tabla.buscador}

      {empleados.length === 0 && <p className="vacio">Todavia no hay empleados cargados.</p>}
    </div>
  );
}

export default Empleados;
