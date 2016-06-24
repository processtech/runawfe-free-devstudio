package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;

public class SubprocessPresentation extends VariableRenameProvider<Subprocess> {
    public SubprocessPresentation(Subprocess subprocess) {
        setElement(subprocess);
    }

    @Override
    public List<Change> getChanges(Map<Variable, Variable> variablesMap) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            Variable oldVariable = entry.getKey();
            Variable newVariable = entry.getValue();
            List<VariableMapping> mappingsToChange = new ArrayList<VariableMapping>();
            for (VariableMapping mapping : element.getVariableMappings()) {
                if (mapping.isMultiinstanceLinkByRelation() && mapping.getName().contains("(" + oldVariable.getName() + ")")) {
                    mappingsToChange.add(mapping);
                }
                if (mapping.isText()) {
                    continue;
                }
                if (mapping.getName().equals(oldVariable.getName())) {
                    mappingsToChange.add(mapping);
                }
            }
            if (mappingsToChange.size() > 0) {
                changes.add(new VariableMappingsChange(element, oldVariable, newVariable, mappingsToChange));
            }
        }
        return changes;
    }

}
