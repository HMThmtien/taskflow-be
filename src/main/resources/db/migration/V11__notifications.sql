CREATE TABLE notifications (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               actor_id UUID REFERENCES users(id),

                               type VARCHAR(50) NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               body TEXT NOT NULL,

                               entity_type VARCHAR(30),
                               entity_id UUID,
                               route TEXT,

                               is_read BOOLEAN DEFAULT false,
                               created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_notifications_user_created
    ON notifications(user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread
    ON notifications(user_id, is_read);