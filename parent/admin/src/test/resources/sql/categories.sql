INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Electronics', 'electronics', 'electronics.png', 1, NULL, NULL);

INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Computers', 'computers', 'computers.png', 1, 1, '-1-'),
       ('Computer Components', 'computer_components', 'computer components.png', 1, 2, '-1-2-'),
       ('CPU Processors Unit', 'computer_processors', 'computer processors.png', 1, 3, '-1-2-3-'),
       ('Graphic Cards', 'computer_graphic_cards', 'graphic cards.png', 1, 3, '-1-2-3-'),
       ('Internal Hard Drives', 'hard_drive', 'internal hard drive.png', 1, 3, '-1-2-3-'),
       ('Internal Optical Drives', 'computer_optical_drives', 'internal optical drives.png', 1, 3, '-1-2-3-'),
       ('Power Supplies', 'computer_power_supplies', 'power supplies.png', 1, 3, '-1-2-3-'),
       ('Solid State Drives', 'solid_state_drives', 'solid state drives.png', 1, 3, '-1-2-3-'),
       ('Sound Cards', 'computer_sound_cards', 'sound cards.png', 1, 3, '-1-2-3-'),
       ('Memory', 'computer_memory', 'computer memory.png', 1, 3, '-1-2-3-'),
       ('Motherboard', 'computer_motherboard', 'motherboards.png', 1, 3, '-1-2-3-'),
       ('Network Cards', 'computer_network_cards', 'network cards.png', 1, 3, '-1-2-3-');

INSERT INTO category_icons (icon_path, category_id)
VALUES ('<i>icon</i>', 1);