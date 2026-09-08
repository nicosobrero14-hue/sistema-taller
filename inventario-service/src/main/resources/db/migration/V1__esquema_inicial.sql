/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_inventario` (
  `id_movimiento` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `fecha` datetime(6) DEFAULT NULL,
  `id_orden` bigint DEFAULT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `stock_resultante` int DEFAULT NULL,
  `tipo` enum('AJUSTE','ENTRADA','SALIDA') NOT NULL,
  `repuesto_id` bigint NOT NULL,
  `id_venta` bigint DEFAULT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `idx_mov_repuesto` (`repuesto_id`),
  CONSTRAINT `FK6au5enah516i15i0eilxj50dr` FOREIGN KEY (`repuesto_id`) REFERENCES `repuestos` (`id_repuesto`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `precios_repuesto` (
  `id_precio` bigint NOT NULL AUTO_INCREMENT,
  `fecha` datetime(6) DEFAULT NULL,
  `id_empleado` bigint DEFAULT NULL,
  `precio_compra` decimal(38,2) DEFAULT NULL,
  `precio_venta` decimal(38,2) DEFAULT NULL,
  `repuesto_id` bigint NOT NULL,
  PRIMARY KEY (`id_precio`),
  KEY `idx_precio_repuesto` (`repuesto_id`),
  CONSTRAINT `FKcgo7vqq01sd2xea1xy56ycx5l` FOREIGN KEY (`repuesto_id`) REFERENCES `repuestos` (`id_repuesto`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repuestos` (
  `id_repuesto` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(255) NOT NULL,
  `codigo_barra` varchar(255) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `id_sucursal` bigint DEFAULT NULL,
  `marca` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `precio_compra` decimal(38,2) DEFAULT NULL,
  `precio_venta` decimal(38,2) DEFAULT NULL,
  `stock` int DEFAULT NULL,
  `stock_minimo` int DEFAULT NULL,
  `ubicacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_repuesto`),
  UNIQUE KEY `UK5qbl67yw25ix3d8qkn2etsua0` (`codigo`),
  UNIQUE KEY `UKe8nge62bak00bnarpyk89ilu9` (`codigo_barra`),
  KEY `idx_repuesto_codigo` (`codigo`),
  KEY `idx_repuesto_barra` (`codigo_barra`),
  KEY `idx_repuesto_nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
