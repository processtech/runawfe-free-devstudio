package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.swimlane.SwimlaneInitializer;
import ru.runa.gpd.swimlane.SwimlaneInitializerParser;

public class SwimlanePresentation extends SingleVariableRenameProvider<Swimlane> {
    private final SwimlaneInitializer swimlaneInitializer;

    public SwimlanePresentation(Swimlane swimlane) {
        setElement(swimlane);
        swimlaneInitializer = SwimlaneInitializerParser.parse(element.getDelegationConfiguration());
    }

    @Override
    protected List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<>();
        if (swimlaneInitializer.hasReference(oldVariable)) {
            changes.add(new SwimlaneInitializerChange(element, oldVariable, newVariable));
        }
        return changes;
    }

    private class SwimlaneInitializerChange extends TextCompareChange {

        public SwimlaneInitializerChange(Object element, Variable currentVariable, Variable previewVariable) {
            super(element, currentVariable, previewVariable);
        }

        @Override
        protected void performInUIThread() {
            element.setDelegationConfiguration(getReplacementConfig());
        }

        private String getReplacementConfig() {
            String config = element.getDelegationConfiguration();
            SwimlaneInitializer swimlaneInitializer = SwimlaneInitializerParser.parse(config);
            swimlaneInitializer.onVariableRename(currentVariable.getName(), replacementVariable.getName());
            return swimlaneInitializer.toString();
        }

        @Override
        public String getCurrentContent(IProgressMonitor pm) throws CoreException {
            return element.getDelegationConfiguration();
        }

        @Override
        public String getPreviewContent(IProgressMonitor pm) throws CoreException {
            return getReplacementConfig();
        }

        @Override
        protected String toPreviewContent(Variable variable) {
            throw new UnsupportedOperationException();
        }
    }
}
