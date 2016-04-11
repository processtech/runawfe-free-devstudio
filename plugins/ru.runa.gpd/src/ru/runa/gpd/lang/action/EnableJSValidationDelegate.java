package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.editor.gef.command.EnableJSValidationCommand;
import ru.runa.gpd.lang.model.FormNode;

public class EnableJSValidationDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        FormNode formNode = getSelection();
        EnableJSValidationCommand command = new EnableJSValidationCommand();
        command.setFormNode(formNode);
        command.setEnabled(action.isChecked());
        executeCommand(command);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        FormNode formNode = getSelection();
        if (formNode != null) {
            action.setEnabled(formNode.hasFormValidation());
            action.setChecked(formNode.isUseJSValidation());
        }
    }
}
