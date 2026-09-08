/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuracion_taller` (
  `id_configuracion` bigint NOT NULL AUTO_INCREMENT,
  `condicion_fiscal` varchar(255) DEFAULT NULL,
  `cuit` varchar(255) DEFAULT NULL,
  `domicilio` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `nombre_fantasia` varchar(255) DEFAULT NULL,
  `precio_hora_mano_obra` decimal(38,2) DEFAULT NULL,
  `punto_venta` varchar(255) DEFAULT NULL,
  `razon_social` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `porcentaje_iva` decimal(38,2) DEFAULT NULL,
  `alias_cbu` varchar(255) DEFAULT NULL,
  `banco` varchar(255) DEFAULT NULL,
  `cbu` varchar(255) DEFAULT NULL,
  `datos_qr` varchar(255) DEFAULT NULL,
  `datos_tarjeta` varchar(255) DEFAULT NULL,
  `titular_cuenta` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_configuracion`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empleados` (
  `id_empleado` bigint NOT NULL AUTO_INCREMENT,
  `apellido` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `rol` enum('ADMIN','MECANICO','VENDEDOR') NOT NULL,
  `sucursal_id` bigint NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `activo` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id_empleado`),
  UNIQUE KEY `UK6fdpo2x5rmegfbngre7xb3yoh` (`email`),
  KEY `FKnj7p5afxwcivqvkmwmhymxick` (`sucursal_id`),
  CONSTRAINT `FKnj7p5afxwcivqvkmwmhymxick` FOREIGN KEY (`sucursal_id`) REFERENCES `sucursales` (`id_sucursal`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sucursales` (
  `id_sucursal` bigint NOT NULL AUTO_INCREMENT,
  `direccion` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`id_sucursal`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
