package fi.vm.sade.scheduling;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.OnStartup;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Configuration
public class SchedulingConfiguration {
    private static final Predicate<Task<?>> shouldBeStarted = OnStartup.class::isInstance;

    @Bean(destroyMethod = "stop")
    public Scheduler scheduler(
            DataSource dataSource,
            List<Task<?>> configuredTasks
    ) {
        LOGGER.info("Creating db-scheduler using tasks from Spring context: {}", configuredTasks);
        return Scheduler.create(dataSource, nonStartupTasks(configuredTasks))
                .threads(1)
                .startTasks(startupTasks(configuredTasks))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Task<?> & OnStartup> List<T> startupTasks(List<Task<?>> tasks) {
        return tasks.stream()
                .filter(shouldBeStarted)
                .map(task -> (T) task)
                .toList();
    }

    private static List<Task<?>> nonStartupTasks(List<Task<?>> tasks) {
        return tasks.stream().filter(shouldBeStarted.negate()).toList();
    }


    @Bean
    public Task<Void> ticketRegistryCleanupTask(
            CasConfigurationProperties casProperties,
            TicketRegistryCleaner ticketRegistryCleaner
    ) {
        LOGGER.info("Scheduling ticket registry cleaner task...");
        Duration repeatInterval = Duration.parse(casProperties.getTicket().getRegistry().getCleaner().getSchedule().getRepeatInterval());
        return Tasks.recurring("cas-ticket-registry-cleaner", FixedDelay.of(repeatInterval))
                .execute((taskInstance, executionContext) -> ticketRegistryCleaner.clean());
    }
}
