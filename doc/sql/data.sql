-- 管理员账号：admin / 123456
INSERT INTO `user` (`username`, `password`, `name`, `role`) VALUES
('admin', '$2a$10$NkM2CqXJYw9xXq5YpQzQ.uZxUqWxYxqXqXqXqXqXqXqXqXqXq', '管理员', 1);

-- 普通学生：20240001 / 123456
INSERT INTO `user` (`username`, `password`, `name`, `role`) VALUES
('20240001', '$2a$10$NkM2CqXJYw9xXq5YpQzQ.uZxUqWxYxqXqXqXqXqXqXqXqXqXq', '测试学生', 0);