package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;

public class TimerPresentation extends VariableRenameProvider<Timer> {
    public TimerPresentation(Timer timer) {
        setElement(timer);
    }

    @Override
    public List<Change> getChanges(Map<Variable, Variable> variablesMap) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        String durationVariableName = element.getDelay().getVariableName();
        for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            Variable oldVariable = entry.getKey();
            Variable newVariable = entry.getValue();
            if (Objects.equal(oldVariable.getName(), durationVariableName)) {
                changes.add(new TimedChange(element, oldVariable, newVariable));
            }
        }
        return changes;
    }

    private class TimedChange extends TextCompareChange {

        public TimedChange(NamedGraphElement element, Variable currentVariable, Variable previewVariable) {
            super(element, currentVariable, previewVariable);
        }

        @Override
        protected void performInUIThread() {
            element.getDelay().setVariableName(replacementVariable.getName());
        }

        @Override
        protected String toPreviewContent(Variable variable) {
            StringBuffer buffer = new StringBuffer();
            Duration durationTmp = new Duration(element.getDelay().getDuration());
            durationTmp.setVariableName(variable.getName());
            buffer.append(durationTmp.getDuration());
            return buffer.toString();
        }
    }
}
