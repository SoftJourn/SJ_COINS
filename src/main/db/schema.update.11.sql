ALTER TABLE transaction_history MODIFY COLUMN amount BIGINT;
ALTER TABLE transaction_history MODIFY COLUMN fee BIGINT;
ALTER TABLE transaction_history MODIFY COLUMN gas_limit BIGINT;
ALTER TABLE transaction_history MODIFY COLUMN sequence BIGINT;
ALTER TABLE transaction_history CHANGE `transaction_signature` `signature` VARCHAR(255) ;
ALTER TABLE transaction_history DROP COLUMN identifier;
ALTER TABLE transaction_history DROP COLUMN function_name_hash;
ALTER TABLE transaction_history DROP COLUMN tx_type_call;