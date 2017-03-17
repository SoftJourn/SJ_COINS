ALTER TABLE sj_coins.instances ADD COLUMN account_ldap_id varchar(255) NOT NULL;
ALTER TABLE contracts ADD COLUMN active bit(1) NOT NULL DEFAULT b'1';

UPDATE instances SET account_ldap_id=(SELECT UUID());

INSERT INTO accounts (ldap_id, full_name, account_type)
  SELECT account_ldap_id, instances.name, UPPER(contracts.type)
  FROM instances INNER JOIN contracts ON instances.contract_id=contracts.id;

INSERT INTO eris (address, priv_key, pub_key, type, account_ldap_id)
  SELECT address, '0000000000000000000000000000000000000000000000000000000000000000', '00000000000000000000000000000000', 2, account_ldap_id
  FROM instances;

ALTER TABLE instances
  ADD CONSTRAINT FKhr2kxnlv3eb3x30dm2l9fpvdf FOREIGN KEY (account_ldap_id) REFERENCES accounts (ldap_id);

UPDATE sj_coins.accounts set account_type = 'PROJECT' WHERE account_type = 'CROWDSALE';