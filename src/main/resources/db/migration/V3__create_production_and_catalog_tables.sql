CREATE TABLE production (
    id_production INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_section INT UNSIGNED NOT NULL,
    date_collection DATE NOT NULL,
    quantity_kg NUMERIC(10,2),
    id_variety INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_section) REFERENCES sections(id_section),
    FOREIGN KEY (id_variety) REFERENCES varieties(id_variety)
);

CREATE TABLE product_presentations (
    id_presentation INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE products (
    id_product BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    image_url VARCHAR(255),
    price NUMERIC(10,2) NOT NULL,
    id_presentation INT UNSIGNED NOT NULL,
    id_production INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_presentation) REFERENCES product_presentations(id_presentation),
    FOREIGN KEY (id_production) REFERENCES production(id_production)
);
