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
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.ui.enhancement.ConditionalEventExpressionDelegableAdapter;
import ru.runa.gpd.ui.enhancement.ConditionalEventStorageDelegableAdapter;
import ru.runa.gpd.util.XmlUtil;

/**
 * Refactoring and variable rename support for conditional event configuration.
 * Currently used for conditional {@link CatchEventNode}s.
 */
public class ConditionalEventDelegablePresentation extends SingleVariableRenameProvider<CatchEventNode> {

    private static final String INTERNAL_STORAGE_HANDLER = "ru.runa.wfe.office.storage.handler.InternalStorageHandler";

    private final DelegableProvider expressionProvider;
    private final DelegableProvider storageProvider;

    public ConditionalEventDelegablePresentation(CatchEventNode delegable) {
        setElement(delegable);
        expressionProvider = HandlerRegistry.getProvider(GroovyBasedProvider.class.getName());
        storageProvider = HandlerRegistry.getProvider(INTERNAL_STORAGE_HANDLER);
    }

    @Override
    protected List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<>();
        ConditionalEventModel model = ConditionalEventModel.fromXml(element.getDelegationConfiguration());

        boolean expressionChanged = expressionProvider.getUsedVariableNames(adaptForExpression(element, model)).contains(oldVariable.getName());
        boolean storageChanged = storageProvider.getUsedVariableNames(adaptForStorage(element, model)).contains(oldVariable.getName());

        if (expressionChanged || storageChanged) {
            changes.add(new ConfigChange(oldVariable, newVariable, expressionChanged, storageChanged));
        }

        return changes;
    }

    private ConditionalEventExpressionDelegableAdapter adaptForExpression(Delegable delegable, ConditionalEventModel model) {
        return new ConditionalEventExpressionDelegableAdapter((CatchEventNode) delegable, model);
    }

    private ConditionalEventStorageDelegableAdapter adaptForStorage(Delegable delegable, ConditionalEventModel model) {
        return new ConditionalEventStorageDelegableAdapter((CatchEventNode) delegable, model);
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
                            .getConfigurationOnVariableRename(adaptForExpression(element, model), currentVariable, replacementVariable);
                    model.setExpression(renamedExpressionXml);
                }

                if (storageChanged) {
                    String renamedStorageXml = storageProvider
                            .getConfigurationOnVariableRename(adaptForStorage(element, model), currentVariable, replacementVariable);
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
