// Iconos dibujados a mano con SVG, para no depender de ninguna libreria.
// Todos usan la misma grilla de 20x20 y el mismo grosor de trazo, asi se
// ven de la misma familia aunque esten en lugares distintos.
function Icono({ nombre }) {
	const trazos = {
		/* ---------- secciones del menu ---------- */
		ordenes: (
			<>
				<path d="M4 3h9l4 4v11a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z" />
				<path d="M13 3v4h4" />
				<path d="M6.5 11.5h7M6.5 14.5h4" />
			</>
		),
		presupuestos: (
			<>
				<path d="M5 2.5h10a1 1 0 0 1 1 1v14l-2.2-1.5L11.6 17.5 10 16l-1.6 1.5L6.2 16 4 17.5v-14a1 1 0 0 1 1-1z" />
				<path d="M7.5 7h5M7.5 10.5h5M7.5 14h3" />
			</>
		),
		clientes: (
			<>
				<circle cx="10" cy="7" r="3.2" />
				<path d="M3.5 17c0-3 2.9-5 6.5-5s6.5 2 6.5 5" />
			</>
		),
		vehiculos: (
			<>
				<path d="M2.5 12.5h15M4 12.5l1.6-4.3A2 2 0 0 1 7.5 7h5a2 2 0 0 1 1.9 1.2l1.6 4.3" />
				<path d="M3 12.5v3.5h2.5v-1.5h9V16H17v-3.5" />
				<circle cx="6.2" cy="12.8" r="0.9" />
				<circle cx="13.8" cy="12.8" r="0.9" />
			</>
		),
		repuestos: (
			<>
				<path d="M10 3.2 16.2 6.6v6.8L10 16.8 3.8 13.4V6.6z" />
				<path d="M3.8 6.6 10 10l6.2-3.4M10 10v6.8" />
			</>
		),
		sucursales: (
			<>
				<path d="M3.5 8.5 10 3.5l6.5 5" />
				<path d="M5 8.5V16h10V8.5" />
				<path d="M8.5 16v-4h3v4" />
			</>
		),
		empleados: (
			<>
				<circle cx="7.5" cy="7" r="2.6" />
				<path d="M2.5 16.5c0-2.6 2.2-4.3 5-4.3s5 1.7 5 4.3" />
				<path d="M13.5 6.5h4M13.5 9.5h4M14.5 12.5h3" />
			</>
		),

		tablero: (
			<>
				<path d="M3 4.5h4.2v11H3zM7.9 4.5h4.2v7.5H7.9zM12.8 4.5H17v9.5h-4.2z" />
			</>
		),
		ventas: (
			<>
				<path d="M2.5 3.5h2l2.2 9.2h8.1l1.7-6.4H5.4" />
				<circle cx="8.4" cy="16" r="1.2" />
				<circle cx="14.4" cy="16" r="1.2" />
			</>
		),
		caja: (
			<>
				<path d="M2.5 7.5h15v8.5a1 1 0 0 1-1 1h-13a1 1 0 0 1-1-1z" />
				<path d="M2.5 7.5 4.3 3.8h11.4l1.8 3.7" />
				<path d="M10 3.8v3.7M7.8 11.5h4.4" />
			</>
		),

		/* ---------- acciones de las filas ---------- */
		puntos: (
			<>
				<circle cx="10" cy="4.6" r="1.35" fill="currentColor" stroke="none" />
				<circle cx="10" cy="10" r="1.35" fill="currentColor" stroke="none" />
				<circle cx="10" cy="15.4" r="1.35" fill="currentColor" stroke="none" />
			</>
		),
		items: (
			<>
				<path d="M4 5.5h12M4 10h12M4 14.5h8" />
			</>
		),
		foto: (
			<>
				<path d="M3 6.5h3l1.3-2h5.4L14 6.5h3a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1v-8a1 1 0 0 1 1-1z" />
				<circle cx="10" cy="11" r="3" />
			</>
		),
		editar: (
			<>
				<path d="M13.7 3.6a1.7 1.7 0 0 1 2.4 2.4L7.6 14.5l-3.2.8.8-3.2z" />
				<path d="M12.2 5.1 14.6 7.5" />
			</>
		),
		borrar: (
			<>
				<path d="M3.8 5.5h12.4M8 5.5V3.8h4v1.7" />
				<path d="M5.4 5.5 6.2 17h7.6l.8-11.5" />
				<path d="M8.6 8.5v5.5M11.4 8.5v5.5" />
			</>
		),
		cobrar: (
			<>
				<rect x="2.5" y="5" width="15" height="10" rx="1.4" />
				<circle cx="10" cy="10" r="2.3" />
				<path d="M5.2 10h.01M14.8 10h.01" />
			</>
		),
		recibo: (
			<>
				<path d="M4.5 2.8h11v14.4l-2-1.3-1.8 1.3-1.7-1.3-1.7 1.3-1.8-1.3-2 1.3z" />
				<path d="M7.5 7h5M7.5 10.5h5" />
			</>
		),
		historial: (
			<>
				<circle cx="10" cy="10" r="7" />
				<path d="M10 5.8V10l2.8 1.8" />
			</>
		),
		stock: (
			<>
				<path d="M6.5 3.5 3.5 6.5l3 3" />
				<path d="M3.5 6.5H14a2.5 2.5 0 0 1 2.5 2.5" />
				<path d="M13.5 16.5l3-3-3-3" />
				<path d="M16.5 13.5H6a2.5 2.5 0 0 1-2.5-2.5" />
			</>
		),
		compartir: (
			<>
				<circle cx="15" cy="5" r="2.2" />
				<circle cx="5" cy="10" r="2.2" />
				<circle cx="15" cy="15" r="2.2" />
				<path d="M7 8.9 13 6.1M7 11.1l6 2.8" />
			</>
		),
		ver: (
			<>
				<path d="M1.8 10S5 4.8 10 4.8 18.2 10 18.2 10 15 15.2 10 15.2 1.8 10 1.8 10z" />
				<circle cx="10" cy="10" r="2.4" />
			</>
		),
		aceptar: (
			<>
				<path d="M4 10.4 8.2 14.5 16 6.2" />
			</>
		),
		precios: (
			<>
				<path d="M3 16.5V10M7.7 16.5V6M12.3 16.5v-4.5M17 16.5V3.5" />
			</>
		),
		salir: (
			<>
				<path d="M8 3.5H4.5a1 1 0 0 0-1 1v11a1 1 0 0 0 1 1H8" />
				<path d="M12 6.5 15.5 10 12 13.5M15.5 10H7" />
			</>
		),
		buscar: (
			<>
				<circle cx="9" cy="9" r="5.2" />
				<path d="M12.8 12.8 17 17" />
			</>
		),
	};

	return (
		<svg
			className="icono"
			viewBox="0 0 20 20"
			fill="none"
			stroke="currentColor"
			strokeWidth="1.5"
			strokeLinecap="round"
			strokeLinejoin="round"
			aria-hidden="true"
		>
			{trazos[nombre]}
		</svg>
	);
}

export default Icono;
