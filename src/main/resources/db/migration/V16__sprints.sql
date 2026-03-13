CREATE TABLE IF NOT EXISTS public.sprints (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES public.projects(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    goal VARCHAR(1000),
    description VARCHAR(4000),
    status VARCHAR(30) NOT NULL,
    start_date DATE,
    end_date DATE,
    completed_at TIMESTAMPTZ,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE public.issues
    ADD COLUMN IF NOT EXISTS sprint_id UUID NULL REFERENCES public.sprints(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_sprints_project_status
    ON public.sprints(project_id, status, position DESC);

CREATE INDEX IF NOT EXISTS idx_sprints_project_completed
    ON public.sprints(project_id, completed_at DESC);

CREATE INDEX IF NOT EXISTS idx_issues_project_sprint
    ON public.issues(project_id, sprint_id);
