package fi.vm.sade;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.lock.LockRepository;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Based on org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner
// Handles each ticket in their own transaction to avoid the following error:
//   "Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1"
@Slf4j
@RequiredArgsConstructor
@Component("ticketRegistryCleaner")
@Transactional(transactionManager = "ticketTransactionManager")
public class CustomTicketRegistryCleaner implements TicketRegistryCleaner {
    private final LockRepository lockRepository;
    private final TicketRegistry ticketRegistry;
    @Qualifier("ticketTransactionManager")
    private final PlatformTransactionManager transactionManager;
    private TransactionOperations transactionOperations;

    @PostConstruct
    public void postConstruct() {
        transactionOperations = new TransactionTemplate(transactionManager);
    }

    @Override
    public int clean() {
        LOGGER.info("Cleaning up expired tickets...");
        List<String> expiredTicketIds = Objects.requireNonNull(transactionOperations.execute(status -> getExpiredTicketIdsToDelete()));
        return expiredTicketIds.stream().mapToInt(expiredTicketId -> transactionOperations.execute(status -> cleanExpiredTicket(expiredTicketId))).sum();
    }

    @Override
    public int cleanTicket(final Ticket ticket) {
        return cleanExpiredTicket(ticket.getId());
    }

    private int cleanExpiredTicket(String ticketId) {
        return getExpiredTicket(ticketId).map(Unchecked.function(this::cleanExpiredTicket)).orElse(0);
    }

    private int cleanExpiredTicket(Ticket ticket) throws Exception {
        LOGGER.info("Cleaning up expired ticket [{}]", ticket.getId());
        return ticketRegistry.deleteTicket(ticket);
    }

    private Optional<Ticket> getExpiredTicket(String ticketId) {
        return Optional.ofNullable(ticketRegistry.getTicket(ticketId, ticket -> true));
    }

    private List<String> getExpiredTicketIdsToDelete() {
        var expiredTickets = ticketRegistry.stream().filter(Ticket::isExpired);
        var batchToDelete = expiredTickets.limit(1000);
        return batchToDelete.map(Ticket::getId).toList();
    }
}
