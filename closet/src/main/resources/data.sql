-- 주요 도시 날씨 지역 (기상청 격자 좌표)
INSERT INTO weather_regions (id, x, y, latitude, longitude, location_names, created_at)
VALUES
    ('aaaa0001-0000-0000-0000-000000000001', 60, 127, 37.5665, 126.9780, '서울특별시', NOW()),
    ('aaaa0002-0000-0000-0000-000000000002', 98, 76, 35.1796, 129.0756, '부산광역시', NOW()),
    ('aaaa0003-0000-0000-0000-000000000003', 89, 90, 35.8714, 128.6014, '대구광역시', NOW()),
    ('aaaa0004-0000-0000-0000-000000000004', 55, 124, 37.4563, 126.7052, '인천광역시', NOW()),
    ('aaaa0005-0000-0000-0000-000000000005', 58, 74, 35.1595, 126.8526, '광주광역시', NOW()),
    ('aaaa0006-0000-0000-0000-000000000006', 67, 100, 36.3504, 127.3845, '대전광역시', NOW()),
    ('aaaa0007-0000-0000-0000-000000000007', 102, 84, 35.5384, 129.3114, '울산광역시', NOW()),
    ('aaaa0008-0000-0000-0000-000000000008', 66, 103, 36.4800, 127.2890, '세종특별자치시', NOW()),
    ('aaaa0009-0000-0000-0000-000000000009', 52, 38, 33.4996, 126.5312, '제주특별자치도', NOW())
ON CONFLICT (x, y) DO NOTHING;

INSERT INTO users (id, binary_content_id, weather_id, name, email, password, gender, role,
                   birth, temperature_sensitivity, locked, created_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', NULL, NULL,
     '김민수', 'minsu.kim@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'USER', '1995-03-15 00:00:00+09', 5, false, NOW()),
    ('22222222-2222-2222-2222-222222222222', NULL, NULL,
     '박지은', 'jieun.park@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FEMALE', 'USER', '1998-07-22 00:00:00+09', 3, false, NOW()),
    ('33333333-3333-3333-3333-333333333333', NULL, NULL,
     '이준호', 'junho.lee@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'MALE', 'ADMIN', '1992-11-08 00:00:00+09', 4, false, NOW())
ON CONFLICT (email) DO NOTHING;