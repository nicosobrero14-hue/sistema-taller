// Saca una foto de cada seccion del sistema y las deja como PNG.
//
// Maneja un navegador Chromium por el protocolo de DevTools. No hace
// falta instalar nada: Node 24 ya trae WebSocket.

import { writeFileSync, mkdirSync } from "node:fs";
import { setTimeout as esperar } from "node:timers/promises";

const DEVTOOLS = "http://127.0.0.1:9333";
const APP = "http://localhost:5173";
const SALIDA = process.argv[2] || "capturas";

mkdirSync(SALIDA, { recursive: true });

// ------------------------------------------------------------
// conexion al navegador
// ------------------------------------------------------------
const paginas = await (await fetch(DEVTOOLS + "/json/list")).json();
const pagina = paginas.find((p) => p.type === "page");

if (!pagina) {
  throw new Error("no hay ninguna pestaña abierta en el navegador");
}

const ws = new WebSocket(pagina.webSocketDebuggerUrl);
await new Promise((listo) => (ws.onopen = listo));

let numero = 0;
const pendientes = new Map();

ws.onmessage = (e) => {
  const msg = JSON.parse(e.data);
  if (pendientes.has(msg.id)) {
    pendientes.get(msg.id)(msg.result);
    pendientes.delete(msg.id);
  }
};

function mandar(metodo, params) {
  const id = ++numero;
  return new Promise((listo) => {
    pendientes.set(id, listo);
    ws.send(JSON.stringify({ id, method: metodo, params: params || {} }));
  });
}

async function evaluar(js) {
  const r = await mandar("Runtime.evaluate", {
    expression: "(async () => { " + js + " })()",
    awaitPromise: true,
    returnByValue: true,
  });

  return r?.result?.value;
}

async function foto(nombre) {
  await esperar(900);
  const r = await mandar("Page.captureScreenshot", { format: "png" });
  writeFileSync(SALIDA + "/" + nombre + ".png", Buffer.from(r.data, "base64"));
  console.log("  " + nombre + ".png");
}

// ------------------------------------------------------------
// preparar la pantalla
// ------------------------------------------------------------
await mandar("Page.enable");
await mandar("Runtime.enable");
await mandar("Emulation.setDeviceMetricsOverride", {
  width: 1440,
  height: 900,
  deviceScaleFactor: 1,
  mobile: false,
});

async function ir(url) {
  await mandar("Page.navigate", { url });
  await esperar(2500);
}

// ------------------------------------------------------------
// 1) la pantalla de ingreso, sin sesion
// ------------------------------------------------------------
await ir(APP);
await evaluar("localStorage.clear(); location.reload();");
await esperar(2500);
console.log("capturando:");
await foto("01-login");

// ------------------------------------------------------------
// 2) entrar como admin
// ------------------------------------------------------------
await evaluar(`
  const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
  const email = document.querySelector('input[type=email]');
  const pass = document.querySelector('input[type=password]');
  setter.call(email, 'admin@taller.com'); email.dispatchEvent(new Event('input', { bubbles: true }));
  setter.call(pass, 'admin123'); pass.dispatchEvent(new Event('input', { bubbles: true }));
  await new Promise(r => setTimeout(r, 300));
  document.querySelector('.caja-login form').requestSubmit();
  await new Promise(r => setTimeout(r, 2500));
  return 'entro';
`);

// ------------------------------------------------------------
// 3) una foto por seccion
// ------------------------------------------------------------
async function seccion(menu, nombre, extra) {
  await evaluar(`
    const b = [...document.querySelectorAll('.lateral nav button')].find(x => x.textContent.trim() === ${JSON.stringify(menu)});
    if (b) { b.click(); }
    await new Promise(r => setTimeout(r, 1600));
    window.scrollTo(0, 0);
    return 'ok';
  `);

  if (extra) {
    await evaluar(extra);
  }

  await foto(nombre);
}

await seccion("Tablero", "02-tablero");
await seccion("Ordenes", "03-ordenes");

// el archivo de entregadas
await seccion("Ordenes", "04-ordenes-entregadas", `
  const b = [...document.querySelectorAll('.pestanas button')].find(x => x.textContent.trim() === 'Entregadas');
  if (b) { b.click(); }
  await new Promise(r => setTimeout(r, 1800));
  return 'ok';
`);

await seccion("Presupuestos", "05-presupuestos");
await seccion("Clientes", "06-clientes");
await seccion("Vehiculos", "07-vehiculos");
await seccion("Repuestos", "08-deposito");
await seccion("Venta de mostrador", "09-venta-mostrador");
await seccion("Caja", "10-caja");
await seccion("Empleados", "11-empleados");
await seccion("Sucursales", "12-sucursales");
await seccion("Datos del taller", "13-datos-del-taller");

// ------------------------------------------------------------
// 4) algunas pantallas de adentro, que son las que explican el sistema
// ------------------------------------------------------------

// el cobro de una orden, con el recibo
await seccion("Ordenes", "14-cobro-y-recibo", `
  const fila = [...document.querySelectorAll('tbody tr')].find(f => f.innerText.includes('Recibo'));
  const destino = fila || document.querySelectorAll('tbody tr')[0];
  destino.querySelector('.accion-principal').click();
  await new Promise(r => setTimeout(r, 2000));
  const p = document.querySelector('.panel');
  if (p) { window.scrollTo(0, p.getBoundingClientRect().top + window.scrollY - 80); }
  await new Promise(r => setTimeout(r, 600));
  return 'ok';
`);

// el menu de acciones de una fila
await seccion("Ordenes", "15-acciones-de-la-fila", `
  const f = document.querySelectorAll('tbody tr')[0];
  f.querySelector('.puntitos').click();
  await new Promise(r => setTimeout(r, 500));
  return 'ok';
`);

// el buscador del click derecho
await seccion("Repuestos", "16-buscador-click-derecho", `
  const t = document.querySelector('table');
  const caja = t.getBoundingClientRect();
  t.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, clientX: 520, clientY: Math.round(caja.top) + 90 }));
  await new Promise(r => setTimeout(r, 500));
  const campo = document.querySelector('.buscador-tabla input');
  if (campo) {
    const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
    setter.call(campo, 'brembo');
    campo.dispatchEvent(new Event('input', { bubbles: true }));
  }
  await new Promise(r => setTimeout(r, 700));
  return 'ok';
`);

// un cartel de confirmacion
await seccion("Clientes", "17-confirmacion", `
  const f = document.querySelectorAll('tbody tr')[0];
  f.querySelector('.puntitos').click();
  await new Promise(r => setTimeout(r, 400));
  const borrar = [...document.querySelectorAll('.menu-acciones button')].find(b => b.textContent.includes('Borrar'));
  if (borrar) { borrar.click(); }
  await new Promise(r => setTimeout(r, 700));
  return 'ok';
`);

// se cancela el cartel para no borrar nada
await evaluar(`
  const c = document.querySelector('.botones-dialogo .cancelar');
  if (c) { c.click(); }
  return 'cancelado';
`);

// ------------------------------------------------------------
// 5) como ve el sistema alguien que no es administrador
// ------------------------------------------------------------
await evaluar("localStorage.clear(); location.reload();");
await esperar(2500);

await evaluar(`
  const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
  const email = document.querySelector('input[type=email]');
  const pass = document.querySelector('input[type=password]');
  setter.call(email, 'pedro@taller.com'); email.dispatchEvent(new Event('input', { bubbles: true }));
  setter.call(pass, 'pedro123'); pass.dispatchEvent(new Event('input', { bubbles: true }));
  await new Promise(r => setTimeout(r, 300));
  document.querySelector('.caja-login form').requestSubmit();
  await new Promise(r => setTimeout(r, 2500));
  return 'entro';
`);

await foto("18-vista-de-un-mecanico");

// volver a dejar la sesion del admin
await evaluar("localStorage.clear(); location.reload();");
await esperar(2000);
await evaluar(`
  const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
  const email = document.querySelector('input[type=email]');
  const pass = document.querySelector('input[type=password]');
  setter.call(email, 'admin@taller.com'); email.dispatchEvent(new Event('input', { bubbles: true }));
  setter.call(pass, 'admin123'); pass.dispatchEvent(new Event('input', { bubbles: true }));
  await new Promise(r => setTimeout(r, 300));
  document.querySelector('.caja-login form').requestSubmit();
  return 'ok';
`);

console.log("listo");
ws.close();
process.exit(0);
