/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fotos_orden` (
  `id_foto` bigint NOT NULL AUTO_INCREMENT,
  `archivo` varchar(255) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `fecha` datetime(6) DEFAULT NULL,
  `id_empleado` bigint DEFAULT NULL,
  `momento` enum('DIAGNOSTICO','ENTREGA','INGRESO','TRABAJO') NOT NULL,
  `orden_id` bigint NOT NULL,
  `miniatura` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_foto`),
  KEY `idx_foto_orden` (`orden_id`),
  CONSTRAINT `FKecije0unr0hylqfh7m5y8tmo2` FOREIGN KEY (`orden_id`) REFERENCES `ordenes_trabajo` (`id_orden`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items_orden_trabajo` (
  `id_item` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int DEFAULT NULL,
  `descripcion` varchar(255) NOT NULL,
  `id_repuesto` bigint DEFAULT NULL,
  `precio_unitario` decimal(38,2) DEFAULT NULL,
  `tipo` enum('MANO_DE_OBRA','REPUESTO') NOT NULL,
  `orden_id` bigint NOT NULL,
  PRIMARY KEY (`id_item`),
  KEY `idx_item_orden` (`orden_id`),
  CONSTRAINT `FK5rsesr0tfr53j9bx8gwtcdpqv` FOREIGN KEY (`orden_id`) REFERENCES `ordenes_trabajo` (`id_orden`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items_presupuesto` (
  `id_item` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int DEFAULT NULL,
  `descripcion` varchar(255) NOT NULL,
  `id_repuesto` bigint DEFAULT NULL,
  `precio_unitario` decimal(38,2) DEFAULT NULL,
  `presupuesto_id` bigint NOT NULL,
  `elegido` bit(1) DEFAULT NULL,
  `grupo` varchar(255) DEFAULT NULL,
  `opcion` int DEFAULT NULL,
  PRIMARY KEY (`id_item`),
  KEY `FK681qamos26796ye6r3w7qlx7r` (`presupuesto_id`),
  CONSTRAINT `FK681qamos26796ye6r3w7qlx7r` FOREIGN KEY (`presupuesto_id`) REFERENCES `presupuestos` (`id_presupuesto`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_orden` (
  `id_movimiento` bigint NOT NULL AUTO_INCREMENT,
  `detalle` varchar(255) DEFAULT NULL,
  `estado_anterior` enum('DIAGNOSTICO','ENTREGADO','EN_REPARACION','ESPERANDO_REPUESTOS','LISTO','RECIBIDO') DEFAULT NULL,
  `estado_nuevo` enum('DIAGNOSTICO','ENTREGADO','EN_REPARACION','ESPERANDO_REPUESTOS','LISTO','RECIBIDO') DEFAULT NULL,
  `fecha` datetime(6) DEFAULT NULL,
  `id_empleado` bigint DEFAULT NULL,
  `orden_id` bigint NOT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `idx_movimiento_orden` (`orden_id`),
  CONSTRAINT `FKf0e8hn55t7scy6c4jclm17r4q` FOREIGN KEY (`orden_id`) REFERENCES `ordenes_trabajo` (`id_orden`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordenes_trabajo` (
  `id_orden` bigint NOT NULL AUTO_INCREMENT,
  `diagnostico` varchar(255) DEFAULT NULL,
  `estado` enum('DIAGNOSTICO','ENTREGADO','EN_REPARACION','ESPERANDO_REPUESTOS','LISTO','RECIBIDO') DEFAULT NULL,
  `fecha_entrega_estimada` datetime(6) DEFAULT NULL,
  `fecha_ingreso` datetime(6) DEFAULT NULL,
  `id_mecanico` bigint NOT NULL,
  `id_sucursal` bigint NOT NULL,
  `id_vehiculo` bigint NOT NULL,
  `fecha_entrega_real` datetime(6) DEFAULT NULL,
  `kilometraje_ingreso` int DEFAULT NULL,
  `descuento` decimal(38,2) DEFAULT NULL,
  `pagada` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id_orden`),
  KEY `idx_orden_vehiculo` (`id_vehiculo`),
  KEY `idx_orden_mecanico` (`id_mecanico`),
  KEY `idx_orden_estado` (`estado`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presupuestos` (
  `id_presupuesto` bigint NOT NULL AUTO_INCREMENT,
  `detalle_trabajo` varchar(255) DEFAULT NULL,
  `estado` enum('ACEPTADO','RECHAZADO','VENCIDO','VIGENTE') DEFAULT NULL,
  `fecha_emision` datetime(6) DEFAULT NULL,
  `fecha_vencimiento` datetime(6) DEFAULT NULL,
  `horas_estimadas` decimal(38,2) DEFAULT NULL,
  `id_empleado` bigint DEFAULT NULL,
  `id_orden` bigint DEFAULT NULL,
  `id_sucursal` bigint DEFAULT NULL,
  `id_vehiculo` bigint NOT NULL,
  `precio_hora` decimal(38,2) DEFAULT NULL,
  `validez_dias` int DEFAULT NULL,
  `opcion_elegida` int DEFAULT NULL,
  PRIMARY KEY (`id_presupuesto`),
  KEY `idx_presu_vehiculo` (`id_vehiculo`),
  KEY `idx_presu_estado` (`estado`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
