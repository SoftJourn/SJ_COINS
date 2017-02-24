ALTER TABLE transaction_history MODIFY COLUMN amount BIGINT(20);
ALTER TABLE transaction_history ADD COLUMN tx_type_call TINYINT;
ALTER TABLE transaction_history MODIFY COLUMN sequence BIGINT(20);
ALTER TABLE transaction_history MODIFY COLUMN gas_limit BIGINT(20);
ALTER TABLE transaction_history MODIFY COLUMN fee BIGINT(20);
