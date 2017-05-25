SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sj_coins`
--

-- --------------------------------------------------------

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
CREATE TABLE IF NOT EXISTS `accounts` (
  `ldap_id` varchar(255) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `is_new` bit(1) NOT NULL DEFAULT b'1',
  `account_type` varchar(255) DEFAULT NULL,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`ldap_id`),
  KEY `ldap_id` (`ldap_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `contracts`
--

DROP TABLE IF EXISTS `contracts`;
CREATE TABLE IF NOT EXISTS `contracts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `abi` longtext,
  `code` longtext,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`id`),
  KEY `FKhr2kxnlv3eb3x30dm2l9fpvkh` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `contract_type`
--

DROP TABLE IF EXISTS `contract_type`;
CREATE TABLE IF NOT EXISTS `contract_type` (
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `eris`
--

DROP TABLE IF EXISTS `eris`;
CREATE TABLE IF NOT EXISTS `eris` (
  `address` varchar(255) NOT NULL,
  `priv_key` varchar(255) DEFAULT NULL,
  `pub_key` varchar(255) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `account_ldap_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`address`),
  UNIQUE KEY `UKrpsbty0f34qu89n6juppia3ge` (`type`,`account_ldap_id`),
  KEY `FK_account_id` (`account_ldap_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `instances`
--

DROP TABLE IF EXISTS `instances`;
CREATE TABLE IF NOT EXISTS `instances` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `contract_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `account_ldap_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `instances_address_uindex` (`address`),
  KEY `FKhr2kxnlv3eb3x30dm2l9fpvkg` (`contract_id`),
  KEY `FKhr2kxnlv3eb3x30dm2l9fpvdf` (`account_ldap_id`)
) ENGINE=InnoDB AUTO_INCREMENT=253 DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
CREATE TABLE IF NOT EXISTS `transactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_ldap_id` varchar(255) DEFAULT NULL,
  `destination_ldap_id` varchar(255) DEFAULT NULL,
  `amount` decimal(10,0) DEFAULT NULL,
  `comment` text,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(32) NOT NULL,
  `error` text,
  `remain` decimal(19,2) DEFAULT NULL,
  `eris_transaction_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK20w7wsg13u9srbq3bd7chfxdh` (`account_ldap_id`),
  KEY `FKcsb0kdvsdj55hikqj3unk926t` (`destination_ldap_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3381 DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `transaction_history`
--

DROP TABLE IF EXISTS `transaction_history`;
CREATE TABLE IF NOT EXISTS `transaction_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `block_number` bigint(20) DEFAULT NULL,
  `chain_id` varchar(255) DEFAULT NULL,
  `function_name` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `amount` bigint(20) DEFAULT NULL,
  `caller_address` varchar(255) DEFAULT NULL,
  `caller_pub_key` varchar(255) DEFAULT NULL,
  `calling_data` text,
  `contract_address` varchar(255) DEFAULT NULL,
  `fee` bigint(20) DEFAULT NULL,
  `gas_limit` bigint(20) DEFAULT NULL,
  `is_deploy` bit(1) DEFAULT NULL,
  `sequence` bigint(20) DEFAULT NULL,
  `signature` varchar(255) DEFAULT NULL,
  `tx_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `eris_tx_id_unique_index` (`tx_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4211 DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `tx_calling_data`
--

DROP TABLE IF EXISTS `tx_calling_data`;
CREATE TABLE IF NOT EXISTS `tx_calling_data` (
  `tx_id` bigint(20) NOT NULL,
  `calling_value` varchar(255) DEFAULT NULL,
  `function_name` varchar(255) NOT NULL,
  PRIMARY KEY (`tx_id`,`function_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `contracts`
--
ALTER TABLE `contracts`
  ADD CONSTRAINT `FK9hxn5k55mpp0j3yqt1ej4of5y` FOREIGN KEY (`type`) REFERENCES `contract_type` (`type`),
  ADD CONSTRAINT `FKhr2kxnlv3eb3x30dm2l9fpvkh` FOREIGN KEY (`type`) REFERENCES `contract_type` (`type`);

--
-- Constraints for table `eris`
--
ALTER TABLE `eris`
  ADD CONSTRAINT `FK55s0o6jfa48iqty8bc1nxxrf2` FOREIGN KEY (`account_ldap_id`) REFERENCES `accounts` (`ldap_id`),
  ADD CONSTRAINT `FK_account` FOREIGN KEY (`account_ldap_id`) REFERENCES `accounts` (`ldap_id`);

--
-- Constraints for table `instances`
--
ALTER TABLE `instances`
  ADD CONSTRAINT `FKhr2kxnlv3eb3x30dm2l9fpvdf` FOREIGN KEY (`account_ldap_id`) REFERENCES `accounts` (`ldap_id`),
  ADD CONSTRAINT `FKhr2kxnlv3eb3x30dm2l9fpvkg` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`);

--
-- Constraints for table `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `FK20w7wsg13u9srbq3bd7chfxdh` FOREIGN KEY (`account_ldap_id`) REFERENCES `accounts` (`ldap_id`),
  ADD CONSTRAINT `FKcsb0kdvsdj55hikqj3unk926t` FOREIGN KEY (`destination_ldap_id`) REFERENCES `accounts` (`ldap_id`),
  ADD CONSTRAINT `FKdry0jpptvcyggbc302yi4usb5` FOREIGN KEY (`account_ldap_id`) REFERENCES `accounts` (`ldap_id`),
  ADD CONSTRAINT `FKmssqe261eedjh507b7287aonv` FOREIGN KEY (`destination_ldap_id`) REFERENCES `accounts` (`ldap_id`),
  ADD CONSTRAINT `account_fk` FOREIGN KEY (`account_ldap_id`) REFERENCES `accounts` (`ldap_id`),
  ADD CONSTRAINT `destination_fk` FOREIGN KEY (`destination_ldap_id`) REFERENCES `accounts` (`ldap_id`);

--
-- Constraints for table `tx_calling_data`
--
ALTER TABLE `tx_calling_data`
  ADD CONSTRAINT `FKcdwwj4qxq3unv7cveilenig5d` FOREIGN KEY (`tx_id`) REFERENCES `transaction_history` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
