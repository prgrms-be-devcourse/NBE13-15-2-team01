-- 예매 테스트 공통 데이터 (테스트 트랜잭션 안에서 실행되고, 끝나면 롤백)
-- 회원 1, 2 / 공연 1
-- 회차 1: 12/8 14:00 시작, 예매 받는 중(OPEN) / 좌석 1, 2
-- 회차 2: 12/8 19:00 시작, 예매 마감(CLOSED) / 좌석 3

INSERT INTO member (id, email, password, name, role)
VALUES (1, 'user1@test.com', 'pw', '회원1', 'USER'),
       (2, 'user2@test.com', 'pw', '회원2', 'USER');

INSERT INTO performance (id, title)
VALUES (1, '테스트 공연');

INSERT INTO schedule (id, performance_id, start_at, status)
VALUES (1, 1, '2026-12-08 14:00:00', 'OPEN'),
       (2, 1, '2026-12-08 19:00:00', 'CLOSED');

INSERT INTO seat (id, schedule_id, seat_name, grade, price)
VALUES (1, 1, 'A1', 'VIP', 150000),
       (2, 1, 'A2', 'R', 110000),
       (3, 2, 'A1', 'VIP', 150000);