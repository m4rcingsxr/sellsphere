INSERT INTO product_taxes (id, type, description, name) VALUES ('txcd_99999999', 'PHYSICAL', 'A physical good that can be moved or touched. Also known as tangible personal property.', 'General - Tangible Goods');

INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price, discount_percent, length, width, height, weight, main_image, category_id, brand_id, tax_id)
VALUES
    ('Product 1', 'product_1', 'Short description 1', 'Full description 1', NOW(), 1, 1, 100.00, 150.00, 10.00, 10.00, 5.00, 2.00, 1.00, 'product1.png', 2, 1, 'txcd_99999999'),
    ('Product 2', 'product_2', 'Short description 2', 'Full description 2', NOW(), 1, 1, 200.00, 250.00, 15.00, 12.00, 6.00, 3.00, 2.00, 'product2.png', 2, 2, 'txcd_99999999');

INSERT INTO product_images (product_id, name)
VALUES
    (1,  'imagePath1.png'),
    (1,  'imagePath2.png'),
    (1, 'imagePath3.png'),
    (1,  'imagePath4.png'),
    (1,  'imagePath5.png');

INSERT INTO product_details (product_id, name, detail_value)
VALUES
    (1, 'Detail Name 1', 'Detail Value 1'),
    (1, 'Detail Name 2', 'Detail Value 2'),
    (1, 'Detail Name 3', 'Detail Value 3'),
    (1, 'Detail Name 4', 'Detail Value 4'),
    (1, 'Detail Name 5', 'Detail Value 5');