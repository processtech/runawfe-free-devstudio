package ru.runa.gpd.search;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.search.ui.NewSearchUI;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class MultiVariableSearchQuery extends BaseSearchQuery {
    private final IFile mainProcessdefinitionFile;
    private final ProcessDefinition mainProcessDefinition;
    private final List<Variable> variables;

    public MultiVariableSearchQuery(String searchText, IFile definitionFile, ProcessDefinition definition, List<Variable> variables) {
        super(searchText, definition.getName());
        this.mainProcessdefinitionFile = IOUtils.getAdjacentFile(definitionFile, ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
        this.mainProcessDefinition = definition.getMainProcessDefinition();
        this.variables = variables;
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
        getSearchResult().removeAll();
        // SearchMessages.TextSearchEngine_statusMessage
        MultiStatus status = new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, "search", null);
        for (Variable variable : variables) {
            VariableSearchQuery query = new VariableSearchQuery(mainProcessdefinitionFile, mainProcessDefinition, variable);
            status.add(new VariableSearchVisitor(query).search(monitor));
            getSearchResult().merge(query.getSearchResult());
        }
        return status;
    }
}
