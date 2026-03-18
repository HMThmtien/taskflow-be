create table if not exists report_views (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    project_id uuid not null references projects(id) on delete cascade,
    route_key varchar(50) not null,
    name varchar(120) not null,
    is_default boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_report_views_user_route on report_views (user_id, route_key, is_default desc, updated_at desc);
