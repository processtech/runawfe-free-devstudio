package ru.runa.gpd.ui.wizard;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ru.runa.gpd.Localization;
import ru.runa.gpd.bot.BotImportCommand;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.wfe.WFEServerBotElementImporter;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;

public class ImportBotTaskWizardPage extends ImportBotWizardPage {
    public ImportBotTaskWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle(Localization.getString("ImportBotTaskWizardPage.page.title"));
        setDescription(Localization.getString("ImportBotTaskWizardPage.page.description"));
        for (IFolder resource : IOUtils.getAllBotFolders()) {
            importObjectNameFileMap.put(getKey(resource.getProject(), resource), resource);
        }
    }

    @Override
    public void runImport(InputStream parInputStream, String botName) throws InvocationTargetException, InterruptedException {
        String selectedDefinitionName = getBotElementSelection();
        IResource importToResource = importObjectNameFileMap.get(selectedDefinitionName);
        getContainer().run(true, true, new BotImportCommand(parInputStream, importToResource.getName(), importToResource.getProject().getName()));
    }

    @Override
    protected ITreeContentProvider getContentProvider() {
        return new BotTreeContentProvider();
    }

    @Override
    protected String getSelectionResourceKey(IResource resource) {
        return getKey(resource.getProject(), resource);
    }

    public static class BotTreeContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getChildren(Object parentElement) {
            return WFEServerBotElementImporter.getInstance().getBotTasks((Bot) parentElement).toArray(new Object[0]);
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof Bot) {
                List<BotTask> result = WFEServerBotElementImporter.getInstance().getBotTasks((Bot) element);
                return result != null && result.size() > 0;
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                return ((List) inputElement).toArray(new Object[0]);
            }
            return new Object[0];
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
}
