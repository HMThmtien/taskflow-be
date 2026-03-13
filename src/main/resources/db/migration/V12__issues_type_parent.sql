ALTER TABLE public.issues
    ADD COLUMN IF NOT EXISTS type VARCHAR(20) NOT NULL DEFAULT 'TASK',
    ADD COLUMN IF NOT EXISTS parent_issue_id UUID NULL REFERENCES public.issues(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_issues_parent_issue
    ON public.issues(parent_issue_id);

CREATE INDEX IF NOT EXISTS idx_issues_project_type
    ON public.issues(project_id, type);