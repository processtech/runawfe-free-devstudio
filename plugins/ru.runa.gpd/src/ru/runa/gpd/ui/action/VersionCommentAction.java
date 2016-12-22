package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.ui.dialog.VersionCommentDialog;

public class VersionCommentAction extends BaseModelActionDelegate {
    private VersionCommentDialog versionCommentDialog;

    @Override
    public void run(IAction action) {
        versionCommentDialog = new VersionCommentDialog(getActiveDesignerEditor().getDefinition());
        versionCommentDialog.open();
    }
}
