package ru.runa.gpd.ui.wizard;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ru.runa.gpd.Localization;
import ru.runa.gpd.bot.BotImportCommand;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.wfe.DataImporter;
import ru.runa.gpd.wfe.WFEServerBotElementImporter;
import ru.runa.wfe.bot.Bot;

public class ImportBotWizardPage extends ImportBotElementWizardPage {
    public ImportBotWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle(Localization.getString("ImportBotWizardPage.page.title"));
        setDescription(Localization.getString("ImportBotWizardPage.page.description"));
        for (IProject resource : IOUtils.getAllBotStationProjects()) {
            importObjectNameFileMap.put(resource.getName(), resource);
        }
    }

    @Override
    protected Class<Bot> getBotElementClass() {
        return Bot.class;
    }

    @Override
    protected String getInputSuffix() {
        return "*.bot";
    }

    @Override
    protected String getSelectionResourceKey(IResource resource) {
        return resource.getName();
    }

    @Override
    public void runImport(InputStream parInputStream, String botName) throws InvocationTargetException, InterruptedException {
        String selectedDefinitionName = getBotElementSelection();
        IResource importToResource = importObjectNameFileMap.get(selectedDefinitionName);
        getContainer().run(true, true, new BotImportCommand(parInputStream, botName, importToResource.getName()));
    }

    @Override
    protected void populateInputView() {
        if (importFromServerButton.getSelection()) {
            serverDataViewer.setInput(WFEServerBotElementImporter.getInstance().getBots());
        }
    }

    @Override
    protected DataImporter getDataImporter() {
        return WFEServerBotElementImporter.getInstance();
    }

    @Override
    protected Object[] getBotElements() {
        List<Bot> bots = WFEServerBotElementImporter.getInstance().getBots();
        return bots.toArray(new Object[0]);
    }

    @Override
    protected ITreeContentProvider getContentProvider() {
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
