package ru.runa.gpd.form.jointeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.command.FormNodeSetScriptFileCommand;
import ru.runa.gpd.editor.gef.command.FormNodeSetValidationFileCommand;
import ru.runa.gpd.formeditor.FtlFormType;
import ru.runa.gpd.formeditor.ftl.ui.FormComponentsView;
import ru.runa.gpd.lang.action.OpenFormEditorDelegate;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.ValidationUtil;

public class OpenJointFormEditorDelegate extends OpenFormEditorDelegate {

    @Override
    protected void openInEditor(IFile file, FormNode formNode) throws CoreException {
        try {
            if (!formNode.hasFormScript()) {
                setNewScriptFormFile(formNode, formNode.getId() + "." + FormNode.SCRIPT_SUFFIX);
            }
            IFile jsFile = IOUtils.getAdjacentFile(IOUtils.getProcessDefinitionFile((IFolder) file.getParent()), formNode.getScriptFileName());
            if (!jsFile.exists()) {
                IOUtils.createFile(jsFile, getClass().getResourceAsStream("/conf/form.template.js"));
            }
            if (!formNode.hasFormValidation()) {
                setNewValidationFormFile(formNode, formNode.getId() + "." + FormNode.VALIDATION_SUFFIX);
            }
            IFile validationFile = IOUtils.getAdjacentFile(getDefinitionFile(), formNode.getValidationFileName());
            if (!validationFile.exists()) {
                ValidationUtil.createEmptyValidation(getDefinitionFile(), formNode);
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
            throw new RuntimeException(e);
        }
        if (formNode.getFormType().equals(FtlFormType.TYPE)) {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(FormComponentsView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
        }
        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
                formNode.getFormType().equals(FtlFormType.TYPE) ? JointFormEditor.ID : QuickJointFormEditor.ID, true);
    }

    private void setNewScriptFormFile(FormNode formNode, String fileName) {
        FormNodeSetScriptFileCommand command = new FormNodeSetScriptFileCommand();
        command.setFormNode(formNode);
        command.setScriptFileName(fileName);
        executeCommand(command);
    }

    private void setNewValidationFormFile(FormNode formNode, String fileName) {
        FormNodeSetValidationFileCommand command = new FormNodeSetValidationFileCommand();
        command.setFormNode(formNode);
        command.setValidationFileName(fileName);
        executeCommand(command);
    }

}
