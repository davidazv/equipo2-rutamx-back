INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrador del sistema');
INSERT INTO roles (name, description) VALUES ('CEO', 'Chief Executive Officer');
INSERT INTO roles (name, description) VALUES ('COO', 'Chief Operating Officer');
INSERT INTO roles (name, description) VALUES ('CMO', 'Chief Marketing Officer');
INSERT INTO users (email, firebase_uuid, first_name, last_name, role_id, status) VALUES ('admin@rutamx.com', 'seed-admin-placeholder', 'Admin', 'RutaMx', 1, 'ACTIVE');
INSERT INTO users (email, firebase_uuid, first_name, last_name, role_id, status) VALUES ('other@rutamx.com', 'seed-other-placeholder', 'Other', 'User', 2, 'ACTIVE');
