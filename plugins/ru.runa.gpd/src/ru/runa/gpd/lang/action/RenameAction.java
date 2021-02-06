package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.ui.dialog.RenameNodeDialog;

public class RenameAction extends BaseModelActionDelegate {

	@Override
	public void run(IAction action) {
		Node node = getSelection();
		String currentName = node.getName();
		RenameNodeDialog renameNodeDialog = new RenameNodeDialog(Display.getDefault().getActiveShell(), null,
				Localization.getString("InputValue"), currentName, new IInputValidator() {
					@Override
					public String isValid(String value) {
						if (value.isEmpty()) {
							return Localization.getString("VariableNamePage.error.empty", value);
						} else if (value.equals(currentName)) {
							return Localization.getString("VariableNamePage.error.duplicated", value);
						}
						return null;
					}
				});
		renameNodeDialog.open();
		try {
			String newName = renameNodeDialog.getValue();
			if (newName != null)
				node.setName(newName);
		} catch (Exception e) {
			PluginLogger.logError(e);
		}
	}
}
