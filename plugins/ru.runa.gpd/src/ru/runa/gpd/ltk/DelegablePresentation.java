package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;

public class DelegablePresentation extends VariableRenameProvider<Delegable> {

    public DelegablePresentation(final Delegable delegable, String name) {
        setElement(delegable);
    }

    @Override
    public List<Change> getChanges(SortedMap<Variable, Variable> variablesMap) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        DelegableProvider provider = HandlerRegistry.getProvider(element.getDelegationClassName());
        for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            Variable oldVariable = entry.getKey();
            Variable newVariable = entry.getValue();
            try {
                if (provider.getUsedVariableNames(element).contains(oldVariable.getName())) {
                    changes.add(new ConfigChange(oldVariable, newVariable));
                }
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("Unable to get used variables in " + element, e);
            }
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
