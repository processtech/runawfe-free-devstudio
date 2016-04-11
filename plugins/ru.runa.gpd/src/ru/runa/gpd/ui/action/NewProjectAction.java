package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.util.WorkspaceOperations;

public class NewProjectAction extends BaseActionDelegate {

	public void run(IAction action) {
	    WorkspaceOperations.createNewProject();
	}

}
