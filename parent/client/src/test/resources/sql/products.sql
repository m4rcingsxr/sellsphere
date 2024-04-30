-- Inserting Categories
INSERT INTO categories (id, name, alias, image, enabled, parent_id, all_parent_ids)
VALUES (1, 'Electronics', 'electronics', 'electronics.png', 1, NULL, NULL);

-- Inserting Brands
INSERT INTO brands (id, name, logo) VALUES (1, 'Apple', 'apple_logo.jpg'), (2, 'AMD', 'amd_logo.jpg');

-- Inserting Products
INSERT INTO products (id, name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price, discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES
    (1, 'MacBook Pro', 'macbook-pro', 'A high-end laptop', 'A detailed description of MacBook Pro', NOW(), TRUE, TRUE, 1500.00, 2000.00, 10.00, 30.00, 20.00, 2.00, 1.50, 'macbook.png', 1, 1),
    (2, 'AMD Ryzen 5', 'amd-ryzen-5', 'A powerful processor', 'A detailed description of AMD Ryzen 5', NOW(), TRUE, TRUE, 200.00, 300.00, 15.00, 10.00, 10.00, 1.00, 0.50, 'ryzen.png', 1, 2),
    (3, 'iPhone 13', 'iphone-13', 'A cutting-edge smartphone', 'A detailed description of iPhone 13', NOW(), TRUE, TRUE, 700.00, 1000.00, 5.00, 15.00, 8.00, 1.00, 0.20, 'iphone.png', 1, 1),
    (4, 'AMD Radeon RX 6800', 'amd-radeon-rx-6800', 'A powerful GPU', 'A detailed description of AMD Radeon RX 6800', NOW(), TRUE, TRUE, 500.00, 700.00, 20.00, 25.00, 12.00, 3.00, 1.50, 'radeon.png', 1, 2);

-- Inserting Product Details
INSERT INTO product_details (id, name, detail_value, product_id)
VALUES
    (1, 'Processor', 'Intel i7', 1),
    (2, 'RAM', '16GB', 1),
    (3, 'Processor', 'AMD Ryzen 5', 2),
    (4, 'Cores', '6', 2),
    (5, 'Screen Size', '6.1 inch', 3),
    (6, 'Battery Life', '20 hours', 3),
    (7, 'GPU', 'AMD Radeon RX 6800', 4),
    (8, 'Memory', '16GB GDDR6', 4);