ALTER TABLE users
    ADD COLUMN avatar_storage_provider VARCHAR(32),
    ADD COLUMN avatar_storage_bucket VARCHAR(255),
    ADD COLUMN avatar_storage_key VARCHAR(500);
