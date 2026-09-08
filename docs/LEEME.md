# Documentacion del proyecto

- [microservicios.md](microservicios.md) — repaso honesto del patron: que
  cumple, que va a marcar un revisor y que falta para produccion.
- [capturas/](capturas/) — una foto de cada pantalla del sistema andando.
- `capturas-sistema-taller.zip` — las mismas fotos, para descargar.
- [sacar-capturas.mjs](sacar-capturas.mjs) — el script que las genera.

## Volver a generar las capturas

Con el sistema andando (los seis servicios, el gateway y el front):

```
"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" ^
  --headless=new --remote-debugging-port=9333 ^
  --user-data-dir=%TEMP%\perfil-capturas --window-size=1440,900 ^
  http://localhost:5173

node docs/sacar-capturas.mjs docs/capturas
```

Maneja el navegador por su protocolo de depuracion. No hace falta
instalar nada: Node 24 ya trae WebSocket.
