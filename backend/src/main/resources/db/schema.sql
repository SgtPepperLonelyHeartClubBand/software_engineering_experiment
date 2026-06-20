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

CREATE TABLE IF NOT EXISTS trade_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    cancelled_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_trade_orders_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_trade_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT fk_trade_orders_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    created_at DATETIME NULL,
    CONSTRAINT uk_favorites_user_item UNIQUE (user_id, item_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_favorites_item FOREIGN KEY (item_id) REFERENCES items(id)
);

CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    last_message VARCHAR(220) NULL,
    last_message_at DATETIME NULL,
    buyer_unread_count INT NOT NULL DEFAULT 0,
    seller_unread_count INT NOT NULL DEFAULT 0,
    buyer_hidden TINYINT(1) NOT NULL DEFAULT 0,
    seller_hidden TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT uk_conversations_item_buyer_seller UNIQUE (item_id, buyer_id, seller_id),
    CONSTRAINT fk_conversations_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_conversations_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT fk_conversations_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content VARCHAR(200) NOT NULL,
    quote_message_id BIGINT NULL,
    recalled TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_chat_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_chat_messages_recipient FOREIGN KEY (recipient_id) REFERENCES users(id),
    CONSTRAINT fk_chat_messages_quote FOREIGN KEY (quote_message_id) REFERENCES chat_messages(id)
);

CREATE TABLE IF NOT EXISTS system_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(64) NOT NULL,
    content VARCHAR(300) NOT NULL,
    icon VARCHAR(32) NOT NULL DEFAULT 'bell',
    item_id BIGINT NULL,
    conversation_id BIGINT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    read_at DATETIME NULL,
    created_at DATETIME NULL,
    CONSTRAINT fk_system_notifications_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_system_notifications_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_system_notifications_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id)
);
