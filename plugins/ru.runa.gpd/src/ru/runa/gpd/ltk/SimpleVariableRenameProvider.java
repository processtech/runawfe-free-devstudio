package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.Variable;

public abstract class SimpleVariableRenameProvider<T> extends VariableRenameProvider<T> {
    @Override
    public List<Change> getChanges(SortedMap<Variable, Variable> variablesMap) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            changes.addAll(getChangesForVariable(entry.getKey(), entry.getValue()));
        }
        return changes;
    }

    /**
     * Get change list for pair "old variable - new variable"<br>
     * Rerturn empty list if no change created for this pair<br>
     * 
     * @param oldVariable
     *            old variable
     * @param newVariable
     *            new variable
     * @return list of changes
     * @throws Exception
     */
    protected abstract List<TextCompareChange> getChangesForVariable(Variable oldVariable, Variable newVariable) throws Exception;
}
