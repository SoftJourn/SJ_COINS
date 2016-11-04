ALTER TABLE eris CHANGE privKey priv_key VARCHAR(255);
ALTER TABLE eris CHANGE pubKey pub_key VARCHAR(255);

ALTER TABLE accounts CHANGE type account_type VARCHAR(255)
