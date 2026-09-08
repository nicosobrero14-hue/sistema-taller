// Todo pasa por el api-gateway: el front no conoce los puertos de cada servicio.
// El gateway le pregunta a eureka en que puerto esta cada uno.
export const URL_API = "http://localhost:8080";

// La sesion vive en el navegador. Si cerras y volves a abrir, seguis adentro
// hasta que el token venza (12 horas).
export function guardarSesion(sesion) {
  localStorage.setItem("sesion", JSON.stringify(sesion));
}

export function traerSesion() {
  try {
    return JSON.parse(localStorage.getItem("sesion"));
  } catch (e) {
    return null;
  }
}

// Quien esta usando el sistema puede hacer menos cosas si no es admin.
// Esto es para esconder botones: el candado de verdad esta en el gateway,
// que rechaza igual aunque alguien llame a la API a mano.
export function esAdmin() {
  const sesion = traerSesion();

  return !!sesion && sesion.rol === "ADMIN";
}

export function cerrarSesion() {
  localStorage.removeItem("sesion");
}

// Reemplaza a fetch en toda la app: le agrega el token a cada pedido.
// Si el token vencio, el gateway responde 401 y volvemos al login.
export function pedir(ruta, opciones) {
  const sesion = traerSesion();
  const config = opciones || {};

  config.headers = {
    "Content-Type": "application/json",
    ...(config.headers || {}),
  };

  if (sesion && sesion.token) {
    config.headers.Authorization = "Bearer " + sesion.token;
  }

  return fetch(URL_API + ruta, config).then((res) => {
    if (res.status === 401) {
      cerrarSesion();
      window.location.reload();
    }

    return res;
  });
}

// Igual que pedir(), pero para subir archivos. No se pone Content-Type
// a proposito: el navegador lo arma solo con el separador que necesita
// el multipart.
export function pedirArchivo(ruta, formulario) {
  const sesion = traerSesion();
  const cabeceras = {};

  if (sesion && sesion.token) {
    cabeceras.Authorization = "Bearer " + sesion.token;
  }

  return fetch(URL_API + ruta, {
    method: "POST",
    headers: cabeceras,
    body: formulario,
  });
}

// Lee lo que responde el backend y devuelve { ok, mensaje }.
// El mensaje puede ser un texto ("El Cliente fue eliminado correctamente")
// o, cuando falla una validacion, el json con el campo que fallo.
// El ok sirve para no limpiar el formulario si la operacion se rechazo.
export async function leerRespuesta(res) {
  const texto = await res.text();

  let mensaje = texto;

  try {
    const json = JSON.parse(texto);

    mensaje = Object.keys(json)
      .map((campo) => campo + ": " + json[campo])
      .join(" | ");
  } catch (e) {
    // no era json, se deja el texto tal cual
  }

  return { ok: res.ok, mensaje: mensaje };
}
