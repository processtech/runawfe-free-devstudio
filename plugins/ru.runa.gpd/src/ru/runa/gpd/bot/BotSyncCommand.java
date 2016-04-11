package ru.runa.gpd.bot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public abstract class BotSyncCommand implements IRunnableWithProgress {
    protected abstract void execute(IProgressMonitor progressMonitor) throws InvocationTargetException;

    protected void write(ZipOutputStream zipOutputStream, ZipEntry entry, IFile contents) throws IOException, CoreException {
        byte[] readBuffer = new byte[1024];
        zipOutputStream.putNextEntry(entry);
        InputStream contentStream = contents.getContents();
        try {
            int n;
            while ((n = contentStream.read(readBuffer)) > 0) {
                zipOutputStream.write(readBuffer, 0, n);
            }
        } finally {
            if (contentStream != null) {
                contentStream.close();
            }
        }
        zipOutputStream.closeEntry();
    }

    @Override
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
        try {
            execute(progressMonitor);
        } finally {
            progressMonitor.done();
        }
    }
}
