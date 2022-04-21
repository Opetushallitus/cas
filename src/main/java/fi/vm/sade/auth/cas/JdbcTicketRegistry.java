package fi.vm.sade.auth.cas;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.AbstractTicketRegistry;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * <code>
 * CREATE TABLE ticket (
 * id text PRIMARY KEY,
 * type text NOT NULL,
 * number_of_times_used integer NOT NULL,
 * creation_time timestamp with time zone NOT NULL,
 * ticket_granting_ticket_id text REFERENCES ticket (id) ON DELETE CASCADE,
 * data jsonb NOT NULL
 * );
 * <p>
 * CREATE INDEX ticket_type_idx ON ticket (type);
 * </code>
 */
@Component("ticketRegistry")
@Transactional(transactionManager = "ticketTransactionManager")
public class JdbcTicketRegistry extends AbstractTicketRegistry {

    private final JdbcOperations jdbcOperations;
    private final TicketSerializer ticketSerializer;

    public JdbcTicketRegistry(JdbcOperations jdbcOperations,
                              TicketSerializer ticketSerializer) {
        this.jdbcOperations = jdbcOperations;
        this.ticketSerializer = ticketSerializer;
    }

    @Override
    public boolean deleteSingleTicket(String ticketId) {
        return jdbcOperations.update("DELETE FROM ticket WHERE id = ?", ticketId) > 0;
    }

    @Override
    public void addTicketInternal(Ticket ticket) {
        OffsetDateTime creationTime = ticket.getCreationTime().toOffsetDateTime();
        String ticketGrantingTicketId =
                Optional.ofNullable(ticket.getTicketGrantingTicket()).map(Ticket::getId).orElse(null);
        jdbcOperations.update("INSERT INTO ticket (id, type, number_of_times_used, creation_time, " +
                        "ticket_granting_ticket_id, data) VALUES (?, ?, ?, ?, ?, ?::jsonb)",
                ticket.getId(), ticket.getPrefix(), ticket.getCountOfUses(), creationTime, ticketGrantingTicketId,
                ticketSerializer.toJson(ticket));
    }

    @Override
    public Ticket getTicket(String ticketId, Predicate<Ticket> predicate) {
        return findById(ticketId).filter(predicate).orElse(null);
    }

    @Override
    public long deleteAll() {
        //noinspection SqlWithoutWhere
        return jdbcOperations.update("DELETE FROM ticket");
    }

    public Collection<? extends Ticket> getTickets() {
        RowMapper<Ticket> mapper = (rs, rowNum) -> ticketSerializer.fromJson(rs.getString("data"
        ), rs.getString("ticket_granting_ticket_data"));


        return jdbcOperations.query(
                "SELECT t1.data AS data, t2.data AS ticket_granting_ticket_data FROM ticket t1 LEFT JOIN ticket t2 ON" +
                        " t2.id = t1.ticket_granting_ticket_id",
                new RowMapperResultSetExtractor<>(mapper));
    }

    @Override
    public Ticket updateTicket(Ticket ticket) {
        jdbcOperations.update("UPDATE ticket SET number_of_times_used = ?, data = ?::jsonb WHERE id = ?",
                ticket.getCountOfUses(), ticketSerializer.toJson(ticket), ticket.getId());
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
        Row row = DataAccessUtils.singleResult(jdbcOperations.query("SELECT data, ticket_granting_ticket_id FROM " +
                        "ticket WHERE id = ? FOR UPDATE",
                (rs, rowNum) -> new Row(rs.getString("data"), rs.getString(
                        "ticket_granting_ticket_id")), id));
        return Optional.ofNullable(row).map(this::rowToTicket);
    }

    private Ticket rowToTicket(Row row) {
        String ticketGrantingTicketData = row.getTicketGrantingTicketId().flatMap(this::findDataById).orElse(null);
        return ticketSerializer.fromJson(row.getData(), ticketGrantingTicketData);
    }

    private Optional<String> findDataById(String id) {
        return Optional.ofNullable(DataAccessUtils.singleResult(jdbcOperations.query("SELECT data FROM ticket WHERE " +
                        "id = ?",
                SingleColumnRowMapper.newInstance(String.class), id)));
    }

    private long countByType(String type) {
        //noinspection ConstantConditions
        return jdbcOperations.queryForObject("SELECT count(*) FROM ticket WHERE type = ?",
                Long.class, type);
    }

    private static class Row {

        private String data;
        private Optional<String> ticketGrantingTicketId;

        public Row(String data, String ticketGrantingTicketId) {
            this.setData(data);
            this.setTicketGrantingTicketId(Optional.ofNullable(ticketGrantingTicketId));
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public Optional<String> getTicketGrantingTicketId() {
            return ticketGrantingTicketId;
        }

        public void setTicketGrantingTicketId(Optional<String> ticketGrantingTicketId) {
            this.ticketGrantingTicketId = ticketGrantingTicketId;
        }
    }

}
