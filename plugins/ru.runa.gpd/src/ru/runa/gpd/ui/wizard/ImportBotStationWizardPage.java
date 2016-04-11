package ru.runa.gpd.ui.wizard;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ru.runa.gpd.Localization;
import ru.runa.gpd.bot.BotStationImportCommand;
import ru.runa.gpd.wfe.DataImporter;
import ru.runa.gpd.wfe.WFEServerBotStationElementImporter;
import ru.runa.wfe.bot.BotStation;

public class ImportBotStationWizardPage extends ImportBotElementWizardPage {
    public ImportBotStationWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle(Localization.getString("ImportBotStationWizardPage.page.title"));
        setDescription(Localization.getString("ImportBotStationWizardPage.page.description"));
    }

    @Override
    protected Class<BotStation> getBotElementClass() {
        return BotStation.class;
    }

    @Override
    protected String getInputSuffix() {
        return "*.botstation";
    }

    @Override
    protected String getSelectionResourceKey(IResource resource) {
        return resource.getName();
    }

    @Override
    protected void populateInputView() {
        if (importFromServerButton.getSelection()) {
            serverDataViewer.setInput(WFEServerBotStationElementImporter.getInstance().getBotStations());
        }
    }

    @Override
    protected DataImporter getDataImporter() {
        return WFEServerBotStationElementImporter.getInstance();
    }

    @Override
    protected Object[] getBotElements() {
        List<BotStation> botStations = WFEServerBotStationElementImporter.getInstance().getBotStations();
        return botStations.toArray(new Object[0]);
    }

    @Override
    protected ITreeContentProvider getContentProvider() {
        return new BotStationTreeContentProvider();
    }

    @Override
    public void runImport(InputStream parInputStream, String botName) throws InvocationTargetException, InterruptedException {
        getContainer().run(false, true, new BotStationImportCommand(parInputStream));
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
