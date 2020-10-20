package ru.runa.gpd.util.files;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.internal.wizards.datatransfer.IFileExporter;
import ru.runa.gpd.util.LambdaUtils;

/**
 * @author Vitaly Alekseev
 *
 * @since Aug 6, 2019
 */
@SuppressWarnings("restriction")
public class FileResourcesExportOperation {

    protected final List<IFile> resourcesToExport;
    protected final IFileExporter exporter;

    public FileResourcesExportOperation(List<IFile> resourcesToExport, IFileExporter exporter) {
        this.resourcesToExport = resourcesToExport;
        this.exporter = exporter;
    }

    public void exportResources(IProgressMonitor progressMonitor) {
        final Optional<IProgressMonitor> progress = Optional.ofNullable(progressMonitor);
        try {
            LambdaUtils.forEach(resourcesToExport, (i, resource) -> {
                LambdaUtils.call(() -> exportResource(resource));
                progress.ifPresent(pm -> pm.worked(i));
            });
        } finally {
            LambdaUtils.call(() -> exporter.finished());
        }
    }

    private void exportResource(IFile fileResource) throws IOException, CoreException {
        if (!fileResource.isSynchronized(IResource.DEPTH_ONE)) {
            fileResource.refreshLocal(IResource.DEPTH_ONE, null);
        }
        if (!fileResource.isAccessible()) {
            return;
        }
        exporter.write(fileResource, fileResource.getName());
    }
}
