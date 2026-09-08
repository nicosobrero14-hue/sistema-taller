// Los carteles del navegador (window.confirm y compañia) no se pueden
// maquillar: rompen el diseño y encima dicen "localhost:5173 dice".
//
// Aca vive el reemplazo. El componente Dialogo se registra al arrancar y
// estas funciones devuelven una promesa, asi los llamadores quedan casi
// igual que antes:
//
//     if (!(await confirmar({ texto: "Borrar?" }))) {
//         return;
//     }

let abrirDialogo = null;

export function registrarDialogo(fn) {
  abrirDialogo = fn;
}

/**
 * Pregunta y devuelve true o false.
 *
 * opciones: { titulo, texto, detalle, boton, tono }
 * El tono puede ser "peligro" (rojo) o "verde"; por defecto va en ambar.
 */
export function confirmar(opciones) {
  if (!abrirDialogo) {
    // por las dudas: si el dialogo no esta montado, no nos quedamos mudos
    return Promise.resolve(window.confirm(opciones.texto));
  }

  return new Promise((resolver) => abrirDialogo({ ...opciones, resolver: resolver }));
}

// Un solo boton: avisa algo y no pregunta nada.
export function avisar(opciones) {
  if (!abrirDialogo) {
    window.alert(opciones.texto);
    return Promise.resolve();
  }

  return new Promise((resolver) =>
    abrirDialogo({ ...opciones, soloAviso: true, resolver: resolver })
  );
}

/**
 * Pide que escriban algo. Devuelve el texto, o null si cancelaron.
 *
 * opciones: { titulo, texto, etiqueta, placeholder, boton, tono }
 */
export function pedirTexto(opciones) {
  if (!abrirDialogo) {
    return Promise.resolve(window.prompt(opciones.texto));
  }

  return new Promise((resolver) =>
    abrirDialogo({ ...opciones, pideTexto: true, resolver: resolver })
  );
}

/**
 * Pide que elijan de una lista. Devuelve el valor elegido, o null.
 *
 * opciones: { titulo, texto, etiqueta, boton, lista: [{ valor, texto }] }
 */
export function pedirOpcion(opciones) {
  if (!abrirDialogo) {
    return Promise.resolve(window.prompt(opciones.texto));
  }

  return new Promise((resolver) =>
    abrirDialogo({ ...opciones, pideOpcion: true, resolver: resolver })
  );
}
