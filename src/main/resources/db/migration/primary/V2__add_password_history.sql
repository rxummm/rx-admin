CREATE TABLE IF NOT EXISTS sys_user_password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);
