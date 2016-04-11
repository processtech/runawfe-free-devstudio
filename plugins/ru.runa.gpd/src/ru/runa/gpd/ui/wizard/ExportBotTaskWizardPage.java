package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;

import ru.runa.gpd.Localization;
import ru.runa.gpd.bot.BotTaskDeployCommand;
import ru.runa.gpd.bot.BotTaskExportCommand;
import ru.runa.gpd.util.IOUtils;

public class ExportBotTaskWizardPage extends ExportBotWizardPage {
    public ExportBotTaskWizardPage(IStructuredSelection selection) {
        super(selection);
        setTitle(Localization.getString("ExportBotTaskWizardPage.page.title"));
        setDescription(Localization.getString("ExportBotTaskWizardPage.page.description"));
        this.exportObjectNameFileMap = new TreeMap<String, IResource>();
        for (IFile resource : IOUtils.getAllBotTasks()) {
            exportObjectNameFileMap.put(getSelectionResourceKey(resource), resource);
        }
    }

    @Override
    protected String getSelectionResourceKey(IResource resource) {
        return resource.getProject().getName() + "/" + resource.getParent().getName() + "/" + resource.getName();
    }

    @Override
    protected String getFileName(String selectionName) {
        return selectionName.substring(selectionName.indexOf("/") + 1, selectionName.lastIndexOf("/"))+"."+selectionName.substring(selectionName.lastIndexOf("/")+1) + getOutputSuffix();
    }

    @Override
    protected void exportToZipFile(IResource exportResource) throws Exception {
        getContainer().run(true, true, new BotTaskExportCommand(exportResource, new FileOutputStream(getDestinationValue())));
    }

    @Override
    protected void deployToServer(IResource exportResource) throws Exception {
        getContainer().run(true, true, new BotTaskDeployCommand(exportResource, new ByteArrayOutputStream()));
    }
}
