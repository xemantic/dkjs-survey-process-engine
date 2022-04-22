SET DATABASE SQL SIZE FALSE;

CREATE SEQUENCE hibernate_sequence;

CREATE TABLE provider (
  id                      VARCHAR NOT NULL PRIMARY KEY,
  name                    VARCHAR NOT NULL
);

create table survey_process (
  id                      VARCHAR NOT NULL PRIMARY KEY,
  start                   TIMESTAMP NOT NULL,
  phase                   VARCHAR NOT NULL
);

CREATE TABLE activity (
  name                    VARCHAR NOT NULL,
  survey_process_id       VARCHAR NOT NULL FOREIGN KEY REFERENCES survey_process(id),
  executed_at             TIMESTAMP NOT NULL,
  mail_type               VARCHAR,
  result                  VARCHAR,
  failure                 VARCHAR,
  PRIMARY KEY (name, survey_process_id),
);

CREATE INDEX ix_activity_executed_at ON activity (executed_at);

CREATE TABLE project (
  id                      VARCHAR NOT NULL PRIMARY KEY,
  name                    VARCHAR NOT NULL,
  status                  VARCHAR NOT NULL,
  provider_id             VARCHAR NOT NULL FOREIGN KEY REFERENCES provider(id),
  survey_process_id       VARCHAR FOREIGN KEY REFERENCES survey_process(id),
  contact_email           VARCHAR NOT NULL,
  contact_first_name      VARCHAR NOT NULL,
  contact_last_name       VARCHAR NOT NULL,
  contact_pronoun         VARCHAR NOT NULL,
  participants_age11to15  INTEGER,
  participants_age16to19  INTEGER,
  participants_age1to5    INTEGER,
  participants_age20to26  INTEGER,
  participants_age6to10   INTEGER,
  participants_worker     INTEGER,
  start                   TIMESTAMP NOT NULL,
  end                     TIMESTAMP NOT NULL
);

CREATE TABLE project_goals (
  project_id              VARCHAR NOT NULL FOREIGN KEY REFERENCES project(id),
  goals                   INTEGER NOT NULL
)
