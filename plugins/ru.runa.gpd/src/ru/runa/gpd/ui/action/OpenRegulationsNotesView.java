package ru.runa.gpd.ui.action;

import ru.runa.gpd.ui.view.RegulationsNotesView;

public class OpenRegulationsNotesView extends OpenViewBaseAction {
    @Override
    protected String getViewId() {
        return RegulationsNotesView.ID;
    }
}
