ALTER TABLE contracts
  ADD COLUMN active bit(1) NOT NULL DEFAULT b'1';