-- Seed Users (Passwords are 'password')
-- Hash: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG

INSERT INTO users (name, email, password, role) VALUES 
('Admin User', 'admin@test.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN'),
('Member User', 'member@test.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'MEMBER')
ON CONFLICT (email) DO NOTHING;

-- Seed Tasks (Only insert if not already present, dynamically resolving user IDs)
INSERT INTO tasks (title, description, status, priority, due_date, created_at, created_by_id, assigned_to_id)
SELECT 'Setup Backend', 'Initialize Spring Boot app', 'DONE', 'HIGH', '2026-07-05 10:00:00', CURRENT_TIMESTAMP, u.id, u.id
FROM users u WHERE u.email = 'admin@test.com'
AND NOT EXISTS (SELECT 1 FROM tasks WHERE title = 'Setup Backend');

INSERT INTO tasks (title, description, status, priority, due_date, created_at, created_by_id, assigned_to_id)
SELECT 'Build Frontend', 'Create UI in React', 'TODO', 'HIGH', '2026-07-10 10:00:00', CURRENT_TIMESTAMP, u1.id, u2.id
FROM users u1, users u2 WHERE u1.email = 'admin@test.com' AND u2.email = 'member@test.com'
AND NOT EXISTS (SELECT 1 FROM tasks WHERE title = 'Build Frontend');

INSERT INTO tasks (title, description, status, priority, due_date, created_at, created_by_id, assigned_to_id)
SELECT 'Write Documentation', 'Document the API endpoints', 'IN_PROGRESS', 'MEDIUM', '2026-07-15 10:00:00', CURRENT_TIMESTAMP, u.id, u.id
FROM users u WHERE u.email = 'member@test.com'
AND NOT EXISTS (SELECT 1 FROM tasks WHERE title = 'Write Documentation');
