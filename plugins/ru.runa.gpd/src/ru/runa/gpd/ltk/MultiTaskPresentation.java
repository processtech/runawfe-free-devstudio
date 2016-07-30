package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;

import com.google.common.base.Objects;

public class MultiTaskPresentation extends SingleVariableRenameProvider<MultiTaskState> {

    public MultiTaskPresentation(MultiTaskState timed) {
        setElement(timed);
    }

    @Override
    protected List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<>();
        VariableMapping discriminatorMapping = element.getDiscriminatorMapping();
        if (discriminatorMapping.isMultiinstanceLinkByVariable() && Objects.equal(oldVariable.getName(), discriminatorMapping.getName())) {
            changes.add(new MultiTaskDiscriminatorChange(element, oldVariable, newVariable));
        }
        if (discriminatorMapping.isMultiinstanceLinkByGroup() && !discriminatorMapping.isText()
                && Objects.equal(oldVariable.getName(), discriminatorMapping.getName())) {
            changes.add(new MultiTaskDiscriminatorChange(element, oldVariable, newVariable));
        }
        if (discriminatorMapping.isMultiinstanceLinkByRelation() && discriminatorMapping.getName().contains("(" + oldVariable.getName() + ")")) {
            changes.add(new MultiTaskDiscriminatorChange(element, oldVariable, newVariable));
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
            changes.add(new VariableMappingsChange(element, oldVariable, newVariable, mappingsToChange));
        }
        return changes;
    }

    private class MultiTaskDiscriminatorChange extends TextCompareChange {

        public MultiTaskDiscriminatorChange(Object element, Variable currentVariable, Variable previewVariable) {
            super(element, currentVariable, previewVariable);
        }

        @Override
        protected void performInUIThread() {
            try {
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
            } catch (Exception e) {
                // TODO notify user
                PluginLogger.logErrorWithoutDialog("Unable to perform change in " + element, e);
            }
        }

        @Override
        protected String toPreviewContent(Variable variable) {
            return variable.getName();
        }
    }

}
