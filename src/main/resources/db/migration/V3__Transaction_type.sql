ALTER TABLE transactions ADD COLUMN type varchar(32);

UPDATE transactions
SET type = 'EXPENSE'
WHERE transactions.destination_ldap_id in (SELECT account_ldap_id from instances) and transactions.type IS NULL
and transactions.account_ldap_id IS NOT NULL;

UPDATE transactions
SET type = 'EXPENSE'
WHERE transactions.destination_ldap_id in (SELECT ldap_id from accounts where account_type = 'MERCHANT') and transactions.type IS NULL
and transactions.account_ldap_id IS NOT NULL;

UPDATE transactions
SET type = 'DEPLOYMENT'
WHERE transactions.comment LIKE '%deploy%' and transactions.type IS NULL;

UPDATE transactions
SET type = 'WITHDRAW_OFFLINE'
WHERE transactions.comment LIKE '%Withdrawing money%' and transactions.type IS NULL;

UPDATE transactions
SET type = 'WITHDRAW_FOUNDATION'
WHERE transactions.comment LIKE '%Withdraw coins%' and transactions.type IS NULL;

UPDATE transactions
SET type = 'TRANSFER'
WHERE transactions.comment LIKE '%Transfer money%' and transactions.type IS NULL;

UPDATE transactions
SET type = 'ROLLBACK'
WHERE transactions.comment LIKE '%Rollback%' and transactions.type IS NULL;

UPDATE transactions
SET type = 'DEPOSIT'
WHERE transactions.comment LIKE '%Deposite cash%' and transactions.type IS NULL;

UPDATE transactions
SET type = 'REGULAR_REPLENISHMENT'
WHERE transactions.account_ldap_id IS NULL AND transactions.destination_ldap_id IS NOT NULL AND
      transactions.amount = 1500 and transactions.type IS NULL;

UPDATE transactions
SET type = 'SINGLE_REPLENISHMENT'
WHERE transactions.account_ldap_id IS NULL AND transactions.destination_ldap_id IS NOT NULL AND
      transactions.amount != 1500 and transactions.type IS NULL;