package ru.runa.gpd.search;

import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;

import ru.runa.gpd.Localization;

public abstract class BaseSearchQuery implements ISearchQuery {
    private final String searchText;
    private final String context;
    private SearchResult result;

    public BaseSearchQuery(String searchText, String description) {
        this.searchText = searchText;
        this.context = description;
    }

    public String getSearchText() {
        return searchText;
    }

    public String getContext() {
        return context;
    }

    @Override
    public boolean canRunInBackground() {
        return true;
    }

    @Override
    public String getLabel() {
        return Localization.getString("Search.jobName");
    }

    @Override
    public boolean canRerun() {
        return true;
    }

    @Override
    public SearchResult getSearchResult() {
        if (result == null) {
            result = new SearchResult(this);
            new SearchResultUpdater(result);
        }
        return result;
    }

    public static class SearchResultUpdater implements IQueryListener {
        private final SearchResult result;

        public SearchResultUpdater(SearchResult result) {
            this.result = result;
            NewSearchUI.addQueryListener(this);
        }

        @Override
        public void queryAdded(ISearchQuery query) {
            // don't care
        }

        @Override
        public void queryRemoved(ISearchQuery query) {
            if (result.equals(query.getSearchResult())) {
                NewSearchUI.removeQueryListener(this);
            }
        }

        @Override
        public void queryStarting(ISearchQuery query) {
            // don't care
        }

        @Override
        public void queryFinished(ISearchQuery query) {
            // don't care
        }
    }
}
