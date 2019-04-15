package fi.vm.sade.saml.action;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class SAMLActionConfigurer extends AbstractCasWebflowConfigurer {

    private static final String STATE_ID_SAML_DECISION = "samlDecision";
    private static final String STATE_ID_SAML_ACTION = "samlAction";

    private final SAMLAction samlAction;

    public SAMLActionConfigurer(FlowBuilderServices flowBuilderServices,
                                FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                ApplicationContext applicationContext,
                                CasConfigurationProperties casProperties,
                                SAMLAction samlAction) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.samlAction = samlAction;
    }

    @Override
    protected void doInitialize() {
        Flow loginFlow = super.getLoginFlow();
        StateDefinition originalStartState = loginFlow.getStartState();

        String decision = "requestParameters.authToken != null && !requestParameters.authToken.isEmpty()";
        DecisionState decisionState = super.createDecisionState(loginFlow, STATE_ID_SAML_DECISION, decision,
                STATE_ID_SAML_ACTION, originalStartState.getId());
        loginFlow.setStartState(decisionState);

        ActionState actionState = super.createActionState(loginFlow, STATE_ID_SAML_ACTION, this.samlAction);
        super.createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
        super.createStateDefaultTransition(actionState, originalStartState);
        actionState.getExitActionList().add(super.createEvaluateAction(
                CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
    }

}
