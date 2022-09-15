package ru.runa.gpd.ui.action;

import ru.runa.gpd.ui.view.SearchResultView;

public class OpenSearchResult extends OpenViewBaseAction  {

    @Override
    protected String getViewId() {
        return SearchResultView.ID;
    }

}
