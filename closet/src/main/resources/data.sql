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
     'MALE', 'ADMIN', '1992-11-08 00:00:00+09', 4, false, NOW());