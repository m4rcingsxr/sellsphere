-- Root Category (lvl 0)
INSERT INTO categories (id, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES (1, 'Laptops', 'laptops', 'laptops.png', 1, NULL, NULL);

-- Categories under 'Laptops' (lvl 1)
INSERT INTO categories (id, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES (2, 'Laptops and Tablets', 'laptops and tablets', 'laptop_and_tablets.png', 1, 1, '-1-'),
       (3, 'Laptop Accessories', 'laptop accessories', 'laptop_accessories.png', 1, 1, '-1-'),
       (4, 'Laptop Components', 'laptop components', 'laptop_components.png', 1, 1, '-1-'),
       (5, 'Tablet Accessories', 'tablet accessories', 'tablet_accessories.png', 1, 1, '-1-');

INSERT INTO brands (id, name, logo)
VALUES (1, 'Apple', 'logo.jpg'),
       (2, 'Samsung', 'logo.jpg');

INSERT INTO products (id, name, alias, short_description, full_description, created_time, enabled, in_stock, cost,
                      price, discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES (1, 'Product One', 'product-one', 'Short description of Product One', 'Detailed description of Product One',
        '2024-07-03 10:00:00', true, true, 10.00, 15.00, 10.00, 10.00, 5.00, 1.50, 0.50, 'product_one.png', 5, 1),
       (2, 'Product Two', 'product-two', 'Short description of Product Two', 'Detailed description of Product Two',
        '2024-07-03 10:00:00', true, true, 20.00, 30.00, 5.00, 15.00, 7.50, 2.00, 0.75, 'product_two.png', 2, 1),
       (3, 'Product Three', 'product-three', 'Short description of Product Three',
        'Detailed description of Product Three', '2024-07-03 10:00:00', true, true, 30.00, 45.00, 15.00, 20.00, 10.00,
        2.50, 1.00, 'product_three.png', 2, 1),
       (4, 'Product Four', 'product-four', 'Short description of Product Four', 'Detailed description of Product Four',
        '2024-07-03 10:00:00', true, true, 40.00, 60.00, 20.00, 25.00, 12.50, 3.00, 1.25, 'product_four.png', 3, 2),
       (5, 'Product Five', 'product-five', 'Short description of Product Five', 'Detailed description of Product Five',
        '2024-07-03 10:00:00', true, true, 50.00, 75.00, 25.00, 30.00, 15.00, 3.50, 1.50, 'product_five.png', 4, 2),
       (6, 'Product Six', 'product-six', 'Short description of Product Six', 'Detailed description of Product Six',
        '2024-07-03 10:00:00', true, true, 60.00, 90.00, 30.00, 35.00, 17.50, 4.00, 1.75, 'product_six.png', 5, 2),
       (7, 'Product Seven', 'product-seven', 'Short description of Product Seven',
        'Detailed description of Product Seven', '2024-07-03 10:00:00', true, true, 70.00, 105.00, 35.00, 40.00, 20.00,
        4.50, 2.00, 'product_seven.png', 2, 1),
       (8, 'Product Eight', 'product-eight', 'Short description of Product Eight',
        'Detailed description of Product Eight', '2024-07-03 10:00:00', true, true, 80.00, 120.00, 40.00, 45.00, 22.50,
        5.00, 2.25, 'product_eight.png', 1, 2),
       (9, 'Product Nine', 'product-nine', 'Short description of Product Nine', 'Detailed description of Product Nine',
        '2024-07-03 10:00:00', true, true, 90.00, 135.00, 45.00, 50.00, 25.00, 5.50, 2.50, 'product_nine.png', 3, 2),
       (10, 'Product Ten', 'product-ten', 'Short description of Product Ten', 'Detailed description of Product Ten',
        '2024-07-03 10:00:00', true, true, 100.00, 150.00, 50.00, 55.00, 27.50, 6.00, 2.75, 'product_ten.png', 4, 2);

INSERT INTO product_details (id, name, detail_value, product_id)
VALUES (1, 'Color', 'Red', 1),
       (2, 'Size', 'Medium', 1),
       (3, 'Weight', '1.5kg', 1),

       (4, 'Color', 'Red', 2),
       (5, 'Material', 'Plastic', 2),
       (6, 'Size', 'Medium', 2),

       (7, 'Color', 'Green', 3),
       (8, 'Size', 'Large', 3),
       (9, 'Weight', '2kg', 3),

       (10, 'Color', 'Red', 4),
       (11, 'Material', 'Metal', 4),
       (12, 'Weight', '1.5kg', 4),

       (13, 'Color', 'Black', 5),
       (14, 'Size', 'Small', 5),
       (15, 'Weight', '0.5kg', 5),

       (16, 'Color', 'White', 6),
       (17, 'Material', 'Plastic', 6),
       (18, 'Warranty', '3 years', 6),

       (19, 'Color', 'Yellow', 7),
       (20, 'Size', 'Extra Large', 7),
       (21, 'Weight', '3kg', 7),

       (22, 'Color', 'Blue', 8),
       (23, 'Material', 'Plastic', 8),
       (24, 'Warranty', '5 years', 8),

       (25, 'Color', 'Green', 9),
       (26, 'Size', 'Medium', 9),
       (27, 'Weight', '2kg', 9),

       (28, 'Color', 'Black', 10),
       (29, 'Material', 'Glass', 10),
       (30, 'Warranty', '6 months', 10);
