package ru.runa.gpd.bot;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.util.EmbeddedFileUtils;

import com.google.common.collect.Lists;

public class BotTaskExportCommand extends BotExportCommand {
    public BotTaskExportCommand(IResource exportResource, OutputStream outputStream) {
        super(exportResource, outputStream);
    }

    @Override
    protected IFolder getBotFolder() {
        return (IFolder) exportResource.getParent();
    }

    @Override
    protected List<BotTask> getBotTasksForExport(IFolder botFolder) throws CoreException, IOException {
        return Lists.newArrayList(BotCache.getBotTaskNotNull(botFolder.getName(), exportResource.getName()));
    }

    @Override
    protected void writeConfigurationFiles(IFolder botFolder, ZipOutputStream zipStream) throws CoreException, IOException {
        for (IResource resource : botFolder.members()) {
            if (resource instanceof IFile && resource.getName().equals(exportResource.getName() + "." + BotCache.CONFIGURATION_FILE_EXTENSION)) {
                write(zipStream, new ZipEntry(resource.getName()), (IFile) resource);
            }
        }
    }

    @Override
    protected void writeEmbeddedFiles(IFolder botFolder, ZipOutputStream zipStream) throws CoreException, IOException {
        for (IResource resource : botFolder.members()) {
            // TODO must be replaced to IBotFileSupportProvider.getEmbeddedFileName(BotTask)
            if (resource instanceof IFile && EmbeddedFileUtils.isBotTaskFileName(resource.getName(), exportResource.getName())) {
                write(zipStream, new ZipEntry(resource.getName()), (IFile) resource);
            }
        }
    }
}
