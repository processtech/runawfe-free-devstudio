package ru.runa.gpd.ui.dialog;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.TaskState;

public class EscalationActionEditDialog extends TimerActionEditDialog {
    
    public EscalationActionEditDialog(TaskState taskState) {
        super(taskState.getProcessDefinition(), taskState.getEscalationAction());
    }

    @Override
    protected boolean isClassNameFieldEnabled() {
        return false;
    }

    @Override
    protected String getConfigurationLabel() {
        return Localization.getString("property.escalation.configuration");
    }

    @Override
    protected boolean isDeleteButtonEnabled() {
        return false;
    }
}
