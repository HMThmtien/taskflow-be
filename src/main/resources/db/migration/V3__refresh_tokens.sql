CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                token VARCHAR(500) UNIQUE NOT NULL,
                                user_id UUID NOT NULL,
                                expiry_date TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL,
                                CONSTRAINT fk_refresh_user FOREIGN KEY (user_id)
                                    REFERENCES users(id) ON DELETE CASCADE
);