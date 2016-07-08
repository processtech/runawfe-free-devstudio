package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;

public class DelegablePresentation extends VariableRenameProvider<Delegable> {
    private final DelegableProvider provider;

    public DelegablePresentation(final Delegable delegable, String name) {
        setElement(delegable);
        provider = HandlerRegistry.getProvider(element.getDelegationClassName());
    }

    @Override
    protected List<TextCompareChange> getChangeList(Variable oldVariable, Variable newVariable) throws Exception {
        List<TextCompareChange> changeList = new ArrayList<TextCompareChange>();
        try {
            if (provider.getUsedVariableNames(element).contains(oldVariable.getName())) {
                changeList.add(new ConfigChange(oldVariable, newVariable));
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Unable to get used variables in " + element, e);
        }
        return changeList;
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
