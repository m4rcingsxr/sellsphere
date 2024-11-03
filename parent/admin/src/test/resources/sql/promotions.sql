-- Insert promotions
INSERT INTO promotions (name)
VALUES
    ('Gamer Gear Deals'),
    ('4K TV Exclusive Offer'),
    ('Gaming PC Guide Offer');

-- Update articles to set promotion_id based on article alias
UPDATE articles SET promotion_id = (SELECT id FROM promotions WHERE name = 'Gamer Gear Deals')
WHERE alias = 'gaming-accessories-you-need';

UPDATE articles SET promotion_id = (SELECT id FROM promotions WHERE name = '4K TV Exclusive Offer')
WHERE alias = '4k-tvs-what-to-look-for';

UPDATE articles SET promotion_id = (SELECT id FROM promotions WHERE name = 'Gaming PC Guide Offer')
WHERE alias = 'ultimate-guide-gaming-pcs';