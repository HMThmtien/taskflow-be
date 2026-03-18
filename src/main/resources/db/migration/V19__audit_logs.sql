create table if not exists audit_logs (
    id uuid primary key,
    action varchar(100) not null,
    actor_id uuid null,
    entity_type varchar(100) not null,
    entity_id uuid null,
    request_id varchar(100) null,
    details_json text null,
    created_at timestamptz not null default now()
);

create index if not exists idx_audit_logs_created_at on audit_logs (created_at desc);
create index if not exists idx_audit_logs_actor_id on audit_logs (actor_id);
create index if not exists idx_audit_logs_entity on audit_logs (entity_type, entity_id);
