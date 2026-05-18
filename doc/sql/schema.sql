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