package ru.runa.gpd.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public class BotTaskSearchQuery extends BaseSearchQuery {
    public BotTaskSearchQuery(String botName, String botTaskName) {
        super(botTaskName, botName);
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
        getSearchResult().removeAll();
        return new BotTaskSearchVisitor(this).search(monitor);
    }
}
