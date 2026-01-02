INSERT INTO users (
    id, binary_content_id, weather_id,
    name, email, password, gender, role,
    birth, temperature_sensitivity, locked, created_at
)
VALUES
    ('00000000-0000-0000-0000-000000000001', NULL, NULL, '김민수', 'minsu.kim@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1995-03-15 00:00:00+09', 3, false, NOW() - INTERVAL '2 days 3 hours'),

    ('00000000-0000-0000-0000-000000000002', NULL, NULL, '박지은', 'jieun.park@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1998-07-22 00:00:00+09', 3, false, NOW() - INTERVAL '1 day 6 hours'),

    ('00000000-0000-0000-0000-000000000003', NULL, NULL, '이준호', 'junho.lee@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'ADMIN', '1992-11-08 00:00:00+09', 4, false, NOW() - INTERVAL '5 hours'),

    ('00000000-0000-0000-0000-000000000004', NULL, NULL, '최민석', 'minseok.choi@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1996-05-11 00:00:00+09', 3, false, NOW() - INTERVAL '3 days'),

    ('00000000-0000-0000-0000-000000000005', NULL, NULL, '이수빈', 'subin.lee@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1999-02-20 00:00:00+09', 3, false, NOW() - INTERVAL '12 hours'),

    ('00000000-0000-0000-0000-000000000006', NULL, NULL, '박정호', 'jungho.park@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1994-10-01 00:00:00+09', 4, false, NOW() - INTERVAL '8 hours'),

    ('00000000-0000-0000-0000-000000000007', NULL, NULL, '김수진', 'sujin.kim@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1997-06-30 00:00:00+09', 2, false, NOW() - INTERVAL '1 day 2 hours'),

    ('00000000-0000-0000-0000-000000000008', NULL, NULL, '윤민재', 'minjae.yoon@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1993-09-18 00:00:00+09', 4, false, NOW() - INTERVAL '4 days'),

    ('00000000-0000-0000-0000-000000000009', NULL, NULL, '서지민', 'jimin.seo@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '2000-01-09 00:00:00+09', 3, false, NOW() - INTERVAL '30 minutes'),

    ('00000000-0000-0000-0000-000000000010', NULL, NULL, '조현우', 'hyunwoo.jo@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1991-12-12 00:00:00+09', 5, false, NOW() - INTERVAL '10 minutes'),

-- 이하 동일 패턴
    ('00000000-0000-0000-0000-000000000011', NULL, NULL, '이지은', 'jieun.lee@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1996-08-08 00:00:00+09', 3, false, NOW() - INTERVAL '9 hours'),

    ('00000000-0000-0000-0000-000000000012', NULL, NULL, '강민호', 'minho.kang@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1993-04-14 00:00:00+09', 4, false, NOW() - INTERVAL '2 days'),

    ('00000000-0000-0000-0000-000000000013', NULL, NULL, '윤서현', 'seohyun.yoon@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1997-11-11 00:00:00+09', 3, false, NOW() - INTERVAL '6 days'),

    ('00000000-0000-0000-0000-000000000014', NULL, NULL, '한석훈', 'seokhun.han@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1990-09-09 00:00:00+09', 5, false, NOW() - INTERVAL '7 hours'),

    ('00000000-0000-0000-0000-000000000015', NULL, NULL, '최지우', 'jiwoo.choi@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1998-03-03 00:00:00+09', 3, false, NOW() - INTERVAL '15 minutes'),

    ('00000000-0000-0000-0000-000000000016', NULL, NULL, '박준호', 'junho.park@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1994-05-05 00:00:00+09', 4, false, NOW() - INTERVAL '11 hours'),

    ('00000000-0000-0000-0000-000000000017', NULL, NULL, '김지우', 'jiwoo.kim@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1999-09-19 00:00:00+09', 2, false, NOW() - INTERVAL '1 hour'),

    ('00000000-0000-0000-0000-000000000018', NULL, NULL, '이민호', 'minho.lee@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1992-02-02 00:00:00+09', 4, false, NOW() - INTERVAL '20 hours'),

    ('00000000-0000-0000-0000-000000000019', NULL, NULL, '윤서진', 'seojin.yoon@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1996-06-16 00:00:00+09', 3, false, NOW() - INTERVAL '3 hours'),

    ('00000000-0000-0000-0000-000000000020', NULL, NULL, '강민재', 'minjae.kang@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1991-01-01 00:00:00+09', 5, false, NOW() - INTERVAL '4 hours'),

    ('00000000-0000-0000-0000-000000000021', NULL, NULL, '이서현', 'seohyun.lee@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1998-08-18 00:00:00+09', 3, false, NOW() - INTERVAL '2 hours'),

    ('00000000-0000-0000-0000-000000000022', NULL, NULL, '박준영', 'junyoung.park@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1993-03-23 00:00:00+09', 4, false, NOW() - INTERVAL '13 hours'),

    ('00000000-0000-0000-0000-000000000023', NULL, NULL, '김서연', 'seoyeon.kim@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '2001-04-04 00:00:00+09', 2, false, NOW() - INTERVAL '35 minutes'),

    ('00000000-0000-0000-0000-000000000024', NULL, NULL, '조민재', 'minjae.jo@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1990-10-10 00:00:00+09', 5, false, NOW() - INTERVAL '9 days'),

    ('00000000-0000-0000-0000-000000000025', NULL, NULL, '서민지', 'minji.seo@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1999-12-12 00:00:00+09', 3, false, NOW() - INTERVAL '6 hours'),

    ('00000000-0000-0000-0000-000000000026', NULL, NULL, '이현우', 'hyunwoo.lee@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1994-07-07 00:00:00+09', 4, false, NOW() - INTERVAL '45 minutes'),

    ('00000000-0000-0000-0000-000000000027', NULL, NULL, '윤지아', 'jia.yoon@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '2002-02-02 00:00:00+09', 2, false, NOW() - INTERVAL '18 hours'),

    ('00000000-0000-0000-0000-000000000028', NULL, NULL, '강지훈', 'jihun.kang@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1991-09-09 00:00:00+09', 5, false, NOW() - INTERVAL '27 hours'),

    ('00000000-0000-0000-0000-000000000029', NULL, NULL, '한서윤', 'seoyun.han@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1997-01-17 00:00:00+09', 3, false, NOW() - INTERVAL '50 minutes'),

    ('00000000-0000-0000-0000-000000000030', NULL, NULL, '김준호', 'junho.kim@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1993-11-11 00:00:00+09', 4, false, NOW() - INTERVAL '5 minutes');
