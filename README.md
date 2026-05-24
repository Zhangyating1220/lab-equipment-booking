# lab-equipment-booking
实验室设备预约系统 - 学生可预约设备，管理员审批并查看使用记录。

# 技术栈声明
后端：Spring Boot 3.5.14 + MyBatis-Plus 3.5.5 + JWT
前端：Vue 3 + Vite + Element Plus + Axios + Vue Router
数据库：MySQL 9.7
中间件：Redis（本项目已配置但未使用）

# 环境要求
JDK 17+
Node.js 18+

# 本地部署步骤
1. 数据库初始化
方式一：CREATE DATABASE lab_booking;
USE lab_booking;
-- 执行doc/sql/schema.sql建表
-- 执行doc/sql/data.sql初始化测试数据
方式二：或者直接执行项目根目录下的run_sql.ps1脚本（Windows PowerShell）
2. 启动 Redis（可以不使用）
redis-server
保持窗口运行，或安装为Windows服务
3. 后端启动
cd backend
# 修改 src/main/resources/application.yml 中的数据库账号密码
# 默认配置：username=root, password=Zyt041109
方式一（IDE）：运行EquipmentBookingApplication.java
（D:\lab-equipment-booking\backend\src\main\java\com\lab\equipment_booking\EquipmentBookingApplication.java）
方式二（命令行）：mvn spring-boot:run
4. 前端启动
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
5.测试账号
管理员： admin / 123456
普通学生： 20240001 / 123456

# 项目结构
lab-equipment-booking/
├── backend/                    # 后端代码
│   ├── src/main/java/            # Java 源代码
│   ├── src/main/resources/       # 配置文件
│   └── pom.xml                # Maven 依赖配置
├── frontend/                    # 前端代码
│   ├── src/                    # Vue 源代码
│   ├── package.json             # npm 依赖配置
│   └── vite.config.js             # Vite 构建配置
├── doc/                         # 文档目录
│   └── sql/                     # 数据库脚本
└── README.md                 # 项目说明文档

# 功能模块
用户管理：登录、注册、身份鉴权（学生/管理员）
设备管理：设备CRUD、列表查询（支持按类别、状态筛选）、状态管理（可用/维修中/已废弃）
预约管理：学生查看可预约时段、提交预约、取消预约、时间冲突校验
审批模块：管理员审批预约（通过/拒绝）、查看待审批列表、违规管理
使用记录：设备使用记录生成（支持自动/手动方式）、历史查询、按时长统计、超时释放

# 说明
后端端口默认8080，前端端口默认5173
数据库名称为lab_booking
数据库连接配置在application.yml 中，默认用户名root，密码Zyt041109
测试账号密码默认为123456
！注意：启动后端前请确保MySQL和Redis服务已运行
