CREATE INDEX IF NOT EXISTS idx_issues_project_status
    ON public.issues(project_id, status);

CREATE INDEX IF NOT EXISTS idx_issues_project_priority
    ON public.issues(project_id, priority);

CREATE INDEX IF NOT EXISTS idx_issues_project_assignee
    ON public.issues(project_id, assignee_id);

CREATE INDEX IF NOT EXISTS idx_issues_project_due
    ON public.issues(project_id, due_date);

CREATE INDEX IF NOT EXISTS idx_issues_project_sprint_status
    ON public.issues(project_id, sprint_id, status);
