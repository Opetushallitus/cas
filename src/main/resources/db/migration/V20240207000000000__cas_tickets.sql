CREATE TABLE cas_tickets (
    id text CONSTRAINT cas_tickets_pkey PRIMARY KEY,
    body text NOT NULL,
    creation_time timestamp NOT NULL,
    parent_id text,
    principal_id text,
    type text NOT NULL,
    attributes json
);