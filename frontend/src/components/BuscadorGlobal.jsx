import { useEffect, useState } from "react";
import { pedir } from "../api";
import { filtrar } from "../listas";

/**
 * Lo primero que pasa en el mostrador es que alguien dice una patente.
 * Este buscador esta en la barra de arriba y busca a la vez en vehiculos,
 * clientes y repuestos: al elegir un resultado lleva a esa seccion con
 * el filtro ya puesto.
 */
function BuscadorGlobal({ onIr }) {
  const [texto, setTexto] = useState("");
  const [vehiculos, setVehiculos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [repuestos, setRepuestos] = useState([]);

  useEffect(() => {
    pedir("/vehiculos/traer")
      .then((res) => (res.ok ? res.json() : []))
      .then(setVehiculos)
      .catch(() => setVehiculos([]));

    pedir("/clientes/traer")
      .then((res) => (res.ok ? res.json() : []))
      .then(setClientes)
      .catch(() => setClientes([]));

    pedir("/repuestos/traer")
      .then((res) => (res.ok ? res.json() : []))
      .then(setRepuestos)
      .catch(() => setRepuestos([]));
  }, []);

  // recien busca con dos letras, si no muestra todo
  const busca = texto.trim().length >= 2 ? texto : "";

  const vehiculosHallados = busca
    ? filtrar(vehiculos, busca, ["patente", "marca", "modelo", "cliente.nombre"]).slice(0, 5)
    : [];

  const clientesHallados = busca
    ? filtrar(clientes, busca, ["nombre", "telefono", "email"]).slice(0, 4)
    : [];

  const repuestosHallados = busca
    ? filtrar(repuestos, busca, ["codigo", "codigoBarra", "nombre", "marca"]).slice(0, 4)
    : [];

  const hayAlgo =
    vehiculosHallados.length + clientesHallados.length + repuestosHallados.length > 0;

  function ir(seccion, filtro) {
    onIr(seccion, filtro);
    setTexto("");
  }

  return (
    <div className="buscador-global">
      <input
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
        placeholder="Buscar patente, cliente o repuesto..."
      />

      {busca && (
        <div className="resultados">
          {!hayAlgo && <p className="sin-resultados">Nada coincide con "{texto}"</p>}

          {vehiculosHallados.length > 0 && (
            <>
              <p className="grupo-resultado">Vehiculos</p>
              {vehiculosHallados.map((v) => (
                <button key={v.id_vehiculo} onClick={() => ir("vehiculos", v.patente)}>
                  <b className="clave">{v.patente}</b>
                  <span>
                    {v.marca} {v.modelo}
                    {v.cliente ? " · " + v.cliente.nombre : ""}
                  </span>
                </button>
              ))}
            </>
          )}

          {clientesHallados.length > 0 && (
            <>
              <p className="grupo-resultado">Clientes</p>
              {clientesHallados.map((c) => (
                <button key={c.id_cliente} onClick={() => ir("clientes", c.nombre)}>
                  <b>{c.nombre}</b>
                  <span>{c.telefono || c.email || ""}</span>
                </button>
              ))}
            </>
          )}

          {repuestosHallados.length > 0 && (
            <>
              <p className="grupo-resultado">Repuestos</p>
              {repuestosHallados.map((r) => (
                <button key={r.id_repuesto} onClick={() => ir("repuestos", r.codigo)}>
                  <b className="clave">{r.codigo}</b>
                  <span>
                    {r.nombre}
                    {r.marca ? " · " + r.marca : ""} · stock {r.stock}
                  </span>
                </button>
              ))}
            </>
          )}
        </div>
      )}
    </div>
  );
}

export default BuscadorGlobal;
