INSERT IGNORE INTO member (id, email, password, name, role) VALUES
<<<<<<< HEAD
(1, 'admin@ticketing.com', '$2a$10$/JQeZf.T4lGJAwu8X8IOnOj0DkWMPt.jyR9iYGWkfOMUBpUTcVKuq', '관리자', 'ADMIN'),
(2, 'user1@ticketing.com', '$2a$10$/JQeZf.T4lGJAwu8X8IOnOj0DkWMPt.jyR9iYGWkfOMUBpUTcVKuq', '테스트유저1', 'USER'),
(3, 'user2@ticketing.com', '$2a$10$/JQeZf.T4lGJAwu8X8IOnOj0DkWMPt.jyR9iYGWkfOMUBpUTcVKuq', '테스트유저2', 'USER');

INSERT IGNORE INTO performance (id, kopis_id, title, genre, poster_url, venue_name, start_date, end_date) VALUES
(1, 'SAMPLE-001', '샘플 뮤지컬 - 별빛 아래서', '뮤지컬', NULL, '샘플 아트홀', '2026-12-10', '2026-12-31'),
(2, 'SAMPLE-002', '샘플 연극 - 기억의 방', '연극', NULL, '샘플 소극장', '2026-12-12', '2026-12-28'),
(3, 'SAMPLE-003', '샘플 콘서트 - 겨울 밤 라이브', '콘서트', NULL, '샘플 공연장', '2026-12-20', '2026-12-25');

INSERT IGNORE INTO schedule (id, performance_id, start_at, status) VALUES
(1, 1, '2026-12-10 19:00:00', 'OPEN'),
(2, 1, '2026-12-11 14:00:00', 'OPEN'),
(3, 2, '2026-12-12 19:30:00', 'OPEN'),
(4, 2, '2026-12-13 15:00:00', 'OPEN'),
(5, 3, '2026-12-20 18:00:00', 'OPEN'),
(6, 3, '2026-12-21 18:00:00', 'CLOSED');
=======
                                                                (1, 'admin@ticketing.com', '$2a$10$/JQeZf.T4lGJAwu8X8IOnOj0DkWMPt.jyR9iYGWkfOMUBpUTcVKuq', '관리자', 'ADMIN'),
                                                                (2, 'user1@ticketing.com', '$2a$10$/JQeZf.T4lGJAwu8X8IOnOj0DkWMPt.jyR9iYGWkfOMUBpUTcVKuq', '테스트유저1', 'USER'),
                                                                (3, 'user2@ticketing.com', '$2a$10$/JQeZf.T4lGJAwu8X8IOnOj0DkWMPt.jyR9iYGWkfOMUBpUTcVKuq', '테스트유저2', 'USER');

INSERT IGNORE INTO performance (id, kopis_id, title, genre, poster_url, venue_name, start_date, end_date) VALUES
                                                                                                              (1, 'SAMPLE-001', '샘플 뮤지컬 - 별빛 아래서', '뮤지컬', NULL, '샘플 아트홀', '2026-12-10', '2026-12-31'),
                                                                                                              (2, 'SAMPLE-002', '샘플 연극 - 기억의 방', '연극', NULL, '샘플 소극장', '2026-12-12', '2026-12-28'),
                                                                                                              (3, 'SAMPLE-003', '샘플 콘서트 - 겨울 밤 라이브', '콘서트', NULL, '샘플 공연장', '2026-12-20', '2026-12-25');

INSERT IGNORE INTO schedule (id, performance_id, start_at, status) VALUES
                                                                       (1, 1, '2026-12-10 19:00:00', 'OPEN'),
                                                                       (2, 1, '2026-12-11 14:00:00', 'OPEN'),
                                                                       (3, 2, '2026-12-12 19:30:00', 'OPEN'),
                                                                       (4, 2, '2026-12-13 15:00:00', 'OPEN'),
                                                                       (5, 3, '2026-12-20 18:00:00', 'OPEN'),
                                                                       (6, 3, '2026-12-21 18:00:00', 'CLOSED');
>>>>>>> origin/dev

INSERT IGNORE INTO seat (schedule_id, seat_name, grade, price)
SELECT s.id,
       CONCAT(r.row_name, n.num),
       CASE WHEN r.row_name IN ('A', 'B') THEN 'VIP'
            WHEN r.row_name IN ('C', 'D') THEN 'R'
            ELSE 'S' END,
       CASE WHEN r.row_name IN ('A', 'B') THEN 150000
            WHEN r.row_name IN ('C', 'D') THEN 110000
            ELSE 80000 END
FROM schedule s
<<<<<<< HEAD
CROSS JOIN (SELECT 'A' AS row_name UNION ALL SELECT 'B' UNION ALL SELECT 'C'
            UNION ALL SELECT 'D' UNION ALL SELECT 'E') r
CROSS JOIN (SELECT 1 AS num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
            UNION ALL SELECT 9 UNION ALL SELECT 10) n
=======
         CROSS JOIN (SELECT 'A' AS row_name UNION ALL SELECT 'B' UNION ALL SELECT 'C'
                     UNION ALL SELECT 'D' UNION ALL SELECT 'E') r
         CROSS JOIN (SELECT 1 AS num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
                     UNION ALL SELECT 9 UNION ALL SELECT 10) n
>>>>>>> origin/dev
WHERE s.id IN (1, 2, 3, 4, 5, 6);
