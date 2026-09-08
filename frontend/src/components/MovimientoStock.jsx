import { useEffect, useState } from "react";
import { pedir, leerRespuesta } from "../api";
import { confirmar } from "../avisos";

function MovimientoStock({ repuesto, onCerrar, onCambio }) {
  const [movimientos, setMovimientos] = useState([]);
  const [stockActual, setStockActual] = useState(repuesto.stock);
  const [mensaje, setMensaje] = useState("");

  const [tipo, setTipo] = useState("ENTRADA");
  const [cantidad, setCantidad] = useState("1");
  const [motivo, setMotivo] = useState("");

  //1- historial del deposito para este repuesto
  function traerMovimientos() {
    pedir("/movimientos/repuesto/" + repuesto.id_repuesto)
      .then((res) => res.json())
      .then((data) => setMovimientos(data))
      .catch(() => setMovimientos([]));

    pedir("/repuestos/traer/" + repuesto.id_repuesto)
      .then((res) => res.json())
      .then((data) => setStockActual(data.stock))
      .catch(() => setStockActual(repuesto.stock));
  }

  useEffect(() => {
    traerMovimientos();
  }, [repuesto]);

  //2- registrar el movimiento. Es la unica forma de que cambie el stock:
  // asi siempre queda anotado por que subio o bajo.
  async function crearMovimiento(e) {
    e.preventDefault();

    const texto =
      tipo === "AJUSTE"
        ? "Dejar el stock en " + cantidad + " unidades?"
        : "Registrar " + tipo + " de " + cantidad + " unidad(es)?";

    const seguro = await confirmar({
      titulo: tipo === "AJUSTE" ? "Ajustar el stock" : "Registrar movimiento",
      texto: texto,
      boton: "Registrar",
    });

    if (!seguro) {
      return;
    }

    pedir("/movimientos/crear", {
      method: "POST",
      body: JSON.stringify({
        id_repuesto: repuesto.id_repuesto,
        tipo: tipo,
        cantidad: Number(cantidad),
        motivo: motivo,
        id_orden: null,
      }),
    })
      .then(leerRespuesta)
      .then((r) => {
        setMensaje(r.mensaje);

        if (r.ok) {
          setCantidad("1");
          setMotivo("");
        }

        traerMovimientos();
        onCambio();
      });
  }

  return (
    <div className="panel">
      <h3>
        Stock de {repuesto.nombre} ({repuesto.codigo}) — hoy hay {stockActual}
        <button className="secundario" onClick={onCerrar}>
          Cerrar
        </button>
      </h3>

      <form onSubmit={crearMovimiento}>
        <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
          <option value="ENTRADA">ENTRADA (compra, devolucion)</option>
          <option value="SALIDA">SALIDA (se uso o se vendio)</option>
          <option value="AJUSTE">AJUSTE (conteo de deposito)</option>
        </select>
        <input
          type="number"
          placeholder={tipo === "AJUSTE" ? "Stock real contado" : "Cantidad"}
          value={cantidad}
          onChange={(e) => setCantidad(e.target.value)}
          required
        />
        <input
          placeholder="Motivo"
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
        />
        <button type="submit">Registrar</button>
      </form>

      {mensaje && <p className="mensaje">{mensaje}</p>}

      <table>
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Tipo</th>
            <th className="numero">Cantidad</th>
            <th>Stock despues</th>
            <th>Motivo</th>
            <th>Orden</th>
          </tr>
        </thead>
        <tbody>
          {movimientos.map((m) => (
            <tr key={m.id_movimiento}>
              <td>{m.fecha ? m.fecha.substring(0, 16) : ""}</td>
              <td>{m.tipo}</td>
              <td>{m.cantidad}</td>
              <td>{m.stockResultante}</td>
              <td>{m.motivo}</td>
              <td>{m.id_orden ? "Orden " + m.id_orden : "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {movimientos.length === 0 && <p>Todavia no hay movimientos.</p>}
    </div>
  );
}

export default MovimientoStock;
