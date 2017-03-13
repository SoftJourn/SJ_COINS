ALTER TABLE sj_coins.instances ADD COLUMN account_ldap_id varchar(255) NOT NULL;
ALTER TABLE contracts ADD COLUMN active bit(1) NOT NULL DEFAULT b'1';

ALTER TABLE instances
  ADD CONSTRAINT FKhr2kxnlv3eb3x30dm2l9fpvdf FOREIGN KEY (account_ldap_id) REFERENCES accounts (ldap_id);

UPDATE sj_coins.accounts set account_type = 'PROJECT' WHERE account_type = 'CROWDSALE';