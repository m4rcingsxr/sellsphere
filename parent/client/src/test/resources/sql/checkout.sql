insert into couriers (id, courier_name, courier_logo_url, min_delivery_time, max_delivery_time)
values (1, 'UPS', 'https://logo.com', 2, 2);

insert into currencies (id, name, symbol, code, unit_amount) values (1, 'Euro', '€', 'eur', 100);
insert into currencies (id, name, symbol, code, unit_amount) values (2, 'Polish Złoty', 'zł', 'pln', 100);

insert into customers (email, password, first_name, last_name, enabled, email_verified, created_time, stripe_id)
values ('johndoe@gmail.com', '123', 'John', 'Doe', 1, 1, LOCALTIMESTAMP, 'c_123');

insert into countries (id, name, code, currency_id)
values (1, 'Poland', 'PL', 2);


insert into addresses (first_name, last_name, phone_number, address_line_1, address_line_2, city, state, country_id, postal_code, primary_address, customer_id)
               values ('John', 'Doe', '+48 123456789', 'line1', null, 'Wrocław', 'Lower Silesia', 1, '12-345', 1, 1)