package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;

public class TimedPresentation extends VariableRenameProvider<ITimed> {
    public TimedPresentation(ITimed timed) {
        setElement(timed);
    }

    @Override
    public List<Change> getChanges(SortedMap<Variable, Variable> variablesMap) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        Timer timer = element.getTimer();
        for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            Variable oldVariable = entry.getKey();
            Variable newVariable = entry.getValue();
            if (timer != null && Objects.equal(oldVariable.getName(), timer.getDelay().getVariableName())) {
                changes.add(new TimedChange(element, oldVariable, newVariable));
            }
        }
        return changes;
    }

    private class TimedChange extends TextCompareChange {

        public TimedChange(Object element, Variable currentVariable, Variable previewVariable) {
            super(element, currentVariable, previewVariable);
        }

        @Override
        protected void performInUIThread() {
            element.getTimer().getDelay().setVariableName(replacementVariable.getName());
        }

        @Override
        protected String toPreviewContent(Variable variable) {
            StringBuffer buffer = new StringBuffer();
            Duration durationTmp = new Duration(element.getTimer().getDelay().getDuration());
            durationTmp.setVariableName(variable.getName());
            buffer.append(durationTmp.getDuration());
            return buffer.toString();
        }
    }
}
