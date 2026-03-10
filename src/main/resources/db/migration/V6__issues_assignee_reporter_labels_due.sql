ALTER TABLE issues
    ADD COLUMN reporter_id uuid,
  ADD COLUMN assignee_id uuid,
  ADD COLUMN due_date date,
  ADD COLUMN labels jsonb NOT NULL DEFAULT '[]'::jsonb;

-- backfill reporter_id = current owner? (tạm set NULL rồi fill bằng script nếu chưa có data)
-- nếu hệ thống đang có data, bạn nên backfill reporter_id bằng user tạo issue (hiện chưa lưu) => tạm cho phép null, rồi siết sau.

-- index hay dùng
CREATE INDEX IF NOT EXISTS idx_issues_project_assignee ON issues(project_id, assignee_id);
CREATE INDEX IF NOT EXISTS idx_issues_project_due_date ON issues(project_id, due_date);
CREATE INDEX IF NOT EXISTS idx_issues_labels_gin ON issues USING GIN (labels);