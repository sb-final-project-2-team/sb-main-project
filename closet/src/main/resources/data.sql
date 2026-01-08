-- 주요 도시 날씨 지역 (기상청 격자 좌표)
INSERT INTO weather_regions (id, weather_data_id, x, y, latitude, longitude, location_names, last_collected_at)
VALUES
    ('aaaa0001-0000-0000-0000-000000000001', NULL, 60, 127, 37.5665, 126.9780, '서울특별시', NOW()),
    ('aaaa0002-0000-0000-0000-000000000002', NULL, 98, 76, 35.1796, 129.0756, '부산광역시', NOW()),
    ('aaaa0003-0000-0000-0000-000000000003', NULL, 89, 90, 35.8714, 128.6014, '대구광역시', NOW()),
    ('aaaa0004-0000-0000-0000-000000000004', NULL, 55, 124, 37.4563, 126.7052, '인천광역시', NOW()),
    ('aaaa0005-0000-0000-0000-000000000005', NULL, 58, 74, 35.1595, 126.8526, '광주광역시', NOW()),
    ('aaaa0006-0000-0000-0000-000000000006', NULL, 67, 100, 36.3504, 127.3845, '대전광역시', NOW()),
    ('aaaa0007-0000-0000-0000-000000000007', NULL, 102, 84, 35.5384, 129.3114, '울산광역시', NOW()),
    ('aaaa0008-0000-0000-0000-000000000008', NULL, 66, 103, 36.4800, 127.2890, '세종특별자치시', NOW()),
    ('aaaa0009-0000-0000-0000-000000000009', NULL, 52, 38, 33.4996, 126.5312, '제주특별자치도', NOW())
ON CONFLICT (x, y) DO NOTHING;

INSERT INTO users (id, binary_content_id, weather_id,
                   name, email, password, gender, role,
                   birth, temperature_sensitivity, locked, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', NULL, NULL, '김민수', 'minsu.kim@example.com',
        '$2a$10$K9dYk4mXkZ3Z9s3r3Y2BvO8JH1n6w1p1vYp7XyRr7Kx3mEw4mF0qG',
        'MALE', 'ADMIN', '1995-03-15 00:00:00+09', 3, false, NOW() - INTERVAL '2 days 3 hours'),

       ('00000000-0000-0000-0000-000000000002', NULL, NULL, '박지은', 'jieun.park@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1998-07-22 00:00:00+09', 3, false, NOW() - INTERVAL '1 day 6 hours')
        ,

       ('00000000-0000-0000-0000-000000000003', NULL, NULL, '이준호', 'junho.lee@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'ADMIN', '1992-11-08 00:00:00+09', 4, false, NOW() - INTERVAL '5 hours')
        ,

       ('00000000-0000-0000-0000-000000000004', NULL, NULL, '최민석', 'minseok.choi@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1996-05-11 00:00:00+09', 3, false, NOW() - INTERVAL '3 days')
        ,

       ('00000000-0000-0000-0000-000000000005', NULL, NULL, '이수빈', 'subin.lee@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1999-02-20 00:00:00+09', 3, false, NOW() - INTERVAL '12 hours')
        ,

       ('00000000-0000-0000-0000-000000000006', NULL, NULL, '박정호', 'jungho.park@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1994-10-01 00:00:00+09', 4, false, NOW() - INTERVAL '8 hours')
        ,

       ('00000000-0000-0000-0000-000000000007', NULL, NULL, '김수진', 'sujin.kim@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1997-06-30 00:00:00+09', 2, false, NOW() - INTERVAL '1 day 2 hours')
        ,

       ('00000000-0000-0000-0000-000000000008', NULL, NULL, '윤민재', 'minjae.yoon@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1993-09-18 00:00:00+09', 4, false, NOW() - INTERVAL '4 days')
        ,

       ('00000000-0000-0000-0000-000000000009', NULL, NULL, '서지민', 'jimin.seo@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '2000-01-09 00:00:00+09', 3, false, NOW() - INTERVAL '30 minutes')
        ,

       ('00000000-0000-0000-0000-000000000010', NULL, NULL, '조현우', 'hyunwoo.jo@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1991-12-12 00:00:00+09', 5, false, NOW() - INTERVAL '10 minutes')
        ,

-- 이하 동일 패턴
       ('00000000-0000-0000-0000-000000000011', NULL, NULL, '이지은', 'jieun.lee@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1996-08-08 00:00:00+09', 3, false, NOW() - INTERVAL '9 hours')
        ,

       ('00000000-0000-0000-0000-000000000012', NULL, NULL, '강민호', 'minho.kang@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1993-04-14 00:00:00+09', 4, false, NOW() - INTERVAL '2 days')
        ,

       ('00000000-0000-0000-0000-000000000013', NULL, NULL, '윤서현', 'seohyun.yoon@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1997-11-11 00:00:00+09', 3, false, NOW() - INTERVAL '6 days')
        ,

       ('00000000-0000-0000-0000-000000000014', NULL, NULL, '한석훈', 'seokhun.han@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1990-09-09 00:00:00+09', 5, false, NOW() - INTERVAL '7 hours')
        ,

       ('00000000-0000-0000-0000-000000000015', NULL, NULL, '최지우', 'jiwoo.choi@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1998-03-03 00:00:00+09', 3, false, NOW() - INTERVAL '15 minutes')
        ,

       ('00000000-0000-0000-0000-000000000016', NULL, NULL, '박준호', 'junho.park@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1994-05-05 00:00:00+09', 4, false, NOW() - INTERVAL '11 hours')
        ,

       ('00000000-0000-0000-0000-000000000017', NULL, NULL, '김지우', 'jiwoo.kim@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1999-09-19 00:00:00+09', 2, false, NOW() - INTERVAL '1 hour')
        ,

       ('00000000-0000-0000-0000-000000000018', NULL, NULL, '이민호', 'minho.lee@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1992-02-02 00:00:00+09', 4, false, NOW() - INTERVAL '20 hours')
        ,

       ('00000000-0000-0000-0000-000000000019', NULL, NULL, '윤서진', 'seojin.yoon@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1996-06-16 00:00:00+09', 3, false, NOW() - INTERVAL '3 hours')
        ,

       ('00000000-0000-0000-0000-000000000020', NULL, NULL, '강민재', 'minjae.kang@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1991-01-01 00:00:00+09', 5, false, NOW() - INTERVAL '4 hours')
        ,

       ('00000000-0000-0000-0000-000000000021', NULL, NULL, '이서현', 'seohyun.lee@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1998-08-18 00:00:00+09', 3, false, NOW() - INTERVAL '2 hours')
        ,

       ('00000000-0000-0000-0000-000000000022', NULL, NULL, '박준영', 'junyoung.park@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1993-03-23 00:00:00+09', 4, false, NOW() - INTERVAL '13 hours')
        ,

       ('00000000-0000-0000-0000-000000000023', NULL, NULL, '김서연', 'seoyeon.kim@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '2001-04-04 00:00:00+09', 2, false, NOW() - INTERVAL '35 minutes')
        ,

       ('00000000-0000-0000-0000-000000000024', NULL, NULL, '조민재', 'minjae.jo@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1990-10-10 00:00:00+09', 5, false, NOW() - INTERVAL '9 days')
        ,

       ('00000000-0000-0000-0000-000000000025', NULL, NULL, '서민지', 'minji.seo@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1999-12-12 00:00:00+09', 3, false, NOW() - INTERVAL '6 hours')
        ,

       ('00000000-0000-0000-0000-000000000026', NULL, NULL, '이현우', 'hyunwoo.lee@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1994-07-07 00:00:00+09', 4, false, NOW() - INTERVAL '45 minutes')
        ,

       ('00000000-0000-0000-0000-000000000027', NULL, NULL, '윤지아', 'jia.yoon@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '2002-02-02 00:00:00+09', 2, false, NOW() - INTERVAL '18 hours')
        ,

       ('00000000-0000-0000-0000-000000000028', NULL, NULL, '강지훈', 'jihun.kang@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1991-09-09 00:00:00+09', 5, false, NOW() - INTERVAL '27 hours')
        ,

       ('00000000-0000-0000-0000-000000000029', NULL, NULL, '한서윤', 'seoyun.han@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'FEMALE', 'USER', '1997-01-17 00:00:00+09', 3, false, NOW() - INTERVAL '50 minutes')
        ,

       ('00000000-0000-0000-0000-000000000030', NULL, NULL, '김준호', 'junho.kim@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'MALE', 'USER', '1993-11-11 00:00:00+09', 4, false, NOW() - INTERVAL '5 minutes')
ON CONFLICT (email) DO NOTHING;

-- 샘플 날씨 데이터용 지역
INSERT INTO weather_regions (id,
                             weather_data_id,
                             x,
                             y,
                             latitude,
                             longitude,
                             location_names,
                             last_collected_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        NULL,
        60,
        127,
        18.0,
        26.0,
        '서울특별시 종로구',
        NOW()),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        NULL,
        61,
        126,
        18.0,
        26.0,
        '서울특별시 강남구',
        NOW())
ON CONFLICT (x, y) DO NOTHING;

INSERT INTO weather_data (id,
                          weather_region_id,
                          forecast_kind,
                          forecast_at,
                          forecasted_at,
                          sky_status,
                          temperature_current,
                          temperature_comp_prev_day,
                          temperature_min,
                          temperature_max,
                          precipitation_type,
                          precipitation_amount,
                          precipitation_prob,
                          humidity_current,
                          humidity_comp_to_day_before,
                          wind_speed,
                          wind_as_word)
VALUES ('11111111-1111-1111-1111-111111111111',
        'aaaa0001-0000-0000-0000-000000000001',
        'SHORT_FCST',
        NOW(),
        NOW() - INTERVAL '1 hour',
        'CLEAR',
        23.5,
        1.2,
        18.0,
        26.0,
        'NONE',
        0.0,
        10.0,
        55.0,
        -3.0,
        2.3,
        'WEAK'),
       ('22222222-2222-2222-2222-222222222222',
        'aaaa0002-0000-0000-0000-000000000002',
        'SHORT_FCST',
        NOW(),
        NOW() - INTERVAL '1 hour',
        'CLOUDY',
        25.1,
        0.3,
        21.0,
        28.5,
        'RAIN',
        3.5,
        60.0,
        70.0,
        5.0,
        4.8,
        'MODERATE')
ON CONFLICT DO NOTHING;

UPDATE weather_regions
SET weather_data_id = '11111111-1111-1111-1111-111111111111'
WHERE id = 'aaaa0001-0000-0000-0000-000000000001';

UPDATE weather_regions
SET weather_data_id = '22222222-2222-2222-2222-222222222222'
WHERE id = 'aaaa0002-0000-0000-0000-000000000002';

INSERT INTO clothes_attributes (id,
                                name,
                                attributes_values,
                                created_at)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccccc',
        'COLOR',
        '[
          "BLACK",
          "WHITE",
          "BLUE",
          "RED",
          "GRAY",
          "BEIGE"
        ]'::jsonb,
        NOW()),
       ('dddddddd-dddd-dddd-dddd-dddddddddddd',
        'SIZE',
        '[
          "XS",
          "S",
          "M",
          "L",
          "XL"
        ]'::jsonb,
        NOW()),
       ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'SEASON',
        '[
          "SPRING",
          "SUMMER",
          "FALL",
          "WINTER"
        ]'::jsonb,
        NOW())
ON CONFLICT DO NOTHING;

INSERT INTO clothes (id,
                     owner_id,
                     clothes_attributes_id,
                     name,
                     binary_content_id,
                     type,
                     created_at,
                     updated_at)
VALUES ('aaaa1111-1111-1111-1111-111111111111',
        '00000000-0000-0000-0000-000000000001',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'test1',
        NULL,
        'TOP',
        NOW(),
        NOW()),
       ('aaaa2222-2222-2222-2222-222222222222',
        '00000000-0000-0000-0000-000000000001',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'test2',
        NULL,
        'BOTTOM',
        NOW(),
        NOW()),
       ('aaaa3333-3333-3333-3333-333333333333',
        '00000000-0000-0000-0000-000000000001',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'test3',
        NULL,
        'OUTER',
        NOW(),
        NOW())
ON CONFLICT DO NOTHING;

-- Feed 1
INSERT INTO feeds (id,
                   user_id,
                   weather_id,
                   content,
                   comment_count,
                   like_count,
                   created_at,
                   updated_at)
VALUES ('feed0001-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        'aaaa0001-0000-0000-0000-000000000001',
        '오늘은 비 와서 레인코트 착용',
        2,
        10,
        NOW(),
        NOW())
ON CONFLICT DO NOTHING;

-- Feed 2
INSERT INTO feeds (id,
                   user_id,
                   weather_id,
                   content,
                   comment_count,
                   like_count,
                   created_at)
VALUES ('feed0002-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-000000000001',
        'aaaa0002-0000-0000-0000-000000000002',
        '한파 대비 롱패딩 OOTD',
        0,
        3,
        NOW())
ON CONFLICT DO NOTHING;

INSERT INTO ootds (
    id,
    feed_id,
    clothes_id,
    created_at
) VALUES
      (
          '00010001-0000-0000-0000-000000000001',
          'feed0001-0000-0000-0000-000000000001',
          'aaaa1111-1111-1111-1111-111111111111',
          NOW()
      ),
      (
          '00020002-0000-0000-0000-000000000002',
          'feed0001-0000-0000-0000-000000000001',
          'aaaa2222-2222-2222-2222-222222222222',
          NOW()
      )
ON CONFLICT DO NOTHING;

UPDATE users
SET weather_id = 'aaaa0001-0000-0000-0000-000000000001'
WHERE id = '00000000-0000-0000-0000-000000000001';
