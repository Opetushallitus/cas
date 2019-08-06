CREATE OR REPLACE FUNCTION delete_large_object() RETURNS TRIGGER AS $delete_large_object$
    DECLARE
        column_name text := quote_ident(TG_ARGV[0]);
        old_value oid;
        new_value oid;
    BEGIN
        IF (TG_OP = 'UPDATE') THEN
            EXECUTE format('SELECT $1.%1$I, $2.%1$I', column_name) INTO old_value, new_value USING OLD, NEW;
            IF (old_value IS DISTINCT FROM new_value) THEN
                EXECUTE format('SELECT lo_unlink($1.%1$I)', column_name) USING OLD;
            END IF;
            RETURN NEW;
        ELSIF (TG_OP = 'DELETE') THEN
            EXECUTE format('SELECT lo_unlink($1.%1$I)', column_name) USING OLD;
            RETURN OLD;
        END IF;
        RAISE EXCEPTION 'Operation % is not supported', TG_OP;
    END;
$delete_large_object$ LANGUAGE plpgsql;

-- serviceticket
DROP TRIGGER IF EXISTS serviceticket_expiration_policy_trig ON serviceticket;
CREATE TRIGGER serviceticket_expiration_policy_trig
BEFORE UPDATE OF expiration_policy OR DELETE ON serviceticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(expiration_policy);

DROP TRIGGER IF EXISTS serviceticket_service_trig ON serviceticket;
CREATE TRIGGER serviceticket_service_trig
BEFORE UPDATE OF service OR DELETE ON serviceticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(service);

-- ticketgrantingticket
DROP TRIGGER IF EXISTS ticketgrantingticket_expiration_policy_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_expiration_policy_trig
BEFORE UPDATE OF expiration_policy OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(expiration_policy);

DROP TRIGGER IF EXISTS ticketgrantingticket_authentication_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_authentication_trig
BEFORE UPDATE OF authentication OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(authentication);

DROP TRIGGER IF EXISTS ticketgrantingticket_descendant_tickets_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_descendant_tickets_trig
BEFORE UPDATE OF descendant_tickets OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(descendant_tickets);

DROP TRIGGER IF EXISTS ticketgrantingticket_proxied_by_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_proxied_by_trig
BEFORE UPDATE OF proxied_by OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(proxied_by);

DROP TRIGGER IF EXISTS ticketgrantingticket_proxy_granting_tickets_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_proxy_granting_tickets_trig
BEFORE UPDATE OF proxy_granting_tickets OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(proxy_granting_tickets);

DROP TRIGGER IF EXISTS ticketgrantingticket_services_granted_access_to_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_services_granted_access_to_trig
BEFORE UPDATE OF services_granted_access_to OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(services_granted_access_to);

-- transientsessionticket
DROP TRIGGER IF EXISTS transientsessionticket_expiration_policy_trig ON transientsessionticket;
CREATE TRIGGER transientsessionticket_expiration_policy_trig
BEFORE UPDATE OF expiration_policy OR DELETE ON transientsessionticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(expiration_policy);

DROP TRIGGER IF EXISTS transientsessionticket_properties_trig ON transientsessionticket;
CREATE TRIGGER transientsessionticket_properties_trig
BEFORE UPDATE OF properties OR DELETE ON transientsessionticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(properties);

DROP TRIGGER IF EXISTS transientsessionticket_service_trig ON transientsessionticket;
CREATE TRIGGER transientsessionticket_service_trig
BEFORE UPDATE OF service OR DELETE ON transientsessionticket
FOR EACH ROW EXECUTE PROCEDURE delete_large_object(service);
