CREATE EXTENSION IF NOT EXISTS lo;

-- serviceticket
DROP TRIGGER IF EXISTS serviceticket_expiration_policy_trig ON serviceticket;
CREATE TRIGGER serviceticket_expiration_policy_trig
BEFORE UPDATE OF expiration_policy OR DELETE ON serviceticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(expiration_policy);

DROP TRIGGER IF EXISTS serviceticket_service_trig ON serviceticket;
CREATE TRIGGER serviceticket_service_trig
BEFORE UPDATE OF service OR DELETE ON serviceticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(service);

-- ticketgrantingticket
DROP TRIGGER IF EXISTS ticketgrantingticket_expiration_policy_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_expiration_policy_trig
BEFORE UPDATE OF expiration_policy OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(expiration_policy);

DROP TRIGGER IF EXISTS ticketgrantingticket_authentication_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_authentication_trig
BEFORE UPDATE OF authentication OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(authentication);

DROP TRIGGER IF EXISTS ticketgrantingticket_descendant_tickets_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_descendant_tickets_trig
BEFORE UPDATE OF descendant_tickets OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(descendant_tickets);

DROP TRIGGER IF EXISTS ticketgrantingticket_proxied_by_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_proxied_by_trig
BEFORE UPDATE OF proxied_by OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(proxied_by);

DROP TRIGGER IF EXISTS ticketgrantingticket_proxy_granting_tickets_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_proxy_granting_tickets_trig
BEFORE UPDATE OF proxy_granting_tickets OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(proxy_granting_tickets);

DROP TRIGGER IF EXISTS ticketgrantingticket_services_granted_access_to_trig ON ticketgrantingticket;
CREATE TRIGGER ticketgrantingticket_services_granted_access_to_trig
BEFORE UPDATE OF services_granted_access_to OR DELETE ON ticketgrantingticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(services_granted_access_to);

-- transientsessionticket
DROP TRIGGER IF EXISTS transientsessionticket_expiration_policy_trig ON transientsessionticket;
CREATE TRIGGER transientsessionticket_expiration_policy_trig
BEFORE UPDATE OF expiration_policy OR DELETE ON transientsessionticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(expiration_policy);

DROP TRIGGER IF EXISTS transientsessionticket_properties_trig ON transientsessionticket;
CREATE TRIGGER transientsessionticket_properties_trig
BEFORE UPDATE OF properties OR DELETE ON transientsessionticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(properties);

DROP TRIGGER IF EXISTS transientsessionticket_service_trig ON transientsessionticket;
CREATE TRIGGER transientsessionticket_service_trig
BEFORE UPDATE OF service OR DELETE ON transientsessionticket
FOR EACH ROW EXECUTE PROCEDURE lo_manage(service);
