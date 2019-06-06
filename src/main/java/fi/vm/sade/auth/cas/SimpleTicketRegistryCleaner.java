package fi.vm.sade.auth.cas;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Modified from {@link org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner}, changes:
 * 1) removed {@link org.apereo.cas.ticket.registry.support.LockingStrategy} (locking is handled with db-scheduler).
 * 2) tickets are removed in their own transactions to fix error "Batch update returned unexpected row count".
 */
@Component("ticketRegistryCleaner")
@Transactional(transactionManager = "ticketTransactionManager", propagation = Propagation.NEVER)
public class SimpleTicketRegistryCleaner implements TicketRegistryCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTicketRegistryCleaner.class);

    private final LogoutManager logoutManager;
    private final TicketRegistry ticketRegistry;
    private final TransactionOperations transactionOperations;

    public SimpleTicketRegistryCleaner(LogoutManager logoutManager,
                                       TicketRegistry ticketRegistry,
                                       @Qualifier("ticketTransactionManager") PlatformTransactionManager transactionManager) {
        this.logoutManager = logoutManager;
        this.ticketRegistry = ticketRegistry;
        this.transactionOperations = new TransactionTemplate(transactionManager);
    }

    @Override
    public int clean() {
        List<String> expiredTicketIds = transactionOperations.execute(status -> getExpiredTicketIds());
        int ticketsDeleted = expiredTicketIds.stream()
                .mapToInt(expiredTicketId -> transactionOperations.execute(status -> cleanExpiredTicket(expiredTicketId)))
                .sum();
        LOGGER.info("[{}] expired tickets removed.", ticketsDeleted);
        return ticketsDeleted;
    }

    private List<String> getExpiredTicketIds() {
        try (Stream<? extends Ticket> ticketsStream = ticketRegistry.getTicketsStream()) {
            return ticketsStream.filter(Ticket::isExpired).map(Ticket::getId).collect(toList());
        }
    }

    @Override
    public int cleanTicket(final Ticket ticket) {
        return transactionOperations.execute(status -> cleanExpiredTicket(ticket.getId()));
    }

    private int cleanExpiredTicket(String ticketId) {
        return getExpiredTicket(ticketId).map(this::cleanExpiredTicket).orElse(0);
    }

    private Optional<Ticket> getExpiredTicket(String ticketId) {
        return Optional.ofNullable(ticketRegistry.getTicket(ticketId, ticket -> true));
    }

    private int cleanExpiredTicket(Ticket ticket) {
        if (ticket instanceof TicketGrantingTicket) {
            LOGGER.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
            logoutManager.performLogout((TicketGrantingTicket) ticket);
        }
        LOGGER.debug("Cleaning up expired service ticket [{}]", ticket.getId());
        return ticketRegistry.deleteTicket(ticket);
    }

}
