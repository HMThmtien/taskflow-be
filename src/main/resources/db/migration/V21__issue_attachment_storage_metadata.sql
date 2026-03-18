alter table issue_attachments add column if not exists storage_provider varchar(50);
alter table issue_attachments add column if not exists storage_bucket varchar(100);
alter table issue_attachments add column if not exists storage_key varchar(500);

create index if not exists idx_issue_attachments_storage_key on issue_attachments (storage_key);
