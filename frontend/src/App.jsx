import { useState } from "react";
import { traerSesion, cerrarSesion } from "./api";
import { confirmar } from "./avisos";
import Icono from "./components/Icono";
import Dialogo from "./components/Dialogo";
import BuscadorGlobal from "./components/BuscadorGlobal";
import Login from "./components/Login";
import Clientes from "./components/Clientes";
import Vehiculos from "./components/Vehiculos";
import Ordenes from "./components/Ordenes";
import Tablero from "./components/Tablero";
import Caja from "./components/Caja";
import Presupuestos from "./components/Presupuestos";
import Repuestos from "./components/Repuestos";
import Ventas from "./components/Ventas";
import Sucursales from "./components/Sucursales";
import Empleados from "./components/Empleados";
import DatosTaller from "./components/DatosTaller";

// Cada seccion con su titulo y su explicacion, para que el que usa el
// sistema sepa donde esta parado sin tener que adivinar.
const SECCIONES = {
  tablero: {
    titulo: "Tablero",
    bajada: "El taller de un vistazo: arrastra las ordenes entre estados",
  },
  ordenes: {
    titulo: "Ordenes de trabajo",
    bajada: "Lo que esta en el taller ahora y su estado",
  },
  caja: {
    titulo: "Caja",
    bajada: "Cuanto entro, en que, y que quedo sin confirmar",
  },
  clientes: {
    titulo: "Clientes",
    bajada: "Quien trae el vehiculo y como contactarlo",
  },
  presupuestos: {
    titulo: "Presupuestos",
    bajada: "Precios congelados con fecha de vencimiento",
  },
  vehiculos: {
    titulo: "Vehiculos",
    bajada: "Los autos de cada cliente y su historial de reparaciones",
  },
  repuestos: {
    titulo: "Deposito",
    bajada: "Stock de repuestos, entradas y salidas",
  },
  ventas: {
    titulo: "Venta de mostrador",
    bajada: "Repuestos que se venden sueltos, sin orden de trabajo",
  },
  sucursales: {
    titulo: "Sucursales",
    bajada: "Los locales del taller",
  },
  empleados: {
    titulo: "Empleados",
    bajada: "Quienes entran al sistema y con que permisos",
  },
  taller: {
    titulo: "Datos del taller",
    bajada: "Lo que sale impreso en presupuestos y comprobantes",
  },
};

function App() {
  const [sesion, setSesion] = useState(traerSesion());
  const [seccion, setSeccion] = useState("ordenes");
  // lo que el buscador global deja puesto en la seccion a la que lleva
  const [filtroInicial, setFiltroInicial] = useState("");

  // sin sesion no se ve nada del sistema
  if (!sesion) {
    return <Login onEntrar={setSesion} />;
  }

  const esAdmin = sesion.rol === "ADMIN";
  const actual = SECCIONES[seccion];

  async function salir() {
    const seguro = await confirmar({
      titulo: "Cerrar sesion",
      texto: "Vas a salir del sistema. Para volver a entrar necesitas tu email y contraseña.",
      boton: "Cerrar sesion",
    });

    if (!seguro) {
      return;
    }

    cerrarSesion();
    setSesion(null);
  }

  // el buscador global lleva a la seccion con el filtro ya cargado
  function irA(destino, filtro) {
    setFiltroInicial(filtro);
    setSeccion(destino);
  }

  // un boton del menu lateral
  function Opcion({ id, icono, texto }) {
    return (
      <button
        className={seccion === id ? "activa" : ""}
        onClick={() => {
          setFiltroInicial("");
          setSeccion(id);
        }}
      >
        <Icono nombre={icono || id} />
        {texto}
      </button>
    );
  }

  return (
    <div className="app">
      <Dialogo />

      <aside className="lateral">
        <div className="marca">
          <div className="logo">TM</div>
          <div>
            <b>Taller Mecanico</b>
            <span>Sistema de gestion</span>
          </div>
        </div>

        <nav>
          <p className="grupo">Taller</p>
          <Opcion id="tablero" texto="Tablero" />
          <Opcion id="ordenes" texto="Ordenes" />
          <Opcion id="presupuestos" texto="Presupuestos" />
          <Opcion id="clientes" texto="Clientes" />
          <Opcion id="vehiculos" texto="Vehiculos" />

          <p className="grupo">Deposito</p>
          <Opcion id="repuestos" texto="Repuestos" />
          <Opcion id="ventas" texto="Venta de mostrador" />

          {/* Solo los administradores ven esta parte. El gateway ademas
              rechaza estas rutas si el token no es de un ADMIN, asi que
              esconderlas es comodidad, no seguridad. */}
          {esAdmin && (
            <>
              <p className="grupo">Administracion</p>
              <Opcion id="caja" texto="Caja" />
              <Opcion id="sucursales" texto="Sucursales" />
              <Opcion id="empleados" texto="Empleados" />
              <Opcion id="taller" icono="sucursales" texto="Datos del taller" />
            </>
          )}
        </nav>

        <div className="usuario">
          <div className="avatar">{sesion.nombre.charAt(0)}</div>
          <div className="quien">
            <b>{sesion.nombre}</b>
            <span>{sesion.rol}</span>
          </div>
          <button onClick={salir} title="Cerrar sesion" aria-label="Cerrar sesion">
            <Icono nombre="salir" />
          </button>
        </div>
      </aside>

      <main>
        <header className="barra">
          <div>
            <h1>{actual.titulo}</h1>
            <p>{actual.bajada}</p>
          </div>
          <BuscadorGlobal onIr={irA} />
        </header>

        <div className="contenido">
          {seccion === "tablero" && <Tablero />}
          {seccion === "ordenes" && <Ordenes />}
          {seccion === "caja" && esAdmin && <Caja />}
          {seccion === "presupuestos" && <Presupuestos />}
          {seccion === "clientes" && (
            <Clientes key={filtroInicial} filtroInicial={filtroInicial} />
          )}
          {seccion === "vehiculos" && (
            <Vehiculos key={filtroInicial} filtroInicial={filtroInicial} />
          )}
          {seccion === "ventas" && <Ventas />}
          {seccion === "repuestos" && (
            <Repuestos key={filtroInicial} filtroInicial={filtroInicial} />
          )}
          {seccion === "sucursales" && esAdmin && <Sucursales />}
          {seccion === "empleados" && esAdmin && <Empleados />}
          {seccion === "taller" && esAdmin && <DatosTaller />}
        </div>
      </main>
    </div>
  );
}

export default App;
