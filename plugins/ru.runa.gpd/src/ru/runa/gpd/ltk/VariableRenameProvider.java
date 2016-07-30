package ru.runa.gpd.ltk;

import java.util.List;
import java.util.SortedMap;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Preconditions;

public abstract class VariableRenameProvider<T> {
    protected T element;

    public final void setElement(T element) {
        Preconditions.checkNotNull(element);
        this.element = element;
    }

    /**
     * Get changes list
     * 
     * @param variablesList
     *            list of pairs "old variable - new variable"
     * @return list of changes
     * @throws Exception
     */
    public abstract List<Change> getChanges(SortedMap<Variable, Variable> variablesMap) throws Exception;
}
