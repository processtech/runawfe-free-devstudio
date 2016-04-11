package ru.runa.gpd.ui.action;

import ru.runa.gpd.ui.view.SleakView;

public class OpenSleakView extends OpenViewBaseAction {
    @Override
    protected String getViewId() {
        return SleakView.ID;
    }
}
