import { useEffect, useState } from "react";
import { pedir, leerRespuesta, esAdmin } from "../api";
import { confirmar } from "../avisos";
import { filtrar, POR_PAGINA } from "../listas";
import { CampoFiltro, VerMas } from "./Filtro";
import MovimientoStock from "./MovimientoStock";
import Acciones from "./Acciones";
import { useTabla, Th } from "./Tabla";

function Repuestos({ filtroInicial }) {
  const [repuestos, setRepuestos] = useState([]);
  const [editandoId, setEditandoId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [filtro, setFiltro] = useState(filtroInicial || "");
  const [cuantas, setCuantas] = useState(POR_PAGINA);
  const [verMovimientosDe, setVerMovimientosDe] = useState(null);

  // lo que se escanea con la pistola o se escribe a mano en el buscador
  const [busqueda, setBusqueda] = useState("");
  const [soloBajoStock, setSoloBajoStock] = useState(false);

  const [codigo, setCodigo] = useState("");
  const [codigoBarra, setCodigoBarra] = useState("");
  const [nombre, setNombre] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [marca, setMarca] = useState("");
  const [ubicacion, setUbicacion] = useState("");
  const [precioCompra, setPrecioCompra] = useState("");
  const [precioVenta, setPrecioVenta] = useState("");
  const [stock, setStock] = useState("");
  const [stockMinimo, setStockMinimo] = useState("");

  //1- traer todo el deposito
  function traerRepuestos() {
    setSoloBajoStock(false);

    pedir("/repuestos/traer")
      .then((res) => res.json())
      .then((data) => setRepuestos(data))
      .catch(() => setRepuestos([]));
  }

  useEffect(() => {
    traerRepuestos();
  }, []);

  //2- ESCANER: la pistola escribe el codigo y manda Enter sola.
  // Busca el codigo de barras exacto y deja solo ese repuesto en la tabla.
  function escanear(e) {
    if (e.key !== "Enter") {
      return;
    }

    e.preventDefault();

    const codigoLeido = busqueda.trim();

    if (!codigoLeido) {
      return;
    }

    pedir("/repuestos/codigo/" + codigoLeido).then((res) => {
      if (res.ok) {
        res.json().then((repuesto) => {
          setRepuestos([repuesto]);
          setSoloBajoStock(false);
          setMensaje("Encontrado: " + repuesto.nombre + " (stock " + repuesto.stock + ")");
          setBusqueda("");
        });
      } else {
        // no esta cargado: se deja el codigo listo en el formulario
        // para darlo de alta sin tener que volver a escanearlo
        setCodigoBarra(codigoLeido);
        setMensaje(
          "El codigo " + codigoLeido + " no esta cargado. Quedo puesto en el formulario para darlo de alta."
        );
        setBusqueda("");
      }
    });
  }

  //3- buscador a mano: texto parcial contra codigo, codigo de barras,
  // nombre y marca
  function buscar() {
    pedir("/repuestos/buscar?texto=" + encodeURIComponent(busqueda))
      .then((res) => res.json())
      .then((data) => {
        setRepuestos(data);
        setSoloBajoStock(false);
        setMensaje(data.length + " repuesto(s) encontrado(s)");
      })
      .catch(() => setRepuestos([]));
  }

  //4- los que hay que reponer
  function verBajoStock() {
    pedir("/repuestos/bajo-stock")
      .then((res) => res.json())
      .then((data) => {
        setRepuestos(data);
        setSoloBajoStock(true);
        setMensaje(data.length + " repuesto(s) para reponer");
      })
      .catch(() => setRepuestos([]));
  }

  //5- el mismo formulario carga a mano o edita
  async function guardarRepuesto(e) {
    e.preventDefault();

    if (
      editandoId &&
      !(await confirmar({
        titulo: "Guardar los cambios",
        texto: "Se van a actualizar los datos del repuesto.",
        detalle: "Si cambiaste el precio, queda anotado en el historial.",
        boton: "Guardar",
      }))
    ) {
      return;
    }

    const repuesto = {
      codigo: codigo,
      codigoBarra: codigoBarra,
      nombre: nombre,
      descripcion: descripcion,
      marca: marca,
      ubicacion: ubicacion,
      precioCompra: Number(precioCompra),
      precioVenta: Number(precioVenta),
      stock: Number(stock),
      stockMinimo: Number(stockMinimo),
      id_sucursal: 1,
    };

    const ruta = editandoId
      ? "/repuestos/editar/" + editandoId
      : "/repuestos/crear";

    pedir(ruta, {
      method: editandoId ? "PUT" : "POST",
      body: JSON.stringify(repuesto),
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);

        // si el backend rechazo los datos, el formulario queda como estaba
        if (r.ok) {
          limpiarFormulario();
        }

        traerRepuestos();
      });
  }

  function editarRepuesto(r) {
    setEditandoId(r.id_repuesto);
    setCodigo(r.codigo || "");
    setCodigoBarra(r.codigoBarra || "");
    setNombre(r.nombre || "");
    setDescripcion(r.descripcion || "");
    setMarca(r.marca || "");
    setUbicacion(r.ubicacion || "");
    setPrecioCompra(r.precioCompra || "");
    setPrecioVenta(r.precioVenta || "");
    setStock(r.stock || "");
    setStockMinimo(r.stockMinimo || "");
  }

  function limpiarFormulario() {
    setEditandoId(null);
    setCodigo("");
    setCodigoBarra("");
    setNombre("");
    setDescripcion("");
    setMarca("");
    setUbicacion("");
    setPrecioCompra("");
    setPrecioVenta("");
    setStock("");
    setStockMinimo("");
  }

  //6- borrar un repuesto
  async function borrarRepuesto(id) {
    const seguro = await confirmar({
      titulo: "Borrar repuesto",
      texto: "El repuesto se borra del deposito junto con sus movimientos de stock.",
      boton: "Borrar",
      tono: "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/repuestos/borrar/" + id, {
      method: "DELETE",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerRepuestos();
      });
  }

  // se resalta la fila cuando hay que reponer
  function hayQueReponer(r) {
    return r.stock <= r.stockMinimo;
  }

  // resumen del deposito, sobre lo que hay en pantalla
  const paraReponer = repuestos.filter((r) => hayQueReponer(r));
  const valorStock = repuestos.reduce(
    (suma, r) => suma + Number(r.stock || 0) * Number(r.precioVenta || 0),
    0
  );


  // lo que se ve: filtrado por el buscador y de a tandas
  const filtrados = filtrar(repuestos, filtro, ["codigo", "codigoBarra", "nombre", "marca", "ubicacion"]);
  const tabla = useTabla(filtrados);
  const visibles = tabla.filas.slice(0, cuantas);
  return (
    <div>
      <div className="metricas">
        <div className="metrica">
          <span className="valor">{repuestos.length}</span>
          <span className="rotulo">Repuestos listados</span>
        </div>
        <div className="metrica destacada">
          <span className="valor">{paraReponer.length}</span>
          <span className="rotulo">Para reponer</span>
        </div>
        <div className="metrica">
          <span className="valor">${valorStock}</span>
          <span className="rotulo">Valor del stock</span>
        </div>
      </div>

      {esAdmin() && (
        <h2>{editandoId ? "Editando repuesto" : "Cargar repuesto a mano"}</h2>
      )}

      <div className="escaner">
        <input
          className="campo-escaner"
          placeholder="Escanea el codigo de barras o escribi para buscar..."
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          onKeyDown={escanear}
          autoFocus
        />
        <button type="button" onClick={buscar}>
          Buscar
        </button>
        <button type="button" className="secundario" onClick={verBajoStock}>
          Para reponer
        </button>
        <button type="button" className="secundario" onClick={traerRepuestos}>
          Ver todo
        </button>
      </div>

      <p className="ayuda">
        La pistola de codigo de barras escribe sola y manda Enter: con el cursor
        en ese campo, escanear alcanza. Si el codigo no esta cargado, queda
        puesto en el formulario de abajo para darlo de alta.
      </p>

      <form onSubmit={guardarRepuesto} hidden={!esAdmin()}>
        <input
          placeholder="Codigo interno"
          value={codigo}
          onChange={(e) => setCodigo(e.target.value)}
          required
        />
        <input
          placeholder="Codigo de barras"
          value={codigoBarra}
          onChange={(e) => setCodigoBarra(e.target.value)}
        />
        <input
          placeholder="Nombre"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          required
        />
        <input
          placeholder="Marca"
          value={marca}
          onChange={(e) => setMarca(e.target.value)}
        />
        <input
          placeholder="Ubicacion"
          value={ubicacion}
          onChange={(e) => setUbicacion(e.target.value)}
        />
        <input
          placeholder="Descripcion"
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
        />
        <input
          type="number"
          placeholder="Precio compra"
          value={precioCompra}
          onChange={(e) => setPrecioCompra(e.target.value)}
        />
        <input
          type="number"
          placeholder="Precio venta"
          value={precioVenta}
          onChange={(e) => setPrecioVenta(e.target.value)}
        />
        {!editandoId && (
          <input
            type="number"
            placeholder="Stock inicial"
            value={stock}
            onChange={(e) => setStock(e.target.value)}
          />
        )}
        <input
          type="number"
          placeholder="Stock minimo"
          value={stockMinimo}
          onChange={(e) => setStockMinimo(e.target.value)}
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
        placeholder="Buscar por codigo, nombre, marca o ubicacion..."
        cantidad={filtrados.length}
        total={repuestos.length}
      />

      <table onContextMenu={tabla.alClickDerecho}>
        <thead>
          <tr>
            <Th tabla={tabla} campo="codigo">
              Codigo
            </Th>
            <Th tabla={tabla} campo="codigoBarra">
              Cod. barras
            </Th>
            <Th tabla={tabla} campo="nombre">
              Nombre
            </Th>
            <Th tabla={tabla} campo="marca">
              Marca
            </Th>
            <Th tabla={tabla} campo="ubicacion">
              Ubicacion
            </Th>
            <Th tabla={tabla} campo="stock" className="numero">
              Stock
            </Th>
            <Th tabla={tabla} campo="stockMinimo" className="numero">
              Minimo
            </Th>
            <Th tabla={tabla} campo="precioVenta" className="importe">
              Venta
            </Th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {visibles.map((r) => (
            <tr key={r.id_repuesto} className={hayQueReponer(r) ? "reponer" : ""}>
              <td className="clave">{r.codigo}</td>
              <td className="clave">{r.codigoBarra}</td>
              <td>{r.nombre}</td>
              <td>{r.marca}</td>
              <td>{r.ubicacion}</td>
              <td className="numero">
                {hayQueReponer(r) && <span className="alerta">reponer</span>}
                <b>{r.stock}</b>
              </td>
              <td className="numero">{r.stockMinimo}</td>
              <td className="importe">${r.precioVenta}</td>
              <td className="col-acciones">
                <Acciones
                  principal={{
                    texto: "Stock",
                    icono: "stock",
                    ayuda: "Entradas y salidas de este repuesto",
                    alHacer: () => setVerMovimientosDe(r),
                  }}
                  opciones={[
                    esAdmin() && {
                      texto: "Editar",
                      icono: "editar",
                      alHacer: () => editarRepuesto(r),
                    },
                    esAdmin() && {
                      texto: "Borrar",
                      icono: "borrar",
                      tono: "peligro",
                      alHacer: () => borrarRepuesto(r.id_repuesto),
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

      {repuestos.length === 0 && (
        <p className="vacio">
          {soloBajoStock
            ? "No hay repuestos para reponer, el deposito esta al dia."
            : "No hay repuestos que mostrar."}
        </p>
      )}

      {verMovimientosDe && (
        <MovimientoStock
          repuesto={verMovimientosDe}
          onCerrar={() => setVerMovimientosDe(null)}
          onCambio={traerRepuestos}
        />
      )}
    </div>
  );
}

export default Repuestos;
