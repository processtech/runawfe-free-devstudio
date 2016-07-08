package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
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
     * Get changes array
     * 
     * @param variablesList
     *            list of pairs "old variable - new variable"
     * @return list of changes
     * @throws Exception
     */
    public List<Change> getChanges(SortedMap<Variable, Variable> variablesMap) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            changes.addAll(getChangeList(entry.getKey(), entry.getValue()));
        }
        return changes;
    }

    /**
     * Get change list for pair "old variable - new variable"<br>
     * Rerturn empty list if no change created for this pair<br>
     * Subclass must override this method if standard and simple processing is applicable for pair "old variable - new variable", or
     * <code>getChanges(SortedMap<Variable, Variable>)<code> for difficult logic
     * 
     * @param oldVariable
     *            old variable
     * @param newVariable
     *            new variable
     * @return changes list to be added in result of <code>getChanges(SortedMap<Variable, Variable>)<code>
     * @throws Exception
     */
    protected List<TextCompareChange> getChangeList(Variable oldVariable, Variable newVariable) throws Exception {
        return new ArrayList<TextCompareChange>();
    }
}
