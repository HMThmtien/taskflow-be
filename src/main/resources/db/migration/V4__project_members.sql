CREATE TABLE project_members (
                                 id UUID PRIMARY KEY,
                                 project_id UUID NOT NULL,
                                 user_id UUID NOT NULL,
                                 role VARCHAR(50) NOT NULL,
                                 CONSTRAINT fk_pm_project FOREIGN KEY (project_id)
                                     REFERENCES projects(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_pm_user FOREIGN KEY (user_id)
                                     REFERENCES users(id) ON DELETE CASCADE,
                                 CONSTRAINT uq_project_user UNIQUE (project_id, user_id)
);