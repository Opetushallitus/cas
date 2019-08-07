ALTER TABLE ticket ADD COLUMN ticket_granting_ticket_id text REFERENCES ticket (id) ON DELETE CASCADE;
