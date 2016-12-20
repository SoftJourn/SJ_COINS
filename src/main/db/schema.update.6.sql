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