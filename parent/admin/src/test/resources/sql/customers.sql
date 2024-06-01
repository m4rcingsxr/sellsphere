-- Insert Countries
INSERT INTO currencies (name, symbol, code, unit_amount) values ('PLN', 'zł', 'PLN', 100);
INSERT INTO countries (name, code, currency_id) VALUES ('Poland', 'PL', 1);

-- Insert Customers
INSERT INTO customers (email, customer_password, first_name, last_name, enabled, email_verified, created_time)
VALUES ('john.doe@example.com', 'password123', 'John', 'Doe', true, true, '2023-01-01T00:00:00');

-- Insert Addresses
INSERT INTO addresses (first_name, last_name, phone_number, address_line_1, address_line_2, city, state, postal_code,
                       primary_address, customer_id, country_id)
VALUES ('John', 'Doe', '123-456-7890', '123 Main St', 'Apt 4', 'Warsaw', 'Lower Silesia', '01-200', true, 1, 1);
