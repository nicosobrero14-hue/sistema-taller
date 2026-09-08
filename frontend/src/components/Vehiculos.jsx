import { useEffect, useState } from "react";
import { pedir, leerRespuesta, esAdmin } from "../api";
import { confirmar } from "../avisos";
import { filtrar, POR_PAGINA } from "../listas";
import { CampoFiltro, VerMas } from "./Filtro";
import HistorialVehiculo from "./HistorialVehiculo";
import Acciones from "./Acciones";
import { useTabla, Th } from "./Tabla";

function Vehiculos({ filtroInicial }) {
  const [vehiculos, setVehiculos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [editandoId, setEditandoId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [filtro, setFiltro] = useState(filtroInicial || "");
  const [cuantas, setCuantas] = useState(POR_PAGINA);
  const [verHistorialDe, setVerHistorialDe] = useState(null);

  const [patente, setPatente] = useState("");
  const [marca, setMarca] = useState("");
  const [modelo, setModelo] = useState("");
  const [anio, setAnio] = useState("");
  const [kilometraje, setKilometraje] = useState("");
  const [idCliente, setIdCliente] = useState("");

  //1- traer todos los vehiculos
  function traerVehiculos() {
    pedir("/vehiculos/traer")
      .then((res) => res.json())
      .then((data) => setVehiculos(data))
      .catch(() => setVehiculos([]));
  }

  //2- traer los clientes para el select
  function traerClientes() {
    pedir("/clientes/traer")
      .then((res) => res.json())
      .then((data) => setClientes(data))
      .catch(() => setClientes([]));
  }

  useEffect(() => {
    traerVehiculos();
    traerClientes();
  }, []);

  //3- el mismo formulario crea o edita
  async function guardarVehiculo(e) {
    e.preventDefault();

    if (
      editandoId &&
      !(await confirmar({
        titulo: "Guardar los cambios",
        texto: "Se van a actualizar los datos del vehiculo.",
        boton: "Guardar",
      }))
    ) {
      return;
    }

    if (editandoId) {
      // al editar viaja la entidad: el cliente va como objeto con su id
      const vehiculo = {
        patente: patente,
        marca: marca,
        modelo: modelo,
        anio: Number(anio),
        kilometraje: Number(kilometraje),
        cliente: { id_cliente: Number(idCliente) },
      };

      pedir("/vehiculos/editar/" + editandoId, {
        method: "PUT",
          body: JSON.stringify(vehiculo),
      })
        .then(leerRespuesta)
        .then((r) => {
          setMensaje(r.mensaje);

          // si el backend rechazo los datos, el formulario queda como estaba
          if (r.ok) {
            limpiarFormulario();
          }

          traerVehiculos();
        });
    } else {
      pedir("/vehiculos/crear", {
        method: "POST",
          body: JSON.stringify({
          patente: patente,
          marca: marca,
          modelo: modelo,
          anio: Number(anio),
          kilometraje: Number(kilometraje),
          id_cliente: Number(idCliente),
        }),
      })
        .then(leerRespuesta)
        .then((r) => {
          setMensaje(r.mensaje);

          // si el backend rechazo los datos, el formulario queda como estaba
          if (r.ok) {
            limpiarFormulario();
          }

          traerVehiculos();
        });
    }
  }

  //4- carga el vehiculo en el formulario
  function editarVehiculo(v) {
    setEditandoId(v.id_vehiculo);
    setPatente(v.patente || "");
    setMarca(v.marca || "");
    setModelo(v.modelo || "");
    setAnio(v.anio || "");
    setKilometraje(v.kilometraje || "");
    setIdCliente(v.cliente ? v.cliente.id_cliente : "");
  }

  function limpiarFormulario() {
    setEditandoId(null);
    setPatente("");
    setMarca("");
    setModelo("");
    setAnio("");
    setKilometraje("");
    setIdCliente("");
  }

  //5- borrar un vehiculo
  async function borrarVehiculo(id) {
    const seguro = await confirmar({
      titulo: "Borrar vehiculo",
      texto: "El vehiculo se borra del sistema y no se puede recuperar.",
      detalle: "Si tuvo ordenes de trabajo, el sistema no va a dejar borrarlo.",
      boton: "Borrar",
      tono: "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/vehiculos/borrar/" + id, {
      method: "DELETE",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerVehiculos();
      });
  }


  // lo que se ve: filtrado por el buscador y de a tandas
  const filtrados = filtrar(vehiculos, filtro, ["patente", "marca", "modelo", "cliente.nombre"]);
  const tabla = useTabla(filtrados);
  const visibles = tabla.filas.slice(0, cuantas);
  return (
    <div>
      <h2>{editandoId ? "Editando vehiculo" : "Nuevo vehiculo"}</h2>

      <form onSubmit={guardarVehiculo}>
        <input
          placeholder="Patente"
          value={patente}
          onChange={(e) => setPatente(e.target.value)}
          required
        />
        <input
          placeholder="Marca"
          value={marca}
          onChange={(e) => setMarca(e.target.value)}
        />
        <input
          placeholder="Modelo"
          value={modelo}
          onChange={(e) => setModelo(e.target.value)}
        />
        <input
          type="number"
          placeholder="Anio"
          value={anio}
          onChange={(e) => setAnio(e.target.value)}
        />
        <input
          type="number"
          placeholder="Kilometraje"
          value={kilometraje}
          onChange={(e) => setKilometraje(e.target.value)}
        />
        <select
          value={idCliente}
          onChange={(e) => setIdCliente(e.target.value)}
          required
        >
          <option value="">-- Cliente --</option>
          {clientes.map((c) => (
            <option key={c.id_cliente} value={c.id_cliente}>
              {c.nombre}
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

      <CampoFiltro
        valor={filtro}
        onCambio={(v) => {
          setFiltro(v);
          setCuantas(POR_PAGINA);
        }}
        placeholder="Buscar por patente, marca, modelo o cliente..."
        cantidad={filtrados.length}
        total={vehiculos.length}
      />

      <table onContextMenu={tabla.alClickDerecho}>
        <thead>
          <tr>
            <Th tabla={tabla} campo="id_vehiculo">
              Id
            </Th>
            <Th tabla={tabla} campo="patente">
              Patente
            </Th>
            <Th tabla={tabla} campo="marca">
              Marca
            </Th>
            <Th tabla={tabla} campo="modelo">
              Modelo
            </Th>
            <Th tabla={tabla} campo="anio">
              Anio
            </Th>
            <Th tabla={tabla} campo="kilometraje" className="numero">
              Km
            </Th>
            <Th tabla={tabla} campo="cliente.nombre">
              Cliente
            </Th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {visibles.map((v) => (
            <tr key={v.id_vehiculo}>
              <td>{v.id_vehiculo}</td>
              <td className="clave">{v.patente}</td>
              <td>{v.marca}</td>
              <td>{v.modelo}</td>
              <td>{v.anio}</td>
              <td className="numero">{v.kilometraje}</td>
              <td>{v.cliente ? v.cliente.nombre : ""}</td>
              <td className="col-acciones">
                <Acciones
                  principal={{
                    texto: "Historial",
                    icono: "historial",
                    ayuda: "Todo lo que se le hizo a este vehiculo",
                    alHacer: () => setVerHistorialDe(v),
                  }}
                  opciones={[
                    {
                      texto: "Editar",
                      icono: "editar",
                      alHacer: () => editarVehiculo(v),
                    },
                    esAdmin() && {
                      texto: "Borrar",
                      icono: "borrar",
                      tono: "peligro",
                      alHacer: () => borrarVehiculo(v.id_vehiculo),
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

      {vehiculos.length === 0 && (
        <p className="vacio">Todavia no hay vehiculos cargados.</p>
      )}

      {verHistorialDe && (
        <HistorialVehiculo
          vehiculo={verHistorialDe}
          onCerrar={() => setVerHistorialDe(null)}
        />
      )}
    </div>
  );
}

export default Vehiculos;
