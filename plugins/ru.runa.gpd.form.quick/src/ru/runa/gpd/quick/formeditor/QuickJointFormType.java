package ru.runa.gpd.quick.formeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.quick.jointformeditor.QuickJointFormEditor;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.TemplateUtils;
import ru.runa.gpd.validation.ValidationUtil;

public class QuickJointFormType extends QuickFormType {

    @Override
    public IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException {
        try {
            if (!formNode.hasFormScript()) {
                formNode.setScriptFileName(formNode.getId() + "." + FormNode.SCRIPT_SUFFIX);
            }
            IFile processDefinitionFile = IOUtils.getProcessDefinitionFile((IFolder) formFile.getParent());
            IFile jsFile = IOUtils.getAdjacentFile(processDefinitionFile, formNode.getScriptFileName());
            if (!jsFile.exists()) {
                IOUtils.createFile(jsFile, TemplateUtils.getFormTemplateAsStream());
            }
            if (!formNode.hasFormValidation()) {
                formNode.setValidationFileName(formNode.getId() + "." + FormNode.VALIDATION_SUFFIX);
            }
            IFile validationFile = IOUtils.getAdjacentFile(processDefinitionFile, formNode.getValidationFileName());
            if (!validationFile.exists()) {
                ValidationUtil.createEmptyValidation(processDefinitionFile, formNode);
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
            throw new RuntimeException(e);
        }
        ((ProcessDefinition) formNode.getParent()).setDirty(false);
        return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), formFile, QuickJointFormEditor.ID, true);
    }

}
