INSERT INTO footer_items (item_number, article_id, footer_section_id)
VALUES
    (1, (SELECT id FROM articles WHERE alias = 'latest-smartphone-releases'), 1),
    (2, (SELECT id FROM articles WHERE alias = 'wireless-earbuds-review'), 1),
    (3, (SELECT id FROM articles WHERE alias = 'rise-of-electric-scooters'), 1);

INSERT INTO footer_items (item_number, article_id, footer_section_id)
VALUES
    (1, (SELECT id FROM articles WHERE alias = 'best-budget-laptops'), 2),
    (2, (SELECT id FROM articles WHERE alias = 'home-office-essentials'), 2),
    (3, (SELECT id FROM articles WHERE alias = 'top-fitness-gadgets'), 2),
    (4, (SELECT id FROM articles WHERE alias = 'affordable-4k-monitors'), 2);
