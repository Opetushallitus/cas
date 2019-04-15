package fi.vm.sade.auth.config;

import fi.vm.sade.saml.action.SAMLAction;
import fi.vm.sade.saml.action.SAMLActionConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WebflowExecutionPlanConfiguration implements CasWebflowExecutionPlanConfigurer {

    // configurer
    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final ApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;

    // action
    private final SAMLAction samlAction;

    public WebflowExecutionPlanConfiguration(FlowBuilderServices flowBuilderServices,
                                             @Qualifier("loginFlowRegistry") FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                             ApplicationContext applicationContext,
                                             CasConfigurationProperties casProperties,
                                             SAMLAction samlAction) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
        this.samlAction = samlAction;
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(new SAMLActionConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties, samlAction));
    }

}
