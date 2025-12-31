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
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN-管理员, USER-普通用户',
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
