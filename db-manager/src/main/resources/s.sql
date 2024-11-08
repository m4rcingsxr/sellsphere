-- Subcategories under 'Sub - Smartphones' (Parent ID: 168)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (169, 'iOS', 'ios', 'ios.png', 1, 168, '-42-168-'),
    (170, 'Android', 'android', 'android.png', 1, 168, '-42-168-');

-- Create a new subcategory 'Sub - Popular Smartphone Manufacturers' under 'Sub - Smartphones' (Parent ID: 168)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (171, 'Sub - Popular Smartphone Manufacturers', 'sub-popular-smartphone-manufacturers', 'sub_popular_smartphone_manufacturers.png', 1, 168, '-42-168-');

-- Subcategories under 'Sub - Popular Smartphone Manufacturers' (Parent ID: 171)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (172, 'Smartphone Samsung', 'smartphone-samsung', 'smartphone_samsung.png', 1, 171, '-42-168-171-'),
    (173, 'Smartphone Apple', 'smartphone-apple', 'smartphone_apple.png', 1, 171, '-42-168-171-'),
    (174, 'Smartphone Xiaomi', 'smartphone-xiaomi', 'smartphone_xiaomi.png', 1, 171, '-42-168-171-'),
    (175, 'Smartphone Huawei', 'smartphone-huawei', 'smartphone_huawei.png', 1, 171, '-42-168-171-');

-- Create a subcategory 'Sub - Refurbished Smartphones' under 'Sub - Smartphones' (Parent ID: 168)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (176, 'Sub - Refurbished Smartphones', 'sub-refurbished-smartphones', 'sub_refurbished_smartphones.png', 1, 168, '-42-168-');

-- Create a new subcategory 'Sub - Mobile Phones' under 'Sub - Smartphones' (Parent ID: 168)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (177, 'Sub - Mobile Phones', 'sub-mobile-phones', 'sub_mobile_phones.png', 1, 168, '-42-168-');

-- Subcategories under 'Sub - Mobile Phones' (Parent ID: 177)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (178, 'For Active Users', 'for-active-users', 'for_active_users.png', 1, 177, '-42-168-177-'),
    (179, 'For Seniors', 'for-seniors', 'for_seniors.png', 1, 177, '-42-168-177-'),
    (180, 'Universal', 'universal', 'universal.png', 1, 177, '-42-168-177-');

-- Create a new subcategory 'Sub - Accessories for Smartphones' under 'Sub - Smartphones' (Parent ID: 168)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (181, 'Sub - Accessories for Smartphones', 'sub-accessories-smartphones', 'sub_accessories_smartphones.png', 1, 168, '-42-168-');

-- Subcategories under 'Sub - Accessories for Smartphones' (Parent ID: 181)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (182, 'Phone Cases', 'phone-cases', 'phone_cases.png', 1, 181, '-42-168-181-'),
    (183, 'Protective Foils and Glasses', 'protective-foils-glasses', 'protective_foils_glasses.png', 1, 181, '-42-168-181-'),
    (184, 'Bluetooth Speakers', 'bluetooth-speakers', 'bluetooth_speakers.png', 1, 181, '-42-168-181-'),
    (185, 'Memory Cards', 'memory-cards', 'memory_cards.png', 1, 181, '-42-168-181-'),
    (186, 'Car Chargers', 'car-chargers', 'car_chargers.png', 1, 181, '-42-168-181-'),
    (187, 'Wall Chargers', 'wall-chargers', 'wall_chargers.png', 1, 181, '-42-168-181-'),
    (188, 'Power Banks', 'power-banks', 'power_banks.png', 1, 181, '-42-168-181-'),
    (189, 'Styluses', 'styluses', 'styluses.png', 1, 181, '-42-168-181-'),
    (190, 'Selfie Sticks', 'selfie-sticks', 'selfie_sticks.png', 1, 181, '-42-168-181-'),
    (191, 'Wireless Headphones', 'wireless-headphones', 'wireless_headphones.png', 1, 181, '-42-168-181-'),
    (192, 'Phone Holders', 'phone-holders', 'phone_holders.png', 1, 181, '-42-168-181-'),
    (193, 'GSM Speaker Sets', 'gsm-speaker-sets', 'gsm_speaker_sets.png', 1, 181, '-42-168-181-'),
    (194, 'GSM Headphone Sets', 'gsm-headphone-sets', 'gsm_headphone_sets.png', 1, 181, '-42-168-181-');

-- Create a new subcategory 'Sub - Smartwatches and Watches' under 'Sub - Smartphones' (Parent ID: 168)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (195, 'Sub - Smartwatches and Watches', 'sub-smartwatches-watches', 'sub_smartwatches_watches.png', 1, 168, '-42-168-');

-- Subcategories under 'Sub - Smartwatches and Watches' (Parent ID: 195)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (196, 'Smartwatches', 'smartwatches', 'smartwatches.png', 1, 195, '-42-168-195-'),
    (197, 'Smartbands', 'smartbands', 'smartbands.png', 1, 195, '-42-168-195-'),
    (198, 'Sports Watches', 'sports-watches', 'sports_watches.png', 1, 195, '-42-168-195-'),
    (199, 'Pulse Meters and Pedometers', 'pulse-meters-pedometers', 'pulse_meters_pedometers.png', 1, 195, '-42-168-195-'),
    (200, 'Watches', 'watches', 'watches.png', 1, 195, '-42-168-195-'),
    (201, 'Children’s Watches', 'childrens-watches', 'childrens_watches.png', 1, 195, '-42-168-195-'),
    (202, 'Accessories for Smartwatches', 'accessories-smartwatches', 'accessories_smartwatches.png', 1, 195, '-42-168-195-'),
    (203, 'Accessories for Watches', 'accessories-watches', 'accessories_watches.png', 1, 195, '-42-168-195-');

-- Create a new subcategory 'Sub - Landline Phones' under 'Sub - Smartphones' (Parent ID: 168)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (204, 'Sub - Landline Phones', 'sub-landline-phones', 'sub_landline_phones.png', 1, 168, '-42-168-');

-- Subcategories under 'Sub - Landline Phones' (Parent ID: 204)
INSERT INTO categories (ID, NAME, ALIAS, IMAGE, ENABLED, PARENT_ID, ALL_PARENT_IDS)
VALUES
    (205, 'Landline Phones', 'landline-phones-category', 'landline_phones_category.png', 1, 204, '-42-168-204-'),
    (206, 'Internet Phones', 'internet-phones', 'internet_phones.png', 1, 204, '-42-168-204-'),
    (207, 'VoIP Gateways', 'voip-gateways', 'voip_gateways.png', 1, 204, '-42-168-204-'),
    (208, 'Accessories for Landline Phones', 'accessories-landline-phones', 'accessories_landline_phones.png', 1, 204, '-42-168-204-');
;