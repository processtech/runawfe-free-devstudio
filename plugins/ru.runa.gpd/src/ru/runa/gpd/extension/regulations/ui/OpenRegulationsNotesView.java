package ru.runa.gpd.extension.regulations.ui;

import ru.runa.gpd.ui.action.OpenViewBaseAction;

public class OpenRegulationsNotesView extends OpenViewBaseAction {
    @Override
    protected String getViewId() {
        return RegulationsNotesView.ID;
    }
}
