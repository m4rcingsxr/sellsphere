INSERT INTO customers (id, email, customer_password, first_name, last_name, enabled, email_verified, created_time)
VALUES (1, 'john.doe@example.com', 'password123', 'John', 'Doe', 1, 1, '2023-01-01 10:00:00');



INSERT INTO categories (id, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES (1, 'Computers', 'computers', 'computers.png', 1, 1, '-1-'), (2, 'Computer Components', 'computer_components',
                                                                  'computer components.png', 1, 2, '-1-2-'),
                                                                 (3, 'CPU Processors Unit', 'computer_processors',
                                                                  'computer processors.png', 1, 3, '-1-2-3-');

INSERT INTO brands (id, name, logo)
VALUES (1, 'Brand A', 'logo.png'),
       (2, 'Brand B', 'logo.png'),
       (3, 'Brand C', 'logo.png');

-- Insert into products
INSERT INTO products (id, name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES (1, 'Product 1', 'product-1', 'Short description for product 1', 'Long description for product 1',
        '2023-01-01 12:00:00', 1, 1, 10.00, 20.00, 5.00, 10.00, 5.00, 2.00, 1.00, 'product1.jpg', 1, 1),
       (2, 'Product 2', 'product-2', 'Short description for product 2', 'Long description for product 2',
        '2023-01-02 13:00:00', 1, 1, 15.00, 30.00, 10.00, 12.00, 6.00, 3.00, 2.00, 'product2.jpg', 2, 2),
       (3, 'Product 3', 'product-3', 'Short description for product 3', 'Long description for product 3',
        '2023-01-03 14:00:00', 1, 1, 20.00, 40.00, 15.00, 14.00, 7.00, 4.00, 3.00, 'product3.jpg', 3, 3);

-- Insert into cart_items
INSERT INTO cart_items (customer_id, product_id, quantity)
VALUES (1, 1, 2),
       (1, 2, 1),
       (1, 3, 3);
