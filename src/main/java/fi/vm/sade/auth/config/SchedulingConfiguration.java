package fi.vm.sade.auth.config;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.OnStartup;
import com.github.kagkarlsson.scheduler.task.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

@Configuration
public class SchedulingConfiguration {

    private static final Predicate<Task> ON_STARTUP = OnStartup.class::isInstance;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Scheduler scheduler(DataSource dataSource, List<Task<?>> tasks) {
        return Scheduler
                .create(dataSource, tasks.stream().filter(not(ON_STARTUP)).collect(toList()))
                .startTasks(tasks.stream().filter(ON_STARTUP).map(task -> (Task & OnStartup) task).collect(toList()))
                .threads(1)
                .build();
    }

}
