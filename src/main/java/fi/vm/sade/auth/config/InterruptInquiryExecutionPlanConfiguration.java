package fi.vm.sade.auth.config;

import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class InterruptInquiryExecutionPlanConfiguration implements InterruptInquiryExecutionPlanConfigurer {

    private final List<InterruptInquirer> interruptInquirers;

    public InterruptInquiryExecutionPlanConfiguration(Optional<List<InterruptInquirer>> interruptInquirers) {
        this.interruptInquirers = interruptInquirers.orElseGet(ArrayList::new);
    }

    @Override
    public void configureInterruptInquiryExecutionPlan(InterruptInquiryExecutionPlan plan) {
        interruptInquirers.forEach(plan::registerInterruptInquirer);
    }

}
