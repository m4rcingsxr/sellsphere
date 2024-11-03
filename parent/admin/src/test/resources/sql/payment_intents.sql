-- Insert Currencies
INSERT INTO currencies (name, symbol, code, unit_amount)
VALUES ('PLN', 'zł', 'PLN', 100);

-- Insert Countries
INSERT INTO countries (name, code, currency_id)
VALUES ('Poland', 'PL', 1);

-- Insert Customers
INSERT INTO customers (email, password, first_name, last_name, enabled, email_verified, created_time)
VALUES ('john.doe@example.com', 'password123', 'John', 'Doe', true, true, '2023-01-01 00:00:00');

-- Insert Addresses (Ensure customer_id and country_id exist)
INSERT INTO addresses (first_name, last_name, phone_number, address_line_1, address_line_2, city, state, postal_code, primary_address, customer_id, country_id)
VALUES ('John', 'Doe', '123-456-7890', '123 Main St', 'Apt 4', 'Warsaw', 'Lower Silesia', '01-200', true, 1, 1);

-- Insert Couriers
INSERT INTO couriers (courier_name, courier_logo_url, min_delivery_time, max_delivery_time)
VALUES ('FedEx', 'https://example.com/logos/fedex.png', 2, 5),
       ('DHL', 'https://example.com/logos/dhl.png', 1, 4);

-- Insert Payment Methods (Ensure customer_id exists)
INSERT INTO payment_methods (stripe_id, customer_id, payment_type)
VALUES ('pm_stripe_12345', 1, 'card'),
       ('pm_stripe_54321', 1, 'bank_transfer');

-- Insert Balance Transactions (Ensure currency_id exists)
INSERT INTO balance_transactions (stripe_id, amount, created, fee, net, currency_id)
VALUES ('bt_stripe_12345', 5000, 1698060900, 150, 4850, 1),
       ('bt_stripe_54321', 7000, 1698061500, 200, 6800, 1);

-- Insert Payment Intents (Ensure foreign key references are valid)
-- Insert Payment Intents
INSERT INTO payment_intents (stripe_id, amount, customer_id, order_id, status, created, canceled_at, cancellation_reason, payment_method_id, address_id, courier_id, shipping_amount, shipping_tax, target_currency_id, tax_amount)
VALUES
    ('pi_stripe_12345', 5000, 1, NULL, 'succeeded', 1698060900, NULL, NULL, 1, 1, 1, 100, 50, 1, 300),
    ('pi_stripe_54321', 7000, 1, NULL, 'pending', 1698061500, NULL, NULL, 2, 1, 1, 150, 60, 1, 400);


-- Insert Charges for Payment Intents (Ensure balance_transaction_id and payment_intent_id exist)
INSERT INTO charges (stripe_id, amount, amount_refunded, balance_transaction_id, receipt_url, status, payment_intent_id, refuned)
VALUES ('ch_stripe_12345', 5000, 0, 1, 'https://example.com/receipt_12345', 'captured', 1, 0),
       ('ch_stripe_54321', 7000, 0, 2, 'https://example.com/receipt_54321', 'authorized', 2, 0);
