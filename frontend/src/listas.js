// Helpers para las tablas: filtrar por texto y mostrar de a tandas.
// Con 40 repuestos ya molesta scrollear; con 500 no se puede usar.

export const POR_PAGINA = 15;

/**
 * Deja las filas donde alguno de los campos indicados contiene el texto.
 * Los campos pueden ser anidados: "cliente.nombre".
 */
export function filtrar(datos, texto, campos) {
  if (!texto || !texto.trim()) {
    return datos;
  }

  const busca = texto.trim().toLowerCase();

  return datos.filter((fila) =>
    campos.some((campo) => {
      const valor = leerCampo(fila, campo);
      return valor !== null && String(valor).toLowerCase().includes(busca);
    })
  );
}

function leerCampo(fila, campo) {
  return campo.split(".").reduce((obj, parte) => (obj ? obj[parte] : null), fila);
}
