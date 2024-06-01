INSERT INTO addresses (first_name, last_name, phone_number, address_line_1, address_line_2, city, state, country_id, postal_code, primary_address, customer_id)
VALUES
    ('Alice', 'Smith', '1234567890', '123 Main St', 'Apt 1', 'Springfield', 'IL', 1, '62701', 1, 1),
    ('Alice', 'Smith', '1234567891', '456 Oak St', NULL, 'Springfield', 'IL', 1, '62702', 0, 1),
    ('Alice', 'Smith', '1234567892', '789 Pine St', 'Suite 100', 'Springfield', 'IL', 1, '62703', 0, 1);

#     ('Bob', 'Johnson', '2234567890', '234 Main St', 'Apt 2', 'Metropolis', 'NY', 1, '10001', 1, 2),
#     ('Bob', 'Johnson', '2234567891', '567 Oak St', NULL, 'Metropolis', 'NY', 1, '10002', 0, 2),
#     ('Bob', 'Johnson', '2234567892', '890 Pine St', 'Suite 200', 'Metropolis', 'NY', 1, '10003', 0, 2),
#
#     ('Carol', 'Wilson', '3234567890', '345 Main St', 'Apt 3', 'Gotham', 'NJ', 1, '07001', 1, 3),
#     ('Carol', 'Wilson', '3234567891', '678 Oak St', NULL, 'Gotham', 'NJ', 1, '07002', 0, 3),
#     ('Carol', 'Wilson', '3234567892', '901 Pine St', 'Suite 300', 'Gotham', 'NJ', 1, '07003', 0, 3),
#
#     ('Dave', 'Brown', '4234567890', '456 Main St', 'Apt 4', 'Central City', 'CA', 1, '90001', 1, 4),
#     ('Dave', 'Brown', '4234567891', '789 Oak St', NULL, 'Central City', 'CA', 1, '90002', 0, 4),
#     ('Dave', 'Brown', '4234567892', '012 Pine St', 'Suite 400', 'Central City', 'CA', 1, '90003', 0, 4),
#
#     ('Eve', 'Jones', '5234567890', '567 Main St', 'Apt 5', 'Star City', 'FL', 1, '33001', 1, 5),
#     ('Eve', 'Jones', '5234567891', '890 Oak St', NULL, 'Star City', 'FL', 1, '33002', 0, 5),
#     ('Eve', 'Jones', '5234567892', '123 Pine St', 'Suite 500', 'Star City', 'FL', 1, '33003', 0, 5),
#
#     ('Frank', 'Davis', '6234567890', '678 Main St', 'Apt 6', 'Coast City', 'TX', 1, '75001', 1, 6),
#     ('Frank', 'Davis', '6234567891', '901 Oak St', NULL, 'Coast City', 'TX', 1, '75002', 0, 6),
#     ('Frank', 'Davis', '6234567892', '234 Pine St', 'Suite 600', 'Coast City', 'TX', 1, '75003', 0, 6),
#
#     ('Grace', 'Miller', '7234567890', '789 Main St', 'Apt 7', 'Smallville', 'KS', 1, '66001', 1, 7),
#     ('Grace', 'Miller', '7234567891', '012 Oak St', NULL, 'Smallville', 'KS', 1, '66002', 0, 7),
#     ('Grace', 'Miller', '7234567892', '345 Pine St', 'Suite 700', 'Smallville', 'KS', 1, '66003', 0, 7),
#
#     ('Henry', 'Moore', '8234567890', '890 Main St', 'Apt 8', 'National City', 'CA', 1, '91950', 1, 8),
#     ('Henry', 'Moore', '8234567891', '123 Oak St', NULL, 'National City', 'CA', 1, '91951', 0, 8),
#     ('Henry', 'Moore', '8234567892', '456 Pine St', 'Suite 800', 'National City', 'CA', 1, '91952', 0, 8),
#
#     ('Irene', 'Taylor', '9234567890', '901 Main St', 'Apt 9', 'Blüdhaven', 'DE', 1, '19701', 1, 9),
#     ('Irene', 'Taylor', '9234567891', '234 Oak St', NULL, 'Blüdhaven', 'DE', 1, '19702', 0, 9),
#     ('Irene', 'Taylor', '9234567892', '567 Pine St', 'Suite 900', 'Blüdhaven', 'DE', 1, '19703', 0, 9),
#
#     ('Jack', 'Anderson', '1023456789', '012 Main St', 'Apt 10', 'Fawcett City', 'IL', 1, '60401', 1, 10),
#     ('Jack', 'Anderson', '1023456781', '345 Oak St', NULL, 'Fawcett City', 'IL', 1, '60402', 0, 10),
#     ('Jack', 'Anderson', '1023456782', '678 Pine St', 'Suite 1000', 'Fawcett City', 'IL', 1, '60403', 0, 10),
#
#     ('Karen', 'Thomas', '1123456789', '123 Main St', 'Apt 11', 'Keystone City', 'PA', 1, '19101', 1, 11),
#     ('Karen', 'Thomas', '1123456781', '456 Oak St', NULL, 'Keystone City', 'PA', 1, '19102', 0, 11),
#     ('Karen', 'Thomas', '1123456782', '789 Pine St', 'Suite 1100', 'Keystone City', 'PA', 1, '19103', 0, 11),
#
#     ('Larry', 'Jackson', '1223456789', '234 Main St', 'Apt 12', 'Midway City', 'MI', 1, '48101', 1, 12),
#     ('Larry', 'Jackson', '1223456781', '567 Oak St', NULL, 'Midway City', 'MI', 1, '48102', 0, 12),
#     ('Larry', 'Jackson', '1223456782', '890 Pine St', 'Suite 1200', 'Midway City', 'MI', 1, '48103', 0, 12),
#
#     ('Mary', 'White', '1323456789', '345 Main St', 'Apt 13', 'St. Roch', 'LA', 1, '70117', 1, 13),
#     ('Mary', 'White', '1323456781', '678 Oak St', NULL, 'St. Roch', 'LA', 1, '70118', 0, 13),
#     ('Mary', 'White', '1323456782', '901 Pine St', 'Suite 1300', 'St. Roch', 'LA', 1, '70119', 0, 13),
#
#     ('Nick', 'Harris', '1423456789', '456 Main St', 'Apt 14', 'Hub City', 'IL', 1, '61601', 1, 14),
#     ('Nick', 'Harris', '1423456781', '789 Oak St', NULL, 'Hub City', 'IL', 1, '61602', 0, 14),
#     ('Nick', 'Harris', '1423456782', '012 Pine St', 'Suite 1400', 'Hub City', 'IL', 1, '61603', 0, 14),
#
#     ('Olivia', 'Martin', '1523456789', '567 Main St', 'Apt 15', 'Starling City', 'WA', 1, '98101', 1, 15),
#     ('Olivia', 'Martin', '1523456781', '890 Oak St', NULL, 'Starling City', 'WA', 1, '98102', 0, 15),
#     ('Olivia', 'Martin', '1523456782', '123 Pine St', 'Suite 1500', 'Starling City', 'WA', 1, '98103', 0, 15),
#
#     ('Paul', 'Thompson', '1623456789', '678 Main St', 'Apt 16', 'Gateway City', 'CO', 1, '80001', 1, 16),
#     ('Paul', 'Thompson', '1623456781', '901 Oak St', NULL, 'Gateway City', 'CO', 1, '80002', 0, 16),
#     ('Paul', 'Thompson', '1623456782', '234 Pine St', 'Suite 1600', 'Gateway City', 'CO', 1, '80003', 0, 16),
#
#     ('Quincy', 'Garcia', '1723456789', '789 Main St', 'Apt 17', 'Opal City', 'VA', 1, '20101', 1, 17),
#     ('Quincy', 'Garcia', '1723456781', '012 Oak St', NULL, 'Opal City', 'VA', 1, '20102', 0, 17),
#     ('Quincy', 'Garcia', '1723456782', '345 Pine St', 'Suite 1700', 'Opal City', 'VA', 1, '20103', 0, 17),
#
#     ('Rachel', 'Martinez', '1823456789', '890 Main St', 'Apt 18', 'Gotham', 'NY', 1, '10001', 1, 18),
#     ('Rachel', 'Martinez', '1823456781', '123 Oak St', NULL, 'Gotham', 'NY', 1, '10002', 0, 18),
#     ('Rachel', 'Martinez', '1823456782', '456 Pine St', 'Suite 1800', 'Gotham', 'NY', 1, '10003', 0, 18),
#
#     ('Steve', 'Robinson', '1923456789', '901 Main St', 'Apt 19', 'Central City', 'MO', 1, '64101', 1, 19),
#     ('Steve', 'Robinson', '1923456781', '234 Oak St', NULL, 'Central City', 'MO', 1, '64102', 0, 19),
#     ('Steve', 'Robinson', '1923456782', '567 Pine St', 'Suite 1900', 'Central City', 'MO', 1, '64103', 0, 19),
#
#     ('Tina', 'Clark', '2023456789', '012 Main St', 'Apt 20', 'Smallville', 'KS', 1, '66001', 1, 20),
#     ('Tina', 'Clark', '2023456781', '345 Oak St', NULL, 'Smallville', 'KS', 1, '66002', 0, 20),
#     ('Tina', 'Clark', '2023456782', '678 Pine St', 'Suite 2000', 'Smallville', 'KS', 1, '66003', 0, 20),
#
#     ('Victor', 'Rodriguez', '2123456789', '123 Main St', 'Apt 21', 'National City', 'CA', 1, '91950', 1, 21),
#     ('Victor', 'Rodriguez', '2123456781', '456 Oak St', NULL, 'National City', 'CA', 1, '91951', 0, 21),
#     ('Victor', 'Rodriguez', '2123456782', '789 Pine St', 'Suite 2100', 'National City', 'CA', 1, '91952', 0, 21);
