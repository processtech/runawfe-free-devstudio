package ru.runa.gpd.ui.wizard;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import ru.runa.gpd.Localization;
import ru.runa.gpd.bot.BotImportCommand;
import ru.runa.gpd.sync.WfeServerBotImporter;
import ru.runa.gpd.sync.WfeServerConnectorDataImporter;
import ru.runa.gpd.util.IOUtils;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;

public class ImportBotTaskWizardPage extends ImportBotElementWizardPage {

    public ImportBotTaskWizardPage(IStructuredSelection selection) {
        super(ImportBotTaskWizardPage.class, selection);
        setTitle(Localization.getString("ImportBotTaskWizardPage.page.title"));
        setDescription(Localization.getString("ImportBotTaskWizardPage.page.description"));
    }

    @Override
    protected List<? extends IContainer> getProjectDataViewerInput() {
        return IOUtils.getAllBotFolders();
    }

    @Override
    protected LabelProvider getProjectDataViewerLabelProvider() {
        return new LabelProvider() {
            @Override
            public String getText(Object element) {
                IContainer container = (IContainer) element;
                return container.getProject().getName() + "/" + container.getName();
            }
        };
    }

    @Override
    protected String getFilterExtension() {
        return "*.bot";
    }

    @Override
    public void runImport(InputStream parInputStream, String botName) throws InvocationTargetException, InterruptedException {
        IContainer selectedProject = getSelectedProject();
        if (selectedProject == null) {
            return;
        }
        getContainer().run(true, true, new BotImportCommand(parInputStream, selectedProject.getName(), selectedProject.getProject().getName()));
    }

    @Override
    protected Object getServerDataViewerInput() {
        return WfeServerBotImporter.getInstance().getBots();
    }

    @Override
    protected WfeServerConnectorDataImporter<?> getDataImporter() {
        return WfeServerBotImporter.getInstance();
    }

    @Override
    protected ITreeContentProvider getServerDataViewerContentProvider() {
        return new BotTreeContentProvider();
    }

    public static class BotTreeContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getChildren(Object parentElement) {
            return WfeServerBotImporter.getInstance().getBotTasks((Bot) parentElement).toArray(new Object[0]);
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof Bot) {
                List<BotTask> result = WfeServerBotImporter.getInstance().getBotTasks((Bot) element);
                return result != null && result.size() > 0;
            }
            return false;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
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
