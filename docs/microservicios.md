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


