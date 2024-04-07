-- Inserting products for Apple (brand_id = 1)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Apple MacBook Air', 'macbook-air', '<p>Lightweight laptop</p>', '<p>Apple MacBook Air with M1 chip</p>',
        '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 30.41, 21.24, 1.56, 1.29, 'macbook_air.png', 1, 1),
       ('Apple MacBook Pro', 'macbook-pro', '<p>High-performance laptop</p>',
        '<p>Apple MacBook Pro with M1 Pro chip</p>', '2024-06-17 00:00:00', 1, 1, 1499.00, 1999.00, 15.00, 35.79, 24.07,
        1.62, 1.83, 'macbook_pro.png', 1, 1),
       ('Apple iPad Pro', 'ipad-pro', '<p>High-performance tablet</p>', '<p>Apple iPad Pro with M1 chip</p>',
        '2024-06-17 00:00:00', 1, 1, 799.00, 1099.00, 10.00, 28.06, 21.49, 0.59, 0.47, 'ipad_pro.png', 2, 1),
       ('Apple Mac Mini', 'mac-mini', '<p>Compact desktop</p>', '<p>Apple Mac Mini with M1 chip</p>',
        '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 5.00, 19.70, 19.70, 3.60, 1.30, 'mac_mini.png', 1, 1),
       ('Apple Mac Pro', 'mac-pro', '<p>High-end desktop</p>', '<p>Apple Mac Pro with Intel Xeon processor</p>',
        '2024-06-17 00:00:00', 1, 1, 5999.00, 7999.00, 10.00, 45.00, 21.00, 21.00, 18.00, 'mac_pro.png', 1, 1),
       ('Apple iPad Air', 'ipad-air', '<p>Lightweight tablet</p>', '<p>Apple iPad Air with A14 Bionic chip</p>',
        '2024-06-17 00:00:00', 1, 1, 599.00, 799.00, 10.00, 24.76, 17.85, 0.61, 0.46, 'ipad_air.png', 2, 1);

-- Inserting products for Samsung (brand_id = 2)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Samsung Galaxy Tab S7', 'galaxy-tab-s7', '<p>High-performance tablet</p>',
        '<p>Samsung Galaxy Tab S7 with Snapdragon processor</p>', '2024-06-17 00:00:00', 1, 1, 649.00, 849.00, 5.00,
        25.38, 16.53, 0.64, 0.50, 'galaxy_tab_s7.png', 6, 2),
       ('Samsung Notebook 9', 'notebook-9', '<p>Lightweight laptop</p>',
        '<p>Samsung Notebook 9 with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1099.00, 1399.00, 10.00,
        31.24, 22.13, 1.29, 1.20, 'notebook_9.png', 2, 2),
       ('Samsung Galaxy Book Flex', 'galaxy-book-flex', '<p>2-in-1 laptop</p>',
        '<p>Samsung Galaxy Book Flex with QLED display</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 33.07,
        22.34, 1.23, 1.26, 'galaxy_book_flex.png', 2, 2),
       ('Samsung Galaxy Book Ion', 'galaxy-book-ion', '<p>Lightweight laptop</p>',
        '<p>Samsung Galaxy Book Ion with QLED display</p>', '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00, 32.00,
        22.00, 1.20, 1.10, 'galaxy_book_ion.png', 2, 2),
       ('Samsung Chromebook Plus', 'chromebook-plus', '<p>2-in-1 Chromebook</p>',
        '<p>Samsung Chromebook Plus with Google Chrome OS</p>', '2024-06-17 00:00:00', 1, 1, 449.00, 599.00, 5.00,
        28.60, 21.10, 1.50, 1.30, 'chromebook_plus.png', 2, 2),
       ('Samsung Galaxy Tab A', 'galaxy-tab-a', '<p>Affordable tablet</p>',
        '<p>Samsung Galaxy Tab A with Snapdragon processor</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 5.00,
        24.50, 15.00, 0.60, 0.40, 'galaxy_tab_a.png', 6, 2);

-- Inserting products for Sony (brand_id = 3)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Sony VAIO Z', 'vaio-z', '<p>High-performance laptop</p>', '<p>Sony VAIO Z with carbon fiber body</p>',
        '2024-06-17 00:00:00', 1, 1, 1999.00, 2499.00, 20.00, 32.80, 22.30, 1.50, 1.20, 'vaio_z.png', 2, 3),
       ('Sony Xperia Tablet', 'xperia-tablet', '<p>High-resolution tablet</p>',
        '<p>Sony Xperia Tablet with 4K display</p>', '2024-06-17 00:00:00', 1, 1, 549.00, 749.00, 5.00, 24.38, 16.53,
        0.59, 0.45, 'xperia_tablet.png', 6, 3),
       ('Sony VAIO S', 'vaio-s', '<p>Lightweight laptop</p>', '<p>Sony VAIO S with Intel i5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 31.60, 22.80, 1.40, 1.25, 'vaio_s.png', 2, 3),
       ('Sony VAIO E', 'vaio-e', '<p>Affordable laptop</p>', '<p>Sony VAIO E with Intel i3 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 599.00, 799.00, 10.00, 32.00, 21.00, 1.50, 1.40, 'vaio_e.png', 2, 3),
       ('Sony Xperia Laptop', 'xperia-laptop', '<p>High-performance laptop</p>',
        '<p>Sony Xperia Laptop with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1499.00, 1999.00, 15.00,
        34.00, 23.00, 1.40, 1.30, 'xperia_laptop.png', 2, 3),
       ('Sony Xperia Book', 'xperia-book', '<p>2-in-1 laptop</p>', '<p>Sony Xperia Book with detachable keyboard</p>',
        '2024-06-17 00:00:00', 1, 1, 1299.00, 1699.00, 15.00, 33.50, 22.50, 1.20, 1.20, 'xperia_book.png', 2, 3);

-- Inserting products for LG (brand_id = 4)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('LG Gram', 'lg-gram', '<p>Ultra-lightweight laptop</p>', '<p>LG Gram with Intel i7 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1099.00, 1399.00, 10.00, 32.20, 22.40, 1.14, 0.99, 'lg_gram.png', 2, 4),
       ('LG Ultra Tab', 'ultra-tab', '<p>High-resolution tablet</p>', '<p>LG Ultra Tab with OLED display</p>',
        '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 5.00, 23.60, 16.00, 0.60, 0.48, 'ultra_tab.png', 6, 4),
       ('LG Gram 2-in-1', 'gram-2in1', '<p>2-in-1 laptop</p>', '<p>LG Gram 2-in-1 with Intel i5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1099.00, 1399.00, 10.00, 31.00, 22.00, 1.20, 1.10, 'gram_2in1.png', 2, 4),
       ('LG ThinQ Laptop', 'thinq-laptop', '<p>Smart laptop</p>', '<p>LG ThinQ Laptop with AI features</p>',
        '2024-06-17 00:00:00', 1, 1, 1399.00, 1799.00, 15.00, 32.50, 22.50, 1.30, 1.20, 'thinq_laptop.png', 2, 4),
       ('LG G Pad', 'g-pad', '<p>Affordable tablet</p>', '<p>LG G Pad with Snapdragon processor</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 5.00, 23.00, 15.00, 0.60, 0.45, 'g_pad.png', 6, 4),
       ('LG Stylo Tablet', 'stylo-tablet', '<p>Stylus-equipped tablet</p>',
        '<p>LG Stylo Tablet with integrated stylus</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 10.00, 25.00,
        16.00, 0.65, 0.50, 'stylo_tablet.png', 6, 4);

-- Inserting products for Panasonic (brand_id = 5)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Panasonic Toughbook', 'toughbook', '<p>Rugged laptop</p>',
        '<p>Panasonic Toughbook with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 1799.00, 2199.00, 15.00,
        36.00, 25.00, 2.00, 2.50, 'toughbook.png', 4, 5),
       ('Panasonic Lumix Tablet', 'lumix-tablet', '<p>High-resolution tablet</p>',
        '<p>Panasonic Lumix Tablet with 4K display</p>', '2024-06-17 00:00:00', 1, 1, 599.00, 799.00, 5.00, 25.00,
        17.00, 0.60, 0.55, 'lumix_tablet.png', 6, 5),
       ('Panasonic Elite Laptop', 'elite-laptop', '<p>Lightweight laptop</p>',
        '<p>Panasonic Elite Laptop with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1299.00, 1599.00, 10.00,
        32.00, 22.50, 1.40, 1.30, 'elite_laptop.png', 2, 5),
       ('Panasonic Toughpad', 'toughpad', '<p>Rugged tablet</p>', '<p>Panasonic Toughpad with Intel processor</p>',
        '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 26.00, 18.00, 0.70, 0.65, 'toughpad.png', 6, 5),
       ('Panasonic IdeaPad', 'ideapad', '<p>Affordable laptop</p>', '<p>Panasonic IdeaPad with Intel i3 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 10.00, 32.50, 22.50, 1.40, 1.30, 'ideapad.png', 2, 5);


-- Inserting products for Microsoft (brand_id = 6)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Microsoft Surface Pro', 'surface-pro', '<p>High-performance tablet</p>',
        '<p>Microsoft Surface Pro with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00,
        29.00, 20.00, 0.50, 0.60, 'surface_pro.png', 6, 6),
       ('Microsoft Surface Laptop', 'surface-laptop', '<p>Lightweight laptop</p>',
        '<p>Microsoft Surface Laptop with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 1099.00, 1399.00, 10.00,
        31.50, 22.20, 1.25, 1.20, 'surface_laptop.png', 2, 6),
       ('Microsoft Surface Book', 'surface-book', '<p>2-in-1 laptop</p>',
        '<p>Microsoft Surface Book with detachable keyboard</p>', '2024-06-17 00:00:00', 1, 1, 1299.00, 1699.00, 15.00,
        33.00, 22.50, 1.20, 1.30, 'surface_book.png', 2, 6),
       ('Microsoft Surface Go', 'surface-go', '<p>Affordable tablet</p>',
        '<p>Microsoft Surface Go with Intel Pentium processor</p>', '2024-06-17 00:00:00', 1, 1, 399.00, 549.00, 5.00,
        24.50, 17.50, 0.60, 0.45, 'surface_go.png', 6, 6),
       ('Microsoft Surface Studio', 'surface-studio', '<p>High-end desktop</p>',
        '<p>Microsoft Surface Studio with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 3499.00, 4499.00, 15.00,
        45.00, 30.00, 6.00, 9.50, 'surface_studio.png', 1, 6),
       ('Microsoft Surface Hub', 'surface-hub', '<p>Collaboration device</p>',
        '<p>Microsoft Surface Hub with large touch display</p>', '2024-06-17 00:00:00', 1, 1, 6999.00, 8999.00, 10.00,
        56.00, 32.00, 10.00, 12.00, 'surface_hub.png', 1, 6);

-- Inserting products for Dell (brand_id = 7)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Dell XPS 13', 'xps-13', '<p>Ultra-portable laptop</p>', '<p>Dell XPS 13 with InfinityEdge display</p>',
        '2024-06-17 00:00:00', 1, 1, 1099.00, 1399.00, 10.00, 30.00, 20.00, 1.20, 1.25, 'xps_13.png', 1, 7),
       ('Dell Latitude 5420', 'latitude-5420', '<p>Business laptop</p>',
        '<p>Dell Latitude 5420 with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 32.00,
        22.00, 1.30, 1.35, 'latitude_5420.png', 1, 7),
       ('Dell Inspiron 15', 'inspiron-15', '<p>Affordable laptop</p>',
        '<p>Dell Inspiron 15 with Intel i3 processor</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 5.00, 34.00,
        23.00, 1.40, 1.50, 'inspiron_15.png', 1, 7),
       ('Dell Precision 5550', 'precision-5550', '<p>Workstation laptop</p>',
        '<p>Dell Precision 5550 with Xeon processor</p>', '2024-06-17 00:00:00', 1, 1, 1999.00, 2499.00, 15.00, 36.00,
        24.00, 1.50, 1.70, 'precision_5550.png', 1, 7),
       ('Dell G5 15', 'g5-15', '<p>Gaming laptop</p>', '<p>Dell G5 15 with NVIDIA GTX 1660 Ti</p>',
        '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 35.00, 25.00, 1.60, 1.80, 'g5_15.png', 1, 7),
       ('Dell XPS 15', 'xps-15', '<p>High-performance laptop</p>', '<p>Dell XPS 15 with Intel i7 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1499.00, 1999.00, 15.00, 34.00, 23.00, 1.40, 1.60, 'xps_15.png', 1, 7);

-- Inserting products for HP (brand_id = 8)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('HP Spectre x360', 'spectre-x360', '<p>2-in-1 laptop</p>', '<p>HP Spectre x360 with Intel i7 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1299.00, 1699.00, 15.00, 32.00, 22.00, 1.30, 1.35, 'spectre_x360.png', 1, 8),
       ('HP Envy 13', 'envy-13', '<p>Lightweight laptop</p>', '<p>HP Envy 13 with Intel i5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 30.00, 20.00, 1.20, 1.25, 'envy_13.png', 1, 8),
       ('HP Pavilion 15', 'pavilion-15', '<p>Affordable laptop</p>', '<p>HP Pavilion 15 with Intel i3 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 5.00, 34.00, 23.00, 1.40, 1.50, 'pavilion_15.png', 1, 8),
       ('HP EliteBook 840', 'elitebook-840', '<p>Business laptop</p>',
        '<p>HP EliteBook 840 with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 1299.00, 1599.00, 10.00, 32.00,
        22.00, 1.30, 1.35, 'elitebook_840.png', 1, 8),
       ('HP Omen 15', 'omen-15', '<p>Gaming laptop</p>', '<p>HP Omen 15 with NVIDIA RTX 2060</p>',
        '2024-06-17 00:00:00', 1, 1, 1399.00, 1799.00, 15.00, 35.00, 25.00, 1.50, 1.70, 'omen_15.png', 1, 8),
       ('HP Chromebook 14', 'chromebook-14', '<p>Affordable Chromebook</p>',
        '<p>HP Chromebook 14 with Intel Celeron processor</p>', '2024-06-17 00:00:00', 1, 1, 249.00, 349.00, 5.00,
        30.00, 20.00, 1.20, 1.25, 'chromebook_14.png', 1, 8);

-- Inserting products for Lenovo (brand_id = 9)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Lenovo ThinkPad X1 Carbon', 'thinkpad-x1-carbon', '<p>Business laptop</p>',
        '<p>Lenovo ThinkPad X1 Carbon with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1499.00, 1999.00,
        15.00, 32.00, 22.00, 1.20, 1.25, 'thinkpad_x1_carbon.png', 1, 9),
       ('Lenovo Yoga 7i', 'yoga-7i', '<p>2-in-1 laptop</p>', '<p>Lenovo Yoga 7i with Intel i5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00, 30.00, 20.00, 1.20, 1.30, 'yoga_7i.png', 1, 9),
       ('Lenovo IdeaPad 3', 'ideapad-3', '<p>Affordable laptop</p>',
        '<p>Lenovo IdeaPad 3 with AMD Ryzen 3 processor</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 5.00, 34.00,
        23.00, 1.40, 1.50, 'ideapad_3.png', 1, 9),
       ('Lenovo Legion 5', 'legion-5', '<p>Gaming laptop</p>', '<p>Lenovo Legion 5 with NVIDIA GTX 1660 Ti</p>',
        '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 35.00, 25.00, 1.50, 1.70, 'legion_5.png', 1, 9),
       ('Lenovo ThinkBook 14s', 'thinkbook-14s', '<p>Lightweight laptop</p>',
        '<p>Lenovo ThinkBook 14s with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 799.00, 1099.00, 10.00,
        32.00, 22.00, 1.30, 1.35, 'thinkbook_14s.png', 1, 9),
       ('Lenovo Chromebook Duet', 'chromebook-duet', '<p>2-in-1 Chromebook</p>',
        '<p>Lenovo Chromebook Duet with detachable keyboard</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 5.00,
        25.00, 16.00, 0.70, 0.60, 'chromebook_duet.png', 1, 9);

-- Inserting products for Asus (brand_id = 10)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Asus ZenBook 14', 'zenbook-14', '<p>Ultra-portable laptop</p>',
        '<p>Asus ZenBook 14 with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1099.00, 1399.00, 10.00, 31.00,
        21.00, 1.20, 1.10, 'zenbook_14.png', 1, 10),
       ('Asus ROG Zephyrus G14', 'rog-zephyrus-g14', '<p>Gaming laptop</p>',
        '<p>Asus ROG Zephyrus G14 with AMD Ryzen 9 processor</p>', '2024-06-17 00:00:00', 1, 1, 1499.00, 1899.00, 10.00,
        32.00, 22.00, 1.50, 1.40, 'rog_zephyrus_g14.png', 1, 10),
       ('Asus VivoBook S15', 'vivobook-s15', '<p>Affordable laptop</p>',
        '<p>Asus VivoBook S15 with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 10.00, 33.00,
        23.00, 1.40, 1.30, 'vivobook_s15.png', 1, 10),
       ('Asus Chromebook Flip', 'chromebook-flip', '<p>2-in-1 Chromebook</p>',
        '<p>Asus Chromebook Flip with Intel Celeron processor</p>', '2024-06-17 00:00:00', 1, 1, 349.00, 499.00, 5.00,
        29.00, 20.00, 1.20, 1.15, 'chromebook_flip.png', 1, 10),
       ('Asus TUF Gaming A15', 'tuf-gaming-a15', '<p>Gaming laptop</p>',
        '<p>Asus TUF Gaming A15 with AMD Ryzen 7 processor</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00,
        34.00, 24.00, 1.60, 1.50, 'tuf_gaming_a15.png', 1, 10),
       ('Asus ExpertBook B9', 'expertbook-b9', '<p>Business laptop</p>',
        '<p>Asus ExpertBook B9 with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1199.00, 1499.00, 10.00,
        31.50, 21.50, 1.10, 1.20, 'expertbook_b9.png', 1, 10);

-- Inserting products for Acer (brand_id = 11)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Acer Swift 3', 'swift-3', '<p>Ultra-portable laptop</p>', '<p>Acer Swift 3 with AMD Ryzen 7 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 10.00, 30.00, 20.00, 1.20, 1.25, 'swift_3.png', 1, 11),
       ('Acer Predator Helios 300', 'predator-helios-300', '<p>Gaming laptop</p>',
        '<p>Acer Predator Helios 300 with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1199.00, 1499.00, 15.00,
        35.00, 25.00, 1.60, 1.50, 'predator_helios_300.png', 1, 11),
       ('Acer Aspire 5', 'aspire-5', '<p>Affordable laptop</p>', '<p>Acer Aspire 5 with Intel i5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 5.00, 34.00, 23.00, 1.40, 1.30, 'aspire_5.png', 1, 11),
       ('Acer Chromebook 714', 'chromebook-714', '<p>Business Chromebook</p>',
        '<p>Acer Chromebook 714 with Intel i3 processor</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00, 31.00,
        21.00, 1.30, 1.25, 'chromebook_714.png', 1, 11),
       ('Acer Nitro 5', 'nitro-5', '<p>Gaming laptop</p>', '<p>Acer Nitro 5 with AMD Ryzen 5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 799.00, 999.00, 10.00, 33.00, 23.00, 1.50, 1.40, 'nitro_5.png', 1, 11),
       ('Acer Spin 5', 'spin-5', '<p>2-in-1 laptop</p>', '<p>Acer Spin 5 with Intel i7 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 32.00, 22.00, 1.30, 1.20, 'spin_5.png', 1, 11);

-- Inserting products for Toshiba (brand_id = 12)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Toshiba Portege X30', 'portege-x30', '<p>Ultra-portable laptop</p>',
        '<p>Toshiba Portege X30 with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1299.00, 1699.00, 15.00,
        31.00, 21.00, 1.20, 1.10, 'portege_x30.png', 1, 12),
       ('Toshiba Satellite Pro', 'satellite-pro', '<p>Business laptop</p>',
        '<p>Toshiba Satellite Pro with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00,
        33.00, 23.00, 1.30, 1.20, 'satellite_pro.png', 1, 12),
       ('Toshiba Tecra A50', 'tecra-a50', '<p>Affordable laptop</p>',
        '<p>Toshiba Tecra A50 with Intel i3 processor</p>', '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 5.00, 34.00,
        24.00, 1.40, 1.30, 'tecra_a50.png', 1, 12),
       ('Toshiba Chromebook 2', 'chromebook-2', '<p>Lightweight Chromebook</p>',
        '<p>Toshiba Chromebook 2 with Intel Celeron processor</p>', '2024-06-17 00:00:00', 1, 1, 349.00, 499.00, 5.00,
        29.00, 19.00, 1.10, 1.20, 'chromebook_2.png', 1, 12),
       ('Toshiba Dynabook', 'dynabook', '<p>Business laptop</p>', '<p>Toshiba Dynabook with Intel i5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1099.00, 1399.00, 10.00, 32.00, 22.00, 1.20, 1.30, 'dynabook.png', 1, 12),
       ('Toshiba Qosmio X75', 'qosmio-x75', '<p>Gaming laptop</p>', '<p>Toshiba Qosmio X75 with NVIDIA GTX 770M</p>',
        '2024-06-17 00:00:00', 1, 1, 1499.00, 1999.00, 10.00, 35.00, 25.00, 1.50, 1.60, 'qosmio_x75.png', 1, 12);

-- Inserting products for Philips (brand_id = 13)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Philips Ultrabook 5000', 'ultrabook-5000', '<p>Ultra-portable laptop</p>',
        '<p>Philips Ultrabook 5000 with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00,
        30.00, 20.00, 1.20, 1.10, 'ultrabook_5000.png', 4, 13),
       ('Philips SmartBook', 'smartbook', '<p>Smart laptop</p>', '<p>Philips SmartBook with AI features</p>',
        '2024-06-17 00:00:00', 1, 1, 1199.00, 1499.00, 10.00, 32.00, 22.00, 1.20, 1.10, 'smartbook.png', 4, 13),
       ('Philips Gaming Pro', 'gaming-pro', '<p>Gaming laptop</p>', '<p>Philips Gaming Pro with Intel i9 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1799.00, 2199.00, 15.00, 35.00, 25.00, 1.50, 1.40, 'gaming_pro.png', 4, 13),
       ('Philips Convertible 360', 'convertible-360', '<p>2-in-1 laptop</p>',
        '<p>Philips Convertible 360 with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00,
        31.00, 21.00, 1.20, 1.15, 'convertible_360.png', 4, 13),
       ('Philips Toughbook', 'philips_toughbook', '<p>Rugged laptop</p>', '<p>Philips Toughbook with Intel i5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1599.00, 1999.00, 15.00, 34.00, 24.00, 2.00, 2.50, 'toughbook.png', 4, 13);

-- Inserting products for Intel (brand_id = 14)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Intel NUC Mini PC', 'nuc-mini-pc', '<p>Compact desktop</p>',
        '<p>Intel NUC Mini PC with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 5.00, 15.00,
        15.00, 5.00, 1.20, 'nuc_mini_pc.png', 5, 14),
       ('Intel Core i9', 'core-i9', '<p>High-performance CPU</p>', '<p>Intel Core i9 11th Gen processor</p>',
        '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00, 5.00, 5.00, 5.00, 0.50, 'core_i9.png', 5, 14),
       ('Intel Core i7', 'core-i7', '<p>High-performance CPU</p>', '<p>Intel Core i7 11th Gen processor</p>',
        '2024-06-17 00:00:00', 1, 1, 399.00, 599.00, 10.00, 5.00, 5.00, 5.00, 0.50, 'core_i7.png', 5, 14),
       ('Intel Core i5', 'core-i5', '<p>Mid-range CPU</p>', '<p>Intel Core i5 11th Gen processor</p>',
        '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 10.00, 5.00, 5.00, 5.00, 0.50, 'core_i5.png', 5, 14),
       ('Intel Core i3', 'core-i3', '<p>Entry-level CPU</p>', '<p>Intel Core i3 11th Gen processor</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 5.00, 5.00, 5.00, 5.00, 0.50, 'core_i3.png', 5, 14),
       ('Intel Xeon', 'xeon', '<p>High-performance server CPU</p>', '<p>Intel Xeon processor for workstations</p>',
        '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00, 5.00, 5.00, 5.00, 0.50, 'xeon.png', 5, 14);

-- Inserting products for AMD (brand_id = 15)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('AMD Ryzen 9 5900X', 'ryzen-9-5900x', '<p>High-performance CPU</p>', '<p>AMD Ryzen 9 5900X processor</p>',
        '2024-06-17 00:00:00', 1, 1, 549.00, 749.00, 10.00, 5.00, 5.00, 5.00, 0.50, 'ryzen_9_5900x.png', 5, 15),
       ('AMD Ryzen 7 5800X', 'ryzen-7-5800x', '<p>High-performance CPU</p>', '<p>AMD Ryzen 7 5800X processor</p>',
        '2024-06-17 00:00:00', 1, 1, 449.00, 649.00, 10.00, 5.00, 5.00, 5.00, 0.50, 'ryzen_7_5800x.png', 5, 15),
       ('AMD Ryzen 5 5600X', 'ryzen-5-5600x', '<p>Mid-range CPU</p>', '<p>AMD Ryzen 5 5600X processor</p>',
        '2024-06-17 00:00:00', 1, 1, 299.00, 449.00, 10.00, 5.00, 5.00, 5.00, 0.50, 'ryzen_5_5600x.png', 5, 15),
       ('AMD Ryzen 3 5300G', 'ryzen-3-5300g', '<p>Entry-level CPU</p>', '<p>AMD Ryzen 3 5300G processor</p>',
        '2024-06-17 00:00:00', 1, 1, 149.00, 249.00, 5.00, 5.00, 5.00, 5.00, 0.50, 'ryzen_3_5300g.png', 5, 15);


-- Inserting products for AMD (brand_id = 15) continued
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('AMD Radeon RX 6800 XT', 'radeon-rx-6800-xt', '<p>High-performance GPU</p>',
        '<p>AMD Radeon RX 6800 XT graphics card</p>', '2024-06-17 00:00:00', 1, 1, 649.00, 899.00, 10.00, 20.00, 10.00,
        5.00, 2.00, 'radeon_rx_6800_xt.png', 5, 15),
       ('AMD Radeon RX 6700 XT', 'radeon-rx-6700-xt', '<p>Mid-range GPU</p>',
        '<p>AMD Radeon RX 6700 XT graphics card</p>', '2024-06-17 00:00:00', 1, 1, 479.00, 629.00, 10.00, 20.00, 10.00,
        5.00, 2.00, 'radeon_rx_6700_xt.png', 5, 15),
       ('AMD Radeon RX 6600', 'radeon-rx-6600', '<p>Affordable GPU</p>', '<p>AMD Radeon RX 6600 graphics card</p>',
        '2024-06-17 00:00:00', 1, 1, 329.00, 429.00, 5.00, 20.00, 10.00, 5.00, 2.00, 'radeon_rx_6600.png', 5, 15),
       ('AMD EPYC 7742', 'epyc-7742', '<p>Server CPU</p>', '<p>AMD EPYC 7742 processor</p>', '2024-06-17 00:00:00', 1,
        1, 4999.00, 5999.00, 10.00, 20.00, 20.00, 5.00, 2.00, 'epyc_7742.png', 5, 15),
       ('AMD Ryzen Threadripper 3990X', 'threadripper-3990x', '<p>High-end desktop CPU</p>',
        '<p>AMD Ryzen Threadripper 3990X processor</p>', '2024-06-17 00:00:00', 1, 1, 3999.00, 4999.00, 10.00, 20.00,
        10.00, 5.00, 2.00, 'threadripper_3990x.png', 5, 15),
       ('AMD Ryzen Threadripper 3960X', 'threadripper-3960x', '<p>High-end desktop CPU</p>',
        '<p>AMD Ryzen Threadripper 3960X processor</p>', '2024-06-17 00:00:00', 1, 1, 1399.00, 1799.00, 10.00, 20.00,
        10.00, 5.00, 2.00, 'threadripper_3960x.png', 5, 15);

-- Inserting products for Nvidia (brand_id = 16)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Nvidia GeForce RTX 3080', 'geforce-rtx-3080', '<p>High-performance GPU</p>',
        '<p>Nvidia GeForce RTX 3080 graphics card</p>', '2024-06-17 00:00:00', 1, 1, 699.00, 999.00, 10.00, 20.00,
        10.00, 5.00, 2.00, 'geforce_rtx_3080.png', 5, 16),
       ('Nvidia GeForce RTX 3070', 'geforce-rtx-3070', '<p>Mid-range GPU</p>',
        '<p>Nvidia GeForce RTX 3070 graphics card</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00, 20.00,
        10.00, 5.00, 2.00, 'geforce_rtx_3070.png', 5, 16),
       ('Nvidia GeForce RTX 3060', 'geforce-rtx-3060', '<p>Affordable GPU</p>',
        '<p>Nvidia GeForce RTX 3060 graphics card</p>', '2024-06-17 00:00:00', 1, 1, 329.00, 499.00, 5.00, 20.00, 10.00,
        5.00, 2.00, 'geforce_rtx_3060.png', 5, 16),
       ('Nvidia Titan RTX', 'titan-rtx', '<p>High-end GPU</p>', '<p>Nvidia Titan RTX graphics card</p>',
        '2024-06-17 00:00:00', 1, 1, 2499.00, 2999.00, 10.00, 20.00, 10.00, 5.00, 2.00, 'titan_rtx.png', 5, 16),
       ('Nvidia Quadro RTX 8000', 'quadro-rtx-8000', '<p>Professional GPU</p>',
        '<p>Nvidia Quadro RTX 8000 graphics card</p>', '2024-06-17 00:00:00', 1, 1, 5499.00, 6999.00, 10.00, 20.00,
        10.00, 5.00, 2.00, 'quadro_rtx_8000.png', 5, 16),
       ('Nvidia Jetson Nano', 'jetson-nano', '<p>AI development kit</p>',
        '<p>Nvidia Jetson Nano AI development kit</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 129.00, 5.00, 20.00, 10.00,
        5.00, 2.00, 'jetson_nano.png', 5, 16);

-- Inserting products for Canon (brand_id = 17)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Canon EOS R5', 'eos-r5', '<p>Mirrorless camera</p>', '<p>Canon EOS R5 with 45MP sensor</p>',
        '2024-06-17 00:00:00', 1, 1, 3899.00, 4999.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'eos_r5.png', 1, 17),
       ('Canon EOS R6', 'eos-r6', '<p>Mirrorless camera</p>', '<p>Canon EOS R6 with 20MP sensor</p>',
        '2024-06-17 00:00:00', 1, 1, 2499.00, 2999.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'eos_r6.png', 1, 17),
       ('Canon PowerShot G7 X', 'powershot-g7-x', '<p>Compact camera</p>',
        '<p>Canon PowerShot G7 X with 20MP sensor</p>', '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 10.00, 20.00,
        10.00, 5.00, 0.50, 'powershot_g7_x.png', 1, 17),
       ('Canon EOS M50', 'eos-m50', '<p>Mirrorless camera</p>', '<p>Canon EOS M50 with 24MP sensor</p>',
        '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'eos_m50.png', 1, 17),
       ('Canon EOS 90D', 'eos-90d', '<p>DSLR camera</p>', '<p>Canon EOS 90D with 32MP sensor</p>',
        '2024-06-17 00:00:00', 1, 1, 1199.00, 1499.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'eos_90d.png', 1, 17),
       ('Canon Rebel T7', 'rebel-t7', '<p>DSLR camera</p>', '<p>Canon Rebel T7 with 24MP sensor</p>',
        '2024-06-17 00:00:00', 1, 1, 499.00, 649.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'rebel_t7.png', 1, 17);

-- Inserting products for Nikon (brand_id = 18)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Nikon Z6 II', 'z6-ii', '<p>Mirrorless camera</p>', '<p>Nikon Z6 II with 24MP sensor</p>',
        '2024-06-17 00:00:00', 1, 1, 1999.00, 2499.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'z6_ii.png', 1, 18),
       ('Nikon Z7 II', 'z7-ii', '<p>Mirrorless camera</p>', '<p>Nikon Z7 II with 45MP sensor</p>',
        '2024-06-17 00:00:00', 1, 1, 2999.00, 3499.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'z7_ii.png', 1, 18),
       ('Nikon D850', 'd850', '<p>DSLR camera</p>', '<p>Nikon D850 with 45MP sensor</p>', '2024-06-17 00:00:00', 1, 1,
        2999.00, 3499.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'd850.png', 1, 18),
       ('Nikon D7500', 'd7500', '<p>DSLR camera</p>', '<p>Nikon D7500 with 21MP sensor</p>', '2024-06-17 00:00:00', 1,
        1, 999.00, 1299.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'd7500.png', 1, 18),
       ('Nikon COOLPIX P1000', 'coolpix-p1000', '<p>Superzoom camera</p>',
        '<p>Nikon COOLPIX P1000 with 16MP sensor</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 20.00,
        10.00, 5.00, 1.00, 'coolpix_p1000.png', 1, 18),
       ('Nikon Z50', 'z50', '<p>Mirrorless camera</p>', '<p>Nikon Z50 with 21MP sensor</p>', '2024-06-17 00:00:00', 1,
        1, 899.00, 1199.00, 10.00, 20.00, 15.00, 10.00, 1.20, 'z50.png', 1, 18);

-- Inserting products for Huawei (brand_id = 19)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Huawei MateBook X Pro', 'matebook-x-pro', '<p>Ultra-portable laptop</p>',
        '<p>Huawei MateBook X Pro with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1199.00, 1499.00, 10.00,
        31.00, 21.00, 1.20, 1.10, 'matebook_x_pro.png', 2, 19),
       ('Huawei MatePad Pro', 'matepad-pro', '<p>High-performance tablet</p>',
        '<p>Huawei MatePad Pro with Kirin 990 processor</p>', '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 10.00, 25.00,
        16.00, 0.60, 0.50, 'matepad_pro.png', 6, 19),
       ('Huawei MateBook D 15', 'matebook-d-15', '<p>Affordable laptop</p>',
        '<p>Huawei MateBook D 15 with AMD Ryzen 5 processor</p>', '2024-06-17 00:00:00', 1, 1, 599.00, 799.00, 5.00,
        34.00, 24.00, 1.50, 1.40, 'matebook_d_15.png', 2, 19),
       ('Huawei MediaPad M5', 'mediapad-m5', '<p>High-resolution tablet</p>',
        '<p>Huawei MediaPad M5 with Kirin 960 processor</p>', '2024-06-17 00:00:00', 1, 1, 399.00, 599.00, 10.00, 24.00,
        16.00, 0.60, 0.45, 'mediapad_m5.png', 6, 19),
       ('Huawei MateBook 13', 'matebook-13', '<p>Lightweight laptop</p>',
        '<p>Huawei MateBook 13 with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00, 32.00,
        22.00, 1.20, 1.25, 'matebook_13.png', 2, 19),
       ('Huawei MatePad T10s', 'matepad-t10s', '<p>Affordable tablet</p>',
        '<p>Huawei MatePad T10s with Kirin 710A processor</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 5.00,
        24.00, 15.00, 0.60, 0.45, 'matepad_t10s.png', 6, 19);

-- Inserting products for Xiaomi (brand_id = 20)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Xiaomi Mi Notebook Pro', 'mi-notebook-pro', '<p>High-performance laptop</p>',
        '<p>Xiaomi Mi Notebook Pro with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00,
        32.00, 22.00, 1.30, 1.20, 'mi_notebook_pro.png', 2, 20),
       ('Xiaomi Pad 5', 'pad-5', '<p>High-resolution tablet</p>', '<p>Xiaomi Pad 5 with Snapdragon 860 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 399.00, 599.00, 10.00, 25.00, 16.00, 0.60, 0.50, 'pad_5.png', 6, 20),
       ('Xiaomi Mi Notebook Air', 'mi-notebook-air', '<p>Ultra-portable laptop</p>',
        '<p>Xiaomi Mi Notebook Air with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 10.00,
        31.00, 21.00, 1.20, 1.10, 'mi_notebook_air.png', 2, 20),
       ('Xiaomi RedmiBook 14', 'redmibook-14', '<p>Affordable laptop</p>',
        '<p>Xiaomi RedmiBook 14 with Intel i3 processor</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 5.00, 33.00,
        23.00, 1.40, 1.30, 'redmibook_14.png', 2, 20),
       ('Xiaomi Mi Pad 4', 'mi-pad-4', '<p>Affordable tablet</p>',
        '<p>Xiaomi Mi Pad 4 with Snapdragon 660 processor</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 5.00,
        24.00, 15.00, 0.60, 0.45, 'mi_pad_4.png', 6, 20),
       ('Xiaomi Mi Notebook 15.6', 'mi-notebook-15.6', '<p>Lightweight laptop</p>',
        '<p>Xiaomi Mi Notebook 15.6 with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00,
        34.00, 24.00, 1.40, 1.30, 'mi_notebook_15_6.png', 2, 20);

-- Inserting products for Oppo (brand_id = 21)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Oppo Find X2 Pro', 'find-x2-pro', '<p>High-performance smartphone</p>',
        '<p>Oppo Find X2 Pro with Snapdragon 865 processor</p>', '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'find_x2_pro.png', 2, 21),
       ('Oppo Reno4 Pro', 'reno4-pro', '<p>Mid-range smartphone</p>',
        '<p>Oppo Reno4 Pro with Snapdragon 720G processor</p>', '2024-06-17 00:00:00', 1, 1, 599.00, 799.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'reno4_pro.png', 2, 21),
       ('Oppo A92', 'a92', '<p>Affordable smartphone</p>', '<p>Oppo A92 with Snapdragon 665 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'a92.png', 2, 21),
       ('Oppo Find X3 Lite', 'find-x3-lite', '<p>Mid-range smartphone</p>',
        '<p>Oppo Find X3 Lite with Snapdragon 765G processor</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'find_x3_lite.png', 2, 21),
       ('Oppo Reno5 5G', 'reno5-5g', '<p>5G smartphone</p>', '<p>Oppo Reno5 5G with Snapdragon 765G processor</p>',
        '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00, 16.00, 7.50, 0.80, 0.50, 'reno5_5g.png', 2, 21),
       ('Oppo Find X2 Neo', 'find-x2-neo', '<p>High-performance smartphone</p>',
        '<p>Oppo Find X2 Neo with Snapdragon 865 processor</p>', '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'find_x2_neo.png', 2, 21);

-- Inserting products for Vivo (brand_id = 22)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Vivo X60 Pro', 'x60-pro', '<p>High-performance smartphone</p>',
        '<p>Vivo X60 Pro with Snapdragon 870 processor</p>', '2024-06-17 00:00:00', 1, 1, 799.00, 999.00, 10.00, 16.00,
        7.50, 0.80, 0.50, 'x60_pro.png', 2, 22),
       ('Vivo Y20', 'y20', '<p>Affordable smartphone</p>', '<p>Vivo Y20 with Snapdragon 460 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'y20.png', 2, 22),
       ('Vivo V20', 'v20', '<p>Mid-range smartphone</p>', '<p>Vivo V20 with Snapdragon 720G processor</p>',
        '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'v20.png', 2, 22),
       ('Vivo X50 Pro', 'x50-pro', '<p>High-performance smartphone</p>',
        '<p>Vivo X50 Pro with Snapdragon 765G processor</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00, 16.00,
        7.50, 0.80, 0.50, 'x50_pro.png', 2, 22),
       ('Vivo Y51', 'y51', '<p>Mid-range smartphone</p>', '<p>Vivo Y51 with Snapdragon 665 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 249.00, 349.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'y51.png', 2, 22),
       ('Vivo NEX 3', 'nex-3', '<p>High-end smartphone</p>', '<p>Vivo NEX 3 with Snapdragon 855+ processor</p>',
        '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00, 16.00, 7.50, 0.80, 0.50, 'nex_3.png', 2, 22);

-- Inserting products for OnePlus (brand_id = 23)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('OnePlus 9 Pro', '9-pro', '<p>High-performance smartphone</p>',
        '<p>OnePlus 9 Pro with Snapdragon 888 processor</p>', '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00,
        16.00, 7.50, 0.80, 0.50, '9_pro.png', 2, 23),
       ('OnePlus Nord', 'nord', '<p>Mid-range smartphone</p>', '<p>OnePlus Nord with Snapdragon 765G processor</p>',
        '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00, 16.00, 7.50, 0.80, 0.50, 'nord.png', 2, 23),
       ('OnePlus 8T', '8t', '<p>High-performance smartphone</p>', '<p>OnePlus 8T with Snapdragon 865 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 599.00, 799.00, 10.00, 16.00, 7.50, 0.80, 0.50, '8t.png', 2, 23),
       ('OnePlus 7 Pro', '7-pro', '<p>High-end smartphone</p>', '<p>OnePlus 7 Pro with Snapdragon 855 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 10.00, 16.00, 7.50, 0.80, 0.50, '7_pro.png', 2, 23),
       ('OnePlus Nord CE', 'nord-ce', '<p>Affordable smartphone</p>',
        '<p>OnePlus Nord CE with Snapdragon 750G processor</p>', '2024-06-17 00:00:00', 1, 1, 399.00, 499.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'nord_ce.png', 2, 23),
       ('OnePlus 6T', '6t', '<p>Mid-range smartphone</p>', '<p>OnePlus 6T with Snapdragon 845 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 499.00, 599.00, 5.00, 16.00, 7.50, 0.80, 0.50, '6t.png', 2, 23);

-- Inserting products for Motorola (brand_id = 24)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Motorola Edge Plus', 'edge-plus', '<p>High-performance smartphone</p>',
        '<p>Motorola Edge Plus with Snapdragon 865 processor</p>', '2024-06-17 00:00:00', 1, 1, 799.00, 999.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'edge_plus.png', 2, 24),
       ('Motorola Moto G Power', 'moto-g-power', '<p>Affordable smartphone</p>',
        '<p>Motorola Moto G Power with Snapdragon 665 processor</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'moto_g_power.png', 2, 24),
       ('Motorola Moto G Stylus', 'moto-g-stylus', '<p>Mid-range smartphone</p>',
        '<p>Motorola Moto G Stylus with Snapdragon 678 processor</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00,
        5.00, 16.00, 7.50, 0.80, 0.50, 'moto_g_stylus.png', 2, 24),
       ('Motorola Razr', 'razr', '<p>Foldable smartphone</p>', '<p>Motorola Razr with Snapdragon 710 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1199.00, 1499.00, 10.00, 16.00, 7.50, 0.80, 0.50, 'razr.png', 2, 24),
       ('Motorola Moto E', 'moto-e', '<p>Budget smartphone</p>', '<p>Motorola Moto E with Snapdragon 632 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 149.00, 199.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'moto_e.png', 2, 24),
       ('Motorola Moto Z4', 'moto-z4', '<p>High-performance smartphone</p>',
        '<p>Motorola Moto Z4 with Snapdragon 675 processor</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'moto_z4.png', 2, 24);

-- Inserting products for Sharp (brand_id = 25)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Sharp Aquos R5G', 'aquos-r5g', '<p>High-performance smartphone</p>',
        '<p>Sharp Aquos R5G with Snapdragon 865 processor</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'aquos_r5g.png', 2, 25),
       ('Sharp Aquos Sense4', 'aquos-sense4', '<p>Mid-range smartphone</p>',
        '<p>Sharp Aquos Sense4 with Snapdragon 720G processor</p>', '2024-06-17 00:00:00', 1, 1, 399.00, 599.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'aquos_sense4.png', 2, 25),
       ('Sharp Aquos Zero2', 'aquos-zero2', '<p>Lightweight smartphone</p>',
        '<p>Sharp Aquos Zero2 with Snapdragon 855 processor</p>', '2024-06-17 00:00:00', 1, 1, 699.00, 899.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'aquos_zero2.png', 2, 25),
       ('Sharp Aquos V', 'aquos-v', '<p>Affordable smartphone</p>',
        '<p>Sharp Aquos V with Snapdragon 835 processor</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 5.00, 16.00,
        7.50, 0.80, 0.50, 'aquos_v.png', 2, 25),
       ('Sharp Aquos R3', 'aquos-r3', '<p>High-end smartphone</p>',
        '<p>Sharp Aquos R3 with Snapdragon 855 processor</p>', '2024-06-17 00:00:00', 1, 1, 799.00, 999.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'aquos_r3.png', 2, 25),
       ('Sharp Aquos Sense3', 'aquos-sense3', '<p>Mid-range smartphone</p>',
        '<p>Sharp Aquos Sense3 with Snapdragon 630 processor</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 499.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'aquos_sense3.png', 2, 25);

-- Inserting products for Pioneer (brand_id = 26)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Pioneer DJ Controller', 'dj-controller', '<p>Professional DJ controller</p>',
        '<p>Pioneer DJ Controller with built-in effects</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00,
        40.00, 25.00, 5.00, 5.00, 'dj_controller.png', 4, 26),
       ('Pioneer Home Theater', 'home-theater', '<p>5.1 Channel Home Theater</p>',
        '<p>Pioneer Home Theater system with surround sound</p>', '2024-06-17 00:00:00', 1, 1, 599.00, 799.00, 10.00,
        50.00, 30.00, 10.00, 15.00, 'home_theater.png', 4, 26),
       ('Pioneer Car Stereo', 'car-stereo', '<p>In-dash car stereo</p>',
        '<p>Pioneer Car Stereo with Bluetooth connectivity</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 5.00,
        18.00, 10.00, 5.00, 2.00, 'car_stereo.png', 4, 26),
       ('Pioneer Turntable', 'turntable', '<p>Professional turntable</p>', '<p>Pioneer Turntable with direct drive</p>',
        '2024-06-17 00:00:00', 1, 1, 399.00, 599.00, 10.00, 35.00, 25.00, 10.00, 10.00, 'turntable.png', 4, 26),
       ('Pioneer AV Receiver', 'av-receiver', '<p>7.2 Channel AV Receiver</p>',
        '<p>Pioneer AV Receiver with Dolby Atmos support</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00,
        45.00, 30.00, 15.00, 12.00, 'av_receiver.png', 4, 26),
       ('Pioneer Headphones', 'pioneer_headphones', '<p>Over-ear headphones</p>',
        '<p>Pioneer Headphones with noise cancellation</p>', '2024-06-17 00:00:00', 1, 1, 149.00, 199.00, 5.00, 20.00,
        15.00, 5.00, 0.50, 'headphones.png', 4, 26);

-- Inserting products for JVC (brand_id = 27)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('JVC 4K Camcorder', '4k-camcorder', '<p>Professional camcorder</p>',
        '<p>JVC 4K Camcorder with 20x optical zoom</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00, 20.00,
        10.00, 5.00, 2.00, '4k_camcorder.png', 4, 27),
       ('JVC Smart TV', 'smart-tv', '<p>55-inch 4K Smart TV</p>', '<p>JVC Smart TV with HDR support</p>',
        '2024-06-17 00:00:00', 1, 1, 599.00, 799.00, 10.00, 125.00, 75.00, 10.00, 20.00, 'smart_tv.png', 4, 27),
       ('JVC Bluetooth Speaker', 'bluetooth-speaker', '<p>Portable Bluetooth speaker</p>',
        '<p>JVC Bluetooth Speaker with 12-hour battery life</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 149.00, 5.00,
        10.00, 10.00, 10.00, 0.50, 'bluetooth_speaker.png', 4, 27),
       ('JVC Car Audio System', 'car-audio-system', '<p>In-dash car audio system</p>',
        '<p>JVC Car Audio System with touchscreen display</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 10.00,
        20.00, 10.00, 10.00, 2.00, 'car_audio_system.png', 4, 27),
       ('JVC Headphones', 'headphones', '<p>Wireless headphones</p>', '<p>JVC Headphones with noise cancellation</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 10.00, 20.00, 15.00, 5.00, 0.50, 'headphones.png', 4, 27),
       ('JVC Soundbar', 'soundbar', '<p>2.1 Channel Soundbar</p>', '<p>JVC Soundbar with wireless subwoofer</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 10.00, 20.00, 10.00, 5.00, 2.00, 'soundbar.png', 4, 27);

-- Inserting products for Harman Kardon (brand_id = 28)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Harman Kardon Onyx Studio 6', 'onyx-studio-6', '<p>Portable Bluetooth speaker</p>',
        '<p>Harman Kardon Onyx Studio 6 with 8-hour battery life</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 299.00,
        10.00, 20.00, 20.00, 20.00, 0.50, 'onyx_studio_6.png', 4, 28),
       ('Harman Kardon Soundsticks III', 'soundsticks-iii', '<p>2.1 Channel speaker system</p>',
        '<p>Harman Kardon Soundsticks III with subwoofer</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 10.00,
        20.00, 20.00, 20.00, 2.00, 'soundsticks_iii.png', 4, 28),
       ('Harman Kardon Aura Studio 3', 'aura-studio-3', '<p>Wireless speaker</p>',
        '<p>Harman Kardon Aura Studio 3 with ambient lighting</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 10.00,
        20.00, 20.00, 20.00, 2.00, 'aura_studio_3.png', 4, 28),
       ('Harman Kardon Citation One', 'citation-one', '<p>Smart speaker</p>',
        '<p>Harman Kardon Citation One with Google Assistant</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 10.00,
        20.00, 20.00, 20.00, 2.00, 'citation_one.png', 4, 28),
       ('Harman Kardon Esquire Mini', 'esquire-mini', '<p>Portable speaker</p>',
        '<p>Harman Kardon Esquire Mini with Bluetooth connectivity</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 149.00,
        5.00, 10.00, 10.00, 10.00, 0.50, 'esquire_mini.png', 4, 28),
       ('Harman Kardon Go + Play', 'go-play', '<p>Portable speaker system</p>',
        '<p>Harman Kardon Go + Play with Bluetooth connectivity</p>', '2024-06-17 00:00:00', 1, 1, 399.00, 499.00,
        10.00, 20.00, 20.00, 20.00, 2.00, 'go_play.png', 4, 28);

-- Inserting products for Bose (brand_id = 29)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Bose QuietComfort 35 II', 'qc35-ii', '<p>Noise cancelling headphones</p>',
        '<p>Bose QuietComfort 35 II with Bluetooth connectivity</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00,
        10.00, 20.00, 20.00, 20.00, 0.50, 'qc35_ii.png', 4, 29),
       ('Bose SoundLink Revolve', 'soundlink-revolve', '<p>Portable Bluetooth speaker</p>',
        '<p>Bose SoundLink Revolve with 12-hour battery life</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 10.00,
        10.00, 10.00, 10.00, 0.50, 'soundlink_revolve.png', 4, 29),
       ('Bose Home Speaker 500', 'home-speaker-500', '<p>Smart speaker</p>',
        '<p>Bose Home Speaker 500 with Amazon Alexa</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 10.00, 20.00,
        20.00, 20.00, 2.00, 'home_speaker_500.png', 4, 29),
       ('Bose Soundbar 700', 'soundbar-700', '<p>High-end soundbar</p>',
        '<p>Bose Soundbar 700 with built-in voice control</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00,
        20.00, 20.00, 20.00, 2.00, 'soundbar_700.png', 4, 29),
       ('Bose Frames', 'frames', '<p>Audio sunglasses</p>', '<p>Bose Frames with built-in speakers</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 10.00, 10.00, 10.00, 10.00, 0.50, 'frames.png', 4, 29),
       ('Bose Portable Home Speaker', 'portable-home-speaker', '<p>Smart portable speaker</p>',
        '<p>Bose Portable Home Speaker with Wi-Fi and Bluetooth</p>', '2024-06-17 00:00:00', 1, 1, 349.00, 499.00,
        10.00, 20.00, 20.00, 20.00, 2.00, 'portable_home_speaker.png', 4, 29);

-- Inserting products for Sennheiser (brand_id = 30)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Sennheiser HD 660 S', 'hd-660-s', '<p>High-end headphones</p>',
        '<p>Sennheiser HD 660 S with open-back design</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00, 10.00, 20.00,
        20.00, 20.00, 0.50, 'hd_660_s.png', 4, 30),
       ('Sennheiser Momentum True Wireless 2', 'momentum-tw2', '<p>Wireless earbuds</p>',
        '<p>Sennheiser Momentum True Wireless 2 with active noise cancellation</p>', '2024-06-17 00:00:00', 1, 1,
        299.00, 399.00, 10.00, 10.00, 10.00, 10.00, 0.50, 'momentum_tw2.png', 4, 30),
       ('Sennheiser PXC 550-II', 'pxc-550-ii', '<p>Noise cancelling headphones</p>',
        '<p>Sennheiser PXC 550-II with adaptive noise cancellation</p>', '2024-06-17 00:00:00', 1, 1, 349.00, 499.00,
        10.00, 20.00, 20.00, 20.00, 0.50, 'pxc_550_ii.png', 4, 30),
       ('Sennheiser GSP 670', 'gsp-670', '<p>Wireless gaming headset</p>',
        '<p>Sennheiser GSP 670 with low-latency connection</p>', '2024-06-17 00:00:00', 1, 1, 349.00, 499.00, 10.00,
        20.00, 20.00, 20.00, 0.50, 'gsp_670.png', 4, 30),
       ('Sennheiser IE 800 S', 'ie-800-s', '<p>High-end in-ear headphones</p>',
        '<p>Sennheiser IE 800 S with audiophile sound quality</p>', '2024-06-17 00:00:00', 1, 1, 799.00, 999.00, 10.00,
        10.00, 10.00, 10.00, 0.50, 'ie_800_s.png', 4, 30),
       ('Sennheiser AMBEO Soundbar', 'ambeo-soundbar', '<p>High-end soundbar</p>',
        '<p>Sennheiser AMBEO Soundbar with 3D sound</p>', '2024-06-17 00:00:00', 1, 1, 1999.00, 2499.00, 10.00, 20.00,
        20.00, 20.00, 2.00, 'ambeo_soundbar.png', 4, 30);

-- Inserting products for Logitech (brand_id = 31)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Logitech MX Master 3', 'mx-master-3', '<p>Wireless mouse</p>',
        '<p>Logitech MX Master 3 with ergonomic design</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 129.00, 5.00, 10.00,
        10.00, 10.00, 0.50, 'mx_master_3.png', 4, 31),
       ('Logitech G Pro X', 'g-pro-x', '<p>Gaming headset</p>', '<p>Logitech G Pro X with Blue VO!CE technology</p>',
        '2024-06-17 00:00:00', 1, 1, 129.00, 199.00, 10.00, 20.00, 20.00, 20.00, 0.50, 'g_pro_x.png', 4, 31),
       ('Logitech K780', 'k780', '<p>Multi-device keyboard</p>', '<p>Logitech K780 with Bluetooth connectivity</p>',
        '2024-06-17 00:00:00', 1, 1, 79.00, 99.00, 5.00, 20.00, 20.00, 20.00, 0.50, 'k780.png', 4, 31),
       ('Logitech StreamCam', 'streamcam', '<p>Streaming webcam</p>', '<p>Logitech StreamCam with 1080p resolution</p>',
        '2024-06-17 00:00:00', 1, 1, 149.00, 199.00, 10.00, 10.00, 10.00, 10.00, 0.50, 'streamcam.png', 4, 31),
       ('Logitech G915 TKL', 'g915-tkl', '<p>Wireless gaming keyboard</p>',
        '<p>Logitech G915 TKL with LIGHTSPEED technology</p>', '2024-06-17 00:00:00', 1, 1, 229.00, 299.00, 10.00,
        20.00, 20.00, 20.00, 0.50, 'g915_tkl.png', 4, 31),
       ('Logitech Z906', 'z906', '<p>5.1 Surround Sound Speaker System</p>',
        '<p>Logitech Z906 with THX certification</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 10.00, 50.00, 30.00,
        15.00, 10.00, 'z906.png', 4, 31);

-- Inserting products for Razer (brand_id = 32)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Razer Blade 15', 'blade-15', '<p>Gaming laptop</p>', '<p>Razer Blade 15 with Intel i7 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1699.00, 2199.00, 10.00, 35.00, 25.00, 1.50, 1.40, 'blade_15.png', 4, 32),
       ('Razer BlackWidow Elite', 'blackwidow-elite', '<p>Mechanical gaming keyboard</p>',
        '<p>Razer BlackWidow Elite with RGB lighting</p>', '2024-06-17 00:00:00', 1, 1, 129.00, 179.00, 10.00, 20.00,
        20.00, 20.00, 0.50, 'blackwidow_elite.png', 4, 32),
       ('Razer Naga Pro', 'naga-pro', '<p>Wireless gaming mouse</p>',
        '<p>Razer Naga Pro with interchangeable side plates</p>', '2024-06-17 00:00:00', 1, 1, 149.00, 199.00, 10.00,
        10.00, 10.00, 10.00, 0.50, 'naga_pro.png', 4, 32),
       ('Razer Kraken X', 'kraken-x', '<p>Gaming headset</p>', '<p>Razer Kraken X with 7.1 surround sound</p>',
        '2024-06-17 00:00:00', 1, 1, 49.00, 69.00, 5.00, 10.00, 10.00, 10.00, 0.50, 'kraken_x.png', 4, 32),
       ('Razer Seiren X', 'seiren-x', '<p>USB streaming microphone</p>',
        '<p>Razer Seiren X with supercardioid pickup pattern</p>', '2024-06-17 00:00:00', 1, 1, 79.00, 99.00, 5.00,
        10.00, 10.00, 10.00, 0.50, 'seiren_x.png', 4, 32),
       ('Razer Huntsman Elite', 'huntsman-elite', '<p>Optical gaming keyboard</p>',
        '<p>Razer Huntsman Elite with RGB lighting</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 249.00, 10.00, 20.00,
        20.00, 20.00, 0.50, 'huntsman_elite.png', 4, 32);

-- Inserting products for MSI (brand_id = 33)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('MSI GE66 Raider', 'ge66-raider', '<p>High-performance gaming laptop</p>',
        '<p>MSI GE66 Raider with Intel i9 processor</p>', '2024-06-17 00:00:00', 1, 1, 1999.00, 2499.00, 10.00, 35.00,
        25.00, 1.50, 1.40, 'ge66_raider.png', 4, 33),
       ('MSI Prestige 14', 'prestige-14', '<p>Ultra-portable laptop</p>',
        '<p>MSI Prestige 14 with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1299.00, 1699.00, 10.00, 30.00,
        20.00, 1.20, 1.10, 'prestige_14.png', 4, 33),
       ('MSI GS66 Stealth', 'gs66-stealth', '<p>Gaming laptop</p>', '<p>MSI GS66 Stealth with NVIDIA RTX 2080</p>',
        '2024-06-17 00:00:00', 1, 1, 2199.00, 2699.00, 10.00, 35.00, 25.00, 1.50, 1.40, 'gs66_stealth.png', 4, 33),
       ('MSI Creator 15', 'creator-15', '<p>Content creation laptop</p>',
        '<p>MSI Creator 15 with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1999.00, 2499.00, 10.00, 35.00,
        25.00, 1.50, 1.40, 'creator_15.png', 4, 33),
       ('MSI Alpha 15', 'alpha-15', '<p>Gaming laptop</p>', '<p>MSI Alpha 15 with AMD Ryzen 7 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1199.00, 1499.00, 10.00, 35.00, 25.00, 1.50, 1.40, 'alpha_15.png', 4, 33),
       ('MSI Modern 14', 'modern-14', '<p>Lightweight laptop</p>', '<p>MSI Modern 14 with Intel i5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 799.00, 999.00, 10.00, 30.00, 20.00, 1.20, 1.10, 'modern_14.png', 4, 33);

-- Inserting products for Corsair (brand_id = 34)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Corsair K95 RGB Platinum', 'k95-rgb-platinum', '<p>Mechanical gaming keyboard</p>',
        '<p>Corsair K95 RGB Platinum with customizable lighting</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 249.00,
        10.00, 20.00, 20.00, 20.00, 0.50, 'k95_rgb_platinum.png', 4, 34),
       ('Corsair Virtuoso RGB Wireless', 'virtuoso-rgb-wireless', '<p>Gaming headset</p>',
        '<p>Corsair Virtuoso RGB Wireless with high-fidelity audio</p>', '2024-06-17 00:00:00', 1, 1, 179.00, 229.00,
        10.00, 20.00, 20.00, 20.00, 0.50, 'virtuoso_rgb_wireless.png', 4, 34),
       ('Corsair iCUE H150i Elite Capellix', 'h150i-elite-capellix', '<p>Liquid CPU cooler</p>',
        '<p>Corsair iCUE H150i Elite Capellix with RGB lighting</p>', '2024-06-17 00:00:00', 1, 1, 149.00, 199.00,
        10.00, 20.00, 20.00, 20.00, 0.50, 'h150i_elite_capellix.png', 4, 34),
       ('Corsair One Pro i200', 'one-pro-i200', '<p>Compact gaming PC</p>',
        '<p>Corsair One Pro i200 with Intel i9 processor</p>', '2024-06-17 00:00:00', 1, 1, 2999.00, 3499.00, 10.00,
        35.00, 25.00, 15.00, 10.00, 'one_pro_i200.png', 4, 34);

INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Kingston A2000 NVMe SSD', 'a2000-nvme-ssd', '<p>1TB NVMe SSD</p>',
        '<p>Kingston A2000 NVMe SSD with read speeds up to 2200MB/s</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 129.00,
        5.00, 10.00, 10.00, 10.00, 0.50, 'a2000_nvme_ssd.png', 4, 35),
       ('Kingston DataTraveler 100 G3', 'datatraveler-100-g3', '<p>128GB USB flash drive</p>',
        '<p>Kingston DataTraveler 100 G3 with USB 3.0 support</p>', '2024-06-17 00:00:00', 1, 1, 19.00, 29.00, 5.00,
        5.00, 5.00, 5.00, 0.20, 'datatraveler_100_g3.png', 4, 35),
       ('Kingston FURY Beast 32GB', 'fury-beast-32gb', '<p>DDR4 memory</p>',
        '<p>Kingston FURY Beast 32GB with high-performance speed</p>', '2024-06-17 00:00:00', 1, 1, 159.00, 199.00,
        5.00, 10.00, 10.00, 10.00, 0.50, 'fury_beast_32gb.png', 4, 35),
       ('Kingston KC2500 NVMe SSD', 'kc2500-nvme-ssd', '<p>2TB NVMe SSD</p>',
        '<p>Kingston KC2500 NVMe SSD with read speeds up to 3500MB/s</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00,
        5.00, 10.00, 10.00, 10.00, 0.50, 'kc2500_nvme_ssd.png', 4, 35),
       ('Kingston Canvas Go! Plus', 'canvas-go-plus', '<p>128GB microSD card</p>',
        '<p>Kingston Canvas Go! Plus with up to 170MB/s read speed</p>', '2024-06-17 00:00:00', 1, 1, 29.00, 49.00,
        5.00, 5.00, 5.00, 5.00, 0.10, 'canvas_go_plus.png', 4, 35),
       ('Kingston MobileLite Duo 3C', 'mobilelite-duo-3c', '<p>USB Type-C reader</p>',
        '<p>Kingston MobileLite Duo 3C for microSD cards</p>', '2024-06-17 00:00:00', 1, 1, 19.00, 29.00, 5.00, 5.00,
        5.00, 5.00, 0.10, 'mobilelite_duo_3c.png', 4, 35);

-- Inserting products for Sandisk (brand_id = 36)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Sandisk Extreme Portable SSD', 'extreme-portable-ssd', '<p>1TB portable SSD</p>',
        '<p>Sandisk Extreme Portable SSD with up to 1050MB/s read speed</p>', '2024-06-17 00:00:00', 1, 1, 149.00,
        199.00, 5.00, 10.00, 10.00, 10.00, 0.50, 'extreme_portable_ssd.png', 4, 36),
       ('Sandisk Ultra Dual Drive Luxe', 'ultra-dual-drive-luxe', '<p>128GB USB Type-C flash drive</p>',
        '<p>Sandisk Ultra Dual Drive Luxe with USB 3.1 support</p>', '2024-06-17 00:00:00', 1, 1, 29.00, 49.00, 5.00,
        5.00, 5.00, 5.00, 0.20, 'ultra_dual_drive_luxe.png', 4, 36),
       ('Sandisk Extreme Pro SDXC', 'extreme-pro-sdxc', '<p>128GB SDXC card</p>',
        '<p>Sandisk Extreme Pro SDXC with up to 170MB/s read speed</p>', '2024-06-17 00:00:00', 1, 1, 49.00, 79.00,
        5.00, 5.00, 5.00, 5.00, 0.10, 'extreme_pro_sdxc.png', 4, 36),
       ('Sandisk iXpand Flash Drive Go', 'ixpand-flash-drive-go', '<p>128GB USB flash drive</p>',
        '<p>Sandisk iXpand Flash Drive Go for iPhone and iPad</p>', '2024-06-17 00:00:00', 1, 1, 39.00, 59.00, 5.00,
        5.00, 5.00, 5.00, 0.20, 'ixpand_flash_drive_go.png', 4, 36),
       ('Sandisk Extreme microSDXC', 'extreme-microsdxc', '<p>128GB microSDXC card</p>',
        '<p>Sandisk Extreme microSDXC with up to 160MB/s read speed</p>', '2024-06-17 00:00:00', 1, 1, 29.00, 49.00,
        5.00, 5.00, 5.00, 5.00, 0.10, 'extreme_microsdxc.png', 4, 36),
       ('Sandisk Ultra USB 3.0 Flash Drive', 'ultra-usb-3-0-flash-drive', '<p>128GB USB flash drive</p>',
        '<p>Sandisk Ultra USB 3.0 Flash Drive with up to 100MB/s read speed</p>', '2024-06-17 00:00:00', 1, 1, 19.00,
        29.00, 5.00, 5.00, 5.00, 5.00, 0.20, 'ultra_usb_3_0_flash_drive.png', 4, 36);

-- Inserting products for Western Digital (brand_id = 37)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('WD My Passport 2TB', 'my-passport-2tb', '<p>Portable hard drive</p>',
        '<p>WD My Passport 2TB with USB 3.0 support</p>', '2024-06-17 00:00:00', 1, 1, 79.00, 99.00, 5.00, 10.00, 10.00,
        10.00, 0.50, 'my_passport_2tb.png', 4, 37),
       ('WD Elements 4TB', 'elements-4tb', '<p>Desktop external hard drive</p>',
        '<p>WD Elements 4TB with USB 3.0 support</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 129.00, 5.00, 20.00, 15.00,
        10.00, 0.50, 'elements_4tb.png', 4, 37),
       ('WD Black SN750 NVMe SSD', 'black-sn750-nvme-ssd', '<p>1TB NVMe SSD</p>',
        '<p>WD Black SN750 NVMe SSD with up to 3400MB/s read speed</p>', '2024-06-17 00:00:00', 1, 1, 149.00, 199.00,
        5.00, 10.00, 10.00, 10.00, 0.50, 'black_sn750_nvme_ssd.png', 4, 37),
       ('WD Blue 3D NAND SATA SSD', 'blue-3d-nand-sata-ssd', '<p>1TB SATA SSD</p>',
        '<p>WD Blue 3D NAND SATA SSD with up to 560MB/s read speed</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 129.00,
        5.00, 10.00, 10.00, 10.00, 0.50, 'blue_3d_nand_sata_ssd.png', 4, 37),
       ('WD My Cloud Home 4TB', 'my-cloud-home-4tb', '<p>Personal cloud storage</p>',
        '<p>WD My Cloud Home 4TB with remote access</p>', '2024-06-17 00:00:00', 1, 1, 179.00, 229.00, 5.00, 20.00,
        15.00, 10.00, 0.50, 'my_cloud_home_4tb.png', 4, 37),
       ('WD Purple 6TB', 'purple-6tb', '<p>Surveillance hard drive</p>', '<p>WD Purple 6TB with 64MB cache</p>',
        '2024-06-17 00:00:00', 1, 1, 159.00, 199.00, 5.00, 20.00, 15.00, 10.00, 0.50, 'purple_6tb.png', 4, 37);

-- Inserting products for Seagate (brand_id = 38)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Seagate Backup Plus Slim 2TB', 'backup-plus-slim-2tb', '<p>Portable hard drive</p>',
        '<p>Seagate Backup Plus Slim 2TB with USB 3.0 support</p>', '2024-06-17 00:00:00', 1, 1, 69.00, 99.00, 5.00,
        10.00, 10.00, 10.00, 0.50, 'backup_plus_slim_2tb.png', 4, 38),
       ('Seagate Expansion Desktop 4TB', 'expansion-desktop-4tb', '<p>Desktop external hard drive</p>',
        '<p>Seagate Expansion Desktop 4TB with USB 3.0 support</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 129.00, 5.00,
        20.00, 15.00, 10.00, 0.50, 'expansion_desktop_4tb.png', 4, 38),
       ('Seagate FireCuda 520 NVMe SSD', 'firecuda-520-nvme-ssd', '<p>1TB NVMe SSD</p>',
        '<p>Seagate FireCuda 520 NVMe SSD with up to 5000MB/s read speed</p>', '2024-06-17 00:00:00', 1, 1, 179.00,
        229.00, 5.00, 10.00, 10.00, 10.00, 0.50, 'firecuda_520_nvme_ssd.png', 4, 38),
       ('Seagate Barracuda 4TB', 'barracuda-4tb', '<p>Desktop internal hard drive</p>',
        '<p>Seagate Barracuda 4TB with 256MB cache</p>', '2024-06-17 00:00:00', 1, 1, 89.00, 119.00, 5.00, 20.00, 15.00,
        10.00, 0.50, 'barracuda_4tb.png', 4, 38),
       ('Seagate IronWolf 6TB', 'ironwolf-6tb', '<p>NAS hard drive</p>', '<p>Seagate IronWolf 6TB with 256MB cache</p>',
        '2024-06-17 00:00:00', 1, 1, 179.00, 229.00, 5.00, 20.00, 15.00, 10.00, 0.50, 'ironwolf_6tb.png', 4, 38),
       ('Seagate SkyHawk 8TB', 'skyhawk-8tb', '<p>Surveillance hard drive</p>',
        '<p>Seagate SkyHawk 8TB with 256MB cache</p>', '2024-06-17 00:00:00', 1, 1, 249.00, 299.00, 5.00, 20.00, 15.00,
        10.00, 0.50, 'skyhawk_8tb.png', 4, 38);

-- Inserting products for IBM (brand_id = 39)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('IBM ThinkPad X1 Carbon', 'ibm_thinkpad-x1-carbon', '<p>Ultra-portable laptop</p>',
        '<p>IBM ThinkPad X1 Carbon with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1399.00, 1799.00, 10.00,
        31.00, 21.00, 1.20, 1.10, 'thinkpad_x1_carbon.png', 5, 39),
       ('IBM ThinkPad T14', 'thinkpad-t14', '<p>Business laptop</p>', '<p>IBM ThinkPad T14 with Intel i5 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 1199.00, 1499.00, 10.00, 32.00, 22.00, 1.30, 1.25, 'thinkpad_t14.png', 5, 39),
       ('IBM ThinkPad P53', 'thinkpad-p53', '<p>Mobile workstation</p>',
        '<p>IBM ThinkPad P53 with Intel Xeon processor</p>', '2024-06-17 00:00:00', 1, 1, 1999.00, 2499.00, 10.00,
        34.00, 24.00, 1.50, 1.40, 'thinkpad_p53.png', 5, 39),
       ('IBM ThinkPad L14', 'thinkpad-l14', '<p>Affordable business laptop</p>',
        '<p>IBM ThinkPad L14 with AMD Ryzen 5 processor</p>', '2024-06-17 00:00:00', 1, 1, 899.00, 1199.00, 10.00,
        33.00, 23.00, 1.40, 1.30, 'thinkpad_l14.png', 5, 39),
       ('IBM ThinkCentre M720', 'thinkcentre-m720', '<p>Desktop PC</p>',
        '<p>IBM ThinkCentre M720 with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 799.00, 999.00, 10.00,
        35.00, 25.00, 10.00, 10.00, 'thinkcentre_m720.png', 5, 39),
       ('IBM ThinkStation P330', 'thinkstation-p330', '<p>Workstation PC</p>',
        '<p>IBM ThinkStation P330 with Intel Xeon processor</p>', '2024-06-17 00:00:00', 1, 1, 1499.00, 1999.00, 10.00,
        35.00, 25.00, 10.00, 10.00, 'thinkstation_p330.png', 5, 39);

-- Inserting products for Fujitsu (brand_id = 40)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Fujitsu Lifebook U9310', 'lifebook-u9310', '<p>Ultra-portable laptop</p>',
        '<p>Fujitsu Lifebook U9310 with Intel i7 processor</p>', '2024-06-17 00:00:00', 1, 1, 1299.00, 1699.00, 10.00,
        30.00, 20.00, 1.20, 1.10, 'lifebook_u9310.png', 5, 40),
       ('Fujitsu Lifebook E5410', 'lifebook-e5410', '<p>Business laptop</p>',
        '<p>Fujitsu Lifebook E5410 with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 999.00, 1299.00, 10.00,
        31.00, 21.00, 1.30, 1.25, 'lifebook_e5410.png', 5, 40),
       ('Fujitsu Celsius H980', 'celsius-h980', '<p>Mobile workstation</p>',
        '<p>Fujitsu Celsius H980 with Intel Xeon processor</p>', '2024-06-17 00:00:00', 1, 1, 1999.00, 2499.00, 10.00,
        34.00, 24.00, 1.50, 1.40, 'celsius_h980.png', 5, 40),
       ('Fujitsu Lifebook A3510', 'lifebook-a3510', '<p>Affordable business laptop</p>',
        '<p>Fujitsu Lifebook A3510 with Intel i3 processor</p>', '2024-06-17 00:00:00', 1, 1, 599.00, 799.00, 10.00,
        32.00, 22.00, 1.40, 1.30, 'lifebook_a3510.png', 5, 40),
       ('Fujitsu Esprimo D7010', 'esprimo-d7010', '<p>Desktop PC</p>',
        '<p>Fujitsu Esprimo D7010 with Intel i5 processor</p>', '2024-06-17 00:00:00', 1, 1, 799.00, 999.00, 10.00,
        35.00, 25.00, 10.00, 10.00, 'esprimo_d7010.png', 5, 40),
       ('Fujitsu Celsius W580', 'celsius-w580', '<p>Workstation PC</p>',
        '<p>Fujitsu Celsius W580 with Intel Xeon processor</p>', '2024-06-17 00:00:00', 1, 1, 1499.00, 1999.00, 10.00,
        35.00, 25.00, 10.00, 10.00, 'celsius_w580.png', 5, 40);

-- Inserting products for Alcatel (brand_id = 41)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Alcatel 3X', '3x', '<p>Affordable smartphone</p>', '<p>Alcatel 3X with MediaTek Helio P23 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 129.00, 199.00, 5.00, 16.00, 7.50, 0.80, 0.50, '3x.png', 6, 41),
       ('Alcatel 1S', '1s', '<p>Budget smartphone</p>', '<p>Alcatel 1S with Unisoc SC9863A processor</p>',
        '2024-06-17 00:00:00', 1, 1, 99.00, 149.00, 5.00, 16.00, 7.50, 0.80, 0.50, '1s.png', 6, 41),
       ('Alcatel 3L', '3l', '<p>Mid-range smartphone</p>', '<p>Alcatel 3L with MediaTek Helio P22 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 149.00, 199.00, 5.00, 16.00, 7.50, 0.80, 0.50, '3l.png', 6, 41),
       ('Alcatel 1B', '1b', '<p>Entry-level smartphone</p>', '<p>Alcatel 1B with Qualcomm QM215 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 79.00, 129.00, 5.00, 16.00, 7.50, 0.80, 0.50, '1b.png', 6, 41),
       ('Alcatel 5V', '5v', '<p>High-performance smartphone</p>', '<p>Alcatel 5V with MediaTek Helio P22 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 249.00, 10.00, 16.00, 7.50, 0.80, 0.50, '5v.png', 6, 41),
       ('Alcatel 3T 10', '3t-10', '<p>Affordable tablet</p>', '<p>Alcatel 3T 10 with MediaTek MT8765B processor</p>',
        '2024-06-17 00:00:00', 1, 1, 149.00, 199.00, 5.00, 25.00, 16.00, 0.60, 0.45, '3t_10.png', 6, 41);

-- Inserting products for ZTE (brand_id = 42)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('ZTE Axon 20 5G', 'axon-20-5g', '<p>Mid-range 5G smartphone</p>',
        '<p>ZTE Axon 20 5G with Snapdragon 765G processor</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'axon_20_5g.png', 6, 42),
       ('ZTE Blade A7 Prime', 'blade-a7-prime', '<p>Affordable smartphone</p>',
        '<p>ZTE Blade A7 Prime with MediaTek Helio A22 processor</p>', '2024-06-17 00:00:00', 1, 1, 129.00, 199.00,
        5.00, 16.00, 7.50, 0.80, 0.50, 'blade_a7_prime.png', 6, 42),
       ('ZTE Nubia Red Magic 5G', 'nubia-red-magic-5g', '<p>Gaming smartphone</p>',
        '<p>ZTE Nubia Red Magic 5G with Snapdragon 865 processor</p>', '2024-06-17 00:00:00', 1, 1, 499.00, 699.00,
        10.00, 16.00, 7.50, 0.80, 0.50, 'nubia_red_magic_5g.png', 6, 42),
       ('ZTE Blade V2020', 'blade-v2020', '<p>Mid-range smartphone</p>',
        '<p>ZTE Blade V2020 with MediaTek Helio P70 processor</p>', '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'blade_v2020.png', 6, 42),
       ('ZTE Axon 10 Pro', 'axon-10-pro', '<p>High-end smartphone</p>',
        '<p>ZTE Axon 10 Pro with Snapdragon 855 processor</p>', '2024-06-17 00:00:00', 1, 1, 399.00, 599.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'axon_10_pro.png', 6, 42),
       ('ZTE Blade 10 Smart', 'blade-10-smart', '<p>Entry-level smartphone</p>',
        '<p>ZTE Blade 10 Smart with Unisoc SC9863A processor</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 149.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'blade_10_smart.png', 6, 42);

-- Inserting products for Realme (brand_id = 43)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Realme 7 Pro', 'realme_7-pro', '<p>Mid-range smartphone</p>', '<p>Realme 7 Pro with Snapdragon 720G processor</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 299.00, 5.00, 16.00, 7.50, 0.80, 0.50, '7_pro.png', 6, 43),
       ('Realme X50 Pro 5G', 'x50-pro-5g', '<p>High-performance 5G smartphone</p>',
        '<p>Realme X50 Pro 5G with Snapdragon 865 processor</p>', '2024-06-17 00:00:00', 1, 1, 399.00, 599.00, 10.00,
        16.00, 7.50, 0.80, 0.50, 'x50_pro_5g.png', 6, 43),
       ('Realme C15', 'c15', '<p>Affordable smartphone</p>', '<p>Realme C15 with MediaTek Helio G35 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 129.00, 199.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'c15.png', 6, 43),
       ('Realme Narzo 20', 'narzo-20', '<p>Mid-range smartphone</p>',
        '<p>Realme Narzo 20 with MediaTek Helio G85 processor</p>', '2024-06-17 00:00:00', 1, 1, 149.00, 199.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'narzo_20.png', 6, 43),
       ('Realme 8', '8', '<p>Mid-range smartphone</p>', '<p>Realme 8 with MediaTek Helio G95 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 249.00, 5.00, 16.00, 7.50, 0.80, 0.50, '8.png', 6, 43),
       ('Realme 6', '6', '<p>Mid-range smartphone</p>', '<p>Realme 6 with MediaTek Helio G90T processor</p>',
        '2024-06-17 00:00:00', 1, 1, 179.00, 229.00, 5.00, 16.00, 7.50, 0.80, 0.50, '6.png', 6, 43);

-- Inserting products for Tecno (brand_id = 44)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Tecno Camon 16', 'camon-16', '<p>Mid-range smartphone</p>',
        '<p>Tecno Camon 16 with MediaTek Helio G70 processor</p>', '2024-06-17 00:00:00', 1, 1, 149.00, 199.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'camon_16.png', 6, 44),
       ('Tecno Spark 6', 'spark-6', '<p>Affordable smartphone</p>',
        '<p>Tecno Spark 6 with MediaTek Helio A25 processor</p>', '2024-06-17 00:00:00', 1, 1, 99.00, 149.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'spark_6.png', 6, 44),
       ('Tecno Pouvoir 4', 'pouvoir-4', '<p>Affordable smartphone</p>',
        '<p>Tecno Pouvoir 4 with MediaTek Helio A22 processor</p>', '2024-06-17 00:00:00', 1, 1, 89.00, 129.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'pouvoir_4.png', 6, 44),
       ('Tecno Phantom X', 'phantom-x', '<p>High-end smartphone</p>',
        '<p>Tecno Phantom X with MediaTek Helio G95 processor</p>', '2024-06-17 00:00:00', 1, 1, 299.00, 399.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'phantom_x.png', 6, 44),
       ('Tecno Spark 7', 'spark-7', '<p>Budget smartphone</p>',
        '<p>Tecno Spark 7 with MediaTek Helio A25 processor</p>', '2024-06-17 00:00:00', 1, 1, 79.00, 129.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'spark_7.png', 6, 44),
       ('Tecno Camon 15', 'camon-15', '<p>Mid-range smartphone</p>',
        '<p>Tecno Camon 15 with MediaTek Helio P22 processor</p>', '2024-06-17 00:00:00', 1, 1, 129.00, 199.00, 5.00,
        16.00, 7.50, 0.80, 0.50, 'camon_15.png', 6, 44);

-- Inserting products for Lava (brand_id = 45)
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price,
                      discount_percent, length, width, height, weight, main_image, category_id, brand_id)
VALUES ('Lava Z6', 'z6', '<p>Mid-range smartphone</p>', '<p>Lava Z6 with MediaTek Helio G35 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 149.00, 199.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'z6.png', 6, 45),
       ('Lava Z4', 'z4', '<p>Mid-range smartphone</p>', '<p>Lava Z4 with MediaTek Helio G35 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 129.00, 179.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'z4.png', 6, 45),
       ('Lava Z2 Max', 'z2-max', '<p>Affordable smartphone</p>', '<p>Lava Z2 Max with MediaTek Helio A20 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 99.00, 149.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'z2_max.png', 6, 45),
       ('Lava Agni 5G', 'agni-5g', '<p>5G smartphone</p>', '<p>Lava Agni 5G with MediaTek Dimensity 810 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 199.00, 249.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'agni_5g.png', 6, 45),
       ('Lava Z66', 'z66', '<p>Affordable smartphone</p>', '<p>Lava Z66 with Unisoc SC9863A processor</p>',
        '2024-06-17 00:00:00', 1, 1, 99.00, 149.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'z66.png', 6, 45),
       ('Lava Z1', 'z1', '<p>Budget smartphone</p>', '<p>Lava Z1 with MediaTek Helio A20 processor</p>',
        '2024-06-17 00:00:00', 1, 1, 79.00, 129.00, 5.00, 16.00, 7.50, 0.80, 0.50, 'z1.png', 6, 45);

