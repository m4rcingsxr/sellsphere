INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price, discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES
    ('Product 1', 'product_1', 'Short description 1', 'Full description 1', NOW(), 1, 1, 100.00, 150.00, 10.00, 10.00, 5.00, 2.00, 1.00, 'product1.png', 2, 1),
    ('Product 2', 'product_2', 'Short description 2', 'Full description 2', NOW(), 1, 1, 200.00, 250.00, 15.00, 12.00, 6.00, 3.00, 2.00, 'product2.png', 2, 2),
    ('Product 3', 'product_3', 'Short description 3', 'Full description 3', NOW(), 1, 1, 300.00, 350.00, 20.00, 14.00, 7.00, 4.00, 3.00, 'product3.png', 3, 3),
    ('Product 4', 'product_4', 'Short description 4', 'Full description 4', NOW(), 1, 1, 400.00, 450.00, 25.00, 16.00, 8.00, 5.00, 4.00, 'product4.png', 3, 4),
    ('Product 5', 'product_5', 'Short description 5', 'Full description 5', NOW(), 1, 1, 500.00, 550.00, 30.00, 18.00, 9.00, 6.00, 5.00, 'product5.png', 4, 5),
    ('Product 6', 'product_6', 'Short description 6', 'Full description 6', NOW(), 1, 1, 600.00, 650.00, 35.00, 20.00, 10.00, 7.00, 6.00, 'product6.png', 4, 1),
    ('Product 7', 'product_7', 'Short description 7', 'Full description 7', NOW(), 1, 1, 700.00, 750.00, 40.00, 22.00, 11.00, 8.00, 7.00, 'product7.png', 5, 2),
    ('Product 8', 'product_8', 'Short description 8', 'Full description 8', NOW(), 1, 1, 800.00, 850.00, 45.00, 24.00, 12.00, 9.00, 8.00, 'product8.png', 5, 3),
    ('Product 9', 'product_9', 'Short description 9', 'Full description 9', NOW(), 1, 1, 900.00, 950.00, 50.00, 26.00, 13.00, 10.00, 9.00, 'product9.png', 6, 4),
    ('Product 10', 'product_10', 'Short description 10', 'Full description 10', NOW(), 1, 1, 1000.00, 1050.00, 55.00, 28.00, 14.00, 11.00, 10.00, 'product10.png', 6, 5);

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