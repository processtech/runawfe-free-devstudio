package ru.runa.gpd.formeditor.action;

import ru.runa.gpd.formeditor.ftl.ui.FormComponentsView;
import ru.runa.gpd.ui.action.OpenViewBaseAction;

public class OpenFormComponentsViewAction extends OpenViewBaseAction {

    @Override
    protected String getViewId() {
        return FormComponentsView.ID;
    }

}
