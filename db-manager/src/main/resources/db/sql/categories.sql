-- Root Category (lvl 0)
INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Laptops', 'laptops', 'laptops.png', 1, NULL, NULL);

-- Categories under 'Laptops' (lvl 1)
INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Laptops and Tablets', 'laptops and tablets', 'laptop_and_tablets.png', 1, 1, '-1-'),
       ('Laptop Accessories', 'laptop accessories', 'laptop_accessories.png', 1, 1, '-1-'),
       ('Laptop Components', 'laptop components', 'laptop_components.png', 1, 1, '-1-'),
       ('Tablet Accessories', 'tablet accessories', 'tablet_accessories.png', 1, 1, '-1-');

-- Subcategories under 'Laptops and Tablets' (lvl 2)
INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Laptop', 'laptop', 'laptops.png', 1, 2, '-1-2-'),
       ('2-in-1 Laptops', '2-in-1 laptops', '2-in-1_laptops.png', 1, 2, '-1-2-'),
       ('Used Laptops', 'used laptops', 'used_laptops.png', 1, 2, '-1-2-'),
       ('Tablets', 'tablets', 'tablets.png', 1, 2, '-1-2-'),
       ('Used Tablets', 'used tablets', 'used_tablets.png', 1, 2, '-1-2-'),
       ('E-book Readers', 'e-book readers', 'e-book_readers.png', 1, 2, '-1-2-');

-- Subcategories under 'Laptop Accessories' (lvl 2)
INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Laptop Batteries', 'laptop batteries', 'laptop_batteries.png', 1, 4, '-1-3-'),
       ('External SSDs', 'external ssds', 'external_ssds.png', 1, 4, '-1-3-'),
       ('Laptop Cases', 'laptop cases', 'laptop_cases.png', 1, 4, '-1-3-'),
       ('Screen Filters', 'screen filters', 'screen_filters.png', 1, 4, '-1-3-'),
       ('USB Lamps', 'usb lamps', 'usb_lamps.png', 1, 4, '-1-3-'),
       ('Security Cables', 'security cables', 'security_cables.png', 1, 4, '-1-3-'),
       ('Laptop Backpacks', 'laptop backpacks', 'laptop_backpacks.png', 1, 4, '-1-3-'),
       ('Cooling Pads', 'cooling pads', 'cooling_pads.png', 1, 4, '-1-3-'),
       ('Laptop Stands', 'laptop stands', 'laptop_stands.png', 1, 4, '-1-3-'),
       ('Laptop styluses', 'laptop_styluses', 'styluses.png', 1, 4, '-1-3-'),
       ('Docking Stations', 'docking stations', 'docking_stations.png', 1, 4, '-1-3-'),
       ('Laptop Bags', 'laptop bags', 'laptop_bags.png', 1, 4, '-1-3-'),
       ('USB Fans', 'usb fans', 'usb_fans.png', 1, 4, '-1-3-'),
       ('Laptop Chargers', 'laptop chargers', 'laptop_chargers.png', 1, 4, '-1-3-');

-- Subcategories under 'Laptop Components' (lvl 2)
INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('SSD Drives', 'ssd drives', 'ssd_drives.png', 1, 5, '-1-4-'),
       ('HDD Drives', 'hdd drives', 'hdd_drives.png', 1, 5, '-1-4-'),
       ('Optical Drives', 'optical drives', 'optical_drives.png', 1, 5, '-1-4-'),
       ('Laptop Memory', 'laptop memory', 'laptop_memory.png', 1, 5, '-1-4-');

-- Subcategories under 'Tablet Accessories' (lvl 2)
INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('E-book Accessories', 'e-book accessories', 'e-book_accessories.png', 1, 6, '-1-5-'),
       ('Tablet Cases', 'tablet cases', 'tablet_cases.png', 1, 6, '-1-5-'),
       ('Tablet Screen Protectors', 'tablet screen protectors', 'tablet_screen_protectors.png', 1, 6, '-1-5-'),
       ('Tablet Keyboards', 'tablet keyboards', 'tablet_keyboards.png', 1, 6, '-1-5-'),
       ('Tablet Chargers', 'tablet chargers', 'tablet_chargers.png', 1, 6, '-1-5-'),
       ('E-book Covers', 'e-book covers', 'e-book_covers.png', 1, 6, '-1-5-'),
       ('Tablet styluses', 'tablet_styluses', 'styluses.png', 1, 6, '-1-5-'),
       ('Tablet Stands', 'tablet stands', 'tablet_stands.png', 1, 6, '-1-5-'),
       ('Tablet Holders', 'tablet holders', 'tablet_holders.png', 1, 6, '-1-5-');

