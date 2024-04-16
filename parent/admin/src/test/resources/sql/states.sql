-- Insert states
INSERT INTO states (name, country_id) VALUES ('Kabul', (SELECT id FROM countries WHERE code = 'AFG'));
INSERT INTO states (name, country_id) VALUES ('Herat', (SELECT id FROM countries WHERE code = 'AFG'));
INSERT INTO states (name, country_id) VALUES ('Tirana', (SELECT id FROM countries WHERE code = 'ALB'));
INSERT INTO states (name, country_id) VALUES ('Durres', (SELECT id FROM countries WHERE code = 'ALB'));
INSERT INTO states (name, country_id) VALUES ('Algiers', (SELECT id FROM countries WHERE code = 'DZA'));
INSERT INTO states (name, country_id) VALUES ('Oran', (SELECT id FROM countries WHERE code = 'DZA'));