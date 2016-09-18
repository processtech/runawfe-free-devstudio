package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;

public class DelegablePresentation extends SingleVariableRenameProvider<Delegable> {
    private final DelegableProvider provider;

    public DelegablePresentation(final Delegable delegable) {
        setElement(delegable);
        provider = HandlerRegistry.getProvider(element.getDelegationClassName());
    }

    @Override
    protected List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<>();
        if (provider.getUsedVariableNames(element).contains(oldVariable.getName())) {
            changes.add(new ConfigChange(oldVariable, newVariable));
        }
        return changes;
    }

    private class ConfigChange extends TextCompareChange {

        public ConfigChange(Variable currentVariable, Variable replacementVariable) {
            super(element, currentVariable, replacementVariable);
        }

        @Override
        protected void performInUIThread() {
            String newConfiguration = getConfigurationReplacement();
            element.setDelegationConfiguration(newConfiguration);
        }

        private String getConfigurationReplacement() {
            DelegableProvider provider = HandlerRegistry.getProvider(element.getDelegationClassName());
            return provider.getConfigurationOnVariableRename(element, currentVariable, replacementVariable);
        }

        @Override
        public String getCurrentContent(IProgressMonitor pm) throws CoreException {
            return element.getDelegationConfiguration();
        }

        @Override
        public String getPreviewContent(IProgressMonitor pm) throws CoreException {
            return getConfigurationReplacement();
        }

        @Override
        protected String toPreviewContent(Variable variable) {
            throw new UnsupportedOperationException();
        }
    }

}
