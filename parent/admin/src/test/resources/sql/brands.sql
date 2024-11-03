-- Insert brands with explicit IDs
INSERT INTO brands (id, name, logo)
VALUES (1, 'Canon', 'Canon.png');

INSERT INTO brands (id, name, logo)
VALUES (2, 'Fujifilm', 'Fujifilm.png');

INSERT INTO brands (id, name, logo)
VALUES (3, 'Sony', 'Sony.png');

INSERT INTO brands (id, name, logo)
VALUES (4, 'HP', 'HP.png');

INSERT INTO brands (id, name, logo)
VALUES (5, 'SanDisk', 'SanDisk.png');

INSERT INTO brands (id, name, logo)
VALUES (6, 'SanDisk1', 'SanDisk.png');

INSERT INTO brands (id, name, logo)
VALUES (7, 'SanDisk2', 'SanDisk.png');

INSERT INTO brands (id, name, logo)
VALUES (8, 'SanDisk3', 'SanDisk.png');

INSERT INTO brands (id, name, logo)
VALUES (9, 'SanDisk4', 'SanDisk.png');

INSERT INTO brands (id, name, logo)
VALUES (10, 'SanDisk5', 'SanDisk.png');


ALTER TABLE brands ALTER COLUMN id RESTART WITH 11;
