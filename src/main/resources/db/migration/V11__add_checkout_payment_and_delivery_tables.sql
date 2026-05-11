ALTER TABLE orders
    ADD COLUMN notes VARCHAR(500) NULL;

CREATE TABLE order_shipping_information (
    id_shipping BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_order BIGINT UNSIGNED NOT NULL,
    address VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    neighborhood VARCHAR(100) NOT NULL,
    reference_details VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_order_shipping_order UNIQUE (id_order),
    CONSTRAINT fk_order_shipping_order FOREIGN KEY (id_order) REFERENCES orders(id_order) ON DELETE CASCADE
);

CREATE TABLE order_payments (
    id_payment BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_order BIGINT UNSIGNED NOT NULL,
    payment_method ENUM('CREDIT_CARD', 'DEBIT_CARD') NOT NULL,
    masked_card_number VARCHAR(24) NOT NULL,
    status ENUM('APPROVED', 'DECLINED') NOT NULL,
    transaction_reference VARCHAR(64) NOT NULL,
    paid_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_order_payments_order UNIQUE (id_order),
    CONSTRAINT uk_order_payments_reference UNIQUE (transaction_reference),
    CONSTRAINT fk_order_payments_order FOREIGN KEY (id_order) REFERENCES orders(id_order) ON DELETE CASCADE
);

CREATE TABLE delivery_orders (
    id_delivery_order BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_order BIGINT UNSIGNED NOT NULL,
    status ENUM('PENDING', 'OUT_FOR_SHIPMENT', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_delivery_orders_order UNIQUE (id_order),
    CONSTRAINT fk_delivery_orders_order FOREIGN KEY (id_order) REFERENCES orders(id_order) ON DELETE CASCADE
);

CREATE INDEX idx_order_shipping_city ON order_shipping_information(city);
CREATE INDEX idx_order_payments_status ON order_payments(status);
CREATE INDEX idx_delivery_orders_status ON delivery_orders(status);
