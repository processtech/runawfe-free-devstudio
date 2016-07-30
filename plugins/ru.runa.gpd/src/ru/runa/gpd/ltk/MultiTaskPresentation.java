package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;

import com.google.common.base.Objects;

public class MultiTaskPresentation extends SimpleVariableRenameProvider<MultiTaskState> {

    public MultiTaskPresentation(MultiTaskState timed) {
        setElement(timed);
    }

    @Override
    protected List<TextCompareChange> getChangesForVariable(Variable oldVariable, Variable newVariable) throws Exception {
        List<TextCompareChange> changeList = new ArrayList<TextCompareChange>();
        VariableMapping discriminatorMapping = element.getDiscriminatorMapping();
        if (discriminatorMapping.isMultiinstanceLinkByVariable() && Objects.equal(oldVariable.getName(), discriminatorMapping.getName())) {
            changeList.add(new MultiTaskDiscriminatorChange(element, oldVariable, newVariable));
        }
        if (discriminatorMapping.isMultiinstanceLinkByGroup() && !discriminatorMapping.isText()
                && Objects.equal(oldVariable.getName(), discriminatorMapping.getName())) {
            changeList.add(new MultiTaskDiscriminatorChange(element, oldVariable, newVariable));
        }
        if (discriminatorMapping.isMultiinstanceLinkByRelation() && discriminatorMapping.getName().contains("(" + oldVariable.getName() + ")")) {
            changeList.add(new MultiTaskDiscriminatorChange(element, oldVariable, newVariable));
        }
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

    private class MultiTaskDiscriminatorChange extends TextCompareChange {

        public MultiTaskDiscriminatorChange(Object element, Variable currentVariable, Variable previewVariable) {
            super(element, currentVariable, previewVariable);
        }

        @Override
        protected void performInUIThread() {
            VariableMapping discriminatorMapping = element.getDiscriminatorMapping();
            if (discriminatorMapping.isMultiinstanceLinkByVariable()) {
                element.setDiscriminatorValue(replacementVariable.getName());
            }
            if (discriminatorMapping.isMultiinstanceLinkByGroup() && !discriminatorMapping.isText()) {
                element.setDiscriminatorValue(replacementVariable.getName());
            }
            if (discriminatorMapping.isMultiinstanceLinkByRelation()) {
                String s = element.getDiscriminatorValue();
                s = s.replace("(" + currentVariable.getName() + ")", "(" + replacementVariable.getName() + ")");
                element.setDiscriminatorValue(s);
            }
        }

        @Override
        protected String toPreviewContent(Variable variable) {
            return variable.getName();
        }
    }

}
