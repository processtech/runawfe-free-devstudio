package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;

public class SubprocessPresentation extends VariableRenameProvider<Subprocess> {
    public SubprocessPresentation(Subprocess subprocess) {
        setElement(subprocess);
    }

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
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
        List<Change> changes = new ArrayList<Change>();
        if (mappingsToChange.size() > 0) {
            changes.add(new VariableMappingsChange(element, oldVariable, newVariable, mappingsToChange));
        }
        return changes;
    }

}
