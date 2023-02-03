package ru.runa.gpd.lang.model;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.Activator;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventContainer;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.EscalationActionPropertyDescriptor;
import ru.runa.gpd.settings.LanguageElementPreferenceNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.extension.handler.EscalationActionHandler;
import ru.runa.wfe.lang.AsyncCompletionMode;

public class TaskState extends FormNode implements ActionContainer, ITimed, Synchronizable, IBoundaryEventContainer {
    private TimerAction escalationAction;
    private boolean ignoreSubstitutionRules;
    private boolean useEscalation;
    private Duration escalationDelay = new Duration();
    private boolean async;
    private AsyncCompletionMode asyncCompletionMode = AsyncCompletionMode.ON_MAIN_PROCESS_END;
    private BotTaskLink botTaskLink;
    private Duration timeOutDelay = new Duration();
    protected Boolean reassignSwimlaneToInitializer = null;

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    public Boolean isReassignSwimlaneToInitializer() {
        return reassignSwimlaneToInitializer;
    }

    public void setReassignSwimlaneToInitializer(Boolean reassignSwimlaneToInitializer) {
        Boolean old = this.reassignSwimlaneToInitializer;
        this.reassignSwimlaneToInitializer = reassignSwimlaneToInitializer;
        firePropertyChange(PROPERTY_SWIMLANE_REASSIGN, old, this.reassignSwimlaneToInitializer);
    }

    public String getTimeOutDueDate() {
        if (timeOutDelay == null || !timeOutDelay.hasDuration()) {
            return null;
        }
        return timeOutDelay.getDuration();
    }

    public void setTimeOutDelay(Duration timeOutDuration) {
        Duration old = this.timeOutDelay;
        this.timeOutDelay = timeOutDuration;
        firePropertyChange(PROPERTY_TASK_DEADLINE, old, this.timeOutDelay);
    }

    public Duration getTimeOutDelay() {
        return timeOutDelay;
    }

    @Override
    public String getNextTransitionName(NodeTypeDefinition typeDefinition) {
        if (getProcessDefinition().getLanguage() == Language.JPDL && getTimer() != null
                && getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return PluginConstants.TIMER_TRANSITION_NAME;
        }
        return super.getNextTransitionName(typeDefinition);
    }

    @Override
    public AsyncCompletionMode getAsyncCompletionMode() {
        return asyncCompletionMode;
    }

    @Override
    public void setAsyncCompletionMode(AsyncCompletionMode asyncCompletionMode) {
        AsyncCompletionMode old = this.asyncCompletionMode;
        this.asyncCompletionMode = asyncCompletionMode;
        firePropertyChange(PROPERTY_ASYNC_COMPLETION_MODE, old, asyncCompletionMode);
    }

    /**
     * @see BotTaskUtils#getBotName(org.jbpm.ui.common.model.Swimlane)
     * @return bot name or <code>null</code>
     */
    public String getSwimlaneBotName() {
        return BotTaskUtils.getBotName(getSwimlane());
    }

    @Override
    public void setSwimlane(Swimlane swimlane) {
        super.setSwimlane(swimlane);
        if (getRegulationsProperties().isEnabled() && getSwimlaneBotName() != null) {
            getRegulationsProperties().setEnabledByDefault(false);
        }
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (super.testAttribute(target, name, value)) {
            return true;
        }
        if ("swimlanePointsToBot".equals(name)) {
            String botName = getSwimlaneBotName();
            return botName != null && BotCache.getAllBotNames().contains(botName);
        }
        return false;
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public void setAsync(boolean async) {
        if (this.async != async) {
            this.async = async;
            firePropertyChange(PROPERTY_ASYNC, !async, async);
        }
    }

    public BotTaskLink getBotTaskLink() {
        return botTaskLink;
    }

    public void setBotTaskLink(BotTaskLink botTaskLink) {
        if (!Objects.equal(this.botTaskLink, botTaskLink)) {
            this.botTaskLink = botTaskLink;
            if (this.botTaskLink != null) {
                this.botTaskLink.setTaskState(this);
            }
            firePropertyChange(PROPERTY_BOT_TASK_NAME, null, "");
        }
    }

    public TimerAction getEscalationAction() {
        return escalationAction;
    }

    public void setEscalationAction(TimerAction escalationAction) {
        this.escalationAction = escalationAction;
    }

    public Duration getEscalationDelay() {
        return escalationDelay;
    }

    public void setEscalationDelay(Duration escalationDelay) {
        this.escalationDelay = escalationDelay;
        firePropertyChange(PROPERTY_ESCALATION, null, escalationDelay);
    }

    public boolean isUseEscalation() {
        return useEscalation;
    }

    public void setUseEscalation(boolean useEscalation) { // TODO refactor
        if (escalationAction == null || !this.useEscalation) {
            escalationAction = new TimerAction(getProcessDefinition());
            escalationAction.setDelegationClassName(EscalationActionHandler.class.getName());
            String orgFunction = Activator.getPrefString(PrefConstants.P_ESCALATION_CONFIG);
            escalationAction.setDelegationConfiguration(orgFunction);
            String repeat = Activator.getPrefString(PrefConstants.P_ESCALATION_REPEAT);
            if (!Strings.isNullOrEmpty(repeat)) {
                escalationAction.setRepeatDuration(repeat);
            }
            String expirationTime = Activator.getPrefString(PrefConstants.P_ESCALATION_DURATION);
            if (!Strings.isNullOrEmpty(expirationTime)) {
                escalationDelay = new Duration(expirationTime);
            }
        }
        this.useEscalation = useEscalation;
        firePropertyChange(PROPERTY_ESCALATION, !useEscalation, useEscalation);
    }

    public boolean isIgnoreSubstitutionRules() {
        return ignoreSubstitutionRules;
    }

    public void setIgnoreSubstitutionRules(boolean ignoreSubstitutionRules) {
        boolean old = this.ignoreSubstitutionRules;
        this.ignoreSubstitutionRules = ignoreSubstitutionRules;
        firePropertyChange(PROPERTY_IGNORE_SUBSTITUTION_RULES, old, this.ignoreSubstitutionRules);
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new PropertyDescriptor(PROPERTY_IGNORE_SUBSTITUTION_RULES, Localization.getString("property.ignoreSubstitution")));
        descriptors.add(new DurationPropertyDescriptor(PROPERTY_TASK_DEADLINE, getProcessDefinition(), getTimeOutDelay(), Localization
                .getString("property.deadline")));
        if (useEscalation) {
            descriptors.add(new EscalationActionPropertyDescriptor(PROPERTY_ESCALATION_ACTION, Localization.getString("escalation.action"), this));
            descriptors.add(new DurationPropertyDescriptor(PROPERTY_ESCALATION_DURATION, getProcessDefinition(), getEscalationDelay(), Localization
                    .getString("escalation.duration")));
        }
        if (botTaskLink != null) {
            descriptors.add(new PropertyDescriptor(PROPERTY_BOT_TASK_NAME, Localization.getString("property.botTaskName")));
        }
        descriptors.add(new PropertyDescriptor(PROPERTY_ASYNC, Localization.getString("property.execution.async")));
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_ESCALATION_DURATION.equals(id)) {
            if (escalationDelay.hasDuration()) {
                return escalationDelay;
            }
            return "";
        }
        if (PROPERTY_TASK_DEADLINE.equals(id)) {
            Duration d = getTimeOutDelay();
            if (d == null || !d.hasDuration()) {
                return "";
            }
            return d;
        }
        if (PROPERTY_ESCALATION_ACTION.equals(id)) {
            return escalationAction;
        }
        if (PROPERTY_IGNORE_SUBSTITUTION_RULES.equals(id)) {
            return ignoreSubstitutionRules ? Localization.getString("yes") : Localization.getString("no");
        }
        if (PROPERTY_ASYNC.equals(id)) {
            return async ? Localization.getString("yes") : Localization.getString("no");
        }
        if (PROPERTY_BOT_TASK_NAME.equals(id)) {
            return botTaskLink == null ? "" : botTaskLink.getBotTaskName();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_ESCALATION_ACTION.equals(id)) {
            setEscalationAction((TimerAction) value);
        } else if (PROPERTY_ESCALATION_DURATION.equals(id)) {
            setEscalationDelay((Duration) value);
        } else if (PROPERTY_TASK_DEADLINE.equals(id)) {
            if (value == null) {
                // ignore, edit was canceled
                return;
            }
            setTimeOutDelay((Duration) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    protected void fillCopyCustomFields(GraphElement aCopy) {
        super.fillCopyCustomFields(aCopy);
        TaskState copy = (TaskState) aCopy;
        if (getBotTaskLink() != null) {
            copy.setBotTaskLink(getBotTaskLink().getCopy(copy));
        }
        if (getEscalationAction() != null) {
            copy.setEscalationAction(getEscalationAction().makeCopy(aCopy.getParent().getProcessDefinition()));
        }
        if (getEscalationDelay() != null) {
            copy.setEscalationDelay(new Duration(getEscalationDelay()));
        }
        copy.setIgnoreSubstitutionRules(ignoreSubstitutionRules);
        copy.setReassignSwimlaneToInitializer(reassignSwimlaneToInitializer);
        copy.setReassignSwimlaneToTaskPerformer(reassignSwimlaneToTaskPerformer);
        if (getTimeOutDelay() != null) {
            copy.setTimeOutDelay(new Duration(getTimeOutDelay()));
        }
        copy.setUseEscalation(useEscalation);
    }

    @Override
    public List<Variable> getUsedVariables(IFolder processFolder) {
        List<Variable> result = super.getUsedVariables(processFolder);
        if (getBotTaskLink() != null && !Strings.isNullOrEmpty(getBotTaskLink().getDelegationConfiguration())) {
            Map<String, String> map = ParamDefConfig.getAllParameters(getBotTaskLink().getDelegationConfiguration());
            for (String variableName : map.values()) {
                Variable variable = VariableUtils.getVariableByName(getProcessDefinition(), variableName);
                if (variable != null) {
                    result.add(variable);
                }
            }
        }
        return result;
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (getBotTaskLink() != null) {
            BotTask botTask = BotCache.getBotTask(getSwimlaneBotName(), getBotTaskLink().getBotTaskName());
            if (botTask == null) {
                errors.add(ValidationError.createLocalizedWarning(this, "taskState.botTaskLink.botTaskNotFound", getSwimlaneBotName(),
                        getBotTaskLink().getBotTaskName()));
                return;
            }
            if (botTask.getParamDefConfig() == null) {
                errors.add(ValidationError.createLocalizedWarning(this, "taskState.botTaskParamDefConfig.null"));
                return;
            }
            Set<String> botTaskConfigParameterNames = botTask.getParamDefConfig().getAllParameterNames(true);
            if (Strings.isNullOrEmpty(getBotTaskLink().getDelegationConfiguration())) {
                if (!botTaskConfigParameterNames.isEmpty()) {
                    errors.add(ValidationError.createLocalizedError(this, "taskState.botTaskLinkConfig.empty"));
                    return;
                }
            } else {
                List<String> variableNames = getVariableNames(true);
                Map<String, String> parameters = ParamDefConfig.getAllParameters(getBotTaskLink().getDelegationConfiguration());
                for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                    botTaskConfigParameterNames.remove(parameter.getKey());
                    ParamDef paramDef = botTask.getParamDefConfig().getParamDef(parameter.getKey());
                    if (paramDef != null && paramDef.isUseVariable() && !variableNames.contains(parameter.getValue())) {
                        errors.add(ValidationError.createLocalizedError(this, "taskState.botTaskLinkConfig.variable.doesnotexist",
                                paramDef.getLabel(), parameter.getValue()));
                    }
                }
            }
            if (botTaskConfigParameterNames.size() > 0) {
                errors.add(ValidationError.createLocalizedError(this, "taskState.botTaskLinkConfig.insufficient", botTaskConfigParameterNames));
            }
        } else {
            // validate against simple bot task
            String botName = getSwimlaneBotName();
            if (botName != null) {
                BotTask botTask = BotCache.getBotTask(botName, getName());
                if (botTask != null) {
                    if (botTask.getDelegationClassName() != null) {
                        DelegableProvider provider = HandlerRegistry.getProvider(botTask.getDelegationClassName());
                        if (provider != null) {
                            try {
                                List<String> handlerVariableNames = provider.getUsedVariableNames(botTask);
                                List<String> processVariableNames = getVariableNames(true);
                                for (String variableName : handlerVariableNames) {
                                    if (!processVariableNames.contains(variableName)) {
                                        errors.add(ValidationError.createLocalizedWarning(this, "taskState.simpleBotTask.usedInvalidVariable",
                                                variableName));
                                    }
                                }
                            } catch (Exception e) {
                                errors.add(ValidationError.createWarning(this, e.toString()));
                            }
                        } else {
                            errors.add(ValidationError.createLocalizedWarning(this, "taskState.simpleBotTask.invalidClassName"));
                        }
                    } else {
                        errors.add(ValidationError.createLocalizedWarning(this, "taskState.simpleBotTask.emptyClassName"));
                    }
                } else if (BotCache.getAllBotNames().contains(botName)) {
                    errors.add(ValidationError.createLocalizedWarning(this, "taskState.simpleBotTask.notFoundByName", botName, getName()));
                }
            }
        }
        if (isAsync()) {
            if (getTimer() != null) {
                errors.add(ValidationError.createLocalizedError(this, "taskState.timerInAsyncTask"));
            }
            String propertyName = LanguageElementPreferenceNode.getId(this.getTypeDefinition(), getProcessDefinition().getLanguage()) + '.'
                    + PrefConstants.P_LANGUAGE_TASK_STATE_ASYNC_INPUT_DATA;
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            boolean inputDataAllowedInAsyncTaskNode = store.contains(propertyName) ? store.getBoolean(propertyName) : false;
            if (!inputDataAllowedInAsyncTaskNode) {
                try {
                    Map<String, FormVariableAccess> formVariables = getFormVariables((IFolder) definitionFile.getParent());
                    for (FormVariableAccess access : formVariables.values()) {
                        if (access == FormVariableAccess.WRITE) {
                            errors.add(ValidationError.createLocalizedWarning(this, "taskState.asyncVariablesInput"));
                            break;
                        }
                    }
                } catch (Exception e) {
                    PluginLogger.logError(e);
                }
            }
        }
    }
    
    @Override
    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        return true;
    }
}
