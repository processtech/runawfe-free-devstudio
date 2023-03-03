package ru.runa.gpd.util.files;

import com.google.common.base.Objects;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
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
    private ProcessDefinition definition;

    public ParFileImporter(IContainer container) {
        this.container = container;
    }

    @Override
    public IFolder importFile(final FileImportInfo file) throws Exception {
        final IFolder processFolder = IOUtils.getProcessFolder(container, file.getPath());
        if (processFolder.exists()) {
            return null;
        }
        IOUtils.createFolder(processFolder);
        IOUtils.extractArchiveToFolder(file.getInputStream(), processFolder);
        final IFile definitionFile = IOUtils.getProcessDefinitionFile(processFolder);
        definition = ProcessCache.newProcessDefinitionWasCreated(definitionFile);
        if (definition != null && !Objects.equal(definition.getName(), processFolder.getName())) {
            // if par name differs from definition name
            final IPath destination = IOUtils.getProcessFolder(container, definition.getName()).getFullPath();
            processFolder.move(destination, true, false, null);
            final IFile movedDefinitionFile = IOUtils.getProcessDefinitionFile(IOUtils.getProcessFolder(container, definition.getName()));
            if (definition.isUsingGlobalVars() && definition.getPropertyValue("PROPERTY_USE_GLOBALS").equals(0)) {
                definition.setPropertyValue("PROPERTY_USE_GLOBALS", true);
            }
            ProcessCache.newProcessDefinitionWasCreated(movedDefinitionFile);
            ProcessCache.invalidateProcessDefinition(definitionFile);
        }
        return processFolder;
    }

    public ProcessDefinition getDefinition() {
        return definition;
    }

}
