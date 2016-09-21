package ru.runa.gpd.lang.model;

import java.util.List;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.HandlerArtifact;

import com.google.common.collect.Lists;

/**
 * Provides mapping with formal parameters between {@link TaskState} and {@link BotTask}.
 * 
 * Configuration is in param-based xml.
 * 
 * @author Dofs
 * @since 3.6
 */
public class BotTaskLink implements IDelegable {
    private String botTaskName;
    private String delegationClassName;
    private String delegationConfiguration = "";
    private TaskState taskState;

    /**
     * linked {@link BotTask} name
     */
    public String getBotTaskName() {
        return botTaskName;
    }

    /**
     * linked {@link BotTask} name
     */
    public void setBotTaskName(String botTaskName) {
        this.botTaskName = botTaskName;
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.TASK_HANDLER;
    }

    @Override
    public String getDelegationClassName() {
        return delegationClassName;
    }

    @Override
    public void setDelegationClassName(String delegationClassName) {
        this.delegationClassName = delegationClassName;
    }

    @Override
    public String getDelegationConfiguration() {
        return delegationConfiguration;
    }

    @Override
    public void setDelegationConfiguration(String delegationConfiguration) {
        this.delegationConfiguration = delegationConfiguration;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

    @Override
    public List<String> getVariableNames(boolean includeSwimlanes, String... typeClassNameFilters) {
        if (taskState != null) {
            return taskState.getProcessDefinition().getVariableNames(includeSwimlanes, typeClassNameFilters);
        }
        return Lists.newArrayList();
    }
    
    public BotTaskLink getCopy(TaskState taskState) {
        BotTaskLink copy = new BotTaskLink();
        copy.setBotTaskName(botTaskName);
        copy.setDelegationClassName(delegationClassName);
        copy.setDelegationConfiguration(delegationConfiguration);
        copy.setTaskState(taskState);
        return copy;
    }
    
    @Override
    public String toString() {
        return Localization.getString("BotTaskLink.description", taskState, botTaskName);
    }
}
