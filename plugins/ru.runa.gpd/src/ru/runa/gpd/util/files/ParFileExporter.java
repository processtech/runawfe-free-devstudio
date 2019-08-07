package ru.runa.gpd.util.files;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.par.ProcessDefinitionValidator;
import ru.runa.gpd.ui.view.ValidationErrorsView;

/**
 * @author Vitaly Alekseev
 *
 * @since Aug 6, 2019
 */
public class ParFileExporter {

    private final IFile definitionFile;

    public ParFileExporter(IFile definitionFile) {
        this.definitionFile = definitionFile;
    }
    
    public boolean export(boolean ignoreErrors, ExportOperationSupplier exporter) throws Exception {
        return export(ignoreErrors, exporter, m -> {}, m -> {});
    }

    public boolean export(boolean ignoreErrors, ExportOperationSupplier exporter, StringArgFunction warningHandler, StringArgFunction errorHandler)
            throws Exception {
        final IFolder processFolder = (IFolder) definitionFile.getParent();
        processFolder.refreshLocal(IResource.DEPTH_ONE, null);
        final ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
        int validationResult = ProcessDefinitionValidator.validateDefinition(definition);
        if (!ignoreErrors && validationResult != ProcessDefinitionValidator.NO_ERRORS) {
            warningHandler.apply(ValidationErrorsView.ID);
            if (validationResult == ProcessDefinitionValidator.ERRORS) {
                errorHandler.apply(Localization.getString("ExportParWizardPage.page.errorsExist"));
                return false;
            }
        }
        for (final SubprocessDefinition subprocessDefinition : definition.getEmbeddedSubprocesses().values()) {
            validationResult = ProcessDefinitionValidator.validateDefinition(subprocessDefinition);
            if (!ignoreErrors && validationResult != ProcessDefinitionValidator.NO_ERRORS) {
                if (validationResult == ProcessDefinitionValidator.ERRORS) {
                    errorHandler.apply(Localization.getString("ExportParWizardPage.page.errorsExistInEmbeddedSubprocess"));
                    return false;
                }
            }
        }
        definition.getLanguage().getSerializer().validateProcessDefinitionXML(definitionFile);
        final List<IFile> resourcesToExport = Stream.of(processFolder.members()).filter(resource -> resource instanceof IFile)
                .map(resource -> (IFile) resource).collect(Collectors.toList());
        return exporter.get(definition, resourcesToExport).map(o -> {
            o.exportResources(null);
            return Boolean.TRUE;
        }).orElse(Boolean.FALSE);
    }

    public static interface ExportOperationSupplier {

        Optional<FileResourcessExportOperation> get(ProcessDefinition definition, List<IFile> resourcesToExport) throws Exception;

    }

    public static interface StringArgFunction {

        void apply(String s) throws Exception;

    }
}
