-- ========================================
-- 图书管理系统数据库初始化脚本
-- MySQL 8.x
-- ========================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_db;

-- 确保使用正确的字符集
SET NAMES utf8mb4;

-- ========================================
-- 用户表
-- ========================================
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN-管理员, LIBRARIAN-馆员, USER-普通用户',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 图书分类表
-- ========================================
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- ========================================
-- 图书表
-- ========================================
CREATE TABLE book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图书ID',
    isbn VARCHAR(20) UNIQUE COMMENT 'ISBN号',
    title VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(100) COMMENT '作者',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_date DATE COMMENT '出版日期',
    category_id BIGINT COMMENT '分类ID',
    price DECIMAL(10,2) COMMENT '价格',
    total_count INT NOT NULL DEFAULT 1 COMMENT '总库存',
    available_count INT NOT NULL DEFAULT 1 COMMENT '可借数量',
    description TEXT COMMENT '图书简介',
    cover_url VARCHAR(500) COMMENT '封面图片URL',
    location VARCHAR(50) COMMENT '存放位置',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_category_id (category_id),
    INDEX idx_isbn (isbn),
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- ========================================
-- 借阅记录表
-- ========================================
CREATE TABLE borrow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '借阅记录ID',
    user_id BIGINT NOT NULL COMMENT '借阅用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    borrow_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借阅日期',
    due_date DATETIME NOT NULL COMMENT '应还日期',
    return_date DATETIME COMMENT '实际归还日期',
    status INT NOT NULL DEFAULT 0 COMMENT '状态: 0-借阅中, 1-已归还, 2-逾期',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status),
    INDEX idx_borrow_date (borrow_date),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入管理员账号 (密码: admin123, BCrypt加密)
-- 注意: 以下密码hash对应明文 "admin123"
INSERT INTO user (username, password, real_name, email, role, status) VALUES
('admin', '$2a$10$EqKcp1WFKVQISheBxkVJaOKzAJHvVQd4BY0zMH0cRv6TQrJQdNDPy', '系统管理员', 'admin@library.com', 'ADMIN', 1),
('librarian', '$2a$10$EqKcp1WFKVQISheBxkVJaOKzAJHvVQd4BY0zMH0cRv6TQrJQdNDPy', '图书馆员', 'librarian@library.com', 'LIBRARIAN', 1),
('user1', '$2a$10$EqKcp1WFKVQISheBxkVJaOKzAJHvVQd4BY0zMH0cRv6TQrJQdNDPy', '测试用户', 'user1@library.com', 'USER', 1);

-- 插入图书分类
INSERT INTO category (name, description, sort_order) VALUES
('文学', '文学类图书', 1),
('计算机', '计算机科学与技术', 2),
('历史', '历史类图书', 3),
('经济', '经济管理类', 4),
('艺术', '艺术设计类', 5);

-- 插入子分类
INSERT INTO category (name, description, parent_id, sort_order) VALUES
('小说', '小说类', 1, 1),
('诗歌', '诗歌类', 1, 2),
('编程语言', '编程语言类', 2, 1),
('数据库', '数据库技术', 2, 2),
('人工智能', 'AI与机器学习', 2, 3);

-- 插入示例图书
INSERT INTO book (isbn, title, author, publisher, publish_date, category_id, price, total_count, available_count, description, location) VALUES
('978-7-111-12345-1', 'Java核心技术卷I', 'Cay S. Horstmann', '机械工业出版社', '2023-01-01', 8, 149.00, 5, 5, 'Java经典入门书籍', 'A-01-01'),
('978-7-111-12345-2', 'Spring实战', 'Craig Walls', '人民邮电出版社', '2022-06-01', 8, 89.00, 3, 3, 'Spring框架权威指南', 'A-01-02'),
('978-7-111-12345-3', 'MySQL必知必会', 'Ben Forta', '人民邮电出版社', '2021-03-01', 9, 59.00, 4, 4, 'MySQL入门经典', 'A-02-01'),
('978-7-111-12345-4', '深度学习', 'Ian Goodfellow', '人民邮电出版社', '2020-08-01', 10, 168.00, 2, 2, '深度学习领域圣经', 'A-02-02'),
('978-7-111-12345-5', '红楼梦', '曹雪芹', '人民文学出版社', '2019-01-01', 6, 68.00, 10, 10, '中国古典四大名著之一', 'B-01-01');

-- ========================================
-- 收藏表
-- ========================================
CREATE TABLE favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_book (user_id, book_id),
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ========================================
-- 通知表
-- ========================================
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(30) NOT NULL COMMENT '通知类型: DUE_REMINDER-到期提醒, OVERDUE_NOTICE-逾期通知, RETURN_SUCCESS-归还成功, BORROW_SUCCESS-借阅成功, FINE_NOTICE-罚款通知, SYSTEM-系统通知',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content VARCHAR(500) NOT NULL COMMENT '通知内容',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    borrow_record_id BIGINT COMMENT '关联的借阅记录ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (borrow_record_id) REFERENCES borrow_record(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ========================================
-- 操作日志表
-- ========================================
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    module VARCHAR(50) COMMENT '操作模块',
    operation_type VARCHAR(30) COMMENT '操作类型',
    description VARCHAR(255) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT 'HTTP方法',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人用户名',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    status INT DEFAULT 1 COMMENT '状态: 0-失败, 1-成功',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_operator_id (operator_id),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ========================================
-- 预约表
-- ========================================
CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT '状态: WAITING-等待中, NOTIFIED-已通知, FULFILLED-已完成, CANCELLED-已取消, EXPIRED-已过期',
    queue_position INT NOT NULL COMMENT '队列位置',
    notified_at DATETIME COMMENT '通知时间',
    expires_at DATETIME COMMENT '过期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_book_status (book_id, status),
    INDEX idx_user_status (user_id, status),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书预约表';

-- ========================================
-- 公告表
-- ========================================
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT COMMENT '公告内容',
    type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '类型: NORMAL-普通, IMPORTANT-重要, ACTIVITY-活动, MAINTENANCE-维护',
    pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
    status INT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布',
    publisher_id BIGINT COMMENT '发布人ID',
    publisher_name VARCHAR(50) COMMENT '发布人用户名',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_pinned (pinned),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 插入示例公告
INSERT INTO announcement (title, content, type, pinned, status, publisher_id, publisher_name) VALUES
('欢迎使用智慧图书馆管理系统', '智慧图书馆管理系统正式上线，为您提供便捷的图书借阅服务。系统支持图书查询、在线借阅、预约等功能，欢迎使用！', 'IMPORTANT', 1, 1, 1, 'admin'),
('春节假期图书馆开放时间通知', '春节期间（2月9日-2月17日），图书馆开放时间调整为9:00-17:00，请合理安排借阅时间。', 'NORMAL', 0, 1, 1, 'admin'),
('新书到馆通知', '本月新增计算机、文学、历史类图书共50余册，欢迎读者前来借阅。', 'ACTIVITY', 0, 1, 1, 'admin');

-- ========================================
-- 评论表
-- ========================================
CREATE TABLE IF NOT EXISTS review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    rating INT NOT NULL COMMENT '评分(1-5)',
    content VARCHAR(1000) NOT NULL COMMENT '评论内容',
    likes INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-隐藏, 1-显示',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_book (user_id, book_id),
    INDEX idx_book_id (book_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书评论表';

-- 插入示例评论
INSERT INTO review (user_id, book_id, rating, content, likes) VALUES
(3, 1, 5, 'Java核心技术是学习Java的必读经典，内容详实，讲解深入浅出，非常推荐！', 12),
(3, 5, 5, '红楼梦是中国古典文学的巅峰之作，每次阅读都有新的感悟。', 28),
(3, 4, 4, '深度学习领域的圣经级著作，数学推导严谨，适合有一定基础的读者。', 15);

-- ========================================
-- 罚款规则表
-- ========================================
CREATE TABLE IF NOT EXISTS fine_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    daily_amount DECIMAL(10,2) NOT NULL DEFAULT 0.50 COMMENT '每日罚金（元/天）',
    max_amount DECIMAL(10,2) NOT NULL DEFAULT 100.00 COMMENT '封顶金额（元），0表示不封顶',
    grace_days INT NOT NULL DEFAULT 0 COMMENT '免罚天数（宽限期）',
    description VARCHAR(500) COMMENT '规则描述/备注',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='罚款规则表';

-- 插入默认罚款规则
INSERT INTO fine_rule (daily_amount, max_amount, grace_days, description, enabled) VALUES
(0.50, 100.00, 0, '默认罚款规则：每日0.5元，封顶100元，无宽限期', 1);

-- ========================================
-- 罚款记录表
-- ========================================
CREATE TABLE IF NOT EXISTS fine_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '罚款记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    borrow_id BIGINT NOT NULL COMMENT '借阅记录ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '罚款金额',
    overdue_days INT NOT NULL COMMENT '逾期天数',
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT '状态: UNPAID-未缴, PAID-已缴, WAIVED-已免除',
    paid_at DATETIME COMMENT '缴费时间',
    waived_at DATETIME COMMENT '免除时间',
    waive_reason VARCHAR(500) COMMENT '免除原因',
    operator_id BIGINT COMMENT '操作人ID（免除操作时记录）',
    operator_name VARCHAR(50) COMMENT '操作人用户名',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_borrow_id (borrow_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (borrow_id) REFERENCES borrow_record(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='罚款记录表';
