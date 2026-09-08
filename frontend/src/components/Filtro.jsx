import { POR_PAGINA } from "../listas";

// Campo de busqueda que va arriba de cada tabla
export function CampoFiltro({ valor, onCambio, placeholder, cantidad, total }) {
  return (
    <div className="campo-filtro">
      <input
        value={valor}
        onChange={(e) => onCambio(e.target.value)}
        placeholder={placeholder || "Buscar..."}
      />
      {valor && (
        <button className="limpiar-filtro" onClick={() => onCambio("")}>
          Limpiar
        </button>
      )}
      <span className="cuenta">
        {valor ? cantidad + " de " + total : total}{" "}
        {total === 1 ? "resultado" : "resultados"}
      </span>
    </div>
  );
}

// "Mostrar mas" al pie de la tabla, cuando la lista no entra de una
export function VerMas({ mostrando, total, onMas }) {
  if (mostrando >= total) {
    return null;
  }

  return (
    <button className="ver-mas" onClick={onMas}>
      Mostrar {Math.min(POR_PAGINA, total - mostrando)} mas ({mostrando} de{" "}
      {total})
    </button>
  );
}
