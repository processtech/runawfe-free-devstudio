package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.WorkspaceOperations;

public class BotTaskConfigRenameProvider extends VariableRenameProvider<BotTask> {

    public BotTaskConfigRenameProvider(BotTask botTask) {
        setElement(botTask);
    }

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        DelegableProvider provider = HandlerRegistry.getProvider(element.getDelegationClassName());
        try {
            if (provider.getUsedVariableNames(element).contains(oldVariable.getName())) {
                changes.add(new ConfigChange(oldVariable, newVariable));
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Unable to get used variables in " + element, e);
        }
        return changes;
    }

    private class ConfigChange extends TextCompareChange {

        public ConfigChange(Variable currentVariable, Variable replacementVariable) {
            super(element, currentVariable, replacementVariable);
            // unchecked by default
            setEnabled(false);
        }

        @Override
        protected void performInUIThread() {
            String newConfiguration = getConfigurationReplacement();
            element.setDelegationConfiguration(newConfiguration);
            IFile botTaskFile = BotCache.getBotTaskFile(element);
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (page != null) {
                IEditorPart editor = page.findEditor(new FileEditorInput(botTaskFile));
                if (editor != null) {
                    page.closeEditor(editor, false);
                }
            }
            WorkspaceOperations.saveBotTask(botTaskFile, element);
            BotCache.invalidateBotTask(botTaskFile, element);
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
