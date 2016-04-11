package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.ui.dialog.MultiTaskDiscriminatorDialog;

public class EditMultiTaskDiscriminatorAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        MultiTaskState state = getSelection();
        MultiTaskDiscriminatorDialog dialog = new MultiTaskDiscriminatorDialog(state);
        if (dialog.open() == IDialogConstants.OK_ID) {
            state.setDiscriminatorUsage(dialog.getParameters().getDiscriminatorMapping().getUsage());
            state.setDiscriminatorValue(dialog.getParameters().getDiscriminatorMapping().getName());
            state.setCreationMode(dialog.getParameters().getCreationMode());
            state.setSwimlane(state.getProcessDefinition().getSwimlaneByName(dialog.getParameters().getSwimlaneName()));
            state.setDiscriminatorCondition(dialog.getParameters().getDiscriminatorCondition());
            state.setVariableMappings(dialog.getVariableMappings());
        }
    }
}
