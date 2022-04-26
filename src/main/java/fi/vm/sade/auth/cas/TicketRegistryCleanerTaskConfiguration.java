package fi.vm.sade.auth.cas;


import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.lock.DefaultLockRepository;
import org.apereo.cas.util.lock.LockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.locks.DefaultLockRegistry;

import java.time.Duration;

@Configuration
public class TicketRegistryCleanerTaskConfiguration {

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner(LogoutManager logoutManager, TicketRegistry ticketRegistry) {
        //TODO: JDBC registry needed!
        LockRepository lockRepository = new DefaultLockRepository(new DefaultLockRegistry());
        return new DefaultTicketRegistryCleaner( lockRepository,  logoutManager,  ticketRegistry);
    }

    @Bean
    public Task<Void> ticketRegistryCleanerTask(CasConfigurationProperties casProperties, TicketRegistryCleaner ticketRegistryCleaner) {
        Duration duration = Duration.parse(casProperties.getTicket().getRegistry().getCleaner().getSchedule().getRepeatInterval());
        return Tasks.recurring("cas-ticket-registry-cleaner", FixedDelay.of(duration))
                .execute((taskInstance, executionContext) -> ticketRegistryCleaner.clean());
    }

}
