package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.dialog.VersionCommentDialog;

public class VersionCommentAction extends BaseModelActionDelegate {
    private VersionCommentDialog versionCommentDialog;

    @Override
    public void run(IAction action) {
        versionCommentDialog = new VersionCommentDialog(getActiveDesignerEditor().getDefinition());
        versionCommentDialog.open();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (getSelection() != null && getSelection().getClass().equals(ProcessDefinition.class)) {
            action.setEnabled(true);
        } else {
            action.setEnabled(false);
        }
    }
}
