INSERT INTO users (email, first_name, last_name, user_password, main_image, enabled)
VALUES ('john.doe@example.com', 'John', 'Doe', 'password123', 'john_doe.png', 1),
       ('jane.smith@example.com', 'Jane', 'Smith', 'password456', 'jane_smith.png', 1),
       ('alice.jones@example.com', 'Alice', 'Jones', 'password789', 'alice_jones.png', 0),
       ('bob.brown@example.com', 'Bob', 'Brown', 'password101', 'bob_brown.png', 0),
       ('charlie.davis@example.com', 'Charlie', 'Davis', 'password102', 'charlie_davis.png', 1),
       ('diana.miller@example.com', 'Diana', 'Miller', 'password103', 'diana_miller.png', 1),
       ('edward.wilson@example.com', 'Edward', 'Wilson', 'password104', 'edward_wilson.png', 0),
       ('fiona.moore@example.com', 'Fiona', 'Moore', 'password105', 'fiona_moore.png', 1),
       ('george.clark@example.com', 'George', 'Clark', 'password106', 'george_clark.png', 1),
       ('hannah.lewis@example.com', 'Hannah', 'Lewis', 'password107', 'hannah_lewis.png', 0);

INSERT INTO users_roles (user_id, role_id)
VALUES (1, 1),
       (1, 2), -- John Doe: ROLE_ADMIN, ROLE_SALESPERSON
       (2, 2), -- Jane Smith: ROLE_SALESPERSON
       (3, 3), -- Alice Jones: ROLE_EDITOR
       (4, 4), -- Bob Brown: ROLE_SHIPPER
       (5, 2),
       (5, 3), -- Charlie Davis: ROLE_SALESPERSON, ROLE_EDITOR
       (6, 1),
       (6, 4), -- Diana Miller: ROLE_ADMIN, ROLE_SHIPPER
       (7, 5), -- Edward Wilson: ROLE_ASSISTANT
       (8, 3),
       (8, 5), -- Fiona Moore: ROLE_EDITOR, ROLE_ASSISTANT
       (9, 2), -- George Clark: ROLE_SALESPERSON
       (10, 1),
       (10, 5);-- Hannah Lewis: ROLE_ADMIN, ROLE_ASSISTANT