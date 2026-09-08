import { useEffect, useState } from "react";
import { pedir, leerRespuesta, esAdmin } from "../api";
import { confirmar, pedirTexto } from "../avisos";
import { filtrar, POR_PAGINA } from "../listas";
import { fechaHora, hoy, esDelDia } from "../fechas";
import { CampoFiltro, VerMas } from "./Filtro";
import Acciones from "./Acciones";
import { useTabla, Th } from "./Tabla";

const MEDIOS = [
  "EFECTIVO",
  "TRANSFERENCIA",
  "TARJETA_DEBITO",
  "TARJETA_CREDITO",
  "QR",
];

/**
 * Venta de mostrador: alguien entra, se lleva un repuesto y paga. Sin
 * orden de trabajo ni vehiculo de por medio.
 *
 * Se arma el carrito, se elige como paga y se cierra. Ahi sale el stock
 * del deposito.
 */
function Ventas() {
  const [ventas, setVentas] = useState([]);
  const [repuestos, setRepuestos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [sucursales, setSucursales] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [mensaje, setMensaje] = useState("");

  // lo que se esta vendiendo ahora
  const [carrito, setCarrito] = useState([]);
  const [medio, setMedio] = useState("EFECTIVO");
  const [idCliente, setIdCliente] = useState("");
  const [nombreCliente, setNombreCliente] = useState("");
  const [idSucursal, setIdSucursal] = useState("");
  const [observaciones, setObservaciones] = useState("");

  const [verDetalleDe, setVerDetalleDe] = useState(null);
  const [filtro, setFiltro] = useState("");
  const [cuantas, setCuantas] = useState(POR_PAGINA);

  //1- traer las ventas ya hechas
  function traerVentas() {
    pedir("/ventas/traer")
      .then((res) => res.json())
      .then((data) => setVentas(data))
      .catch(() => setVentas([]));
  }

  //2- el deposito y las listas de los selects
  function traerDatos() {
    pedir("/repuestos/traer")
      .then((res) => res.json())
      .then((data) => setRepuestos(data))
      .catch(() => setRepuestos([]));

    pedir("/clientes/traer")
      .then((res) => res.json())
      .then((data) => setClientes(data))
      .catch(() => setClientes([]));

    pedir("/sucursales/traer")
      .then((res) => res.json())
      .then((data) => setSucursales(data))
      .catch(() => setSucursales([]));

    pedir("/empleados/traer")
      .then((res) => res.json())
      .then((data) => setEmpleados(data))
      .catch(() => setEmpleados([]));
  }

  useEffect(() => {
    traerVentas();
    traerDatos();
  }, []);

  //3- sumar un repuesto al carrito. Si ya estaba, se le suma la cantidad
  // en vez de repetir el renglon.
  function agregar(id_repuesto, cantidad) {
    const repuesto = repuestos.find(
      (r) => String(r.id_repuesto) === String(id_repuesto)
    );

    if (!repuesto) {
      return;
    }

    const cuantos = Number(cantidad) || 1;
    const estaba = carrito.find((c) => c.id_repuesto === repuesto.id_repuesto);

    if (estaba) {
      setCarrito(
        carrito.map((c) =>
          c.id_repuesto === repuesto.id_repuesto
            ? { ...c, cantidad: c.cantidad + cuantos }
            : c
        )
      );
      return;
    }

    setCarrito([
      ...carrito,
      {
        id_repuesto: repuesto.id_repuesto,
        nombre: repuesto.nombre,
        marca: repuesto.marca,
        stock: repuesto.stock,
        precio: Number(repuesto.precioVenta),
        cantidad: cuantos,
      },
    ]);
  }

  function sacar(id_repuesto) {
    setCarrito(carrito.filter((c) => c.id_repuesto !== id_repuesto));
  }

  //4- cerrar la venta: aca sale el stock del deposito
  async function vender() {
    if (carrito.length === 0) {
      return;
    }

    const seguro = await confirmar({
      titulo: "Cerrar la venta",
      texto: "$" + total + " en " + medio.replace("_", " ") + ".",
      detalle: "Los repuestos salen del deposito y se emite el comprobante.",
      boton: "Vender",
      tono: "verde",
    });

    if (!seguro) {
      return;
    }

    pedir("/ventas/crear", {
      method: "POST",
      body: JSON.stringify({
        medio: medio,
        id_sucursal: Number(idSucursal),
        id_cliente: idCliente === "" ? null : Number(idCliente),
        nombreCliente: nombreCliente,
        observaciones: observaciones,
        detalles: carrito.map((c) => ({
          id_repuesto: c.id_repuesto,
          cantidad: c.cantidad,
        })),
      }),
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);

        // solo se limpia si de verdad se vendio
        if (r.ok) {
          setCarrito([]);
          setIdCliente("");
          setNombreCliente("");
          setObservaciones("");
        }

        traerVentas();
        traerDatos();
      });
  }

  //5- anular: los repuestos vuelven al deposito
  async function anular(venta) {
    const motivo = await pedirTexto({
      titulo: "Anular la venta " + venta.numeroComprobante,
      texto:
        "Los repuestos vuelven al deposito. La venta no se borra: queda con " +
        "el motivo a la vista.",
      etiqueta: "Por que se anula",
      placeholder: "Ej: el cliente devolvio el repuesto",
      boton: "Anular venta",
      tono: "peligro",
    });

    if (motivo === null || !motivo.trim()) {
      return;
    }

    pedir("/ventas/anular/" + venta.id_venta + "?motivo=" + encodeURIComponent(motivo), {
      method: "PUT",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerVentas();
        traerDatos();
      });
  }

  function vendedor(id) {
    const e = empleados.find((x) => x.id_empleado === id);
    return e ? e.nombre + " " + e.apellido : "-";
  }

  function comprador(venta) {
    if (venta.nombreCliente) {
      return venta.nombreCliente;
    }

    const c = clientes.find((x) => x.id_cliente === venta.id_cliente);

    return c ? c.nombre : "Consumidor final";
  }

  const total = carrito.reduce((s, c) => s + c.precio * c.cantidad, 0);

  const hechas = ventas.filter((v) => v.estado !== "ANULADA");
  const delDia = hechas.filter((v) => esDelDia(v.fecha, hoy()));
  const plataDelDia = delDia.reduce((s, v) => s + Number(v.total || 0), 0);

  const filtrados = filtrar(ventas, filtro, [
    "numeroComprobante",
    "nombreCliente",
    "medio",
    "observaciones",
  ]);
  const tabla = useTabla(filtrados);
  const visibles = tabla.filas.slice(0, cuantas);

  return (
    <div>
      <div className="metricas">
        <div className="metrica destacada">
          <span className="valor">${plataDelDia}</span>
          <span className="rotulo">Vendido hoy</span>
        </div>
        <div className="metrica">
          <span className="valor">{delDia.length}</span>
          <span className="rotulo">Ventas de hoy</span>
        </div>
        <div className="metrica">
          <span className="valor">{hechas.length}</span>
          <span className="rotulo">Ventas en total</span>
        </div>
      </div>

      <h2>Nueva venta</h2>

      <SelectorRepuesto repuestos={repuestos} onAgregar={agregar} />

      {carrito.length === 0 ? (
        <p className="vacio">
          Agrega repuestos para armar la venta. El precio sale del deposito.
        </p>
      ) : (
        <div className="carrito">
          <ul className="lista-items">
            {carrito.map((c) => (
              <li key={c.id_repuesto}>
                <span>
                  {c.nombre}
                  {c.marca ? " (" + c.marca + ")" : ""}
                  {c.cantidad > c.stock && (
                    <span className="alerta">solo hay {c.stock}</span>
                  )}
                </span>
                <span className="cant">
                  {c.cantidad} x ${c.precio}
                </span>
                <b>${c.precio * c.cantidad}</b>
                <button onClick={() => sacar(c.id_repuesto)}>Sacar</button>
              </li>
            ))}
          </ul>

          <div className="cerrar-venta">
            <div className="campos">
              <label>
                Como paga
                <select value={medio} onChange={(e) => setMedio(e.target.value)}>
                  {MEDIOS.map((m) => (
                    <option key={m} value={m}>
                      {m.replace("_", " ")}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Sucursal
                <select
                  value={idSucursal}
                  onChange={(e) => setIdSucursal(e.target.value)}
                >
                  <option value="">-- Elegir --</option>
                  {sucursales.map((s) => (
                    <option key={s.id_sucursal} value={s.id_sucursal}>
                      {s.nombre}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Cliente del sistema
                <select
                  value={idCliente}
                  onChange={(e) => {
                    setIdCliente(e.target.value);
                    setNombreCliente("");
                  }}
                >
                  <option value="">-- Consumidor final --</option>
                  {clientes.map((c) => (
                    <option key={c.id_cliente} value={c.id_cliente}>
                      {c.nombre}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                O a nombre de
                <input
                  value={nombreCliente}
                  onChange={(e) => {
                    setNombreCliente(e.target.value);
                    setIdCliente("");
                  }}
                  placeholder="Opcional"
                />
              </label>
            </div>

            <input
              className="observaciones-venta"
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
              placeholder="Observaciones (opcional)"
            />

            <div className="total-venta">
              <span>Total de la venta</span>
              <b>${total}</b>
              <button
                className="cerrar-la-venta"
                onClick={vender}
                disabled={!idSucursal}
              >
                Vender ${total}
              </button>
            </div>

            {!idSucursal && (
              <p className="ayuda">Elegi la sucursal para poder cerrar la venta.</p>
            )}
          </div>
        </div>
      )}

      {mensaje && <p className="mensaje">{mensaje}</p>}

      <h2>Ventas hechas</h2>

      <CampoFiltro
        valor={filtro}
        onCambio={(v) => {
          setFiltro(v);
          setCuantas(POR_PAGINA);
        }}
        placeholder="Buscar por comprobante, cliente o medio..."
        cantidad={filtrados.length}
        total={ventas.length}
      />

      <table onContextMenu={tabla.alClickDerecho}>
        <thead>
          <tr>
            <Th tabla={tabla} campo="numeroComprobante">
              Comprobante
            </Th>
            <Th tabla={tabla} campo="fecha">
              Fecha
            </Th>
            <Th tabla={tabla} campo="nombreCliente">
              Cliente
            </Th>
            <Th tabla={tabla} campo="id_empleado">
              Vendio
            </Th>
            <Th tabla={tabla} campo="medio">
              Medio
            </Th>
            <Th tabla={tabla} campo="total" className="importe">
              Total
            </Th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {visibles.map((v) => (
            <tr key={v.id_venta} className={v.estado === "ANULADA" ? "anulada" : ""}>
              <td className="clave">
                {String(v.numeroComprobante).padStart(6, "0")}
              </td>
              <td className="nombre">{fechaHora(v.fecha)}</td>
              <td>{comprador(v)}</td>
              <td className="nombre">{vendedor(v.id_empleado)}</td>
              <td>{v.medio.replace("_", " ")}</td>
              <td className="importe">${v.total}</td>
              <td className="col-acciones">
                <Acciones
                  principal={{
                    texto: "Ver",
                    icono: "ver",
                    alHacer: () => setVerDetalleDe(v),
                  }}
                  opciones={[
                    esAdmin() && v.estado !== "ANULADA" && {
                      texto: "Anular venta",
                      icono: "borrar",
                      tono: "peligro",
                      alHacer: () => anular(v),
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

      {ventas.length === 0 && (
        <p className="vacio">Todavia no se vendio nada por mostrador.</p>
      )}

      {verDetalleDe && (
        <div className="panel">
          <h3>
            <span>
              Venta {String(verDetalleDe.numeroComprobante).padStart(6, "0")}
            </span>
            <button className="secundario" onClick={() => setVerDetalleDe(null)}>
              Cerrar
            </button>
          </h3>

          {verDetalleDe.estado === "ANULADA" && (
            <div className="cobrado anulado">
              <div>
                <b>Venta anulada</b>
                <span>
                  {verDetalleDe.motivoAnulacion} ·{" "}
                  {fechaHora(verDetalleDe.fechaAnulacion)} ·{" "}
                  {vendedor(verDetalleDe.id_anulo)}
                </span>
              </div>
            </div>
          )}

          <div className="recibo-datos">
            <span>
              <b>Fecha</b> {fechaHora(verDetalleDe.fecha)}
            </span>
            <span>
              <b>Cliente</b> {comprador(verDetalleDe)}
            </span>
            <span>
              <b>Vendio</b> {vendedor(verDetalleDe.id_empleado)}
            </span>
            <span>
              <b>Medio</b> {verDetalleDe.medio.replace("_", " ")}
            </span>
          </div>

          <table>
            <thead>
              <tr>
                <th>Repuesto</th>
                <th className="numero">Cantidad</th>
                <th className="importe">Precio</th>
                <th className="importe">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {(verDetalleDe.detalles || []).map((d) => (
                <tr key={d.id_detalle}>
                  <td>{d.descripcion}</td>
                  <td className="numero">{d.cantidad}</td>
                  <td className="importe">${d.precioUnitario}</td>
                  <td className="importe">${d.cantidad * d.precioUnitario}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <th colSpan="3">Total</th>
                <th className="importe">${verDetalleDe.total}</th>
              </tr>
            </tfoot>
          </table>

          {verDetalleDe.observaciones && (
            <p className="recibo-detalle">{verDetalleDe.observaciones}</p>
          )}
        </div>
      )}
    </div>
  );
}

/**
 * El buscador del deposito. Es el mismo de los presupuestos: con 40
 * repuestos desplegar la lista entera no sirve.
 */
function SelectorRepuesto({ repuestos, onAgregar }) {
  const [busca, setBusca] = useState("");
  const [id, setId] = useState("");
  const [cantidad, setCantidad] = useState("1");

  const hallados = filtrar(repuestos, busca, [
    "codigo",
    "codigoBarra",
    "nombre",
    "marca",
  ]);

  function agregar() {
    onAgregar(id, cantidad);
    setId("");
    setCantidad("1");
    setBusca("");
  }

  return (
    <div className="selector-repuesto armar-items">
      <input
        className="buscar-repuesto"
        value={busca}
        onChange={(e) => {
          setBusca(e.target.value);
          setId("");
        }}
        placeholder="Escanea el codigo o busca por nombre..."
      />
      <select value={id} onChange={(e) => setId(e.target.value)}>
        <option value="">
          {busca
            ? "-- " + hallados.length + " coinciden --"
            : "-- Repuesto del deposito (" + repuestos.length + ") --"}
        </option>
        {hallados.map((r) => (
          <option key={r.id_repuesto} value={r.id_repuesto}>
            {r.codigo} - {r.nombre}
            {r.marca ? " (" + r.marca + ")" : ""} - ${r.precioVenta} - quedan{" "}
            {r.stock}
          </option>
        ))}
      </select>
      <input
        type="number"
        value={cantidad}
        onChange={(e) => setCantidad(e.target.value)}
        placeholder="Cant."
      />
      <button className="secundario" onClick={agregar} disabled={!id}>
        Agregar
      </button>
    </div>
  );
}

export default Ventas;
