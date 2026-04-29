CREATE TABLE inventory (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_product BIGINT UNSIGNED NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_product) REFERENCES products(id_product)
);

CREATE TABLE inventory_movements (
    id_movement BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_product BIGINT UNSIGNED NOT NULL,
    movement_type ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    reference_id BIGINT,
    reference_type ENUM('ORDER', 'PRODUCTION', 'MANUAL'),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    FOREIGN KEY (id_product) REFERENCES products(id_product),
    FOREIGN KEY (created_by) REFERENCES usuario(id)
);
