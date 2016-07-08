package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;

public class SubprocessPresentation extends VariableRenameProvider<Subprocess> {
    public SubprocessPresentation(Subprocess subprocess) {
        setElement(subprocess);
    }

    @Override
    protected List<TextCompareChange> getChangeList(Variable oldVariable, Variable newVariable) throws Exception {
        List<TextCompareChange> changeList = new ArrayList<TextCompareChange>();
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
            changeList.add(new VariableMappingsChange(element, oldVariable, newVariable, mappingsToChange));
        }
        return changeList;
    }

}
