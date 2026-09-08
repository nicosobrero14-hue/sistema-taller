import { useState } from "react";
import { URL_API, guardarSesion } from "../api";

function Login({ onEntrar }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [entrando, setEntrando] = useState(false);

  // Es el unico pedido que no lleva token: todavia no hay sesion.
  function entrar(e) {
    e.preventDefault();
    setError("");
    setEntrando(true);

    fetch(URL_API + "/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email, password: password }),
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error("credenciales");
        }
        return res.json();
      })
      .then((sesion) => {
        guardarSesion(sesion);
        onEntrar(sesion);
      })
      .catch(() => {
        setError("Email o contraseña incorrectos");
        setEntrando(false);
      });
  }

  return (
    <div className="pantalla-login">
      <div className="caja-login">
        <div className="marca">
          <div className="logo">TM</div>
          <div>
            <b>Taller Mecanico</b>
            <span>Sistema de gestion</span>
          </div>
        </div>

        <form onSubmit={entrar}>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="tu@taller.com"
              required
              autoFocus
            />
          </label>

          <label>
            Contraseña
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Tu contraseña"
              required
            />
          </label>

          {error && <p className="error-login">{error}</p>}

          <button type="submit" disabled={entrando}>
            {entrando ? "Entrando..." : "Entrar"}
          </button>
        </form>

        <p className="pie-login">
          Primera vez? El sistema crea un usuario inicial:
          <br />
          <code>admin@taller.com</code> / <code>admin123</code>
        </p>
      </div>
    </div>
  );
}

export default Login;
