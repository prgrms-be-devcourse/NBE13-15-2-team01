-- 로컬 MySQL 최초 1회, root 계정으로 실행
-- 실행: mysql -u root -p < docs/sql/mysql-setup.sql

CREATE DATABASE IF NOT EXISTS ticketing
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 접속 방식(localhost 소켓 / 127.0.0.1 TCP)에 따라 매칭되는 계정이 달라서 둘 다 생성
CREATE USER IF NOT EXISTS 'ticketing'@'localhost' IDENTIFIED BY 'ticketing1234';
CREATE USER IF NOT EXISTS 'ticketing'@'127.0.0.1' IDENTIFIED BY 'ticketing1234';

GRANT ALL PRIVILEGES ON ticketing.* TO 'ticketing'@'localhost';
GRANT ALL PRIVILEGES ON ticketing.* TO 'ticketing'@'127.0.0.1';