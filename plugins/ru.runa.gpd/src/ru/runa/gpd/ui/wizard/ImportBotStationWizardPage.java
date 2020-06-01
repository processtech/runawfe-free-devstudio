package ru.runa.gpd.ui.wizard;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import ru.runa.gpd.Localization;
import ru.runa.gpd.bot.BotStationImportCommand;
import ru.runa.gpd.sync.WfeServerBotStationImporter;
import ru.runa.gpd.sync.WfeServerConnectorDataImporter;

public class ImportBotStationWizardPage extends ImportBotElementWizardPage {

    public ImportBotStationWizardPage(IStructuredSelection selection) {
        super(ImportBotStationWizardPage.class, selection);
        setTitle(Localization.getString("ImportBotStationWizardPage.page.title"));
        setDescription(Localization.getString("ImportBotStationWizardPage.page.description"));
    }

    @Override
    protected String getFilterExtension() {
        return "*.botstation";
    }

    @Override
    protected List<? extends IContainer> getProjectDataViewerInput() {
        return null;
    }

    @Override
    protected Object getServerDataViewerInput() {
        return WfeServerBotStationImporter.getInstance().getData();
    }

    @Override
    protected WfeServerConnectorDataImporter<?> getDataImporter() {
        return WfeServerBotStationImporter.getInstance();
    }

    @Override
    protected ITreeContentProvider getServerDataViewerContentProvider() {
        return new BotStationTreeContentProvider();
    }

    @Override
    public void runImport(InputStream inputStream, String botName) throws InvocationTargetException, InterruptedException {
        getContainer().run(false, true, new BotStationImportCommand(inputStream));
    }

    public static class BotStationTreeContentProvider implements ITreeContentProvider {
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
