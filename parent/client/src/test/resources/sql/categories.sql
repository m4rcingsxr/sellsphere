INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Electronics', 'electronics', 'electronics.png', 1, NULL, NULL);


INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Laptops', 'laptops', 'laptops.png', 1, NULL, NULL);


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

insert into product_details (name, detail_value, product_id)
values ('Number of USB 2.0 Ports', '1', 1),
       (' Standing screen display size', '‎15.6 Inches', 1),
       ('Max Screen Resolution‎', '1920 x 1080 Pixels', 1),
       ('Screen Resolution‎', '1920 x 1080 pixels', 1),
       ('Number of USB 3.0 Ports', '2', 1),
       ('Hard Drive', '‎256 GB SSD', 1),
       ('Memory Speed', '‎2666 MHz', 1),
       ('Processor', '3.7 GHz ryzen_5_3550h', 1),
       ('Graphics Card Ram Size', '‎4 GB', 1),
       ('RAM', '‎8 GB DDR4', 1);


insert into product_details (name, detail_value, product_id)
values
    ('Processor', 'Intel Core i7-8565U', 2),
    ('RAM', '16GB DDR4', 2),
    ('Storage', '512GB SSD', 2),
    ('Display', '13.3" 4K UHD Touchscreen', 2),
    ('Battery Life', '10 hours', 2),
    ('Weight', '2.7 lbs', 2),
    ('Operating System', 'Windows 10', 2),
    ('USB Ports', '2 x USB-C', 2),
    ('Graphics', 'Intel UHD Graphics 620', 2),
    ('Webcam', '720p HD', 2);

insert into product_details (name, detail_value, product_id)
values
    ('Processor', 'Intel Core i7-9750H', 3),
    ('RAM', '16GB DDR4', 3),
    ('Storage', '1TB SSD', 3),
    ('Display', '15.6" 4K UHD Touchscreen', 3),
    ('Battery Life', '12 hours', 3),
    ('Weight', '4.62 lbs', 3),
    ('Operating System', 'Windows 10', 3),
    ('USB Ports', '2 x USB-C, 1 x USB-A', 3),
    ('Graphics', 'NVIDIA GeForce GTX 1650', 3),
    ('Webcam', '1080p FHD', 3);

insert into product_details (name, detail_value, product_id)
values
    ('Processor', 'Intel Core i7-10510U', 4),
    ('RAM', '16GB DDR4', 4),
    ('Storage', '1TB SSD', 4),
    ('Display', '14" FHD', 4),
    ('Battery Life', '15 hours', 4),
    ('Weight', '2.40 lbs', 4),
    ('Operating System', 'Windows 10 Pro', 4),
    ('USB Ports', '2 x USB-C, 2 x USB-A', 4),
    ('Graphics', 'Intel UHD Graphics', 4),
    ('Webcam', '720p HD', 4);


insert into product_details (name, detail_value, product_id)
values
    ('Processor', 'Intel Core i9', 5),
    ('RAM', '16GB DDR4', 5),
    ('Storage', '1TB SSD', 5),
    ('Display', '16" Retina', 5),
    ('Battery Life', '11 hours', 5),
    ('Weight', '4.30 lbs', 5),
    ('Operating System', 'macOS', 5),
    ('USB Ports', '4 x USB-C', 5),
    ('Graphics', 'Radeon Pro 5500M', 5),
    ('Webcam', '720p HD', 5);

insert into product_details (name, detail_value, product_id)
values
    ('Processor', 'AMD Ryzen 7', 6),
    ('RAM', '16GB DDR4', 6),
    ('Storage', '512GB SSD', 6),
    ('Display', '15" Touch-Screen', 6),
    ('Battery Life', '11.5 hours', 6),
    ('Weight', '3.40 lbs', 6),
    ('Operating System', 'Windows 10', 6),
    ('USB Ports', '1 x USB-C, 1 x USB-A', 6),
    ('Graphics', 'Radeon Vega 11', 6),
    ('Webcam', '720p HD', 6);

