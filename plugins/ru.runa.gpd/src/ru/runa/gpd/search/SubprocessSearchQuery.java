package ru.runa.gpd.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public class SubprocessSearchQuery extends BaseSearchQuery {
    
    public SubprocessSearchQuery(String processDefinitionName) {
        super(processDefinitionName, "any");
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
        getSearchResult().removeAll();
        return new SubprocessSearchVisitor(this).search(monitor);
    }

}
