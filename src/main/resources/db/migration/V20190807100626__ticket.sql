CREATE TABLE ticket (
    id text PRIMARY KEY,
    type text NOT NULL,
    number_of_times_used integer NOT NULL,
    creation_time timestamp with time zone NOT NULL,
    data jsonb NOT NULL
);

CREATE INDEX ticket_type_idx ON ticket (type);
