import { useEffect, useState } from "react";
import { pedir, leerRespuesta } from "../api";
import { confirmar } from "../avisos";
import LineaTiempo from "./LineaTiempo";

function DetalleOrden({ orden, onCerrar, onCambio }) {
  const [items, setItems] = useState([]);
  const [total, setTotal] = useState(0);
  const [subtotal, setSubtotal] = useState(0);
  const [descuento, setDescuento] = useState(0);
  const [mensaje, setMensaje] = useState("");

  // el deposito, para elegir el repuesto en vez de escribirlo a mano
  const [repuestos, setRepuestos] = useState([]);

  const [tipo, setTipo] = useState("MANO_DE_OBRA");
  const [idRepuesto, setIdRepuesto] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [cantidad, setCantidad] = useState("1");
  const [precioUnitario, setPrecioUnitario] = useState("");

  //1- traer la orden completa, que ya viene con sus items y el total sumado
  function traerOrden() {
    pedir("/ordenes/traer/" + orden.id_orden)
      .then((res) => res.json())
      .then((data) => {
        setItems(data.items || []);
        setTotal(data.total);
        setSubtotal(data.subtotal);
        setDescuento(data.descuento || 0);
      })
      .catch(() => setItems([]));
  }

  //2- traer el deposito de inventario-service
  function traerRepuestos() {
    pedir("/repuestos/traer")
      .then((res) => res.json())
      .then((data) => setRepuestos(data))
      .catch(() => setRepuestos([]));
  }

  useEffect(() => {
    traerOrden();
    traerRepuestos();
  }, [orden]);

  //3- al elegir un repuesto del deposito se completan solos la descripcion
  // y el precio, asi no hay que copiarlos a mano
  function elegirRepuesto(id) {
    setIdRepuesto(id);

    const repuesto = repuestos.find((r) => String(r.id_repuesto) === String(id));

    if (repuesto) {
      setDescripcion(repuesto.nombre);
      setPrecioUnitario(repuesto.precioVenta);
    }
  }

  //4- al cambiar de tipo se limpia lo que habia cargado
  function cambiarTipo(nuevoTipo) {
    setTipo(nuevoTipo);
    setIdRepuesto("");
    setDescripcion("");
    setPrecioUnitario("");
  }

  //5- agregar un item a esta orden. Si es un repuesto del deposito,
  // taller-service le avisa a inventario-service para que lo descuente.
  function crearItem(e) {
    e.preventDefault();

    pedir("/items/crear", {
      method: "POST",
      body: JSON.stringify({
        id_orden: orden.id_orden,
        tipo: tipo,
        id_repuesto: idRepuesto ? Number(idRepuesto) : null,
        descripcion: descripcion,
        cantidad: Number(cantidad),
        precioUnitario: Number(precioUnitario),
      }),
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);

        // si no habia stock, el item no se cargo y el formulario queda igual
        if (r.ok) {
          setIdRepuesto("");
          setDescripcion("");
          setCantidad("1");
          setPrecioUnitario("");
        }

        traerOrden();
        traerRepuestos();
        onCambio();
      });
  }

  //6- sacar un item de la orden. Si era un repuesto, vuelve al deposito.
  async function borrarItem(item) {
    const vuelveAlDeposito = item.tipo === "REPUESTO" && item.id_repuesto;

    const seguro = await confirmar({
      titulo: "Sacar de la orden",
      texto: item.descripcion,
      detalle: vuelveAlDeposito
        ? "Es un repuesto del deposito: al sacarlo vuelve al stock."
        : "Se saca de la orden y el total se recalcula.",
      boton: "Sacar",
      tono: "peligro",
    });

    if (!seguro) {
      return;
    }

    pedir("/items/borrar/" + item.id_item, {
      method: "DELETE",
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);
        traerOrden();
        traerRepuestos();
        onCambio();
      });
  }

  return (
    <div className="panel">
      <h3>
        Items de la orden {orden.id_orden}
        <button className="secundario" onClick={onCerrar}>
          Cerrar
        </button>
      </h3>

      <form onSubmit={crearItem}>
        <select value={tipo} onChange={(e) => cambiarTipo(e.target.value)}>
          <option value="MANO_DE_OBRA">MANO_DE_OBRA</option>
          <option value="REPUESTO">REPUESTO</option>
        </select>

        {tipo === "REPUESTO" && (
          <select
            value={idRepuesto}
            onChange={(e) => elegirRepuesto(e.target.value)}
            required
          >
            <option value="">-- Repuesto del deposito --</option>
            {repuestos.map((r) => (
              <option key={r.id_repuesto} value={r.id_repuesto}>
                {r.codigo} - {r.nombre} (stock {r.stock})
              </option>
            ))}
          </select>
        )}

        <input
          placeholder="Descripcion"
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
          required
        />
        <input
          type="number"
          placeholder="Cantidad"
          value={cantidad}
          onChange={(e) => setCantidad(e.target.value)}
        />
        <input
          type="number"
          step="0.01"
          placeholder="Precio unitario"
          value={precioUnitario}
          onChange={(e) => setPrecioUnitario(e.target.value)}
        />
        <button type="submit">Agregar</button>
      </form>

      {tipo === "REPUESTO" && (
        <p className="ayuda">
          Al agregarlo se descuenta del deposito. Si despues se saca de la
          orden, vuelve al stock.
        </p>
      )}

      {mensaje && <p className="mensaje">{mensaje}</p>}

      <table>
        <thead>
          <tr>
            <th>Tipo</th>
            <th>Descripcion</th>
            <th className="numero">Cantidad</th>
            <th className="importe">Precio</th>
            <th className="importe">Subtotal</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {items.map((i) => (
            <tr key={i.id_item}>
              <td>
                {i.tipo}
                {i.id_repuesto && <span className="ayuda"> (del deposito)</span>}
              </td>
              <td>{i.descripcion}</td>
              <td className="numero">{i.cantidad}</td>
              <td className="importe">${i.precioUnitario}</td>
              <td className="importe">${i.cantidad * i.precioUnitario}</td>
              <td>
                <button className="quitar-fila" onClick={() => borrarItem(i)}>
                  Borrar
                </button>
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          {Number(descuento) > 0 && (
            <>
              <tr>
                <th colSpan="4" className="sin-relieve">
                  Subtotal
                </th>
                <th colSpan="2" className="sin-relieve">
                  ${subtotal}
                </th>
              </tr>
              <tr>
                <th colSpan="4" className="sin-relieve">
                  Descuento
                </th>
                <th colSpan="2" className="sin-relieve">
                  -${descuento}
                </th>
              </tr>
            </>
          )}
          <tr>
            <th colSpan="4">Total de la orden</th>
            <th colSpan="2">${total}</th>
          </tr>
        </tfoot>
      </table>

      <LineaTiempo id_orden={orden.id_orden} />
    </div>
  );
}

export default DetalleOrden;
