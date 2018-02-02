DELETE FROM instances;

DROP TABLE instances;

DELETE FROM contracts;

DROP TABLE contracts;

DELETE FROM contract_type;

DROP TABLE contract_type;

DELETE FROM tx_calling_data;

DROP TABLE tx_calling_data;

DELETE FROM transaction_history;

DROP TABLE transaction_history;

DELETE FROM transactions
WHERE transactions.account_ldap_id
      IN (SELECT ldap_id
          FROM accounts
          WHERE account_type = 'CURRENCY' AND account_type = 'PROJECT')
      AND transactions.destination_ldap_id IN (SELECT ldap_id
                                               FROM accounts
                                               WHERE account_type = 'CURRENCY' AND account_type = 'PROJECT');

DELETE FROM accounts
WHERE account_type = 'CURRENCY' AND account_type = 'PROJECT';

ALTER TABLE accounts
  ADD COLUMN email VARCHAR(255) NULL;

DELETE FROM transactions;

ALTER TABLE transactions
  CHANGE eris_transaction_id transaction_id VARCHAR(255) NOT NULL;

DELETE FROM eris;

DROP TABLE eris;