CREATE TABLE SPRING_SESSION (
	PRIMARY_ID CHAR(36) NOT NULL,
	SESSION_ID CHAR(36) NOT NULL,
	CREATION_TIME BIGINT NOT NULL,
	LAST_ACCESS_TIME BIGINT NOT NULL,
	MAX_INACTIVE_INTERVAL INT NOT NULL,
	EXPIRY_TIME BIGINT NOT NULL,
	PRINCIPAL_NAME VARCHAR(100),
	CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
	SESSION_PRIMARY_ID CHAR(36) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_BYTES BYTEA NOT NULL,
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);

CREATE TABLE locks (
    application_id character varying(255) NOT NULL,
    expiration_date timestamp without time zone,
    unique_id character varying(255),
    lock_ver bigint DEFAULT 0 NOT NULL
);

CREATE TABLE serviceticket (
    type character varying(31) NOT NULL,
    id character varying(255) NOT NULL,
    number_of_times_used integer,
    creation_time timestamp without time zone,
    expiration_policy oid NOT NULL,
    expired boolean NOT NULL,
    last_time_used timestamp without time zone,
    previous_last_time_used timestamp without time zone,
    from_new_login boolean NOT NULL,
    ticket_already_granted boolean NOT NULL,
    service oid NOT NULL,
    ticket_granting_ticket_id character varying(255)
);

CREATE TABLE ticketgrantingticket (
    type character varying(31) NOT NULL,
    id character varying(255) NOT NULL,
    number_of_times_used integer,
    creation_time timestamp without time zone,
    expiration_policy oid NOT NULL,
    expired boolean NOT NULL,
    last_time_used timestamp without time zone,
    previous_last_time_used timestamp without time zone,
    authentication oid NOT NULL,
    descendant_tickets oid NOT NULL,
    proxied_by oid,
    proxy_granting_tickets oid NOT NULL,
    services_granted_access_to oid NOT NULL,
    ticket_granting_ticket_id character varying(255)
);

CREATE TABLE transientsessionticket (
    type character varying(31) NOT NULL,
    id character varying(255) NOT NULL,
    number_of_times_used integer,
    creation_time timestamp without time zone,
    expiration_policy oid NOT NULL,
    expired boolean NOT NULL,
    last_time_used timestamp without time zone,
    previous_last_time_used timestamp without time zone,
    properties oid NOT NULL,
    service oid
);

ALTER TABLE ONLY locks
    ADD CONSTRAINT locks_pkey PRIMARY KEY (application_id);

ALTER TABLE ONLY serviceticket
    ADD CONSTRAINT serviceticket_pkey PRIMARY KEY (id);

ALTER TABLE ONLY ticketgrantingticket
    ADD CONSTRAINT ticketgrantingticket_pkey PRIMARY KEY (id);

ALTER TABLE ONLY transientsessionticket
    ADD CONSTRAINT transientsessionticket_pkey PRIMARY KEY (id);

ALTER TABLE ONLY serviceticket
    ADD CONSTRAINT serviceticket_ticketgrantingticket_fkey FOREIGN KEY (ticket_granting_ticket_id) REFERENCES ticketgrantingticket(id);

ALTER TABLE ONLY ticketgrantingticket
    ADD CONSTRAINT ticketgrantingticket_ticketgrantingticket_fkey FOREIGN KEY (ticket_granting_ticket_id) REFERENCES ticketgrantingticket(id);

CREATE TABLE login_failure (
    id bigserial PRIMARY KEY,
    key text NOT NULL,
    time bigint NOT NULL
);

CREATE INDEX login_failure_key_idx ON login_failure (key);
