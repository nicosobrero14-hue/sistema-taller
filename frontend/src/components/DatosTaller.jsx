import { useEffect, useState } from "react";
import { pedir, leerRespuesta } from "../api";
import { confirmar } from "../avisos";

function DatosTaller() {
  const [mensaje, setMensaje] = useState("");

  const [razonSocial, setRazonSocial] = useState("");
  const [nombreFantasia, setNombreFantasia] = useState("");
  const [cuit, setCuit] = useState("");
  const [domicilio, setDomicilio] = useState("");
  const [telefono, setTelefono] = useState("");
  const [email, setEmail] = useState("");
  const [condicionFiscal, setCondicionFiscal] = useState("MONOTRIBUTO");
  const [puntoVenta, setPuntoVenta] = useState("");
  const [precioHora, setPrecioHora] = useState("");
  const [iva, setIva] = useState("");

  // adonde tiene que pagar el cliente
  const [banco, setBanco] = useState("");
  const [cbu, setCbu] = useState("");
  const [aliasCbu, setAliasCbu] = useState("");
  const [titular, setTitular] = useState("");
  const [datosTarjeta, setDatosTarjeta] = useState("");
  const [datosQr, setDatosQr] = useState("");

  //1- traer los datos del taller (es una sola fila)
  function traerConfiguracion() {
    pedir("/configuracion/traer")
      .then((res) => res.json())
      .then((c) => {
        setRazonSocial(c.razonSocial || "");
        setNombreFantasia(c.nombreFantasia || "");
        setCuit(c.cuit || "");
        setDomicilio(c.domicilio || "");
        setTelefono(c.telefono || "");
        setEmail(c.email || "");
        setCondicionFiscal(c.condicionFiscal || "MONOTRIBUTO");
        setPuntoVenta(c.puntoVenta || "");
        setPrecioHora(c.precioHoraManoObra || "");
        setIva(c.porcentajeIva || "");
        setBanco(c.banco || "");
        setCbu(c.cbu || "");
        setAliasCbu(c.aliasCbu || "");
        setTitular(c.titularCuenta || "");
        setDatosTarjeta(c.datosTarjeta || "");
        setDatosQr(c.datosQr || "");
      })
      .catch(() => setMensaje("No se pudieron traer los datos del taller"));
  }

  useEffect(() => {
    traerConfiguracion();
  }, []);

  //2- guardarlos
  async function guardar(e) {
    e.preventDefault();

    const seguro = await confirmar({
      titulo: "Guardar los datos del taller",
      texto: "Es lo que sale impreso en presupuestos y comprobantes.",
      boton: "Guardar",
    });

    if (!seguro) {
      return;
    }

    pedir("/configuracion/editar", {
      method: "PUT",
      body: JSON.stringify({
        razonSocial: razonSocial,
        nombreFantasia: nombreFantasia,
        cuit: cuit,
        domicilio: domicilio,
        telefono: telefono,
        email: email,
        condicionFiscal: condicionFiscal,
        puntoVenta: puntoVenta,
        precioHoraManoObra: precioHora === "" ? null : Number(precioHora),
        porcentajeIva: iva === "" ? null : Number(iva),
        banco: banco,
        cbu: cbu,
        aliasCbu: aliasCbu,
        titularCuenta: titular,
        datosTarjeta: datosTarjeta,
        datosQr: datosQr,
      }),
    })
      .then(leerRespuesta)
      .then((r) => setMensaje(r.mensaje));
  }

  return (
    <div>
      <h2>Datos del taller</h2>

      <p className="ayuda">
        Estos datos van a salir impresos en presupuestos y comprobantes. El
        precio de la hora se usa para calcular la mano de obra de los
        presupuestos.
      </p>

      <form onSubmit={guardar}>
        <input
          placeholder="Razon social"
          value={razonSocial}
          onChange={(e) => setRazonSocial(e.target.value)}
        />
        <input
          placeholder="Nombre de fantasia"
          value={nombreFantasia}
          onChange={(e) => setNombreFantasia(e.target.value)}
        />
        <input
          placeholder="CUIT"
          value={cuit}
          onChange={(e) => setCuit(e.target.value)}
        />
        <input
          placeholder="Domicilio"
          value={domicilio}
          onChange={(e) => setDomicilio(e.target.value)}
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
        <select
          value={condicionFiscal}
          onChange={(e) => setCondicionFiscal(e.target.value)}
        >
          <option value="MONOTRIBUTO">Monotributo</option>
          <option value="RESPONSABLE_INSCRIPTO">Responsable inscripto</option>
        </select>
        <input
          placeholder="Punto de venta"
          value={puntoVenta}
          onChange={(e) => setPuntoVenta(e.target.value)}
        />
        <input
          type="number"
          placeholder="Precio hora mano de obra"
          value={precioHora}
          onChange={(e) => setPrecioHora(e.target.value)}
        />
        <input
          type="number"
          placeholder="IVA % (21)"
          value={iva}
          onChange={(e) => setIva(e.target.value)}
        />
        <button type="submit">Guardar</button>
      </form>

      <h2>Datos para cobrar</h2>

      <p className="ayuda">
        Esto es lo que ve el que atiende cuando el cliente elige como pagar, y
        lo que le dicta para que transfiera. Cualquier empleado puede leerlo;
        <b> cambiarlo solo lo puede hacer un administrador</b>, porque el que
        cambia un CBU desvia la plata del taller.
      </p>

      <form onSubmit={guardar}>
        <input
          placeholder="Banco"
          value={banco}
          onChange={(e) => setBanco(e.target.value)}
        />
        <input
          placeholder="Titular de la cuenta"
          value={titular}
          onChange={(e) => setTitular(e.target.value)}
        />
        <input
          placeholder="CBU / CVU"
          value={cbu}
          onChange={(e) => setCbu(e.target.value)}
        />
        <input
          placeholder="Alias"
          value={aliasCbu}
          onChange={(e) => setAliasCbu(e.target.value)}
        />
        <input
          placeholder="Tarjetas: que posnet, cuotas..."
          value={datosTarjeta}
          onChange={(e) => setDatosTarjeta(e.target.value)}
        />
        <input
          placeholder="QR: de que billetera"
          value={datosQr}
          onChange={(e) => setDatosQr(e.target.value)}
        />
        <button type="submit">Guardar</button>
      </form>

      {mensaje && <p className="mensaje">{mensaje}</p>}
    </div>
  );
}

export default DatosTaller;
