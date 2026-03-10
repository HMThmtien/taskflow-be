CREATE INDEX IF NOT EXISTS idx_issues_project_status_position
    ON issues(project_id, status, position);

CREATE INDEX IF NOT EXISTS idx_issues_project_updated_at
    ON issues(project_id, updated_at);