CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    line_user_id VARCHAR(191) NOT NULL,
    display_name VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_members_line_user_id UNIQUE (line_user_id),
    CONSTRAINT ck_members_role CHECK (role IN ('USER', 'ADMIN'))
);
