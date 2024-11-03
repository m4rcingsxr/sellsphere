INSERT INTO questions (product_id, customer_id, question_content, ask_time, answer_content, answerer, answer_time, approval_status, votes)
VALUES
    (1, (SELECT id FROM customers WHERE email = 'john.doe@example.com'), 'Does this camera have Wi-Fi connectivity?', '2023-08-01', 'Yes, the Canon EOS 90D includes built-in Wi-Fi.', 'Support Team', '2023-08-02', 1, 5),
    (2, (SELECT id FROM customers WHERE email = 'john.doe@example.com'), 'Is the Fujifilm X-T30 good for beginner photographers?', '2023-08-03', 'Yes, it is user-friendly for beginners.', 'Support Team', '2023-08-04', 1, 3),
    (3, (SELECT id FROM customers WHERE email = 'john.doe@example.com'), 'What type of battery does the Sony A7R IV use?', '2023-08-05', 'The Sony A7R IV uses an NP-FZ100 rechargeable battery.', 'Support Team', '2023-08-06', 1, 7),
    (4, (SELECT id FROM customers WHERE email = 'john.doe@example.com'), 'Can the HP Pavilion Gaming Desktop be upgraded?', '2023-08-07', 'Yes, RAM and storage upgrades are possible.', 'Tech Expert', '2023-08-08', 1, 10),
    (5, (SELECT id FROM customers WHERE email = 'john.doe@example.com'), 'What is the read and write speed for the SanDisk Ultra SSD?', '2023-08-09', 'The read speed is up to 560 MB/s and write speed is 530 MB/s.', 'Product Specialist', '2023-08-10', 1, 4);
