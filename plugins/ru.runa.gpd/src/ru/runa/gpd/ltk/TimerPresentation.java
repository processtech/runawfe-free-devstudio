package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;

public class TimerPresentation extends SingleVariableRenameProvider<Timer> {
    private final String durationVariableName;

    public TimerPresentation(Timer timer) {
        setElement(timer);
        durationVariableName = element.getDelay().getVariableName();
    }

    @Override
    protected List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<>();
        if (Objects.equal(oldVariable.getName(), durationVariableName)) {
            changes.add(new TimedChange(element, oldVariable, newVariable));
        }
        return changes;
    }

    private class TimedChange extends TextCompareChange {

        public TimedChange(NamedGraphElement element, Variable currentVariable, Variable previewVariable) {
            super(element, currentVariable, previewVariable);
        }

        @Override
        protected void performInUIThread() {
            try {
                element.getDelay().setVariableName(replacementVariable.getName());
            } catch (Exception e) {
                // TODO notify user
                PluginLogger.logErrorWithoutDialog("Unable to perform change in " + element, e);
            }
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
