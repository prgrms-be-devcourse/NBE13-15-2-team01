-- 앱 실행 시마다 실행됨 (spring.sql.init.mode: always)
-- 재실행해도 안전하도록 IF NOT EXISTS 사용
-- 엔티티 추가·수정 시 이 파일도 같은 PR에서 수정할 것
-- 이미 생성된 테이블의 컬럼 변경은 반영되지 않으므로, 변경 시 로컬에서 해당 테이블 삭제 후 재실행

CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL,
    CONSTRAINT uk_member_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS performance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kopis_id VARCHAR(50),
    title VARCHAR(200) NOT NULL,
    genre VARCHAR(50),
    poster_url VARCHAR(500),
    venue_name VARCHAR(200),
    start_date DATE,
    end_date DATE,
    CONSTRAINT uk_performance_kopis_id UNIQUE (kopis_id)
);

CREATE TABLE IF NOT EXISTS schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    performance_id BIGINT NOT NULL,
    start_at DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_schedule_performance FOREIGN KEY (performance_id) REFERENCES performance (id)
);

CREATE TABLE IF NOT EXISTS seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    seat_name VARCHAR(20) NOT NULL,
    grade VARCHAR(20) NOT NULL,
    price INT NOT NULL,
    CONSTRAINT fk_seat_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    CONSTRAINT uk_seat_schedule_name UNIQUE (schedule_id, seat_name)
);

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    active_seat_id BIGINT,
    status VARCHAR(20) NOT NULL,
    reserved_at DATETIME NOT NULL,
    canceled_at DATETIME,
    KEY idx_reservation_member_schedule (member_id, schedule_id, status),
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_reservation_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES seat (id),
    CONSTRAINT uk_reservation_active_seat UNIQUE (active_seat_id)
);