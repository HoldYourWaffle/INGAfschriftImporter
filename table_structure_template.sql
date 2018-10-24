CREATE TABLE `afschriften` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `date` date NOT NULL,
  `name` text NOT NULL,
  `account_a` char(32) NOT NULL,
  `account_b` char(32) NOT NULL,
  `code` char(2) NOT NULL,
  `amount` decimal(65,2) NOT NULL,
  `kind` text NOT NULL,
  `description` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `afschriften`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;
