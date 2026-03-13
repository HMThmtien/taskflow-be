CREATE TABLE IF NOT EXISTS public.comment_mentions (
                                                       id UUID PRIMARY KEY,
                                                       comment_id UUID NOT NULL REFERENCES public.issue_comments(id) ON DELETE CASCADE,
    mentioned_user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_comment_mentions_comment_user
    ON public.comment_mentions(comment_id, mentioned_user_id);

CREATE INDEX IF NOT EXISTS idx_comment_mentions_user
    ON public.comment_mentions(mentioned_user_id, created_at DESC);