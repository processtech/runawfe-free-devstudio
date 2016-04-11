package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.util.WorkspaceOperations;

public class NewBotStationAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        WorkspaceOperations.createNewBotStation(getStructuredSelection());
    }
}
