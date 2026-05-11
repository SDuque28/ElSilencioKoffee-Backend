CREATE TABLE cart (
    id_cart BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_user BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_user UNIQUE (id_user),
    CONSTRAINT fk_cart_user FOREIGN KEY (id_user) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    id_cart_item BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cart BIGINT NOT NULL,
    id_product BIGINT UNSIGNED NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_item_product UNIQUE (id_cart, id_product),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (id_cart) REFERENCES cart(id_cart) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (id_product) REFERENCES products(id_product)
);

CREATE INDEX idx_cart_items_cart ON cart_items(id_cart);
CREATE INDEX idx_cart_items_product ON cart_items(id_product);
