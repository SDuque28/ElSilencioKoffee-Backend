CREATE INDEX idx_production_section ON production(id_section);
CREATE INDEX idx_production_variety ON production(id_variety);

CREATE INDEX idx_products_presentation ON products(id_presentation);
CREATE INDEX idx_products_production ON products(id_production);

CREATE INDEX idx_inventory_movements_product ON inventory_movements(id_product);

CREATE INDEX idx_orders_user ON orders(id_user);

CREATE INDEX idx_order_details_order ON orders_details(id_order);
CREATE INDEX idx_order_details_product ON orders_details(id_product);
