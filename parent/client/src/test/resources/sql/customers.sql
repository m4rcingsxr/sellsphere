INSERT INTO customers (email, customer_password, first_name, last_name, enabled, email_verified, created_time)
VALUES ('alice.smith@example.com', 'password123', 'Alice', 'Smith', 1, 1, CURRENT_TIMESTAMP),
       ('bob.johnson@example.com', 'password123', 'Bob', 'Johnson', 1, 1, CURRENT_TIMESTAMP),
       ('carol.wilson@example.com', 'password123', 'Carol', 'Wilson', 1, 1, CURRENT_TIMESTAMP),
       ('dave.brown@example.com', 'password123', 'Dave', 'Brown', 1, 1, CURRENT_TIMESTAMP),
       ('eve.jones@example.com', 'password123', 'Eve', 'Jones', 1, 1, CURRENT_TIMESTAMP),
       ('frank.davis@example.com', 'password123', 'Frank', 'Davis', 1, 1, CURRENT_TIMESTAMP),
       ('grace.miller@example.com', 'password123', 'Grace', 'Miller', 1, 1, CURRENT_TIMESTAMP);

INSERT INTO countries (name, code) VALUES ('Albania', 'AL');

