package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;

public class TimerPresentation extends SimpleVariableRenameProvider<Timer> {
    private final String durationVariableName;

    public TimerPresentation(Timer timer) {
        setElement(timer);
        durationVariableName = element.getDelay().getVariableName();
    }

    @Override
    protected List<TextCompareChange> getChangesForVariable(Variable oldVariable, Variable newVariable) throws Exception {
        List<TextCompareChange> changeList = new ArrayList<TextCompareChange>();
        if (Objects.equal(oldVariable.getName(), durationVariableName)) {
            changeList.add(new TimedChange(element, oldVariable, newVariable));
        }
        return changeList;
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
