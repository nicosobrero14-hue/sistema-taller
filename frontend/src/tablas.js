// Ordenar y buscar en cualquier tabla del sistema.
//
// La idea es que sirva igual para las ordenes, el deposito o los
// empleados sin escribir nada especifico de cada pantalla: se le pasa la
// lista tal como viene y el nombre del campo.

/**
 * Devuelve la lista ordenada por un campo.
 *
 * El campo puede ser anidado ("cliente.nombre"). No toca la lista
 * original: React necesita un array nuevo para darse cuenta del cambio.
 */
export function ordenar(datos, campo, sentido) {
  if (!campo) {
    return datos;
  }

  const vuelta = sentido === "desc" ? -1 : 1;

  return [...datos].sort((a, b) => comparar(leer(a, campo), leer(b, campo)) * vuelta);
}

/**
 * Compara dos valores de una columna.
 *
 * Los numeros se comparan como numeros (si no, 100 iria antes que 9) y
 * los textos con las reglas del castellano, asi la ñ y los acentos
 * quedan donde corresponde. Lo vacio va siempre al final.
 */
function comparar(a, b) {
  const vacioA = a === null || a === undefined || a === "";
  const vacioB = b === null || b === undefined || b === "";

  if (vacioA || vacioB) {
    return vacioA && vacioB ? 0 : vacioA ? 1 : -1;
  }

  if (typeof a === "number" && typeof b === "number") {
    return a - b;
  }

  // las fechas del backend son "2026-09-08T13:03", que ordenadas como
  // texto ya quedan cronologicas
  return String(a).localeCompare(String(b), "es", { numeric: true });
}

function leer(fila, campo) {
  return campo.split(".").reduce((obj, parte) => (obj ? obj[parte] : null), fila);
}

/**
 * Busca un texto en toda la fila, sin decirle en que campos mirar.
 *
 * Es para el buscador del click derecho, que aparece en tablas que ni
 * siquiera tienen un filtro propio. Junta los valores de la fila —los de
 * adentro tambien— y busca ahi. Los nombres de los campos no entran, si
 * no buscar "nombre" traeria todo.
 */
export function coincide(fila, texto) {
  if (!texto || !texto.trim()) {
    return true;
  }

  return juntarValores(fila).includes(texto.trim().toLowerCase());
}

function juntarValores(valor) {
  if (valor === null || valor === undefined) {
    return "";
  }

  if (Array.isArray(valor)) {
    return valor.map(juntarValores).join(" ");
  }

  if (typeof valor === "object") {
    return Object.values(valor).map(juntarValores).join(" ");
  }

  return String(valor).toLowerCase();
}
