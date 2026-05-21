CREATE DATABASE IF NOT EXISTS lab_booking;
USE lab_booking;

CREATE TABLE `user` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `username` varchar(32) NOT NULL UNIQUE,
  `password` varchar(128) NOT NULL,
  `name` varchar(32) NOT NULL,
  `role` tinyint NOT NULL DEFAULT 0,
  `phone` varchar(16),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `equipment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `category` VARCHAR(32) NOT NULL,
  `model` VARCHAR(64),
  `location` VARCHAR(128),
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-可用,1-维修中,2-已废弃',
  `description` VARCHAR(256),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);
<<<<<<< HEAD
-- 预约表
CREATE TABLE `booking` (
=======

CREATE TABLE `reservation` (
>>>>>>> origin/main
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `equipment_id` BIGINT NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
<<<<<<< HEAD
  `reason` VARCHAR(256),
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-待审批,1-已通过,2-已拒绝,3-已取消',
  `reject_reason` VARCHAR(256),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`equipment_id`) REFERENCES `equipment`(`id`)
=======
  `purpose` VARCHAR(256),
  `status` TINYINT NOT NULL DEFAULT 0,
  `reject_reason` VARCHAR(256),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
>>>>>>> origin/main
);