package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.lang.model.MessagingNode;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;

public class MessagingNodeRenameProvider extends VariableRenameProvider<MessagingNode> {

    @Override
    protected List<TextCompareChange> getChangeList(Variable oldVariable, Variable newVariable) throws Exception {
        List<TextCompareChange> changeList = new ArrayList<TextCompareChange>();
        List<VariableMapping> mappingsToChange = new ArrayList<VariableMapping>();
        for (VariableMapping mapping : element.getVariableMappings()) {
            if (mapping.isPropertySelector()) {
                if (mapping.getMappedName().equals(VariableUtils.wrapVariableName(oldVariable.getName()))) {
                    mappingsToChange.add(mapping);
                }
            } else {
                if (mapping.getName().equals(oldVariable.getName())) {
                    mappingsToChange.add(mapping);
                }
            }
        }
        if (mappingsToChange.size() > 0) {
            changeList.add(new VariableMappingChange(element, oldVariable, newVariable, mappingsToChange));
        }
        return changeList;
    }

    private class VariableMappingChange extends TextCompareChange {

        private final List<VariableMapping> mappingsToChange;

        public VariableMappingChange(NamedGraphElement element, Variable currentVariable, Variable replacementVariable,
                List<VariableMapping> mappingsToChange) {
            super(element, currentVariable, replacementVariable);
            this.mappingsToChange = mappingsToChange;
        }

        @Override
        protected void performInUIThread() {
            for (VariableMapping mapping : mappingsToChange) {
                if (mapping.isPropertySelector()) {
                    mapping.setMappedName(VariableUtils.wrapVariableName(replacementVariable.getName()));
                } else {
                    mapping.setName(replacementVariable.getName());
                }
            }
        }

        @Override
        protected String toPreviewContent(Variable variable) {
            StringBuffer buffer = new StringBuffer();
            for (VariableMapping mapping : mappingsToChange) {
                buffer.append("<variable access=\"").append(mapping.getUsage()).append("\" mapped-name=\"");
                if (mapping.isPropertySelector()) {
                    buffer.append(mapping.getName()).append("\" name=\"").append(VariableUtils.wrapVariableName(variable.getName()));
                } else {
                    buffer.append(variable.getName()).append("\" name=\"").append(mapping.getMappedName());
                }
                buffer.append("\" />").append("\n");
            }
            return buffer.toString();
        }
    }
}
