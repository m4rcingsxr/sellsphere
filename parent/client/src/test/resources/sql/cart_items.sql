-- Insert into cart_items for existing customer (John Doe) and products
INSERT INTO cart_items (customer_id, product_id, quantity)
VALUES
    (1, 1, 2),  -- Canon EOS 90D, quantity 2
    (1, 2, 1),  -- Fujifilm X-T30, quantity 1
    (1, 3, 3),  -- Sony A7R IV, quantity 3
    (1, 4, 1),  -- HP Pavilion Gaming Desktop, quantity 1
    (1, 5, 5);  -- SanDisk Ultra SSD, quantity 5
