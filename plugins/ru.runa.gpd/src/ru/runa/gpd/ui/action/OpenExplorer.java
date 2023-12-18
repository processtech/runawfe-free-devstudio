package ru.runa.gpd.ui.action;

import ru.runa.gpd.ui.view.ProcessExplorerTreeView;

public class OpenExplorer extends OpenViewBaseAction {
    @Override
    protected String getViewId() {
        return ProcessExplorerTreeView.ID;
    }
}
