package ru.runa.gpd.bot;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.BotStationNature;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

public class BotStationImportCommand extends BotSyncCommand {

    private final InputStream inputStream;

    private String botStationName;

    public BotStationImportCommand(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    protected void execute(IProgressMonitor progressMonitor) throws InvocationTargetException {
        try {
            ZipInputStream zin = new ZipInputStream(inputStream);
            ZipEntry entry;
            StringBuilder messages = new StringBuilder();

            BotImportCommand botImportCommand = new BotImportCommand();

            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals("botstation")) {
                    importBotStation(zin);
                    continue;
                }

                // deploy bot
                String botFileName = entry.getName();

                try {
                    botImportCommand.init(new ByteArrayInputStream(ByteStreams.toByteArray(zin)), botFileName, botStationName);
                    botImportCommand.importBot(progressMonitor);
                } catch (Exception e) {
                    if (messages.length() > 0) {
                        messages.append(System.lineSeparator());
                    }
                    messages.append(e.getMessage());
                }
            }

            if (messages.length() > 0) {
                Dialogs.warning(Localization.getString("ImportBotStationWizardPage.warning.botstationImportError"), messages.toString());
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    private void importBotStation(ZipInputStream zin) throws IOException, CoreException {
        BufferedReader r = new BufferedReader(new InputStreamReader(zin, Charsets.UTF_8));
        botStationName = r.readLine();
        String rmiAddress = r.readLine();
        if (BotCache.getAllBotStationNames().contains(botStationName)) {
            throw new UniqueBotStationException(Localization.getString("ImportBotStationWizardPage.error.botstationWithSameNameExists"));
        }
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(botStationName);
        IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(botStationName);
        description.setNatureIds(new String[] { BotStationNature.NATURE_ID });
        newProject.create(description, null);
        newProject.open(IResource.BACKGROUND_REFRESH, null);
        newProject.refreshLocal(IResource.DEPTH_INFINITE, null);
        IFolder folder = newProject.getFolder("/src/botstation/");
        IOUtils.createFolder(folder);
        IFile file = folder.getFile("botstation");
        IOUtils.createOrUpdateFile(file, BotTaskUtils.createBotStationInfo(botStationName, rmiAddress));
    }
}
