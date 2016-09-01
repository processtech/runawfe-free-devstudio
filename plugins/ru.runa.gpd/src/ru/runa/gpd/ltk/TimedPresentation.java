package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;

public class TimedPresentation extends SingleVariableRenameProvider<ITimed> {
    private final Timer timer;

    public TimedPresentation(ITimed timed) {
        setElement(timed);
        timer = element.getTimer();
    }

    @Override
    protected List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<>();
        if (timer != null && Objects.equal(oldVariable.getName(), timer.getDelay().getVariableName())) {
            changes.add(new TimedChange(element, oldVariable, newVariable));
        }
        return changes;
    }

    private class TimedChange extends TextCompareChange {

        public TimedChange(Object element, Variable currentVariable, Variable previewVariable) {
            super(element, currentVariable, previewVariable);
        }

        @Override
        protected void performInUIThread() {
            element.getTimer().setDelayVariableName(replacementVariable.getName());
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
