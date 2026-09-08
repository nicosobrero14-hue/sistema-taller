// El backend manda las fechas como "2026-09-08T13:03:44.213423" y en el
// mostrador se leen como "08/09/2026 13:03".
//
// Esto estaba repetido en nueve pantallas, cada una con su propia version
// —y una de ellas calculaba mal el dia de hoy—, asi que vive aca.

// "2026-09-08T13:03:44" -> "08/09/2026"
export function fecha(valor) {
  if (!valor) {
    return "-";
  }

  return valor.substring(0, 10).split("-").reverse().join("/");
}

// "2026-09-08T13:03:44" -> "08/09/2026 13:03"
export function fechaHora(valor) {
  if (!valor) {
    return "-";
  }

  return fecha(valor) + " " + valor.substring(11, 16);
}

/**
 * El dia de hoy como "2026-09-08".
 *
 * A mano y no con toISOString(), que pasa la fecha a UTC: a las nueve de
 * la noche en Argentina ya seria el dia siguiente y la caja del dia
 * aparecia vacia.
 */
export function hoy() {
  const f = new Date();

  return (
    f.getFullYear() +
    "-" +
    String(f.getMonth() + 1).padStart(2, "0") +
    "-" +
    String(f.getDate()).padStart(2, "0")
  );
}

// true si la fecha que viene del backend cae en el dia indicado
export function esDelDia(valor, dia) {
  return !!valor && valor.substring(0, 10) === dia;
}
