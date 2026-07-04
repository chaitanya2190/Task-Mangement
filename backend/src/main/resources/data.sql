-- Password for both users is 'password'
-- Hash: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG

INSERT INTO users (name, email, password, role) VALUES 
('Admin User', 'admin@test.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN'),
('Member User', 'member@test.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'MEMBER');

INSERT INTO tasks (title, description, status, priority, due_date, created_at, created_by_id, assigned_to_id) VALUES 
('Setup Backend', 'Initialize Spring Boot app', 'DONE', 'HIGH', '2026-07-05 10:00:00', CURRENT_TIMESTAMP, 1, 1),
('Build Frontend', 'Create UI in Next.js', 'TODO', 'HIGH', '2026-07-10 10:00:00', CURRENT_TIMESTAMP, 1, 2),
('Write Documentation', 'Document the API endpoints', 'IN_PROGRESS', 'MEDIUM', '2026-07-15 10:00:00', CURRENT_TIMESTAMP, 2, 2);
