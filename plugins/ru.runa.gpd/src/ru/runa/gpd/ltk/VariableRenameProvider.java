package ru.runa.gpd.ltk;

import java.util.List;
import java.util.SortedMap;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Preconditions;
import ru.runa.gpd.lang.model.VariableUserType;

public abstract class VariableRenameProvider<T> {
    protected T element;

    public final void setElement(T element) {
        Preconditions.checkNotNull(element);
        this.element = element;
    }

    public T getElement() {
        return element;
    }

    /**
     * Get changes list
     * 
     * @param variablesMap
     *            list of pairs "old variable - new variable"
     * @return list of changes
     * @throws Exception
     */
    public abstract List<Change> getChanges(SortedMap<Variable, Variable> variablesMap) throws Exception;

    /**
     * Get changes list with user type.
     *
     * @param variablesMap
     *            list of pairs "old variable - new variable"
     * @param userType
     *            variable user type
     * @return list of changes
     * @throws Exception
     */
    public List<Change> getChanges(SortedMap<Variable, Variable> variablesMap, VariableUserType userType) throws Exception {
        return getChanges(variablesMap);
    }
}
