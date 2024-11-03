-- Insert unique navigation items associated with the specified articles
INSERT INTO navigation_items (item_number, article_id)
VALUES
    (1, (SELECT id FROM articles WHERE alias = 'latest-smartphone-releases')),
    (2, (SELECT id FROM articles WHERE alias = 'top-10-laptops-2024')),
    (3, (SELECT id FROM articles WHERE alias = 'budget-friendly-tablets')),
    (4, (SELECT id FROM articles WHERE alias = 'best-drones-photography'));
