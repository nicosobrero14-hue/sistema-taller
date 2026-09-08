/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pagos` (
  `id_pago` bigint NOT NULL AUTO_INCREMENT,
  `fecha` datetime(6) DEFAULT NULL,
  `id_empleado` bigint DEFAULT NULL,
  `id_orden` bigint NOT NULL,
  `medio` enum('EFECTIVO','QR','TARJETA_CREDITO','TARJETA_DEBITO','TRANSFERENCIA') NOT NULL,
  `monto` decimal(38,2) NOT NULL,
  `numero_recibo` bigint DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  `estado` enum('PENDIENTE','CONFIRMADO','ANULADO') DEFAULT NULL,
  `fecha_confirmacion` datetime(6) DEFAULT NULL,
  `id_confirmo` bigint DEFAULT NULL,
  `fecha_anulacion` datetime(6) DEFAULT NULL,
  `id_anulo` bigint DEFAULT NULL,
  `motivo_anulacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_pago`),
  KEY `idx_pago_orden` (`id_orden`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
