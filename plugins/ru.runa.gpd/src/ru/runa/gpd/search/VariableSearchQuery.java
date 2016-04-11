package ru.runa.gpd.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class VariableSearchQuery extends BaseSearchQuery {
    private final IFile mainProcessdefinitionFile;
    private final ProcessDefinition mainProcessDefinition;
    private final Variable variable;

    public VariableSearchQuery(IFile definitionFile, ProcessDefinition definition, Variable variable) {
        super(variable.getName(), definition.getName());
        this.mainProcessdefinitionFile = IOUtils.getAdjacentFile(definitionFile, ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
        this.mainProcessDefinition = definition.getMainProcessDefinition();
        this.variable = variable;
    }

    public ProcessDefinition getMainProcessDefinition() {
        return mainProcessDefinition;
    }

    public IFile getMainProcessdefinitionFile() {
        return mainProcessdefinitionFile;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
        getSearchResult().removeAll();
        return new VariableSearchVisitor(this).search(monitor);
    }
}
