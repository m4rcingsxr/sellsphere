-- Insert categories with explicit IDs
INSERT INTO categories (id, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES (1, 'Electronics', 'electronics', 'electronics.png', 1, NULL, NULL);

INSERT INTO categories (id, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES (2, 'Computers', 'computers', 'computers.png', 1, 1, '-1-'),
       (3, 'Computer Components', 'computer_components', 'computer components.png', 1, 2, '-1-2-'),
       (4, 'CPU Processors Unit', 'computer_processors', 'computer processors.png', 1, 3, '-1-2-3-'),
       (5, 'Graphic Cards', 'computer_graphic_cards', 'graphic cards.png', 1, 3, '-1-2-3-'),
       (6, 'Internal Hard Drives', 'hard_drive', 'internal hard drive.png', 1, 3, '-1-2-3-'),
       (7, 'Internal Optical Drives', 'computer_optical_drives', 'internal optical drives.png', 1, 3, '-1-2-3-'),
       (8, 'Power Supplies', 'computer_power_supplies', 'power supplies.png', 1, 3, '-1-2-3-'),
       (9, 'Solid State Drives', 'solid_state_drives', 'solid state drives.png', 1, 3, '-1-2-3-'),
       (10, 'Sound Cards', 'computer_sound_cards', 'sound cards.png', 1, 3, '-1-2-3-'),
       (11, 'Memory', 'computer_memory', 'computer memory.png', 1, 3, '-1-2-3-'),
       (12, 'Motherboard', 'computer_motherboard', 'motherboards.png', 1, 3, '-1-2-3-'),
       (13, 'Network Cards', 'computer_network_cards', 'network cards.png', 1, 3, '-1-2-3-');

-- Insert vacuum cleaners with explicit ID
INSERT INTO categories (id, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES (14, 'Vacuum cleaners', 'vacuum_cleaners', 'vacuum_cleaners.png', 1, NULL, NULL);

-- Insert into category_icons with explicit category_id
INSERT INTO category_icons (id, icon_path, category_id)
VALUES (1, '<i>icon</i>', 1);

-- Reset AUTO_INCREMENT value for the categories table to 15 (next available ID)
ALTER TABLE categories ALTER COLUMN id RESTART WITH 15;

-- Reset AUTO_INCREMENT value for the category_icons table (next available ID should be 2)
ALTER TABLE category_icons ALTER COLUMN id RESTART WITH 2;
