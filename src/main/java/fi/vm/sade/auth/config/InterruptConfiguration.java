package fi.vm.sade.auth.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.DefaultInterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.webflow.InterruptWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.TransitionSet;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * This class should include only fixes to default cas interrupt configuration.
 *
 * @see InterruptInquiryExecutionPlanConfiguration actual interrupt configuration
 */
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InterruptConfiguration implements CasWebflowExecutionPlanConfigurer {

    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final ApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;

    public InterruptConfiguration(FlowBuilderServices flowBuilderServices,
                                  @Qualifier("loginFlowRegistry") FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                  ApplicationContext applicationContext,
                                  CasConfigurationProperties casProperties) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        // this is from default interruptWebflowConfigurer bean:
        plan.registerWebflowConfigurer(new InterruptWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties));

        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // fix missing skipped transition from inquireInterruptAction
                ActionState inquireInterruptAction = getState(getLoginFlow(), "inquireInterruptAction", ActionState.class);
                TransitionSet transitions = inquireInterruptAction.getTransitionSet();
                clear(transitions, transitions::remove);
                transitions.add(createTransition(CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
                transitions.add(createTransition(CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, "interruptView"));
            }
        });
        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // fix interrupt inquirers called twice after successful login
                ActionState state = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
                ActionList actions = state.getActionList();
                clear(actions, actions::remove);
                actions.add(super.createEvaluateAction(CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET));
            }
        });

    }

    private static <E, T extends Iterable<E>> void clear(T iterable, Consumer<E> remover) {
        StreamSupport.stream(iterable.spliterator(), false).collect(toList()).forEach(remover::accept);
    }

    // override default interruptWebflowConfigurer to be able to override its flow definitions (see above)
    @Bean
    public CasWebflowConfigurer interruptWebflowConfigurer() {
        return new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // nop
            }
        };
    }

    // fix to remove default interruption defined inside cas
    @Bean
    public InterruptInquiryExecutionPlan interruptInquirer(Optional<List<InterruptInquirer>> interruptInquirers) {
        DefaultInterruptInquiryExecutionPlan plan = new DefaultInterruptInquiryExecutionPlan();
        new InterruptInquiryExecutionPlanConfiguration(interruptInquirers).configureInterruptInquiryExecutionPlan(plan);
        return plan;
    }

}
