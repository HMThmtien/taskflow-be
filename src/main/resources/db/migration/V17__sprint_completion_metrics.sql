ALTER TABLE public.sprints
    ADD COLUMN IF NOT EXISTS completion_action VARCHAR(30),
    ADD COLUMN IF NOT EXISTS completed_target_sprint_id UUID NULL REFERENCES public.sprints(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS completed_total_issues BIGINT,
    ADD COLUMN IF NOT EXISTS completed_done_issues BIGINT,
    ADD COLUMN IF NOT EXISTS completed_in_progress_issues BIGINT,
    ADD COLUMN IF NOT EXISTS completed_todo_issues BIGINT;

CREATE INDEX IF NOT EXISTS idx_sprints_completed_target
    ON public.sprints(completed_target_sprint_id);
