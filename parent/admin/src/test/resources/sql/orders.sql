-- Insert Orders
INSERT INTO orders (id, order_time, payment_intent_id)
VALUES (1, '2023-01-01 10:00:00', 1),
       (2, '2023-01-02 11:30:00', 2);

-- Insert Order Details
INSERT INTO order_details (quantity, product_cost, unit_price, subtotal, product_id, order_id)
VALUES (1, 999.99, 1200.00, 1200.00, 1, 1),
       (2, 799.99, 950.00, 1900.00, 2, 1),
       (1, 2999.99, 3500.00, 3500.00, 3, 2),
       (1, 1099.99, 1300.00, 1300.00, 4, 2);

-- Insert Order Tracks
INSERT INTO order_track (notes, updated_time, status, order_id)
VALUES
-- Order 1
('Order received', '2023-01-01', 'NEW', 1),
('Payment completed', '2023-01-01', 'PAID', 1),
('Order is being processed', '2023-01-01', 'PROCESSING', 1),
('Items packaged', '2023-01-02', 'PACKAGED', 1),
('Items picked for shipping', '2023-01-03', 'PICKED', 1),
('Order shipped', '2023-01-04', 'SHIPPING', 1),
('Order delivered to customer', '2023-01-05', 'DELIVERED', 1),

-- Order 2
('Order received', '2023-01-02', 'NEW', 2),
('Payment completed', '2023-01-02', 'PAID', 2),
('Order is being processed', '2023-01-02', 'PROCESSING', 2),
('Items packaged', '2023-01-03', 'PACKAGED', 2),
('Items picked for shipping', '2023-01-03', 'PICKED', 2),
('Order shipped', '2023-01-04', 'SHIPPING', 2),
('Order delivered to customer', '2023-01-05', 'DELIVERED', 2);
