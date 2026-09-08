import { useEffect, useRef, useState } from "react";
import Icono from "./Icono";

/**
 * La columna de acciones de las tablas.
 *
 * Antes cada fila terminaba con cinco botones pegados uno al lado del otro
 * y no se entendia cual era el importante. Ahora se ve uno solo, el que de
 * verdad se usa en esa fila, y el resto queda detras de los tres puntitos.
 * De paso lo que borra deja de estar a un click de distancia.
 */
function Acciones({ principal, opciones }) {
	const [abierto, setAbierto] = useState(false);
	// si la fila esta al fondo de la pantalla, el menu abre para arriba
	const [haciaArriba, setHaciaArriba] = useState(false);
	const caja = useRef(null);

	// se cierra al hacer click afuera o con Escape. Al scrollear no se
	// cierra: el menu esta pegado a la fila, asi que se mueve con ella
	useEffect(() => {
		if (!abierto) {
			return;
		}

		function afuera(e) {
			if (caja.current && !caja.current.contains(e.target)) {
				setAbierto(false);
			}
		}

		function tecla(e) {
			if (e.key === "Escape") {
				setAbierto(false);
			}
		}

		document.addEventListener("mousedown", afuera);
		document.addEventListener("keydown", tecla);

		return () => {
			document.removeEventListener("mousedown", afuera);
			document.removeEventListener("keydown", tecla);
		};
	}, [abierto]);

	// las opciones se arman con condiciones (ej: solo si es admin), asi que
	// aca se descartan las que quedaron en falso
	const items = (opciones || []).filter((o) => o);

	function abrirMenu(e) {
		const donde = e.currentTarget.getBoundingClientRect();
		setHaciaArriba(window.innerHeight - donde.bottom < 40 + items.length * 34);
		setAbierto(!abierto);
	}

	return (
		<div className="acciones" ref={caja}>
			{principal && (
				<button
					className={"accion-principal " + (principal.tono || "")}
					onClick={principal.alHacer}
					title={principal.ayuda || principal.texto}
				>
					{principal.icono && <Icono nombre={principal.icono} />}
					{principal.texto}
				</button>
			)}

			{items.length > 0 && (
				<button
					className={"puntitos" + (abierto ? " abierto" : "")}
					onClick={abrirMenu}
					title="Mas acciones"
					aria-label="Mas acciones"
				>
					<Icono nombre="puntos" />
				</button>
			)}

			{abierto && (
				<div className={"menu-acciones" + (haciaArriba ? " arriba" : "")}>
					{items.map((o, i) => (
						<button
							key={i}
							className={o.tono === "peligro" ? "peligro" : ""}
							onClick={() => {
								setAbierto(false);
								o.alHacer();
							}}
						>
							{o.icono && <Icono nombre={o.icono} />}
							<span>{o.texto}</span>
							{o.detalle !== undefined && o.detalle !== null && (
								<em>{o.detalle}</em>
							)}
						</button>
					))}
				</div>
			)}
		</div>
	);
}

export default Acciones;
