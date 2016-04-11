package ru.runa.gpd.bot;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.util.BotScriptUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;

public class BotImportCommand extends BotSyncCommand {
    protected InputStream inputStream;
    protected String botName;
    protected String botStationName;

    public BotImportCommand(InputStream inputStream, String botName, String botStationName) {
        this.inputStream = inputStream;
        this.botName = botName;
        this.botStationName = botStationName;
    }

    @Override
    protected void execute(IProgressMonitor progressMonitor) throws InvocationTargetException {
        try {
            ZipInputStream botZin = new ZipInputStream(inputStream);
            Map<String, byte[]> files = new HashMap<String, byte[]>();
            ZipEntry botEntry;
            while ((botEntry = botZin.getNextEntry()) != null) {
                byte[] bytes = ByteStreams.toByteArray(botZin);
                files.put(botEntry.getName(), bytes);
            }
            // create bot
            String botFolderName = botName.replaceAll(Pattern.quote(".bot"), "");
            IPath path = new Path(botStationName).append("/src/botstation/").append(botFolderName);
            IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
            if (!folder.exists()) {
                folder.create(true, true, null);
            }
            byte[] scriptXml = files.remove("script.xml");
            Preconditions.checkNotNull(scriptXml, "No script.xml");
            List<BotTask> botTasks = BotScriptUtils.getBotTasksFromScript(botStationName, botFolderName, scriptXml, files);

            for (BotTask botTask : botTasks) {
                IFile file = folder.getFile(botTask.getName());
                WorkspaceOperations.saveBotTask(file, botTask);

                // Save embedded files too.
                for (String fileToSave : botTask.getFilesToSave()) {
                    if (files.get(fileToSave) == null) {
                        continue;
                    }
                    IOUtils.createOrUpdateFile(folder.getFile(fileToSave), new ByteArrayInputStream(files.get(fileToSave)));
                }
                botTask.getFilesToSave().clear();
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
}
