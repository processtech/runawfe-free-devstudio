package ru.runa.gpd.bot;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.ModalContext;

import ru.runa.gpd.util.IOUtils;

public class BotStationExportCommand extends BotExportCommand {
    public BotStationExportCommand(IResource exportResource, OutputStream outputStream) {
        super(exportResource, outputStream);
    }

    @Override
    protected void execute(IProgressMonitor progressMonitor) throws InvocationTargetException {
        try {
            List<IFolder> botFolders = IOUtils.getBotFolders((IProject) exportResource);
            int totalWork = 1 + botFolders.size();
            progressMonitor.beginTask("", totalWork);
            ZipOutputStream zipStream = new ZipOutputStream(outputStream);
            List<IFile> files = getResourceToExport(exportResource);
            for (IFile file : files) {
                write(zipStream, new ZipEntry("botstation"), file);
            }
            progressMonitor.worked(1);
            for (IFolder botFolder : botFolders) {
                progressMonitor.subTask(botFolder.getName());
                zipStream.putNextEntry(new ZipEntry(botFolder.getName() + ".bot"));
                ByteArrayOutputStream botStream = new ByteArrayOutputStream();
                getBotStream(botStream, botFolder);
                botStream.close();
                zipStream.write(botStream.toByteArray());
                progressMonitor.worked(1);
                ModalContext.checkCanceled(progressMonitor);
            }
            progressMonitor.done();
            zipStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    protected List<IFile> getResourceToExport(IResource exportResource) throws CoreException {
        List<IFile> resourcesToExport = new ArrayList<IFile>();
        IProject processFolder = (IProject) exportResource;
        processFolder.refreshLocal(IResource.DEPTH_ONE, null);
        IFolder folder = processFolder.getFolder("/src/botstation/");
        IResource[] members = folder.members();
        for (IResource resource : members) {
            if (resource instanceof IFile) {
                resourcesToExport.add((IFile) resource);
            }
        }
        return resourcesToExport;
    }
}
