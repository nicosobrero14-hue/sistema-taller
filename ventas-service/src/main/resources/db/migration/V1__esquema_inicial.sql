/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalles_venta` (
  `id_detalle` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `id_repuesto` bigint NOT NULL,
  `precio_unitario` decimal(38,2) NOT NULL,
  `venta_id` bigint NOT NULL,
  PRIMARY KEY (`id_detalle`),
  KEY `idx_detalle_venta` (`venta_id`),
  CONSTRAINT `FK453xcyfk9n6snv6qnjlo0p65p` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`id_venta`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ventas` (
  `id_venta` bigint NOT NULL AUTO_INCREMENT,
  `estado` enum('ANULADA','HECHA') DEFAULT NULL,
  `fecha` datetime(6) DEFAULT NULL,
  `fecha_anulacion` datetime(6) DEFAULT NULL,
  `id_anulo` bigint DEFAULT NULL,
  `id_cliente` bigint DEFAULT NULL,
  `id_empleado` bigint DEFAULT NULL,
  `id_sucursal` bigint DEFAULT NULL,
  `medio` enum('EFECTIVO','QR','TARJETA_CREDITO','TARJETA_DEBITO','TRANSFERENCIA') NOT NULL,
  `motivo_anulacion` varchar(255) DEFAULT NULL,
  `nombre_cliente` varchar(255) DEFAULT NULL,
  `numero_comprobante` bigint DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_venta`),
  KEY `idx_venta_fecha` (`fecha`),
  KEY `idx_venta_cliente` (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
