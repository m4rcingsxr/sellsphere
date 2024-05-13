-- Insert Countries
INSERT INTO countries (name, code)
VALUES ('United States', 'USA'),
       ('Canada', 'CAN'),
       ('United Kingdom', 'GBR');

-- Insert Customers
INSERT INTO customers (email, customer_password, first_name, last_name, enabled, email_verified, created_time)
VALUES ('john.doe@example.com', 'password123', 'John', 'Doe', true, true, '2023-01-01T00:00:00'),
       ('jane.smith@example.com', 'password456', 'Jane', 'Smith', true, true, '2023-01-01T00:00:00'),
       ('alice.jones@example.com', 'password789', 'Alice', 'Jones', true, true, '2023-01-01T00:00:00'),
       ('bob.brown@example.com', 'passwordabc', 'Bob', 'Brown', true, true, '2023-01-01T00:00:00'),
       ('carol.white@example.com', 'passworddef', 'Carol', 'White', true, true, '2023-01-01T00:00:00');

-- Insert Addresses
INSERT INTO addresses (first_name, last_name, phone_number, address_line_1, address_line_2, city, state, postal_code,
                       primary_address, customer_id, country_id)
VALUES ('John', 'Doe', '123-456-7890', '123 Main St', 'Apt 4', 'New York', 'NY', '10001', true, 1, 1),
       ('Jane', 'Smith', '234-567-8901', '456 Oak St', null, 'Toronto', 'ON', 'M5H 2N2', true, 2, 2),
       ('Alice', 'Jones', '345-678-9012', '789 Pine St', 'Suite 2', 'London', null, 'EC1A 1AA', true, 3, 3),
       ('Bob', 'Brown', '456-789-0123', '101 Maple St', null, 'Los Angeles', 'CA', '90001', true, 4, 1),
       ('Carol', 'White', '567-890-1234', '202 Birch St', 'Unit 1', 'Vancouver', 'BC', 'V6B 1V1', true, 5, 2);