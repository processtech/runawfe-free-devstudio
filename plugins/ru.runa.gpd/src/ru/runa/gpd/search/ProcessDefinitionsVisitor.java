package ru.runa.gpd.search;

import java.text.MessageFormat;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.NewSearchUI;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;

public abstract class ProcessDefinitionsVisitor {
    protected final BaseSearchQuery query;
    private IProgressMonitor progressMonitor;
    private int numberOfScannedElements;
    private int numberOfElementsToScan;
    private ProcessDefinition currentDefinition;
    private final MultiStatus status;

    public ProcessDefinitionsVisitor(BaseSearchQuery query) {
        this.query = query;
        this.status = new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, SearchMessages.TextSearchEngine_statusMessage, null);
    }

    public IStatus search(SearchResult searchResult, IProgressMonitor monitor) {
        progressMonitor = monitor == null ? new NullProgressMonitor() : monitor;
        numberOfScannedElements = 0;
        numberOfElementsToScan = ProcessCache.getAllProcessDefinitions().size();
        Job monitorUpdateJob = new Job("Searching") {
            private int lastNumberOfScannedElements = 0;

            @Override
            public IStatus run(IProgressMonitor inner) {
                while (!inner.isCanceled()) {
                    if (currentDefinition != null) {
                        String name = currentDefinition.getName();
                        Object[] args = { name, numberOfScannedElements, numberOfElementsToScan };
                        progressMonitor.subTask(MessageFormat.format(SearchMessages.TextSearchVisitor_scanning, args));
                        int steps = numberOfScannedElements - lastNumberOfScannedElements;
                        progressMonitor.worked(steps);
                        lastNumberOfScannedElements += steps;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return Status.OK_STATUS;
                    }
                }
                return Status.OK_STATUS;
            }
        };
        try {
            String taskName = SearchMessages.TextSearchVisitor_filesearch_task_label;
            progressMonitor.beginTask(taskName, numberOfElementsToScan);
            monitorUpdateJob.setSystem(true);
            monitorUpdateJob.schedule();
            try {
                for (Map.Entry<IFile, ProcessDefinition> entry : ProcessCache.getAllProcessDefinitionsMap().entrySet()) {
                    try {
                        currentDefinition = entry.getValue();
                        findInProcessDefinition(entry.getKey(), entry.getValue());
                    } catch (Exception e) {
                        status.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
                    } finally {
                        numberOfScannedElements++;
                    }
                    if (progressMonitor.isCanceled()) {
                        throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled);
                    }
                }
                return status;
            } finally {
                monitorUpdateJob.cancel();
            }
        } finally {
            progressMonitor.done();
        }
    }

    protected abstract void findInProcessDefinition(IFile definitionFile, ProcessDefinition processDefinition);

    public IStatus search(IProgressMonitor monitor) {
        return search(query.getSearchResult(), monitor);
    }
}
