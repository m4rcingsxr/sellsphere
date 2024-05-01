INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES (1, 'Laptops', 'laptops', 'laptops.png', 1, NULL, NULL);


INSERT INTO brands (id, name, logo) VALUES (1, 'Apple', 'logo.jpg');
INSERT INTO brands (id, name, logo) VALUES (2, 'Samsung', 'logo.jpg');
INSERT INTO brands (id, name, logo) VALUES (3, 'Sony', 'logo.jpg');
INSERT INTO brands (id, name, logo) VALUES (4, 'LG', 'logo.jpg');
INSERT INTO brands (id, name, logo) VALUES (5, 'Panasonic', 'logo.jpg');
INSERT INTO brands (id, name, logo) VALUES (6, 'Microsoft', 'logo.jpg');
INSERT INTO brands (id, name, logo) VALUES (7, 'Dell', 'logo.jpg');
INSERT INTO brands (id, name, logo) VALUES (8, 'HP', 'logo.jpg');
INSERT INTO brands (id, name, logo) VALUES (9, 'Lenovo', 'logo.jpg');
INSERT INTO brands (id, name, logo) VALUES (10, 'Asus', 'logo.jpg');

INSERT INTO products (id, name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price, discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES (2, 'Dell XPS 13 9380 Laptop, 13.3" 4K UHD Touchscreen, Intel Core i7-8565U, 16GB RAM, 512GB SSD, Windows 10', 'Dell_XPS_13_9380_Laptop', '13.3" 4K UHD Touchscreen, Intel Core i7-8565U, 16GB RAM, 512GB SSD', 'A detailed description of Dell XPS 13 9380', CURRENT_TIMESTAMP, 1, 1, 1200.00, 1300.00, 5.00, 11.98, 7.88, 0.46, 2.70, 'dell_xps_13.jpg', 1, 7);

INSERT INTO products (id, name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price, discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES (3, 'HP Spectre x360 15t 2-in-1 Laptop, 15.6" 4K UHD Touchscreen, Intel Core i7-9750H, 16GB RAM, 1TB SSD, Windows 10', 'HP_Spectre_x360_15t', '15.6" 4K UHD Touchscreen, Intel Core i7-9750H, 16GB RAM, 1TB SSD', 'A detailed description of HP Spectre x360 15t', CURRENT_TIMESTAMP, 1, 1, 1400.00, 1500.00, 5.00, 14.17, 9.84, 0.79, 4.62, 'hp_spectre_x360.jpg', 1, 8);

INSERT INTO products (id, name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price, discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES (4, 'Lenovo ThinkPad X1 Carbon, 14" FHD Display, Intel Core i7-10510U, 16GB RAM, 1TB SSD, Windows 10 Pro', 'Lenovo_ThinkPad_X1_Carbon', '14" FHD Display, Intel Core i7-10510U, 16GB RAM, 1TB SSD', 'A detailed description of Lenovo ThinkPad X1 Carbon', CURRENT_TIMESTAMP, 1, 1, 1300.00, 1400.00, 5.00, 12.74, 8.55, 0.59, 2.40, 'lenovo_x1_carbon.jpg', 1, 9);

INSERT INTO products (id, name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price, discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES (5, 'Apple MacBook Pro 16", Intel Core i9, 16GB RAM, 1TB SSD, Radeon Pro 5500M, macOS', 'Apple_MacBook_Pro_16', '16" Retina Display, Intel Core i9, 16GB RAM, 1TB SSD', 'A detailed description of Apple MacBook Pro 16"', CURRENT_TIMESTAMP, 1, 1, 2000.00, 2200.00, 10.00, 14.09, 9.68, 0.64, 4.30, 'macbook_pro_16.jpg', 1, 1);

INSERT INTO products (id, name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price, discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES (6, 'Microsoft Surface Laptop 3, 15" Touch-Screen, AMD Ryzen 7, 16GB RAM, 512GB SSD, Windows 10', 'Microsoft_Surface_Laptop_3', '15" Touch-Screen, AMD Ryzen 7, 16GB RAM, 512GB SSD', 'A detailed description of Microsoft Surface Laptop 3', CURRENT_TIMESTAMP, 1, 1, 1100.00, 1200.00, 5.00, 13.4, 9.6, 0.57, 3.40, 'surface_laptop_3.jpg', 1, 6);

INSERT INTO product_details (id, name, detail_value, product_id)
VALUES
    (11, 'Processor', 'Intel Core i7-8565U', 2),
    (12, 'RAM', '16GB DDR4', 2),
    (13, 'Storage', '512GB SSD', 2),
    (14, 'Display', '13.3" 4K UHD Touchscreen', 2),
    (15, 'Battery Life', '10 hours', 2),
    (16, 'Weight', '2.7 lbs', 2),
    (17, 'Operating System', 'Windows 10', 2),
    (18, 'USB Ports', '2 x USB-C', 2),
    (19, 'Graphics', 'Intel UHD Graphics 620', 2),
    (20, 'Webcam', '720p HD', 2);

INSERT INTO product_details (id, name, detail_value, product_id)
VALUES
    (21, 'Processor', 'Intel Core i7-9750H', 3),
    (22, 'RAM', '16GB DDR4', 3),
    (23, 'Storage', '1TB SSD', 3),
    (24, 'Display', '15.6" 4K UHD Touchscreen', 3),
    (25, 'Battery Life', '12 hours', 3),
    (26, 'Weight', '4.62 lbs', 3),
    (27, 'Operating System', 'Windows 10', 3),
    (28, 'USB Ports', '2 x USB-C, 1 x USB-A', 3),
    (29, 'Graphics', 'NVIDIA GeForce GTX 1650', 3),
    (30, 'Webcam', '1080p FHD', 3);

INSERT INTO product_details (id, name, detail_value, product_id)
VALUES
    (31, 'Processor', 'Intel Core i7-10510U', 4),
    (32, 'RAM', '16GB DDR4', 4),
    (33, 'Storage', '1TB SSD', 4),
    (34, 'Display', '14" FHD', 4),
    (35, 'Battery Life', '15 hours', 4),
    (36, 'Weight', '2.40 lbs', 4),
    (37, 'Operating System', 'Windows 10 Pro', 4),
    (38, 'USB Ports', '2 x USB-C, 2 x USB-A', 4),
    (39, 'Graphics', 'Intel UHD Graphics', 4),
    (40, 'Webcam', '720p HD', 4);

INSERT INTO product_details (id, name, detail_value, product_id)
VALUES
    (41, 'Processor', 'Intel Core i9', 5),
    (42, 'RAM', '16GB DDR4', 5),
    (43, 'Storage', '1TB SSD', 5),
    (44, 'Display', '16" Retina', 5),
    (45, 'Battery Life', '11 hours', 5),
    (46, 'Weight', '4.30 lbs', 5),
    (47, 'Operating System', 'macOS', 5),
    (48, 'USB Ports', '4 x USB-C', 5),
    (49, 'Graphics', 'Radeon Pro 5500M', 5),
    (50, 'Webcam', '720p HD', 5);

INSERT INTO product_details (id, name, detail_value, product_id)
VALUES
    (51, 'Processor', 'AMD Ryzen 7', 6),
    (52, 'RAM', '16GB DDR4', 6),
    (53, 'Storage', '512GB SSD', 6),
    (54, 'Display', '15" Touch-Screen', 6),
    (55, 'Battery Life', '11.5 hours', 6),
    (56, 'Weight', '3.40 lbs', 6),
    (57, 'Operating System', 'Windows 10', 6),
    (58, 'USB Ports', '1 x USB-C, 1 x USB-A', 6),
    (59, 'Graphics', 'Radeon Vega 11', 6),
    (60, 'Webcam', '720p HD', 6);
