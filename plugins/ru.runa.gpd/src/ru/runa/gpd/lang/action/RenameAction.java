package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Node;

public class RenameAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        Node node = getSelection();
        String currentName = node.getName();
        InputDialog renameNodeDialog = new InputDialog(Display.getDefault().getActiveShell(), null, Localization.getString("InputValue"), currentName,
                (String value) -> {
                    if (value.trim().isEmpty()) {
                        return Localization.getString("VariableNamePage.error.empty", value);
                    } else if (value.equals(currentName)) {
                        return Localization.getString("VariableNamePage.error.duplicated", value);
                    }
                    return null;
                });
        renameNodeDialog.open();
        try {
            String newName = renameNodeDialog.getValue();
            if (newName != null && !newName.trim().equals(""))
                node.setName(newName.trim());
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }
}
