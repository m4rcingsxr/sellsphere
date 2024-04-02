INSERT INTO brands(name, logo)
VALUES ('Canon', 'Canon.png');
INSERT INTO brands(name, logo)
VALUES ('Fujifilm', 'Fujifilm.png');
INSERT INTO brands(name, logo)
VALUES ('Sony', 'Sony.png');
INSERT INTO brands(name, logo)
VALUES ('HP', 'HP.png');
INSERT INTO brands(name, logo)
VALUES ('SanDisk', 'SanDisk.png');
INSERT INTO brands(name, logo)
VALUES ('SanDisk1', 'SanDisk.png');
INSERT INTO brands(name, logo)
VALUES ('SanDisk2', 'SanDisk.png');
INSERT INTO brands(name, logo)
VALUES ('SanDisk3', 'SanDisk.png');
INSERT INTO brands(name, logo)
VALUES ('SanDisk4', 'SanDisk.png');
INSERT INTO brands(name, logo)
VALUES ('SanDisk5', 'SanDisk.png');

INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Electronics', 'electronics', 'electronics.png', 1, NULL, NULL);

INSERT INTO categories (NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES ('Computers', 'computers', 'computers.png', 1, 1, '-1-'),
       ('Computer Components', 'computer_components', 'computer components.png', 1, 2, '-1-2-'),
       ('CPU Processors Unit', 'computer_processors', 'computer processors.png', 1, 3, '-1-2-3-'),
       ('Graphic Cards', 'computer_graphic_cards', 'graphic cards.png', 1, 3, '-1-2-3-');

INSERT INTO category_icons (icon_path, category_id)
VALUES ('<i>icon</i>', 1);