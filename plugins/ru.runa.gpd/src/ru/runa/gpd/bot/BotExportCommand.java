package ru.runa.gpd.bot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.dom4j.Document;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.ModalContext;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.util.BotScriptUtils;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.XmlUtil;

public class BotExportCommand extends BotSyncCommand {
    protected final OutputStream outputStream;
    protected final IResource exportResource;

    public BotExportCommand(IResource exportResource, OutputStream outputStream) {
        this.outputStream = outputStream;
        this.exportResource = exportResource;
    }

    @Override
    protected void execute(IProgressMonitor progressMonitor) throws InvocationTargetException {
        try {
            int totalWork = 1;
            progressMonitor.beginTask("", totalWork);
            ByteArrayOutputStream botStream = new ByteArrayOutputStream();
            getBotStream(botStream, getBotFolder());
            botStream.close();
            outputStream.write(botStream.toByteArray());
            progressMonitor.worked(1);
            ModalContext.checkCanceled(progressMonitor);
            progressMonitor.done();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    protected void getBotStream(OutputStream out, IFolder botFolder) throws IOException, CoreException {
        ZipOutputStream zipStream = new ZipOutputStream(out);
        zipStream.putNextEntry(new ZipEntry("script.xml"));
        List<BotTask> botTaskForExport = getBotTasksForExport(botFolder);
        Document document = BotScriptUtils.createScriptForBotLoading(botFolder.getName(), botTaskForExport);
        XmlUtil.writeXml(document, zipStream);
        writeConfigurationFiles(botFolder, zipStream);
        writeEmbeddedFiles(botFolder, zipStream);
        zipStream.close();
        out.flush();
    }

    protected IFolder getBotFolder() {
        return (IFolder) exportResource;
    }

    protected List<BotTask> getBotTasksForExport(IFolder botFolder) throws CoreException, IOException {
        return BotCache.getBotTasks(botFolder.getName());
    }

    protected void writeConfigurationFiles(IFolder botFolder, ZipOutputStream zipStream) throws CoreException, IOException {
        for (IResource resource : botFolder.members()) {
            if (resource instanceof IFile && BotCache.CONFIGURATION_FILE_EXTENSION.equals(resource.getFileExtension())) {
                write(zipStream, new ZipEntry(resource.getName()), (IFile) resource);
            }
        }
    }

    protected void writeEmbeddedFiles(IFolder botFolder, ZipOutputStream zipStream) throws CoreException, IOException {
        for (IResource resource : botFolder.members()) {
            // TODO must be replaced to IBotFileSupportProvider.getEmbeddedFileName(BotTask)
            if (resource instanceof IFile && resource.getName().contains(BotTaskUtils.EMBEDDED_SUFFIX)) {
                write(zipStream, new ZipEntry(resource.getName()), (IFile) resource);
            }
        }
    }

}
