INSERT INTO promotions (name) values ('promo');

INSERT INTO carousels (carousel_type, header, carousel_order, promotion_id)
VALUES ('IMAGE', 'main image carousel', 0, null);

INSERT INTO carousels (carousel_type, header, carousel_order, promotion_id)
VALUES ('ARTICLE', 'main image carousel', 1, null);

INSERT INTO carousels (carousel_type, header, carousel_order, promotion_id)
VALUES ('ARTICLE', 'main image carousel', 2, null);

INSERT INTO carousel_items (entity_id, carousel_id, carousel_item_order)
VALUES (1,2,0);
INSERT INTO carousel_items (entity_id, carousel_id, carousel_item_order)
VALUES (2,2,1);
INSERT INTO carousel_items (entity_id, carousel_id, carousel_item_order)
VALUES (3,2,2);
INSERT INTO carousel_items (entity_id, carousel_id, carousel_item_order)
VALUES (4,2,3);

INSERT INTO carousel_items (entity_id, carousel_id, carousel_item_order)
VALUES (1,3,0);
INSERT INTO carousel_items (entity_id, carousel_id, carousel_item_order)
VALUES (2,3,1);
INSERT INTO carousel_items (entity_id, carousel_id, carousel_item_order)
VALUES (3,3,2);
INSERT INTO carousel_items (entity_id, carousel_id, carousel_item_order)
VALUES (4,3,3);
