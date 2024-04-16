INSERT INTO settings (setting_key, value, category)
VALUES ('SITE_LOGO', '/site-logo/DummyLogo.png', 'GENERAL'),
       ('SITE_NAME', 'DummySite', 'GENERAL'),
       ('COPYRIGHT', 'Copyright (C) 2021 Dummy Ltd.', 'GENERAL');

INSERT INTO settings (setting_key, value, category)
VALUES ('CURRENCY_ID', '1', 'CURRENCY'),
       ('CURRENCY_SYMBOL', '$', 'CURRENCY'),
       ('CURRENCY_SYMBOL_POSITION', 'AFTER PRICE', 'CURRENCY'),
       ('DECIMAL_POINT_TYPE', 'POINT', 'CURRENCY'),
       ('DECIMAL_DIGITS', '2', 'CURRENCY'),
       ('THOUSANDS_POINT_TYPE', 'COMMA', 'CURRENCY');

INSERT INTO settings (setting_key, value, category)
VALUES ('MAIL_HOST', 'smtp.dummy.com', 'MAIL_SERVER'),
       ('MAIL_PORT', '587', 'MAIL_SERVER'),
       ('MAIL_USERNAME', 'DummyShop@gmail.com', 'MAIL_SERVER'),
       ('MAIL_PASSWORD', 'dummy_password', 'MAIL_SERVER'),
       ('SMTP_AUTH', 'true', 'MAIL_SERVER'),
       ('SMTP_SECURED', 'true', 'MAIL_SERVER'),
       ('MAIL_FROM', 'DummyShop@gmail.com', 'MAIL_SERVER'),
       ('MAIL_SENDER_NAME', 'Dummy Company', 'MAIL_SERVER');

INSERT INTO settings (setting_key, value, category)
VALUES ('CUSTOMER_EMAIL_VERIFY_SUBJECT', 'Please verify your registration to continue shopping', 'MAIL_TEMPLATES'),
       ('CUSTOMER_EMAIL_VERIFY_CONTENT',
        '<p>Dear [[NAME]]</p>,<p>Thank you for registering on our website. </p>,<p>Please confirm your account by clicking the verification link</p>,<p>[[URL]]</p>,<p>Best regards, Dummy Team.</p>',
        'MAIL_TEMPLATES');

INSERT INTO settings (setting_key, value, category)
VALUES ('ORDER_CONFIRMATION_SUBJECT', 'Order confirmation', 'MAIL_TEMPLATES'),
       ('ORDER_CONFIRMATION_CONTENT',
        '<p>Dear [[NAME]]</p><p>This email is to confirm that you have successfully placed an order through our website. Please review the following order summary:</p><p>Order ID: [[orderId]]<br>Order time: [[orderTime]]<br>Ship to: [[shippingAddress]]<br>Total: [[total]]<br>Payment Method: [[paymentMethod]]</p><p>We''re currently processing your order. Click here to view full details of your order on our website. [login required]</p><p>,</p><p>Thank you for placing order on our website.</p><p>Best regards, Dummy Team.</p>',
        'MAIL_TEMPLATES');

INSERT INTO settings (setting_key, value, category)
VALUES ('PAYPAL_BASE_URL', 'https://api-m.sandbox.paypal.com', 'PAYMENT'),
       ('PAYPAL_CLIENT_ID', 'dummy_client_id', 'PAYMENT'),
       ('PAYPAL_CLIENT_SECRET', 'dummy_client_secret', 'PAYMENT');