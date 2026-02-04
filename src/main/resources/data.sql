-- Demo users for testing (passwords are BCrypt hashed)
-- teacher / password
INSERT INTO users (username, password, name, email, role, enabled, profile_id) 
VALUES ('teacher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z6qMXPrpcCc8Jq5kvuGvDiBi', 'Demo Teacher', 'teacher@example.com', 'TEACHER', true, 1)
ON CONFLICT (username) DO NOTHING;

-- student / password  
INSERT INTO users (username, password, name, email, role, enabled, profile_id)
VALUES ('student', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z6qMXPrpcCc8Jq5kvuGvDiBi', 'Demo Student', 'student@example.com', 'STUDENT', true, 1)
ON CONFLICT (username) DO NOTHING;
