import { useEffect, useState } from "react";
import { pedir } from "../api";
import { fechaHora } from "../fechas";

function HistorialVehiculo({ vehiculo, onCerrar }) {
  const [ordenes, setOrdenes] = useState([]);

  //1- todas las ordenes que tuvo este vehiculo
  useEffect(() => {
    pedir("/ordenes/vehiculo/" + vehiculo.id_vehiculo)
      .then((res) => res.json())
      .then((data) => setOrdenes(data))
      .catch(() => setOrdenes([]));
  }, [vehiculo]);

  //2- las fechas vienen como 2026-09-15T17:00:00, se corta el segundero
  return (
    <div className="panel">
      <h3>
        <span>
          Historial de {vehiculo.patente} — {vehiculo.marca} {vehiculo.modelo}
        </span>
        <button className="secundario" onClick={onCerrar}>
          Cerrar
        </button>
      </h3>

      {ordenes.length === 0 && (
        <p className="vacio">Este vehiculo todavia no tuvo ordenes.</p>
      )}

      {ordenes.map((o) => (
        <div key={o.id_orden} className="tarjeta">
          <div className="tarjeta-titulo">
            <b>Orden {o.id_orden}</b>
            <span className={"etiqueta estado-" + o.estado}>{o.estado}</span>
            <span className="importe">${o.total}</span>
          </div>

          <p className="tarjeta-texto">{o.diagnostico}</p>

          <div>
            <span className="dato">Ingreso: {fechaHora(o.fechaIngreso)}</span>
            <span className="dato">
              Entregado: {o.fechaEntregaReal ? fechaHora(o.fechaEntregaReal) : "todavia no"}
            </span>
          </div>

          {(o.items || []).length > 0 && (
            <ul>
              {o.items.map((i) => (
                <li key={i.id_item}>
                  {i.descripcion} — {i.cantidad} x ${i.precioUnitario}
                  {i.id_repuesto && " (del deposito)"}
                </li>
              ))}
            </ul>
          )}
        </div>
      ))}
    </div>
  );
}

export default HistorialVehiculo;
