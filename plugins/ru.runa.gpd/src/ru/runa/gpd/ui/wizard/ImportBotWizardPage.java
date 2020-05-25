package ru.runa.gpd.ui.wizard;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import ru.runa.gpd.Localization;
import ru.runa.gpd.bot.BotImportCommand;
import ru.runa.gpd.sync.WfeServerBotImporter;
import ru.runa.gpd.sync.WfeServerConnectorDataImporter;
import ru.runa.gpd.util.IOUtils;

public class ImportBotWizardPage extends ImportBotElementWizardPage {

    public ImportBotWizardPage(IStructuredSelection selection) {
        super(ImportBotWizardPage.class, selection);
        setTitle(Localization.getString("ImportBotWizardPage.page.title"));
        setDescription(Localization.getString("ImportBotWizardPage.page.description"));
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
        getContainer().run(true, true, new BotImportCommand(parInputStream, botName, selectedProject.getName()));
    }

    @Override
    protected List<? extends IContainer> getProjectDataViewerInput() {
        return IOUtils.getAllBotStationProjects();
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
            return null;
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
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
