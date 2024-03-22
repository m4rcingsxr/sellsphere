INSERT INTO users_roles (user_id, role_id)
    VALUE
    (1, 1), -- admin@example.com -> ROLE_ADMIN
    (1, 3), -- admin@example.com -> ROLE_EDITOR

    (2, 2), -- jane.doe@example.com -> ROLE_SALESPERSON
    (2, 4), -- jane.doe@example.com -> ROLE_SHIPPER

    (3, 3), -- alice@example.com -> ROLE_EDITOR

    (4, 4), -- bob@example.com -> ROLE_SHIPPER

    (5, 5), -- charlie@example.com -> ROLE_ASSISTANT

    (6, 2), -- david@example.com -> ROLE_SALESPERSON

    (7, 3), -- eva@example.com -> ROLE_EDITOR

    (8, 4), -- frank@example.com -> ROLE_SHIPPER
    (8, 5), -- frank@example.com -> ROLE_ASSISTANT

    (9, 5), -- grace@example.com -> ROLE_ASSISTANT

    (10, 2), -- harry@example.com -> ROLE_SALESPERSON

    (11, 3), -- isabella@example.com -> ROLE_EDITOR

    (12, 4), -- jackson@example.com -> ROLE_SHIPPER

    (13, 5), -- karen@example.com -> ROLE_ASSISTANT

    (14, 2), -- larry@example.com -> ROLE_SALESPERSON


    (15, 3), -- mary@example.com -> ROLE_EDITOR
    (15, 5), -- mary@example.com -> ROLE_ASSISTANT

    (16, 4), -- nick@example.com -> ROLE_SHIPPER

    (17, 5), -- olivia@example.com -> ROLE_ASSISTANT

    (18, 2), -- paul@example.com -> ROLE_SALESPERSON

    (19, 3), -- quinn@example.com -> ROLE_EDITOR

    (20, 4), -- rachel@example.com -> ROLE_SHIPPER

    (21, 5), -- steve@example.com -> ROLE_ASSISTANT
    (21, 2), -- steve@example.com -> ROLE_SALESPERSON

    (22, 5), -- tina@example.com -> ROLE_ASSISTANT
    (23, 4); -- uma@example.com -> ROLE_SHIPPER