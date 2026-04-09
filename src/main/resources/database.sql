CREATE DATABASE silencio_koffee_db;
USE silencio_koffee_db;


-- ─────────────────────────────────────────
-- Tabla de usuario
-- ─────────────────────────────────────────
CREATE TABLE usuario (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    -- Guardar SIEMPRE un hash (bcrypt/argon2), nunca texto plano
    password   VARCHAR(255) NOT NULL,
    activo     BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- Tabla de roles
-- ─────────────────────────────────────────
CREATE TABLE rol (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- ─────────────────────────────────────────
-- Tabla intermedia usuario_rol (N:N)
-- ─────────────────────────────────────────
CREATE TABLE usuario_rol (
    usuario_id BIGINT NOT NULL,
    rol_id     BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rol     FOREIGN KEY (rol_id)     REFERENCES rol(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- _____SECTIONS TABLE_____

CREATE TABLE sections (
	id_section INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    loction VARCHAR(150) NOT NULL,
    capacity INT UNSIGNED
);


-- _____VARIETIES_____

CREATE TABLE varieties(
	id_variety INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);


-- _____PRODUCTIONS TABLE_____

CREATE TABLE production(
	id_production INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_section INT UNSIGNED NOT NULL,
    date_collection DATE NOT NULL,
    quantity_kg NUMERIC(10,2),
    id_variety int unsigned not null,
    FOREIGN KEY(id_section) REFERENCES sections(id_section),
    FOREIGN KEY(id_variety) REFERENCES varieties(id_variety)
);

CREATE INDEX idx_production_section ON production(id_section);
CREATE INDEX idx_production_variety ON production(id_variety);


-- _____PRESENTATIONS TABLE_____

CREATE TABLE product_presentations (
    id_presentation INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);


-- _____PRODUCTS TABLE_____

CREATE TABLE products (
    id_product SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    id_presentation INT UNSIGNED NOT NULL,
    id_production INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_presentation) REFERENCES product_presentations(id_presentation),
    FOREIGN KEY (id_production) REFERENCES production(id_production)
);

CREATE INDEX idx_products_presentation ON products(id_presentation);
CREATE INDEX idx_products_production ON products(id_production);


-- _____INVENTORY_____

CREATE TABLE inventory (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_product BIGINT UNSIGNED NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_product) REFERENCES products(id_product)
);


-- _____MOVEMENTS TABLE_____

CREATE TABLE inventory_movements (
    id_movement SERIAL PRIMARY KEY,
    id_product BIGINT UNSIGNED NOT NULL,
    movement_type ENUM("IN", "OUT", "ADJUSTMENT") NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    reference_id BIGINT,
    reference_type ENUM("ORDER", "PRODUCTION", "MANUAL"),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT UNSIGNED NOT NULL,
    FOREIGN KEY (id_product) REFERENCES products(id_product),
    FOREIGN KEY (created_by) REFERENCES users(id_user)
);

CREATE INDEX idx_inventory_movements_product ON inventory_movements(id_product);


-- _____ORDERS TABLE_____

CREATE TABLE orders (
    id_order SERIAL PRIMARY KEY,
    id_user BIGINT UNSIGNED NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount NUMERIC(10,2),
    status ENUM("PAID", "NON PAID"),
    FOREIGN KEY (id_user) REFERENCES users(id_user)
);

CREATE INDEX idx_orders_user ON orders(id_user);


-- _____ORDER DETAILS_____

CREATE TABLE orders_details (
    id_details SERIAL PRIMARY KEY,
    id_order BIGINT UNSIGNED NOT NULL,
    id_product BIGINT UNSIGNED NULL,
    quantity NUMERIC(10,2) NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    FOREIGN KEY (id_order) REFERENCES orders(id_order) ON DELETE CASCADE,
    FOREIGN KEY (id_product) REFERENCES products(id_product)
);

CREATE INDEX idx_order_details_order ON orders_details(id_order);
CREATE INDEX idx_order_details_product ON orders_details(id_product);