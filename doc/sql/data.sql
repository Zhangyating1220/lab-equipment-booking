-- 管理员账号：admin / 123456
INSERT INTO `user` (`username`, `password`, `name`, `role`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '管理员', 1);

-- 普通学生：20240001 / 123456
INSERT INTO `user` (`username`, `password`, `name`, `role`) VALUES
('20240001', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '测试学生', 0);

INSERT INTO `equipment` (`name`, `category`, `model`, `location`, `status`, `description`) VALUES
('倒置荧光显微镜', '显微镜', 'IX73', '实验楼A-101', 0, '用于细胞观察'),
('高速冷冻离心机', '离心机', '5424R', '实验楼B-203', 0, '最大转速15000rpm'),
('PCR仪', '分子生物学', 'T100', '实验楼C-305', 1, '维修中'),
('超净工作台', '其它', 'SW-CJ-2D', '实验楼A-102', 0, '细胞操作');