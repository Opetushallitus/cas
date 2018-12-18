package fi.vm.sade.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Component
public class StuckServiceTicketRetrievalThreadDumper {
    private static final Logger LOG = LoggerFactory.getLogger(StuckServiceTicketRetrievalThreadDumper.class);
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(20);

    private final int threadDumpIntervalSeconds;
    private final int threadDumpTimes;

    @Autowired
    public StuckServiceTicketRetrievalThreadDumper(@Value("${stuck.service.ticket.retrieval.thread.dump.interval.seconds}") int threadDumpIntervalSeconds,
                                                   @Value("${stuck.service.ticket.retrieval.thread.dump.times}") int threadDumpTimes) {
        this.threadDumpIntervalSeconds = threadDumpIntervalSeconds;
        this.threadDumpTimes = threadDumpTimes;
        LOG.info(String.format("Initialised with threadDumpIntervalSeconds==%d, threadDumpTimes==%d", threadDumpIntervalSeconds, threadDumpTimes));
    }

    public void triggerRunsToBackground() {
        THREAD_POOL.execute(() -> {
            LOG.info(String.format("Taking %d thread dumps at %d seconds intervals.", threadDumpTimes, threadDumpIntervalSeconds));
            IntStream.range(0, threadDumpTimes).forEach(i -> {
                dump(i + 1);
                try {
                    Thread.sleep(threadDumpIntervalSeconds * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private void dump(int i) {
        LOG.info(String.format("Taking thread dump %d ...", i));
        ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        for (ThreadInfo info : threads) {
            LOG.info(info.toString());
        }
    }
}
