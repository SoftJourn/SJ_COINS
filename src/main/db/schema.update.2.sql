ALTER TABLE accounts DROP amount;
ALTER TABLE transactions ADD remain DECIMAL(19,2);

CREATE TABLE eris
(
  address VARCHAR(255) PRIMARY KEY NOT NULL,
  privKey VARCHAR(255) NOT NULL,
  pubKey VARCHAR(255) NOT NULL,
  type INT(11) NOT NULL,
  account_ldap_id VARCHAR(255),
  CONSTRAINT FK_account FOREIGN KEY (account_ldap_id) REFERENCES accounts (ldap_id)
);
CREATE INDEX FK_account_id ON eris (account_ldap_id);
CREATE UNIQUE INDEX UKrpsbty0f34qu89n6juppia3ge ON eris (type, account_ldap_id);