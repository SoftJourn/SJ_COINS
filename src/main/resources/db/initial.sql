CREATE TABLE IF NOT EXISTS accounts
(
    ldap_id VARCHAR(255) PRIMARY KEY NOT NULL,
    amount DECIMAL(10)
);
CREATE TABLE IF NOT EXISTS transactions
(
    id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    account_id VARCHAR(255),
    destination_id VARCHAR(255),
    amount DECIMAL(10),
    comment TEXT,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL,
    error TEXT,
    CONSTRAINT account_fk FOREIGN KEY (account_id) REFERENCES accounts (ldap_id),
    CONSTRAINT destination_fk FOREIGN KEY (destination_id) REFERENCES accounts (ldap_id)
);
CREATE INDEX account_fk ON transactions (account_id);
CREATE INDEX destination_fk ON transactions (destination_id);

-- UPDATE #1
ALTER TABLE accounts ADD fullName VARCHAR(255);
ALTER TABLE accounts ADD image VARCHAR(255);

-- UPDATE #2
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

-- UPDATE #3
ALTER TABLE accounts ADD type VARCHAR(32) NOT NULL DEFAULT 'REGULAR';
ALTER TABLE transactions MODIFY COLUMN id BIGINT(20) NOT NULL AUTO_INCREMENT;

-- UPDATE #4
ALTER TABLE accounts ADD deleted BIT(1) NOT NULL DEFAULT 0;
ALTER TABLE accounts CHANGE fullName full_name VARCHAR(255);
ALTER TABLE transactions ADD eris_transaction_id VARCHAR(255);

-- UPDATE #5
ALTER TABLE eris CHANGE privKey priv_key VARCHAR(255);
ALTER TABLE eris CHANGE pubKey pub_key VARCHAR(255);

ALTER TABLE accounts CHANGE type account_type VARCHAR(255);

ALTER TABLE transactions CHANGE account_id account_ldap_id VARCHAR(255);
ALTER TABLE transactions CHANGE destination_id destination_ldap_id VARCHAR(255);

-- UPDATE #6
ALTER TABLE sj_coins.accounts ADD is_new BIT(1) DEFAULT b'1' NOT NULL AFTER image;

-- UPDATE #7
CREATE TABLE contracts (
  id   BIGINT(20) NOT NULL AUTO_INCREMENT,
  abi  LONGTEXT,
  code LONGTEXT,
  name VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE instances (
  id          BIGINT(20) NOT NULL AUTO_INCREMENT,
  address     VARCHAR(255),
  contract_id BIGINT(20),
  PRIMARY KEY (id)
);

ALTER TABLE instances
  ADD CONSTRAINT FKhr2kxnlv3eb3x30dm2l9fpvkg FOREIGN KEY (contract_id) REFERENCES contracts (id);

ALTER TABLE contracts
  ADD COLUMN type VARCHAR(255);

CREATE TABLE contract_type (
  type VARCHAR(255) NOT NULL,
  PRIMARY KEY (type)
);

ALTER TABLE contracts
  ADD CONSTRAINT FKhr2kxnlv3eb3x30dm2l9fpvkh FOREIGN KEY (type) REFERENCES contract_type (type);

INSERT INTO contract_type(type) VALUES
  ('project'),
  ('currency');

-- UPDATE #8
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

-- UPDATE #9
ALTER TABLE contracts
  ADD COLUMN active bit(1) NOT NULL DEFAULT b'1';

-- UPDATE #10
ALTER TABLE transaction_history MODIFY COLUMN amount BIGINT(20);
ALTER TABLE transaction_history ADD COLUMN tx_type_call TINYINT;
ALTER TABLE transaction_history MODIFY COLUMN sequence BIGINT(20);
ALTER TABLE transaction_history MODIFY COLUMN gas_limit BIGINT(20);
ALTER TABLE transaction_history MODIFY COLUMN fee BIGINT(20);

-- UPDATE #11
ALTER TABLE transaction_history MODIFY COLUMN amount BIGINT;
ALTER TABLE transaction_history MODIFY COLUMN fee BIGINT;
ALTER TABLE transaction_history MODIFY COLUMN gas_limit BIGINT;
ALTER TABLE transaction_history MODIFY COLUMN sequence BIGINT;
ALTER TABLE transaction_history CHANGE `transaction_signature` `signature` VARCHAR(255) ;
ALTER TABLE transaction_history DROP COLUMN identifier;
ALTER TABLE transaction_history DROP COLUMN function_name_hash;
ALTER TABLE transaction_history DROP COLUMN tx_type_call;

-- UPDATE #12
CREATE UNIQUE INDEX instances_address_uindex ON sj_coins.instances (address);

-- UPDATE #13
CREATE UNIQUE INDEX eris_tx_id_unique_index ON sj_coins.transaction_history (tx_id);

-- UPDATE #14
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

-- UPDATE #15
UPDATE accounts SET image = '/account/default';

INSERT INTO sj_coins.contracts (abi, code, name, type) VALUES ('[{"constant":false,"inputs":[{"name":"spender","type":"address"},{"name":"amount","type":"uint256"}],"name":"approve","outputs":[],"type":"function"},{"constant":false,"inputs":[{"name":"accounts","type":"address[]"},{"name":"amount","type":"uint256"}],"name":"distribute","outputs":[{"name":"success","type":"bool"}],"type":"function"},{"constant":false,"inputs":[{"name":"from","type":"address"},{"name":"to","type":"address"},{"name":"amount","type":"uint256"}],"name":"transferFrom","outputs":[{"name":"success","type":"bool"}],"type":"function"},{"constant":false,"inputs":[{"name":"_tokenColor","type":"uint8"}],"name":"setColor","outputs":[],"type":"function"},{"constant":true,"inputs":[],"name":"tokenColor","outputs":[{"name":"","type":"uint8"}],"type":"function"},{"constant":false,"inputs":[{"name":"spender","type":"address"},{"name":"amount","type":"uint256"}],"name":"approveAndCall","outputs":[{"name":"success","type":"bool"}],"type":"function"},{"constant":false,"inputs":[{"name":"owner","type":"address"},{"name":"amount","type":"uint256"}],"name":"mint","outputs":[],"type":"function"},{"constant":true,"inputs":[{"name":"owner","type":"address"}],"name":"balanceOf","outputs":[{"name":"balance","type":"uint256"}],"type":"function"},{"constant":false,"inputs":[],"name":"getColor","outputs":[{"name":"","type":"uint8"}],"type":"function"},{"constant":false,"inputs":[{"name":"receiver","type":"address"},{"name":"amount","type":"uint256"}],"name":"transfer","outputs":[{"name":"","type":"bool"}],"type":"function"},{"constant":true,"inputs":[{"name":"owner","type":"address"},{"name":"spender","type":"address"}],"name":"allowance","outputs":[{"name":"remaining","type":"uint256"}],"type":"function"},{"inputs":[{"name":"_tokenColor","type":"uint8"}],"type":"constructor"},{"anonymous":false,"inputs":[{"indexed":false,"name":"from","type":"address"},{"indexed":false,"name":"to","type":"address"},{"indexed":false,"name":"value","type":"uint256"}],"name":"Transfer","type":"event"},{"anonymous":false,"inputs":[{"indexed":false,"name":"from","type":"address"},{"indexed":false,"name":"to","type":"address"},{"indexed":false,"name":"value","type":"uint256"}],"name":"Approval","type":"event"}]', '6060604052604051602080610c06833981016040528080519060200190919050505b33600060006101000a81548173ffffffffffffffffffffffffffffffffffffffff0219169083021790555080600060146101000a81548160ff021916908302179055505b50610b92806100746000396000f3606060405236156100ab576000357c010000000000000000000000000000000000000000000000000000000090048063095ea7b3146100ad5780631826c119146100ce57806323b872dd1461013e57806323b9f3bb1461017c578063245f592b146101945780633177029f146101ba57806340c10f19146101ef57806370a08231146102105780639a86139b1461023c578063a9059cbb14610262578063dd62ed3e14610297576100ab565b005b6100cc600480803590602001909190803590602001909190505061079f565b005b61012860048080359060200190820180359060200191919080806020026020016040519081016040528093929190818152602001838360200280828437820191505050505050909091908035906020019091905050610a8e565b6040518082815260200191505060405180910390f35b610166600480803590602001909190803590602001909190803590602001909190505061054c565b6040518082815260200191505060405180910390f35b61019260048080359060200190919050506102df565b005b6101a160048050506102cc565b604051808260ff16815260200191505060405180910390f35b6101d96004808035906020019091908035906020019091905050610879565b6040518082815260200191505060405180910390f35b61020e6004808035906020019091908035906020019091905050610373565b005b6102266004808035906020019091905050610761565b6040518082815260200191505060405180910390f35b6102496004805050610357565b604051808260ff16815260200191505060405180910390f35b6102816004808035906020019091908035906020019091905050610410565b6040518082815260200191505060405180910390f35b6102b66004808035906020019091908035906020019091905050610a25565b6040518082815260200191505060405180910390f35b600060149054906101000a900460ff1681565b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561033b57610002565b80600060146101000a81548160ff021916908302179055505b50565b6000600060149054906101000a900460ff169050610370565b90565b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156103cf5761040c565b80600160005060008473ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828282505401925050819055505b5050565b600081600160005060003373ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000505410156104525760009050610546565b81600160005060003373ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008282825054039250508190555081600160005060008573ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828282505401925050819055507fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef338484604051808473ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390a160019050610546565b92915050565b600081600160005060008673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005054101580156105e6575081600260005060008673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060003373ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000505410155b80156105f25750600082115b156107515781600160005060008573ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008282825054019250508190555081600160005060008673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008282825054039250508190555081600260005060008673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060003373ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828282505403925050819055507fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef848484604051808473ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390a16001905061075a565b6000905061075a565b9392505050565b6000600160005060008373ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005054905061079a565b919050565b80600260005060003373ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008473ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600050819055507f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925338383604051808473ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390a1610875565b5050565b600081600260005060003373ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008573ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600050819055507f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925338484604051808473ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390a18273ffffffffffffffffffffffffffffffffffffffff16638f4ffcb1338430604051847c0100000000000000000000000000000000000000000000000000000000028152600401808473ffffffffffffffffffffffffffffffffffffffff1681526020018381526020018273ffffffffffffffffffffffffffffffffffffffff168152602001806020018281038252600181526020018060008152602001506020019450505050506020604051808303816000876161da5a03f11561000257505050604051805190602001509050610a1f565b92915050565b6000600260005060008473ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008373ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600050549050610a88565b92915050565b60006000600083600160005060003373ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600050541015610ad45760009250610b8a565b845184049150600090505b8451811015610b815781600160005060003373ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082828250540392505081905550816001600050600087848151811015610002579060200190602002015173ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828282505401925050819055506001810190508050610adf565b60019250610b8a565b50509291505056', 'Coin', 'currency');
INSERT INTO sj_coins.contracts (abi, code, name, type) VALUES ('[{"constant":false,"inputs":[],"name":"checkGoalReached","outputs":[{"name":"","type":"bool"}],"type":"function"},{"constant":true,"inputs":[],"name":"creator","outputs":[{"name":"","type":"address"}],"type":"function"},{"constant":true,"inputs":[],"name":"deadline","outputs":[{"name":"","type":"uint256"}],"type":"function"},{"constant":true,"inputs":[],"name":"beneficiary","outputs":[{"name":"","type":"address"}],"type":"function"},{"constant":true,"inputs":[],"name":"getTokensCount","outputs":[{"name":"","type":"uint256"}],"type":"function"},{"constant":true,"inputs":[{"name":"_token","type":"address"}],"name":"isTokenAccumulated","outputs":[{"name":"","type":"bool"}],"type":"function"},{"constant":true,"inputs":[],"name":"fundingGoal","outputs":[{"name":"","type":"uint256"}],"type":"function"},{"constant":true,"inputs":[],"name":"amountRaised","outputs":[{"name":"","type":"uint256"}],"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_value","type":"uint256"},{"name":"_token","type":"address"},{"name":"_extraData","type":"bytes"}],"name":"receiveApproval","outputs":[{"name":"","type":"bool"}],"type":"function"},{"constant":true,"inputs":[{"name":"","type":"address"}],"name":"tokenAmounts","outputs":[{"name":"","type":"uint256"}],"type":"function"},{"constant":true,"inputs":[],"name":"closeOnGoalReached","outputs":[{"name":"","type":"bool"}],"type":"function"},{"constant":true,"inputs":[{"name":"","type":"uint256"}],"name":"tokensAccumulated","outputs":[{"name":"","type":"address"}],"type":"function"},{"constant":true,"inputs":[{"name":"","type":"address"},{"name":"","type":"address"}],"name":"balanceOf","outputs":[{"name":"","type":"uint256"}],"type":"function"},{"constant":false,"inputs":[],"name":"safeWithdrawal","outputs":[{"name":"","type":"bool"}],"type":"function"},{"inputs":[{"name":"ifSuccessfulSendTo","type":"address"},{"name":"fundingGoalInTokens","type":"uint256"},{"name":"durationInMinutes","type":"uint256"},{"name":"onGoalReached","type":"bool"},{"name":"addressOfTokensAccumulated","type":"address[]"}],"type":"constructor"},{"anonymous":false,"inputs":[{"indexed":false,"name":"beneficiary","type":"address"},{"indexed":false,"name":"amountRaised","type":"uint256"}],"name":"GoalReached","type":"event"},{"anonymous":false,"inputs":[{"indexed":false,"name":"backer","type":"address"},{"indexed":false,"name":"token","type":"address"},{"indexed":false,"name":"amount","type":"uint256"},{"indexed":false,"name":"isContribution","type":"bool"}],"name":"FundTransfer","type":"event"}]', '60606040526000600960006101000a81548160ff021916908302179055506000600960016101000a81548160ff0219169083021790555060405161132f38038061132f833981016040528080519060200190919080519060200190919080519060200190919080519060200190919080518201919060200150505b33600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff0219169083021790555084600060006101000a81548173ffffffffffffffffffffffffffffffffffffffff0219169083021790555083600260005081905550603c8302420160046000508190555081600560006101000a81548160ff0219169083021790555061010a8161011f565b505b5050505050611049806102e66000396000f35b600060006000600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141580156101d15750600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614155b156101df57600092506102df565b600091505b83518210156102d757838281518110156100025790602001906020020151905060066000508054806001018281815481835581811511610256578183600052602060002091820191016102559190610237565b808211156102515760008181506000905550600101610237565b5090565b5b5050509190906000526020600020900160005b83909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff02191690830217905550506000600860005060008373ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000508190555081806001019250506101e4565b8192506102df565b505091905056606060405236156100cc576000357c01000000000000000000000000000000000000000000000000000000009004806301cb3b20146100ce57806302d05d3f146100f157806329dcb0cf1461012a57806338af3eed1461014d5780633962f82d146101865780637a2d5add146101a95780637a3a0e84146101d55780637b3e5e7b146101f85780638f4ffcb11461021b578063a0e2e5f6146102a0578063c5064cb0146102cc578063e4e0ef35146102ef578063f7888aec14610331578063fd6b7ef814610366576100cc565b005b6100db6004805050610850565b6040518082815260200191505060405180910390f35b6100fe60048050506103af565b604051808273ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b61013760048050506103e7565b6040518082815260200191505060405180910390f35b61015a6004805050610389565b604051808273ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b610193600480505061048b565b6040518082815260200191505060405180910390f35b6101bf60048080359060200190919050506104a0565b6040518082815260200191505060405180910390f35b6101e260048050506103d5565b6040518082815260200191505060405180910390f35b61020560048050506103de565b6040518082815260200191505060405180910390f35b61028a6004808035906020019091908035906020019091908035906020019091908035906020019082018035906020019191908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505090909190505061056b565b6040518082815260200191505060405180910390f35b6102b66004808035906020019091905050610470565b6040518082815260200191505060405180910390f35b6102d960048050506103f0565b6040518082815260200191505060405180910390f35b6103056004808035906020019091905050610403565b604051808273ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b6103506004808035906020019091908035906020019091905050610445565b6040518082815260200191505060405180910390f35b6103736004805050610963565b6040518082815260200191505060405180910390f35b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60026000505481565b60036000505481565b60046000505481565b600560009054906101000a900460ff1681565b600660005081815481101561000257906000526020600020900160005b9150909054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6007600050602052816000526040600020600050602052806000526040600020600091509150505481565b60086000506020528060005260406000206000915090505481565b6000600660005080549050905061049d565b90565b60006000600960019054906101000a900460ff16156104c25760009150610565565b600090505b60066000508054905081101561055c57600660005081815481101561000257906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff16141561054f5760019150610565565b80806001019150506104c7565b60009150610565565b50919050565b60006000610578846104a0565b15156105875760009150610847565b3090508373ffffffffffffffffffffffffffffffffffffffff166323b872dd878388604051847c0100000000000000000000000000000000000000000000000000000000028152600401808473ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff16815260200182815260200193505050506020604051808303816000876161da5a03f115610002575050506040518051906020015015156106485760009150610847565b6000600760005060008873ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005054141561070d5784600760005060008873ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005081905550610774565b84600760005060008873ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828282505401925050819055505b84600860005060008673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828282505401925050819055508460036000828282505401925050819055507f1fe43a085f5507370c77d08b78179e99076ad281426b5189351310be1b72625c8685876001604051808573ffffffffffffffffffffffffffffffffffffffff1681526020018473ffffffffffffffffffffffffffffffffffffffff16815260200183815260200182815260200194505050505060405180910390a160019150610847565b50949350505050565b6000600460005054421015806108725750600560009054906101000a900460ff165b1561094957600260005054600360005054101515610920576001600960006101000a81548160ff021916908302179055507fec3f991caf7857d61663fd1bba1739e04abd4781238508cde554bb849d790c85600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16600360005054604051808373ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019250505060405180910390a15b60046000505442101515610948576001600960016101000a81548160ff021916908302179055505b5b600960019054906101000a900460ff169050610960565b90565b60006000600060006000610975610850565b15156109845760009450611042565b600960009054906101000a900460ff161515610ce1576000935083505b600660005080549050841015610cd857600660005084815481101561000257906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1692508250600073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1614158015610a8857506000600760005060003373ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008573ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000505414155b15610ccb57600760005060003373ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008473ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005054915081506000600760005060003373ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008573ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600050819055506000821115610cca578273ffffffffffffffffffffffffffffffffffffffff1663a9059cbb3384604051837c0100000000000000000000000000000000000000000000000000000000028152600401808373ffffffffffffffffffffffffffffffffffffffff168152602001828152602001925050506020604051808303816000876161da5a03f115610002575050506040518051906020015015610c6a577f1fe43a085f5507370c77d08b78179e99076ad281426b5189351310be1b72625c3384846000604051808573ffffffffffffffffffffffffffffffffffffffff1681526020018473ffffffffffffffffffffffffffffffffffffffff16815260200183815260200182815260200194505050505060405180910390a1610cc9565b81600760005060003373ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060008573ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600050819055505b5b5b83806001019450506109a1565b60019450611042565b600960009054906101000a900460ff168015610d4a57503373ffffffffffffffffffffffffffffffffffffffff16600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16145b1561103957600190506000935083505b60066000508054905084101561103057600073ffffffffffffffffffffffffffffffffffffffff16600660005085815481101561000257906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614151561102357600660005084815481101561000257906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1692508250600860005060008473ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005054915081508273ffffffffffffffffffffffffffffffffffffffff1663a9059cbb600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1684604051837c0100000000000000000000000000000000000000000000000000000000028152600401808373ffffffffffffffffffffffffffffffffffffffff168152602001828152602001925050506020604051808303816000876161da5a03f11561000257505050604051805190602001501561101d577f1fe43a085f5507370c77d08b78179e99076ad281426b5189351310be1b72625c600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16600660005086815481101561000257906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff166003600050546000604051808573ffffffffffffffffffffffffffffffffffffffff1681526020018473ffffffffffffffffffffffffffffffffffffffff16815260200183815260200182815260200194505050505060405180910390a1600860005060008473ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060005060009055611022565b610002565b5b8380600101945050610d5a565b60019450611042565b60009450611042565b505050509056', 'Crowdsale', 'project');

-- UPDATE #16
ALTER TABLE transactions ADD COLUMN type varchar(32);
