package ru.runa.gpd.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public class BotSearchQuery extends BaseSearchQuery {
    public BotSearchQuery(String botName) {
        super(botName, "any");
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
        getSearchResult().removeAll();
        return new BotSearchVisitor(this).search(monitor);
    }
}
