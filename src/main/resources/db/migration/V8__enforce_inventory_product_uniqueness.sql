ALTER TABLE inventory
ADD CONSTRAINT uk_inventory_product UNIQUE (id_product);
