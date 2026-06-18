-- 阶段 3 表结构存档（JPA ddl-auto=update 会自动建表，本文件仅供文档/答辩参考）

CREATE TABLE IF NOT EXISTS location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NULL,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(32) NOT NULL UNIQUE,
    level INT NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(9) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL,
    nickname VARCHAR(32) NULL,
    avatar_url VARCHAR(512) NULL,
    wechat VARCHAR(64) NULL,
    location_id BIGINT NULL,
    is_profile_complete TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_users_location FOREIGN KEY (location_id) REFERENCES location(id)
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    title VARCHAR(40) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    item_condition VARCHAR(32) NOT NULL,
    category VARCHAR(32) NOT NULL,
    description VARCHAR(500) NULL,
    cover_image_url VARCHAR(512) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT '在售',
    want_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_items_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT fk_items_location FOREIGN KEY (location_id) REFERENCES location(id)
);

CREATE TABLE IF NOT EXISTS item_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    url VARCHAR(512) NOT NULL,
    sort_order INT NOT NULL,
    CONSTRAINT fk_item_images_item FOREIGN KEY (item_id) REFERENCES items(id)
);
