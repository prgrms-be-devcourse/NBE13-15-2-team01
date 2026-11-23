-- localhost·127.0.0.1: 로컬 설치 MySQL용 / %: 도커 MySQL용 (외부 주소로 접속됨)
CREATE USER IF NOT EXISTS 'ticketing'@'localhost' IDENTIFIED BY 'ticketing1234';
CREATE USER IF NOT EXISTS 'ticketing'@'127.0.0.1' IDENTIFIED BY 'ticketing1234';
CREATE USER IF NOT EXISTS 'ticketing'@'%' IDENTIFIED BY 'ticketing1234';

GRANT ALL PRIVILEGES ON ticketing.* TO 'ticketing'@'localhost';
GRANT ALL PRIVILEGES ON ticketing.* TO 'ticketing'@'127.0.0.1';
GRANT ALL PRIVILEGES ON ticketing.* TO 'ticketing'@'%';