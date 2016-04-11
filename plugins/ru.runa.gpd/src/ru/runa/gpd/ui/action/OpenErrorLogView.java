package ru.runa.gpd.ui.action;

public class OpenErrorLogView extends OpenViewBaseAction {

    @Override
    protected String getViewId() {
        return "org.eclipse.pde.runtime.LogView";
    }

}
