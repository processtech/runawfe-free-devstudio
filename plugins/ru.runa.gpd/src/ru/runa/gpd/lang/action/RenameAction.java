package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.ui.dialog.RenameNodeDialog;

public class RenameAction extends BaseModelActionDelegate {

	@Override
	public void run(IAction action) {
		Node node = getSelection();
		RenameNodeDialog renameNodeDialog = new RenameNodeDialog(Display.getDefault().getActiveShell(), null,
				Localization.getString("InputValue"), null, null);
		renameNodeDialog.open();
		try {
			String newName = renameNodeDialog.getValue();
			node.setName(newName);
		} catch (Exception e) {
			PluginLogger.logError(e);
		}
	}
}
