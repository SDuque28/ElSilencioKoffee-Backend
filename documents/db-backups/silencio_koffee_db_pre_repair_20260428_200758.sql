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


