-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS carrot_market CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE carrot_market;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    username VARCHAR(50),
    location VARCHAR(255),
    phone_number VARCHAR(20),
    profile_image_url VARCHAR(200),
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    manner_temperature DOUBLE DEFAULT 36.5,
    is_active BIT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_location (location),
    INDEX idx_phone (phone_number)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 상품 테이블
CREATE TABLE IF NOT EXISTS products (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        seller_id BIGINT NOT NULL,
                                        title VARCHAR(255) NOT NULL,
    description TEXT,
    price INT NOT NULL,
    location VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE',
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    chat_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_seller (seller_id),
    INDEX idx_status (status),
    INDEX idx_location (location),
    INDEX idx_created_at (created_at),
    FULLTEXT INDEX idx_title_desc (title, description)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 상품 이미지 테이블 (1:N 관계)
CREATE TABLE IF NOT EXISTS product_images (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              product_id BIGINT NOT NULL,
                                              image_url VARCHAR(255) NOT NULL,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product (product_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 상품 좋아요 테이블
CREATE TABLE IF NOT EXISTS product_likes (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             user_id BIGINT NOT NULL,
                                             product_id BIGINT NOT NULL,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product (user_id, product_id),
    INDEX idx_user (user_id),
    INDEX idx_product (product_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 채팅방 테이블
CREATE TABLE IF NOT EXISTS chat_rooms (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          product_id BIGINT NOT NULL,
                                          buyer_id BIGINT NOT NULL,
                                          seller_id BIGINT NOT NULL,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_product_buyer (product_id, buyer_id),
    INDEX idx_buyer (buyer_id),
    INDEX idx_seller (seller_id),
    INDEX idx_product (product_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 채팅 메시지 테이블
CREATE TABLE IF NOT EXISTS chat_messages (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             chat_room_id BIGINT NOT NULL,
                                             sender_id BIGINT NOT NULL,
                                             message TEXT NOT NULL,
                                             is_read TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chat_room (chat_room_id),
    INDEX idx_sender (sender_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_read (is_read)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 샘플 데이터 삽입
-- 비밀번호는 'password123'을 bcrypt로 인코딩한 값입니다
INSERT INTO users (email, password, nickname, username, location, phone_number, manner_temperature, role)
VALUES ('seller1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye/J9z1dDMrkLXKwUcXxcHFQEQRXPLCEa', '판매자1', '홍길동', '서울시 강남구',
        '010-1234-5678', 36.5, 'USER'),
       ('buyer1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye/J9z1dDMrkLXKwUcXxcHFQEQRXPLCEa', '구매자1', '김철수', '서울시 서초구',
        '010-9876-5432', 37.0, 'USER'),
       ('admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye/J9z1dDMrkLXKwUcXxcHFQEQRXPLCEa', '관리자', '관리자', '서울시 중구',
        '010-0000-0000', 40.0, 'ADMIN');


INSERT INTO products (seller_id, title, description, price, location, category, status)
VALUES (1, '아이폰 14 Pro 128GB', '깨끗하게 사용했습니다. 케이스 끼고 사용해서 스크래치 없습니다.', 1000000, '서울시 강남구', '디지털/가전', 'ON_SALE'),
       (1, '맥북 프로 M2', '2023년 구매, 거의 새것입니다. 박스 포함', 2000000, '서울시 강남구', '디지털/가전', 'ON_SALE'),
       (1, '에어팟 프로 2세대', '개봉만 했습니다. 미개봉 상태', 300000, '서울시 강남구', '디지털/가전', 'ON_SALE');


-- 상품 이미지 샘플 데이터
INSERT INTO product_images (product_id, image_url, display_order)
VALUES (1, 'https://example.com/images/iphone14_1.jpg', 1),
       (1, 'https://example.com/images/iphone14_2.jpg', 2),
       (2, 'https://example.com/images/macbook_1.jpg', 1),
       (3, 'https://example.com/images/airpods_1.jpg', 1);
