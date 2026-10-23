package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

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

    @Override
    protected List<Change> getChanges(Variable oldVariable, Variable newVariable, VariableUserType userType) throws Exception {
        List<Change> changes = new ArrayList<>();
        if ((provider.getUsedVariableNames(element).contains(oldVariable.getName()))) {
            changes.add(new ConfigChange(oldVariable, newVariable, userType));
        }
        return changes;
    }

    @Override
    public List<Change> getChanges(VariableUserType oldUserType, VariableUserType newUserType) throws Exception {
        List<Change> changes = new ArrayList<>();
        if (provider.getUsedUserTypeNames(element).contains(oldUserType.getName())) {
            changes.add(new UserTypeConfigChange(oldUserType, newUserType));
        }
        return changes;
    }

    private class ConfigChange extends TextCompareChange<Variable> {

        private final VariableUserType userType;

        public ConfigChange(Variable currentVariable, Variable replacementVariable) {
            this(currentVariable, replacementVariable, null);
        }

        public ConfigChange(Variable currentVariable, Variable replacementVariable, VariableUserType userType) {
            super(element, currentVariable, replacementVariable);
            this.userType = userType;
        }

        @Override
        protected void performInUIThread() {
            String newConfiguration = getConfigurationReplacement();
            element.setDelegationConfiguration(newConfiguration);
        }

        private String getConfigurationReplacement() {
            try {
                return userType == null
                        ? provider.getConfigurationOnVariableRename(element, currentObject, replacementObject)
                        : provider.getConfigurationOnVariableRename(element, currentObject, replacementObject, userType);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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

    private class UserTypeConfigChange extends TextCompareChange<VariableUserType> {

        public UserTypeConfigChange(VariableUserType currentUserType, VariableUserType replacementUserType) {
            super(element, currentUserType, replacementUserType);
        }

        @Override
        protected void performInUIThread() {
            String newConfiguration = getConfigurationReplacement();
            element.setDelegationConfiguration(newConfiguration);
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
        protected String toPreviewContent(VariableUserType userType) {
            throw new UnsupportedOperationException();
        }

        private String getConfigurationReplacement() {
            try {
                return provider.getConfigurationOnUserTypeRename(element, currentObject, replacementObject);
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog(this.getClass().getName(), e);
                return element.getDelegationConfiguration();
            }
        }
    }

}
