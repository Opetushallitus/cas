CREATE TABLE IF NOT EXISTS postgres_jpa_ticket_entity
(
    id            VARCHAR(768)   NOT NULL
        PRIMARY KEY,
    body          VARCHAR(32000) NOT NULL,
    creation_time TIMESTAMP      NOT NULL,
    parent_id     VARCHAR(1024),
    principal_id  VARCHAR(1024),
    type          VARCHAR(1024)  NOT NULL
);

DROP TABLE IF EXISTS ticket;