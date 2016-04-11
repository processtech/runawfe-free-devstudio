package ru.runa.gpd.ui.action;

import ru.runa.gpd.ui.view.PropertiesView;

public class OpenProperties extends OpenViewBaseAction {

    @Override
    protected String getViewId() {
        return PropertiesView.ID;
    }

}
