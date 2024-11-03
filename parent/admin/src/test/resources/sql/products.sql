INSERT INTO product_taxes (id, name, type, description)
VALUES ('1','Standard Tax', 'DIGITAL', 'Standard VAT tax applicable in most countries'),
       ('2','Reduced Tax', 'DIGITAL', 'Reduced VAT tax applicable for specific categories'),
       ('3','Exempt Tax', 'DIGITAL', 'Tax exemption for special products or categories');


-- Inserting Products
INSERT INTO products (name, alias, short_description, full_description, created_time, enabled, in_stock, cost, price, discount_percent, length, width, height, weight, main_image, category_id, brand_id, tax_id, contains_baterry_pi966, contains_baterry_pi967, contains_liquids, hs_code, review_count, average_rating, question_count)
VALUES ('Canon EOS 90D', 'canon_eos_90d', 'High-quality DSLR camera', 'Canon EOS 90D offers high-speed performance and professional-grade images.', NOW(), 1, 1, 999.99, 1200.00, 10.00, 15.00, 20.00, 10.00, 1.50, 'canon_eos_90d_main.jpg', 2, 1, '1', 0, 0, 0, '12345678', 5, 4.8, 10),
       ('Fujifilm X-T30', 'fujifilm_xt30', 'Compact and lightweight camera', 'Fujifilm X-T30 offers the perfect balance of power and portability.', NOW(), 1, 1, 799.99, 950.00, 15.00, 10.00, 15.00, 8.00, 1.20, 'fujifilm_xt30_main.jpg', 2, 2, '2', 0, 0, 0, '12345678', 3, 4.5, 8),
       ('Sony A7R IV', 'sony_a7r_iv', 'Full-frame mirrorless camera', 'Sony A7R IV offers excellent image resolution with 61 megapixels.', NOW(), 1, 1, 2999.99, 3500.00, 5.00, 12.00, 18.00, 9.00, 2.00, 'sony_a7r_iv_main.jpg', 2, 3, '1', 0, 0, 0, '12345678', 7, 4.7, 15),
       ('HP Pavilion Gaming Desktop', 'hp_gaming_desktop', 'Powerful gaming desktop', 'HP Pavilion gaming desktop offers superior performance for high-end gaming.', NOW(), 1, 1, 1099.99, 1300.00, 8.00, 40.00, 20.00, 35.00, 10.00, 'hp_gaming_desktop_main.jpg', 3, 4, '2', 0, 0, 0, '12345678', 6, 4.6, 12),
       ('SanDisk Ultra SSD', 'sandisk_ultra_ssd', 'Fast and reliable SSD', 'SanDisk Ultra SSD offers fast read and write speeds for quick data access.', NOW(), 1, 1, 199.99, 250.00, 20.00, 10.00, 6.00, 0.50, 0.20, 'sandisk_ultra_ssd_main.jpg', 9, 5, '3', 0, 0, 0, '12345678', 4, 4.4, 6);


-- Inserting Product Images
INSERT INTO product_images (name, product_id) VALUES
                                                  ('canon_eos_90d_img1.jpg', 1),
                                                  ('canon_eos_90d_img2.jpg', 1),
                                                  ('fujifilm_xt30_img1.jpg', 2),
                                                  ('fujifilm_xt30_img2.jpg', 2),
                                                  ('sony_a7r_iv_img1.jpg', 3),
                                                  ('sony_a7r_iv_img2.jpg', 3),
                                                  ('hp_gaming_desktop_img1.jpg', 4),
                                                  ('hp_gaming_desktop_img2.jpg', 4),
                                                  ('sandisk_ultra_ssd_img1.jpg', 5),
                                                  ('sandisk_ultra_ssd_img2.jpg', 5);

-- Inserting Product Details
INSERT INTO product_details (name, detail_value, product_id) VALUES
                                                                 ('Megapixels', '32.5 MP', 1),
                                                                 ('Lens Mount', 'EF-S', 1),
                                                                 ('Video Resolution', '4K UHD', 1),
                                                                 ('Megapixels', '26.1 MP', 2),
                                                                 ('Sensor Type', 'APS-C', 2),
                                                                 ('Video Resolution', '4K UHD', 2),
                                                                 ('Megapixels', '61 MP', 3),
                                                                 ('Lens Mount', 'E-Mount', 3),
                                                                 ('Video Resolution', '4K UHD', 3),
                                                                 ('Processor', 'Intel Core i7', 4),
                                                                 ('Graphics Card', 'NVIDIA GeForce GTX 1660', 4),
                                                                 ('RAM', '16 GB DDR4', 4),
                                                                 ('Storage', '512 GB SSD', 4),
                                                                 ('Capacity', '1 TB', 5),
                                                                 ('Read Speed', '560 MB/s', 5),
                                                                 ('Write Speed', '530 MB/s', 5);

