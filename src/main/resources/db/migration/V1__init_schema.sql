CREATE TABLE projects (
                          id          UUID PRIMARY KEY,
                          name        VARCHAR(255) NOT NULL,
                          key         VARCHAR(20)  NOT NULL UNIQUE,
                          description TEXT,
                          created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE issues (
                        id          UUID PRIMARY KEY,
                        project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                        title       VARCHAR(255) NOT NULL,
                        description TEXT,
                        status      VARCHAR(20) NOT NULL,
                        priority    VARCHAR(20) NOT NULL,
                        position    INTEGER NOT NULL,
                        created_at  TIMESTAMP NOT NULL DEFAULT now(),
                        updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_issues_project ON issues(project_id);
CREATE INDEX idx_issues_status ON issues(status);
