package fi.vm.sade.auth.cas;

import org.apereo.cas.ticket.Ticket;

public interface TicketSerializer {

    String toJson(Ticket ticket);

    Ticket fromJson(String ticketJson, String ticketGrantingTicketJson);

}
