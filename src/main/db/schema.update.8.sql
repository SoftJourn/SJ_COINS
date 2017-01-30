CREATE TABLE transaction_history
(
  id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  block_number BIGINT(20),
  chain_id VARCHAR(255),
  function_name VARCHAR(255),
  time DATETIME,
  amount VARCHAR(255),
  caller_address VARCHAR(255),
  caller_pub_key VARCHAR(255),
  calling_data TEXT,
  contract_address VARCHAR(255),
  fee VARCHAR(255),
  function_name_hash VARCHAR(255),
  gas_limit VARCHAR(255),
  identifier VARCHAR(255),
  is_deploy BIT(1),
  sequence VARCHAR(255),
  transaction_signature VARCHAR(255),
  tx_id VARCHAR(255)
);

CREATE TABLE tx_calling_data
(
  tx_id BIGINT(20) NOT NULL,
  calling_value VARCHAR(255),
  function_name VARCHAR(255) NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (tx_id, function_name),
  CONSTRAINT FKcdwwj4qxq3unv7cveilenig5d FOREIGN KEY (tx_id) REFERENCES transaction_history (id)
);

ALTER TABLE instances
  ADD COLUMN name VARCHAR(255);