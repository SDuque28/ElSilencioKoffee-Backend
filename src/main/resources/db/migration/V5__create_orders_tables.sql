CREATE TABLE orders (
    id_order BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_user BIGINT NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount NUMERIC(10,2) NOT NULL,
    status ENUM('PAID', 'NON PAID') NOT NULL DEFAULT 'NON PAID',
    FOREIGN KEY (id_user) REFERENCES usuario(id)
);

CREATE TABLE orders_details (
    id_details BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_order BIGINT UNSIGNED NOT NULL,
    id_product BIGINT UNSIGNED NULL,
    quantity NUMERIC(10,2) NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    FOREIGN KEY (id_order) REFERENCES orders(id_order) ON DELETE CASCADE,
    FOREIGN KEY (id_product) REFERENCES products(id_product)
);
