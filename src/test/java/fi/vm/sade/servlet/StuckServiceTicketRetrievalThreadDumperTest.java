package fi.vm.sade.servlet;

import org.junit.Ignore;
import org.junit.Test;

@Ignore // Running this test as a part of the suite does not make much sese.
public class StuckServiceTicketRetrievalThreadDumperTest {
    private final StuckServiceTicketRetrievalThreadDumper dumper = new StuckServiceTicketRetrievalThreadDumper(1, 3);
    @Test
    public void triggerRunsToBackground() throws InterruptedException {
        dumper.triggerRunsToBackground();
        Thread.sleep(10000);
    }
}
