package fi.vm.sade;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.lock.LockRepository;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Based on org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner
// Main difference being that this implementation deletes tickets in smaller batches, so it survives the scenario
// where there are too many tickets to delete at once.
@Slf4j
@RequiredArgsConstructor
@Component("ticketRegistryCleaner")
@Transactional(transactionManager = "ticketTransactionManager")
public class CustomTicketRegistryCleaner implements TicketRegistryCleaner {
    private static final long LOCK_ID = 702779869L;

    private final LockRepository lockRepository;
    private final LogoutManager logoutManager;
    private final TicketRegistry ticketRegistry;
    @Qualifier("ticketTransactionManager")
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;
    private TransactionOperations transactionOperations;

    @PostConstruct
    public void postConstruct() {
        transactionOperations = new TransactionTemplate(transactionManager);
    }

    @Override
    public int clean() {
        var ticketsDeleted = Objects.requireNonNull(transactionOperations.execute(status -> {
            if (!tryAcquireTaskLock()) {
                LOGGER.info("Failed to acquire lock for ticket registry cleaner; it is already running");
                return 0;
            }

            LOGGER.info("Cleaning up expired tickets...");
            List<String> expiredTicketIds = Objects.requireNonNull(getExpiredTicketIdsToDelete());
            return expiredTicketIds.stream().mapToInt(this::cleanExpiredTicket).sum();
        }));
        LOGGER.info("[{}] expired tickets removed.", ticketsDeleted);
        return ticketsDeleted;
    }

    private boolean tryAcquireTaskLock() {
        var sql = "SELECT pg_try_advisory_xact_lock(?)";
        var result = jdbcTemplate.queryForObject(sql, Boolean.class, LOCK_ID);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public int cleanTicket(final Ticket ticket) {
        return cleanExpiredTicket(ticket.getId());
    }

    private int cleanExpiredTicket(String ticketId) {
        return getExpiredTicket(ticketId).map(Unchecked.function(this::cleanExpiredTicket)).orElse(0);
    }

    private int cleanExpiredTicket(Ticket ticket) throws Exception {
        if (ticket instanceof final TicketGrantingTicket tgt) {
            LOGGER.info("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
            logoutManager.performLogout(SingleLogoutExecutionRequest.builder().ticketGrantingTicket(tgt).build());
        }
        LOGGER.info("Cleaning up expired ticket [{}]", ticket.getId());
        return ticketRegistry.deleteTicket(ticket);
    }

    private Optional<Ticket> getExpiredTicket(String ticketId) {
        return Optional.ofNullable(ticketRegistry.getTicket(ticketId, ticket -> true));
    }

    private List<String> getExpiredTicketIdsToDelete() {
        try (var expiredTickets = ticketRegistry.stream().filter(Ticket::isExpired)) {
            var batchToDelete = expiredTickets.limit(1000);
            return batchToDelete.map(Ticket::getId).toList();
        }
    }
}
