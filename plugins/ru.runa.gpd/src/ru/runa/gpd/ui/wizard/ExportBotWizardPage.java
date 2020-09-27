package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.TreeMap;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import ru.runa.gpd.Localization;
import ru.runa.gpd.bot.BotDeployCommand;
import ru.runa.gpd.bot.BotExportCommand;
import ru.runa.gpd.util.IOUtils;

public class ExportBotWizardPage extends ExportBotElementWizardPage {

    public ExportBotWizardPage(IStructuredSelection selection) {
        super(ExportBotWizardPage.class, selection);
        setTitle(Localization.getString("ExportBotWizardPage.page.title"));
        setDescription(Localization.getString("ExportBotWizardPage.page.description"));
        this.exportObjectNameFileMap = new TreeMap<String, IResource>();
        for (IFolder resource : IOUtils.getAllBotFolders()) {
            exportObjectNameFileMap.put(getKey(resource.getProject(), resource), resource);
        }
    }

    @Override
    protected String getOutputSuffix() {
        return ".bot";
    }

    @Override
    protected String getSelectionResourceKey(IResource resource) {
        return getKey(resource.getProject(), resource);
    }

    @Override
    protected void exportToZipFile(IResource exportResource) throws Exception {
        getContainer().run(true, true, new BotExportCommand(exportResource, new FileOutputStream(getDestinationValue())));
    }

    @Override
    protected void deployToServer(IResource exportResource) throws Exception {
        getContainer().run(true, true, new BotDeployCommand(exportResource, new ByteArrayOutputStream()));
    }
}
