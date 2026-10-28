package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.GroovyBasedProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.XmlUtil;

import static ru.runa.gpd.ui.enhancement.ConditionalEventDelegableAdapters.adaptToExpression;
import static ru.runa.gpd.ui.enhancement.ConditionalEventDelegableAdapters.adaptToStorage;

/**
 * Refactoring and variable rename support for conditional event configuration.
 */
public class ConditionalEventDelegablePresentation extends SingleVariableRenameProvider<Delegable> {

    private static final String INTERNAL_STORAGE_HANDLER = "ru.runa.wfe.office.storage.handler.InternalStorageHandler";

    private final DelegableProvider expressionProvider;
    private final DelegableProvider storageProvider;

    public ConditionalEventDelegablePresentation(Delegable delegable) {
        setElement(delegable);
        expressionProvider = HandlerRegistry.getProvider(GroovyBasedProvider.class.getName());
        storageProvider = HandlerRegistry.getProvider(INTERNAL_STORAGE_HANDLER);
    }

    @Override
    protected List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<>();
        ConditionalEventModel model = ConditionalEventModel.fromXml(element.getDelegationConfiguration());

        boolean expressionChanged = expressionProvider.getUsedVariableNames(adaptToExpression(element, model)).contains(oldVariable.getName());
        boolean storageChanged = storageProvider.getUsedVariableNames(adaptToExpression(element, model)).contains(oldVariable.getName());

        if (expressionChanged || storageChanged) {
            changes.add(new ConfigChange(oldVariable, newVariable, expressionChanged, storageChanged));
        }

        return changes;
    }

    private class ConfigChange extends TextCompareChange {

        private final boolean expressionChanged;
        private final boolean storageChanged;

        public ConfigChange(Variable currentVariable, Variable replacementVariable, boolean expressionChanged, boolean storageChanged) {
            super(element, currentVariable, replacementVariable);
            this.expressionChanged = expressionChanged;
            this.storageChanged = storageChanged;
        }

        @Override
        protected void performInUIThread() {
            element.setDelegationConfiguration(getConfigurationReplacement());
        }

        private String getConfigurationReplacement() {
            try {
                ConditionalEventModel model = ConditionalEventModel.fromXml(element.getDelegationConfiguration());

                if (expressionChanged) {
                    String renamedExpressionXml = expressionProvider
                            .getConfigurationOnVariableRename(adaptToExpression(element, model), currentVariable, replacementVariable);
                    model.setExpression(renamedExpressionXml);
                }

                if (storageChanged) {
                    String renamedStorageXml = storageProvider
                            .getConfigurationOnVariableRename(adaptToStorage(element, model), currentVariable, replacementVariable);
                    model.setStorage(XmlUtil.parseWithoutValidation(renamedStorageXml).getRootElement());
                }

                return model.toXml();
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog(this.getClass().getName(), e);
                return element.getDelegationConfiguration();
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
}
