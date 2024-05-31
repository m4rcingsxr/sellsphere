INSERT INTO settings (setting_key, value, category)
VALUES ('SITE_LOGO', '/site-logo/logo.png', 'GENERAL');
INSERT INTO settings (setting_key, value, category)
VALUES ('SITE_NAME', 'SellSphere', 'GENERAL');
INSERT INTO settings (setting_key, value, category)
VALUES ('COPYRIGHT', 'Copyright (C) 2023 SellSphere Ltd.', 'GENERAL');

INSERT INTO settings (setting_key, value, category)
VALUES ('CURRENCY_ID', '1', 'CURRENCY');
INSERT INTO settings (setting_key, value, category)
VALUES ('CURRENCY_SYMBOL', '$', 'CURRENCY');
INSERT INTO settings (setting_key, value, category)
VALUES ('CURRENCY_SYMBOL_POSITION', 'AFTER_PRICE', 'CURRENCY');
INSERT INTO settings (setting_key, value, category)
VALUES ('DECIMAL_POINT_TYPE', 'POINT', 'CURRENCY');
INSERT INTO settings (setting_key, value, category)
VALUES ('DECIMAL_DIGITS', '2', 'CURRENCY');
INSERT INTO settings (setting_key, value, category)
VALUES ('THOUSANDS_POINT_TYPE', 'COMMA', 'CURRENCY');

INSERT INTO settings (setting_key, value, category)
VALUES ('MAIL_HOST', 'smtp.gmail.com', 'MAIL_SERVER');
INSERT INTO settings (setting_key, value, category)
VALUES ('MAIL_PORT', '587', 'MAIL_SERVER');
INSERT INTO settings (setting_key, value, category)
VALUES ('MAIL_USERNAME', 'SellSphereShop@gmail.com', 'MAIL_SERVER');
INSERT INTO settings (setting_key, value, category)
VALUES ('MAIL_PASSWORD', 'lmww wads yqgo qewr', 'MAIL_SERVER');
INSERT INTO settings (setting_key, value, category)
VALUES ('SMTP_AUTH', 'true', 'MAIL_SERVER');
INSERT INTO settings (setting_key, value, category)
VALUES ('SMTP_SECURED', 'true', 'MAIL_SERVER');
INSERT INTO settings (setting_key, value, category)
VALUES ('MAIL_FROM', 'SellSphereShop@gmail.com', 'MAIL_SERVER');
INSERT INTO settings (setting_key, value, category)
VALUES ('MAIL_SENDER_NAME', 'SellSphere Company', 'MAIL_SERVER');

INSERT INTO settings (setting_key, value, category)
VALUES ('CUSTOMER_EMAIL_VERIFY_SUBJECT', 'Please verify your registration to continue shopping', 'MAIL_TEMPLATES');
INSERT INTO settings (setting_key, value, category)
VALUES ('CUSTOMER_EMAIL_VERIFY_CONTENT',
        '<p>Dear [[NAME]]</p>,<p>Thank you for registering on our website. </p>,<p>Please confirm your account by clicking the verification link</p>,<p>[[URL]]</p>,<p>Best regards, SellSphere Team.</p>',
        'MAIL_TEMPLATES');

INSERT INTO settings (setting_key, value, category)
VALUES ('ORDER_CONFIRMATION_SUBJECT', 'Order confirmation', 'MAIL_TEMPLATES');
INSERT INTO settings (setting_key, value, category)
VALUES ('ORDER_CONFIRMATION_CONTENT',
        '<p>Dear [[NAME]]</p><p>This email is to confirm that you have successfully placed an order through our website. Please review the following order summary:</p><p>Order ID: [[orderId]]<br>Order time: [[orderTime]]<br>Ship to: [[shippingAddress]]<br>Total: [[total]]<br>Payment Method: [[paymentMethod]]</p><p>We''re currently processing your order. Click here to view full details of your order on our website. [login required]</p><p>,</p><p>Thank you for placing order on our website.</p><p>Best regards, SellSphere Team.</p>',
        'MAIL_TEMPLATES');

INSERT INTO settings (setting_key, value, category)
VALUES ('TAX', 'INCLUSIVE', 'PAYMENT');
INSERT INTO settings (setting_key, value, category)
VALUES ('SUPPORTED_COUNTRY', '1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,15,17,18,19,20,21,22,23,24,25,26,27', 'PAYMENT');
