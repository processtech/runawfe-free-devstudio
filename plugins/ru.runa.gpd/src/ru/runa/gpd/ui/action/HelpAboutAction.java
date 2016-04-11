package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.ui.dialog.AboutDialog;

public class HelpAboutAction extends BaseActionDelegate {

	public void run(IAction action) {
		if (window != null) {
			new AboutDialog(window.getShell()).open();
        }
	}

}
