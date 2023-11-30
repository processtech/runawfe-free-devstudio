package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class RenameAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        NamedGraphElement element = getSelection();
        String currentName = element.getName();
        InputDialog renameNodeDialog = new InputDialog(Display.getDefault().getActiveShell(), Localization.getString("RenameAction.title"),
                Localization.getString("InputValue"), currentName, element.nameValidator());
        try {
            if (renameNodeDialog.open() == InputDialog.OK) {
                element.setName(renameNodeDialog.getValue().trim());
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }
}
