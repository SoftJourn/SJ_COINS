delete * from contract_type;

drop TABLE contract_type;

delete * from instances;

DROP TABLE instances;

delete * from contracts;

drop table contracts;

delete * from tx_calling_data;

drop table tx_calling_data;

delete * from transaction_history;

drop table transaction_history;

delete * from transactions where transactions.account_ldap_id
                                 in (select ldap_id from accounts where account_type = 'CURRENCY' and account_type = 'PROJECT')
and transactions.destination_ldap_id in (select ldap_id from accounts where account_type = 'CURRENCY' and account_type = 'PROJECT');

delete * from accounts where account_type = 'CURRENCY' and account_type = 'PROJECT';

ALTER TABLE transactions CHANGE eris_transaction_id transaction_id VARCHAR(255) NOT NULL;

ALTER TABLE eris RENAME TO fabric;

delete * from fabric;

ALTER TABLE fabric drop column address;

ALTER TABLE fabric drop index UKrpsbty0f34qu89n6juppia3ge;

alter table fabric drop column type;

alter table fabric add column certificate text not null;

alter table fabric add column id int(11) primary key;