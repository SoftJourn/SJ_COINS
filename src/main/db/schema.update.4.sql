ALTER TABLE accounts ADD deleted BIT(1) NOT NULL DEFAULT 0;
ALTER TABLE accounts CHANGE fullName full_name VARCHAR(255);
ALTER TABLE transactions ADD eris_transaction_id VARCHAR(255);