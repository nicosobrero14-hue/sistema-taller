# Repaso del patron de microservicios

Escrito mirando el codigo que hay hoy, no la intencion. Lo que sigue es
lo que cumple, lo que un revisor va a marcar, y que haria falta para que
esto aguante produccion.

## El mapa

| Servicio | Puerto | Base | De que es dueño |
|---|---|---|---|
| eureka-server | 8761 | — | El registro: quien esta vivo y en que puerto |
| api-gateway | 8080 | — | La unica puerta. Valida el token y reparte |
| usuarios-service | 8081 | `usuarios-service` | Sucursales, empleados, login, datos del taller |
| clientes-service | 8082 | `clientes-service` | Clientes y vehiculos |
| taller-service | 8083 | `taller-service` | Ordenes, items, fotos, presupuestos |
| inventario-service | 8084 | `inventario-service` | Repuestos, stock, movimientos, precios |
| pagos-service | 8085 | `pagos-service` | Cobros de las ordenes |
| ventas-service | 8086 | `ventas-service` | Ventas de mostrador |

El navegador habla solo con el gateway. No conoce ningun puerto de los
servicios de atras.

## Lo que esta bien logrado

**Una base por servicio, sin excepciones.** Son seis bases MySQL
separadas. No hay ninguna consulta que cruce dos.

**Ninguna clave foranea entre servicios.** Lo que en un monolito seria
`@ManyToOne Vehiculo`, aca es un `Long id_vehiculo` suelto. Adentro de un
mismo servicio si hay relaciones de verdad (`OrdenTrabajo` con sus
`ItemOrdenTrabajo`), que es como corresponde.

**Cada servicio no confia en el navegador.** El precio de un repuesto
vendido lo pone `inventario-service`, no el front. El monto de un cobro
se compara contra el total que devuelve `taller-service`. El id del
empleado sale del token, no del cuerpo del pedido.

**La autenticacion esta en un solo lugar.** El gateway valida el JWT y
mete `X-Empleado-Id` y `X-Empleado-Rol` en la peticion. Los servicios de
atras leen esas cabeceras y no saben nada de tokens.

**Descubrimiento real.** Los clientes HTTP apuntan a
`http://taller-service`, no a `localhost:8083`. Eureka resuelve.

**Cuando una llamada remota falla a mitad, se compensa.** En una venta
con tres renglones, si el tercero no se puede descontar, los dos
primeros se devuelven al deposito y la venta no queda guardada.

## Lo que se arreglo

**La dependencia circular ya no existe.** `taller-service` no le pregunta
mas a `pagos-service`: la orden guarda su propio campo `pagada`, que
`pagos-service` actualiza al confirmar o anular. Taller perdio un cliente
HTTP y una llamada remota en cada cambio de estado.

**Todos los clientes HTTP tienen timeout** (2 segundos para conectar, 5
para leer). Si un servicio se cae, el que llama falla rapido en vez de
quedarse colgado. De paso los cinco `HttpClientsConfig` pasaron a un
helper compartido y cada bean quedo en tres lineas.

**El esquema lo maneja Flyway.** `ddl-auto` esta en `none` y cada
servicio tiene su `V1__esquema_inicial.sql`. Las bases que ya existian se
adoptaron con `baseline-on-migrate`, y el primer cambio real
(`V2__marcar_ordenes_cobradas.sql`) ya paso por ahi. El proximo cambio de
enum no se rompe: va en un script.

**Los rechazos son 400, no 200 con un texto adentro.** Cada servicio
tiene una `ReglaNegocioException` que el manejador global convierte en
400. El front dejo de adivinar leyendo el mensaje y ya no limpia el
formulario cuando algo fallo.

**Las credenciales salieron del codigo.** El usuario y la clave de MySQL,
la URL de eureka y el secreto del JWT se leen de variables de entorno,
con el valor de desarrollo como respaldo.

## Lo que sigue faltando

**Circuit breaker.** Los timeouts evitan que se cuelgue, pero si un
servicio esta caido se lo sigue llamando en cada pedido. Resilience4j ya
viene con Spring Cloud.

**Trazabilidad.** No hay un id que cruce las tres llamadas de un cobro.
Para saber por que fallo algo hay que mirar tres logs y adivinar por la
hora.

**Config centralizada.** La misma configuracion esta repetida seis veces.
Es lo que resuelve un Config Server.

**Paginacion.** El unico endpoint que devuelve un pedazo es el archivo de
ordenes entregadas.

**El front arma la pantalla juntando datos de varios servicios.** La
lista de ordenes hace cinco llamadas para dibujar una tabla. Con pocos
datos no se nota; lo que corresponde es un BFF.

**Tests de contrato.** Hay 24 tests unitarios sobre las reglas de plata y
stock, pero nada que avise si un servicio renombra un campo de su DTO.

## Veredicto

Como sistema de microservicios esta bien planteado: los limites entre
servicios estan donde tienen que estar, cada uno es dueño de sus datos, y
las reglas que importan se validan del lado del servidor y no del
navegador. Eso es lo dificil y esta hecho.

Con las migraciones versionadas, los timeouts, los codigos de estado y
los secretos afuera, ya no hay nada que impida ponerlo en un servidor.

Lo que queda —circuit breaker, trazas, config centralizada, paginacion—
son mejoras de operacion: se notan cuando el sistema tiene carga o
cuando hay que debuggear algo en produccion, no antes.
