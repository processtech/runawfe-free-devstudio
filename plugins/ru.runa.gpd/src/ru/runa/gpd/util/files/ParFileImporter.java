package ru.runa.gpd.util.files;

import com.google.common.base.Objects;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.IOUtils;

/**
 * @author Vitaly Alekseev
 *
 * @since Aug 1, 2019
 */
public final class ParFileImporter implements FileImporter {

    private final IContainer container;

    public ParFileImporter(IContainer container) {
        this.container = container;
    }

    @Override
    public IFolder importFile(final FileImportInfo file) throws Exception {
        final IFolder processFolder = IOUtils.getProcessFolder(container, file.getPath());
        if (processFolder.exists()) {
            throw new Exception(Localization.getString("ImportParWizardPage.error.processWithSameNameExists", file.getPath()));
        }
        IOUtils.createFolder(processFolder);
        IOUtils.extractArchiveToFolder(file.getInputStream(), processFolder);
        final IFile definitionFile = IOUtils.getProcessDefinitionFile(processFolder);
        final ProcessDefinition definition = ProcessCache.newProcessDefinitionWasCreated(definitionFile);
        if (definition != null && !Objects.equal(definition.getName(), processFolder.getName())) {
            // if par name differs from definition name
            final IPath destination = IOUtils.getProcessFolder(container, definition.getName()).getFullPath();
            processFolder.move(destination, true, false, null);
            final IFile movedDefinitionFile = IOUtils.getProcessDefinitionFile(IOUtils.getProcessFolder(container, definition.getName()));
            ProcessCache.newProcessDefinitionWasCreated(movedDefinitionFile);
            ProcessCache.invalidateProcessDefinition(definitionFile);
        }
        return processFolder;
    }

}
