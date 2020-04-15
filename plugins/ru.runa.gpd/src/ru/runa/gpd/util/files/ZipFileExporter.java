package ru.runa.gpd.util.files;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.wizards.datatransfer.IFileExporter;

/**
 * @author Vitaly Alekseev
 *
 * @since Aug 6, 2019
 */
@SuppressWarnings("restriction")
public class ZipFileExporter implements IFileExporter {

    private final ZipOutputStream outputStream;

    public ZipFileExporter(OutputStream outputStream) throws IOException {
        this.outputStream = new ZipOutputStream(outputStream);
    }

    @Override
    public void finished() throws IOException {
        outputStream.flush();
        outputStream.finish();
        outputStream.close();
    }

    @Override
    public void write(IFile resource, String destinationPath) throws IOException, CoreException {
        ZipEntry newEntry = new ZipEntry(destinationPath);
        write(newEntry, resource);
    }

    @Override
    public void write(IContainer container, String destinationPath) throws IOException {
        throw new UnsupportedOperationException();
    }

    private void write(ZipEntry entry, IFile contents) throws IOException, CoreException {
        outputStream.putNextEntry(entry);
        try {
            try (InputStream inputStream = contents.getContents()) {
                ByteStreams.copy(inputStream, outputStream);
            }
        } finally {
            outputStream.closeEntry();
        }
    }

}
