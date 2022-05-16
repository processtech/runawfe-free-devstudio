package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.dialog.VersionCommentDialog;

public class VersionCommentAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        VersionCommentDialog versionCommentDialog = new VersionCommentDialog((ProcessDefinition) getSelectionNotNull(), getWorkbenchPage());
        versionCommentDialog.open();
    }

}
