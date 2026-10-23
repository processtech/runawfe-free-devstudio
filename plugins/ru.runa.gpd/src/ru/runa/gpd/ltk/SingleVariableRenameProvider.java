package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

public abstract class SingleVariableRenameProvider<T> extends VariableRenameProvider<T> {

    @Override
    public List<Change> getChanges(SortedMap<Variable, Variable> variablesMap) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            changes.addAll(getChanges(entry.getKey(), entry.getValue()));
        }
        return changes;
    }

    public List<Change> getChanges(SortedMap<Variable, Variable> variablesMap, VariableUserType userType) throws Exception {
        List<Change> changes = new ArrayList<>();
        for (Map.Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            changes.addAll(getChanges(entry.getKey(), entry.getValue(), userType));
        }
        return changes;
    }

    /**
     * Get change list for pair "old UserType - new UserType".
     * <br>
     * @param oldUserType
     *            old UserType
     * @param newUserType
     *            new UserType
     * @return list of changes or empty list
     * @throws Exception
     */
    public List<Change> getChanges(VariableUserType oldUserType, VariableUserType newUserType) throws Exception {
        return new ArrayList<>();
    }

    /**
     * Get change list for pair "old variable - new variable"<br>
     * 
     * @param oldVariable
     *            old variable
     * @param newVariable
     *            new variable
     * @return list of changes or empty list
     * @throws Exception
     *             TODO
     */
    protected abstract List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception;

    /**
     * Get change list for pair "old variable - new variable" with UserType.
     * <br>
     * The default implementation delegates to {@link #getChanges(Variable, Variable)}.
     * Subclasses can override this method when UserType information is required.
     *
     * @param oldVariable old variable
     * @param newVariable new variable
     * @param userType    UserType associated with the variable
     * @return list of changes or empty list
     * @throws Exception
     */
    protected List<Change> getChanges(Variable oldVariable, Variable newVariable, VariableUserType userType) throws Exception {
        return getChanges(oldVariable, newVariable);
    }
}
