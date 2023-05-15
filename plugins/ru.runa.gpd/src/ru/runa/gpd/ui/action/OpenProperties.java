package ru.runa.gpd.ui.action;

import ru.runa.gpd.Activator;
import ru.runa.gpd.settings.PrefConstants;

public class OpenProperties extends OpenViewBaseAction {

    @Override
    protected String getViewId() {
        return Activator.getDefault().getPreferenceStore().getString(PrefConstants.P_PROPERTIES_VIEW_ID);
    }

}
