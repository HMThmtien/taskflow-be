CREATE TABLE IF NOT EXISTS issue_comments (
                                              id uuid PRIMARY KEY,
                                              issue_id uuid NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    author_id uuid NOT NULL REFERENCES users(id),
    content text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_issue_comments_issue_created
    ON issue_comments(issue_id, created_at);

CREATE TABLE IF NOT EXISTS issue_activities (
                                                id uuid PRIMARY KEY,
                                                issue_id uuid NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    actor_id uuid NOT NULL REFERENCES users(id),
    type varchar(50) NOT NULL,
    payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_issue_activities_issue_created
    ON issue_activities(issue_id, created_at);