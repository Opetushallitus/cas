package fi.vm.sade.auth.cas;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.AbstractTicketRegistry;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * <code>
 * CREATE TABLE ticket (
 *   id text PRIMARY KEY,
 *   type text NOT NULL,
 *   number_of_times_used integer NOT NULL,
 *   creation_time timestamp with time zone NOT NULL,
 *   data jsonb NOT NULL
 * );
 *
 * CREATE INDEX ticket_type_idx ON ticket (type);
 * </code>
 */
@Component("ticketRegistry")
@Transactional(transactionManager = "ticketTransactionManager")
public class JdbcTicketRegistry extends AbstractTicketRegistry {

    private final JdbcOperations jdbcOperations;
    private final TicketSerializer ticketSerializer;

    public JdbcTicketRegistry(JdbcOperations jdbcOperations, TicketSerializer ticketSerializer) {
        this.jdbcOperations = jdbcOperations;
        this.ticketSerializer = ticketSerializer;
    }

    @Override
    public boolean deleteSingleTicket(String ticketId) {
        return jdbcOperations.update("DELETE FROM ticket WHERE id = ?", ticketId) > 0;
    }

    @Override
    public void addTicket(Ticket ticket) {
        OffsetDateTime creationTime = ticket.getCreationTime().toOffsetDateTime();
        String json = ticketSerializer.toJson(ticket);
        jdbcOperations.update("INSERT INTO ticket (id, type, number_of_times_used, creation_time, data) VALUES (?, ?, ?, ?, ?::jsonb)",
                ticket.getId(), ticket.getPrefix(), ticket.getCountOfUses(), creationTime, json);
    }

    @Override
    public Ticket getTicket(String ticketId, Predicate<Ticket> predicate) {
        return findById(ticketId).filter(predicate).orElse(null);
    }

    @Override
    public long deleteAll() {
        return jdbcOperations.update("DELETE FROM ticket");
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return jdbcOperations.query("SELECT data FROM ticket FOR UPDATE",
                (rs, rowNum) -> ticketSerializer.fromJson(rs.getString("data"), Ticket.class));
    }

    @Override
    public Ticket updateTicket(Ticket ticket) {
        jdbcOperations.update("UPDATE ticket SET number_of_times_used = ?, data = ?::jsonb WHERE id = ?",
                new Object[] { ticket.getCountOfUses(), ticketSerializer.toJson(ticket), ticket.getId() });
        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public long sessionCount() {
        return countByType(TicketGrantingTicket.PREFIX);
    }

    @Override
    @Transactional(readOnly = true)
    public long serviceTicketCount() {
        return countByType(ServiceTicket.PREFIX);
    }

    private Optional<Ticket> findById(String id) {
        return findDataById(id).map(json -> ticketSerializer.fromJson(json, Ticket.class));
    }

    private Optional<String> findDataById(String id) {
        String data = jdbcOperations.queryForObject("SELECT data FROM ticket WHERE id = ? FOR UPDATE",
                new Object[] { id }, String.class);
        return Optional.ofNullable(data);
    }

    private long countByType(String type) {
        return jdbcOperations.queryForObject("SELECT count(*) FROM ticket WHERE type = ?",
                new Object[] { type }, Long.class);
    }

}
