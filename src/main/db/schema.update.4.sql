ALTER TABLE accounts ADD deleted BIT(1) NOT NULL DEFAULT 0;
ALTER TABLE accounts CHANGE fullName full_name VARCHAR(255);