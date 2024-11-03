-- Inserting Review Records
INSERT INTO reviews (rate, headline, cmt, review_time, approved, votes, product_id, customer_id)
VALUES (5, 'Amazing quality!', 'The Canon EOS 90D exceeded my expectations. Perfect for high-speed photography!', '2023-07-10', 1, 10, 1, (SELECT id FROM customers WHERE email = 'john.doe@example.com')),
       (4, 'Very handy camera', 'Great for travel, light and compact.', '2023-07-15', 1, 8, 2, (SELECT id FROM customers WHERE email = 'john.doe@example.com')),
       (5, 'Fantastic resolution', 'Sony A7R IV offers unmatched image quality. Truly worth the price.', '2023-08-02', 1, 15, 3, (SELECT id FROM customers WHERE email = 'john.doe@example.com')),
       (3, 'Good for gaming', 'HP Pavilion Gaming Desktop works well, but could use more RAM.', '2023-09-01', 1, 6, 4, (SELECT id FROM customers WHERE email = 'john.doe@example.com')),
       (4, 'Fast SSD', 'The SanDisk Ultra SSD performs well, although the price could be lower.', '2023-09-10', 1, 4, 5, (SELECT id FROM customers WHERE email = 'john.doe@example.com'));
