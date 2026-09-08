import { useEffect, useState } from "react";
import { pedir, leerRespuesta, esAdmin } from "../api";
import { confirmar } from "../avisos";
import { filtrar, POR_PAGINA } from "../listas";
import { CampoFiltro, VerMas } from "./Filtro";
import Acciones from "./Acciones";
import { useTabla, Th } from "./Tabla";

function Clientes({ filtroInicial }) {
  const [clientes, setClientes] = useState([]);
  const [editandoId, setEditandoId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [filtro, setFiltro] = useState(filtroInicial || "");
  const [cuantas, setCuantas] = useState(POR_PAGINA);

  const [nombre, setNombre] = useState("");
  const [telefono, setTelefono] = useState("");
  const [email, setEmail] = useState("");

  //1- traer todos los clientes
  function traerClientes() {
    pedir("/clientes/traer")
      .then((res) => res.json())
      .then((data) => setClientes(data))
      .catch(() => setClientes([]));
  }

  useEffect(() => {
    traerClientes();
  }, []);

  //2- el mismo formulario crea o edita, segun si hay un id cargado
  async function guardarCliente(e) {
    e.preventDefault();

    if (
      editandoId &&
      !(await confirmar({
        titulo: "Guardar los cambios",
        texto: "Se van a actualizar los datos del cliente.",
        boton: "Guardar",
      }))
    ) {
      return;
    }

    const cliente = { nombre: nombre, telefono: telefono, email: email };

    const ruta = editandoId
      ? "/clientes/editar/" + editandoId
      : "/clientes/crear";

    pedir(ruta, {
      method: editandoId ? "PUT" : "POST",
      body: JSON.stringify(cliente),
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);

        // si el backend rechazo los datos, el formulario queda como estaba
        if (r.ok) {
          limpiarFormulario();
        }

        traerClientes();
      });
  }

  //3- carga los datos del cliente en el formulario
  function editarCliente(c) {
    setEditandoId(c.id_cliente);
    setNombre(c.nombre || "");
    setTelefono(c.telefono || "");
    setEmail(c.email || "");
  }

  function limpiarFormulario() {
    setEditandoId(null);
    setNombre("");
    setTelefono("");
    setEmail("");
  }

  //4- borrar un cliente
  async function borrarCliente(id) {
    const seguro = await confirmar({
      titulo: "Borrar cliente",
      texto: "El cliente se borra del sistema y no se puede recuperar.",
      detalle: "Si tiene vehiculos cargados, el sistema no va a dejar borrarlo.",
      boton: "Borrar",
      tono: "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/clientes/borrar/" + id, {
      method: "DELETE",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerClientes();
      });
  }


  // lo que se ve: filtrado por el buscador y de a tandas
  const filtrados = filtrar(clientes, filtro, ["nombre", "telefono", "email"]);
  const tabla = useTabla(filtrados);
  const visibles = tabla.filas.slice(0, cuantas);
  return (
    <div>
      <h2>{editandoId ? "Editando cliente" : "Nuevo cliente"}</h2>

      <form onSubmit={guardarCliente}>
        <input
          placeholder="Nombre"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          required
        />
        <input
          placeholder="Telefono"
          value={telefono}
          onChange={(e) => setTelefono(e.target.value)}
        />
        <input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <button type="submit">{editandoId ? "Guardar" : "Crear"}</button>
        {editandoId && (
          <button type="button" onClick={limpiarFormulario}>
            Cancelar
          </button>
        )}
      </form>

      {mensaje && <p className="mensaje">{mensaje}</p>}

      <CampoFiltro
        valor={filtro}
        onCambio={(v) => {
          setFiltro(v);
          setCuantas(POR_PAGINA);
        }}
        placeholder="Buscar por nombre, telefono o email..."
        cantidad={filtrados.length}
        total={clientes.length}
      />

      <table onContextMenu={tabla.alClickDerecho}>
        <thead>
          <tr>
            <Th tabla={tabla} campo="id_cliente">
              Id
            </Th>
            <Th tabla={tabla} campo="nombre">
              Nombre
            </Th>
            <Th tabla={tabla} campo="telefono">
              Telefono
            </Th>
            <Th tabla={tabla} campo="email">
              Email
            </Th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {visibles.map((c) => (
            <tr key={c.id_cliente}>
              <td>{c.id_cliente}</td>
              <td>{c.nombre}</td>
              <td>{c.telefono}</td>
              <td>{c.email}</td>
              <td className="col-acciones">
                <Acciones
                  principal={{
                    texto: "Editar",
                    icono: "editar",
                    alHacer: () => editarCliente(c),
                  }}
                  opciones={[
                    esAdmin() && {
                      texto: "Borrar",
                      icono: "borrar",
                      tono: "peligro",
                      alHacer: () => borrarCliente(c.id_cliente),
                    },
                  ]}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {tabla.buscador}

      <VerMas
        mostrando={visibles.length}
        total={tabla.filas.length}
        onMas={() => setCuantas(cuantas + POR_PAGINA)}
      />

      {clientes.length === 0 && <p className="vacio">Todavia no hay clientes cargados.</p>}
    </div>
  );
}

export default Clientes;
