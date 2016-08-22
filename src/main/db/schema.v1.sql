CREATE TABLE IF NOT EXISTS accounts
(
    ldap_id VARCHAR(255) PRIMARY KEY NOT NULL,
    amount DECIMAL(10)
);
CREATE TABLE IF NOT EXISTS transactions
(
    id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    account_id VARCHAR(255),
    destination_id VARCHAR(255),
    amount DECIMAL(10),
    comment TEXT,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL,
    error TEXT,
    CONSTRAINT account_fk FOREIGN KEY (account_id) REFERENCES accounts (ldap_id),
    CONSTRAINT destination_fk FOREIGN KEY (destination_id) REFERENCES accounts (ldap_id)
);
CREATE INDEX account_fk ON transactions (account_id);
CREATE INDEX destination_fk ON transactions (destination_id);