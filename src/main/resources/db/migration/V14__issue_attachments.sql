CREATE TABLE IF NOT EXISTS public.issue_attachments (
                                                        id UUID PRIMARY KEY,
                                                        issue_id UUID NOT NULL REFERENCES public.issues(id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(120),
    file_size BIGINT NOT NULL,
    storage_path TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_issue_attachments_issue
    ON public.issue_attachments(issue_id, created_at DESC);
