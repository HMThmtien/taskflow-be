CREATE TABLE user_preferences (
                                  user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                  theme VARCHAR(20) NOT NULL DEFAULT 'system',
                                  density VARCHAR(20) NOT NULL DEFAULT 'comfortable',
                                  default_start_page VARCHAR(50) NOT NULL DEFAULT 'dashboard',
                                  email_notifications BOOLEAN NOT NULL DEFAULT true,
                                  in_app_notifications BOOLEAN NOT NULL DEFAULT true,
                                  updated_at TIMESTAMPTZ DEFAULT now()
);