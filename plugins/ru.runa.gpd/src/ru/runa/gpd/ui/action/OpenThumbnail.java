package ru.runa.gpd.ui.action;

import ru.runa.gpd.ui.view.ThumbnailView;

public class OpenThumbnail extends OpenViewBaseAction {

    @Override
    protected String getViewId() {
        return ThumbnailView.VIEW_ID;
    }

}
