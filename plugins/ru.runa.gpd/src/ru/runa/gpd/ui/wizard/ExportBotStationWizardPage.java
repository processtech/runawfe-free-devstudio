package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;

import ru.runa.gpd.Localization;
import ru.runa.gpd.bot.BotStationDeployCommand;
import ru.runa.gpd.bot.BotStationExportCommand;
import ru.runa.gpd.util.IOUtils;

public class ExportBotStationWizardPage extends ExportBotElementWizardPage {
    public ExportBotStationWizardPage(IStructuredSelection selection) {
        super(selection);
        setTitle(Localization.getString("ExportBotStationWizardPage.page.title"));
        setDescription(Localization.getString("ExportBotStationWizardPage.page.description"));
        this.exportObjectNameFileMap = new TreeMap<String, IResource>();
        for (IProject resource : IOUtils.getAllBotStationProjects()) {
            exportObjectNameFileMap.put(resource.getName(), resource);
        }
    }

    @Override
    protected String getOutputSuffix() {
        return ".botstation";
    }

    @Override
    protected String getSelectionResourceKey(IResource resource) {
        return resource.getName();
    }

    @Override
    protected void exportToZipFile(IResource exportResource) throws Exception {
        getContainer().run(true, true, new BotStationExportCommand(exportResource, new FileOutputStream(getDestinationValue())));
    }

    @Override
    protected void deployToServer(IResource exportResource) throws Exception {
        getContainer().run(true, true, new BotStationDeployCommand(exportResource, new ByteArrayOutputStream()));
    }
}
