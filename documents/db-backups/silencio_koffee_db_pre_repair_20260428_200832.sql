SET FOREIGN_KEY_CHECKS=0;

-- cart
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id_cart` bigint NOT NULL AUTO_INCREMENT,
  `id_user` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_cart`),
  UNIQUE KEY `uk_cart_user` (`id_user`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`id_user`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- flyway_schema_history
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `flyway_schema_history` VALUES (1, '6', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'root', '2026-04-27 22:45:05.0', 0, 1);
INSERT INTO `flyway_schema_history` VALUES (2, '7', 'create cart tables', 'SQL', 'V7__create_cart_tables.sql', 110115197, 'root', '2026-04-28 19:55:33.0', 1465, 0);

-- orders
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id_order` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_user` bigint NOT NULL,
  `order_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `status` enum('PAID','NON PAID') DEFAULT NULL,
  PRIMARY KEY (`id_order`),
  UNIQUE KEY `id_order` (`id_order`),
  KEY `idx_orders_user` (`id_user`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `orders` VALUES (1, 1, '2026-04-13T12:01:11', 120.50, 'PAID');
INSERT INTO `orders` VALUES (2, 1, '2026-04-13T12:04:42', 82.30, 'NON PAID');
INSERT INTO `orders` VALUES (3, 1, '2026-04-13T12:04:52', 223.10, 'NON PAID');
INSERT INTO `orders` VALUES (4, 1, '2026-04-13T12:05:09', 52.24, 'NON PAID');
INSERT INTO `orders` VALUES (5, 2, '2026-04-13T13:42:30', 52.24, 'NON PAID');

-- rol
DROP TABLE IF EXISTS `rol`;
CREATE TABLE `rol` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `rol` VALUES (2, 'ROLE_ADMIN');
INSERT INTO `rol` VALUES (1, 'ROLE_USER');

-- usuario
DROP TABLE IF EXISTS `usuario`;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `usuario` VALUES (1, 'Santi', 'santi@email.com', '$2a$10$9PjNbEkpxFxAZhWGxAlOxOFkWLAss3ieqYUYMqW7hJWTXD7lJr/He', 1, '2026-04-09 15:29:30.0');
INSERT INTO `usuario` VALUES (2, 'Pablo', 'pablo@email.com', '$2a$10$Vre.DnOwDmd1DRF2HSE8F.DBvl0plKZ9KrOLRQ2SCFuC8BTEs1xk2', 1, '2026-04-09 15:34:53.0');
INSERT INTO `usuario` VALUES (3, 'Marco', 'marco@gmail.com', '$2a$10$PGhdKUviaNDvEKBwp7g1IuX1EYVoQpHhu4iA08Zk3FDIG0hxeIcQm', 1, '2026-04-09 17:07:07.0');
INSERT INTO `usuario` VALUES (4, 'Triana', 'triana@gmail.com', '$2a$10$HoWxYCXNfS1NFD00vJuaCud8DlMXwvgXzRoAnIvzEuovUSN0aVvXS', 1, '2026-04-17 20:17:18.0');
INSERT INTO `usuario` VALUES (5, 'German', 'german@gmail.com', '$2a$10$/q4B7eFbcxXTH5t5FdkPm.tplvSr7hNzWyUq7PjwpkYALcGaZm6nC', 1, '2026-04-17 20:24:32.0');

-- usuario_rol
DROP TABLE IF EXISTS `usuario_rol`;
CREATE TABLE `usuario_rol` (
  `usuario_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `fk_rol` (`rol_id`),
  CONSTRAINT `fk_rol` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `usuario_rol` VALUES (2, 1);
INSERT INTO `usuario_rol` VALUES (3, 1);
INSERT INTO `usuario_rol` VALUES (4, 1);
INSERT INTO `usuario_rol` VALUES (5, 1);
INSERT INTO `usuario_rol` VALUES (1, 2);

SET FOREIGN_KEY_CHECKS=1;
