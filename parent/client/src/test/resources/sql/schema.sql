CREATE SCHEMA IF NOT EXISTS keycloak;

CREATE TABLE keycloak.customers
(
    id                INT AUTO_INCREMENT PRIMARY KEY,
    email             VARCHAR(45) NOT NULL UNIQUE,
    customer_password VARCHAR(64) NOT NULL,
    first_name        VARCHAR(45) NOT NULL,
    last_name         VARCHAR(45) NOT NULL,
    enabled           BOOLEAN     NOT NULL,
    email_verified    BOOLEAN     NOT NULL,
    created_time      TIMESTAMP   NOT NULL
);