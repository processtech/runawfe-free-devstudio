package ru.runa.gpd.jointformeditor;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.jointformeditor.resources.Messages;
import ru.runa.gpd.jseditor.JavaScriptEditor;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.wizard.FieldValidatorsWizardPage;
import ru.runa.gpd.ui.wizard.GlobalValidatorsWizardPage;
import ru.runa.gpd.ui.wizard.ValidatorWizard;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.TemplateUtils;
import ru.runa.gpd.validation.ValidationUtil;

public class JointFormEditor extends FormEditor {

    public static final String ID = "ru.runa.gpd.jointformeditor";

    private JavaScriptEditor jsEditor;
    private IFile validationFile;
    private ValidatorWizard wizard;
    private FieldValidatorsWizardPage fieldValidatorsPage;
    private GlobalValidatorsWizardPage globalValidatorsPage;

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        if (formNode != null) {
            if (!formNode.hasFormScript()) {
                formNode.setScriptFileNameSoftly(formNode.getId() + "." + FormNode.SCRIPT_SUFFIX);
            }
            IFile processDefinitionFile = IOUtils.getProcessDefinitionFile((IFolder) formFile.getParent());
            IFile jsFile = IOUtils.getAdjacentFile(processDefinitionFile, formNode.getScriptFileName());
            if (!jsFile.exists()) {
                try {
                    IOUtils.createFile(jsFile, TemplateUtils.getFormTemplateAsStream());
                } catch (CoreException e) {
                    throw new PartInitException(e.getMessage(), e);
                }
            }
            if (!formNode.hasFormValidation()) {
                formNode.setValidationFileNameSoftly(formNode.getId() + "." + FormNode.VALIDATION_SUFFIX);
            }
            IFile validationFile = IOUtils.getAdjacentFile(processDefinitionFile, formNode.getValidationFileName());
            if (!validationFile.exists()) {
                ValidationUtil.createEmptyValidation(processDefinitionFile, formNode);
            }
        }
    }

    @Override
    protected void createPages() {
        super.createPages();
        IFile definitionFile = IOUtils.getProcessDefinitionFile((IFolder) formFile.getParent());
        try {

            jsEditor = new JavaScriptEditor();
            IFile jsFile = IOUtils.getAdjacentFile(definitionFile, formNode.getScriptFileName());
            addPage(jsEditor, new FileEditorInput(jsFile));
            setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.script"));

            validationFile = IOUtils.getAdjacentFile(definitionFile, formNode.getValidationFileName());

            wizard = new ValidatorWizard(validationFile, formNode);
            wizard.addPages();

            fieldValidatorsPage = (FieldValidatorsWizardPage) wizard.getPages()[0];
            fieldValidatorsPage.createControl(getContainer());
            addPage(fieldValidatorsPage.getControl());
            setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.field_validators"));
            fieldValidatorsPage.setMarkEditorDirtyCallback(p -> setDirty(p));

            globalValidatorsPage = (GlobalValidatorsWizardPage) wizard.getPages()[1];
            globalValidatorsPage.createControl(getContainer());
            addPage(globalValidatorsPage.getControl());
            setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.global_validators"));
            globalValidatorsPage.setMarkEditorDirtyCallback(p -> setDirty(p));

            addPropertyListener((source, propId) -> {
                if (propId == IEditorPart.PROP_DIRTY && !Strings.isNullOrEmpty(getSourceDocumentHTML())) {
                    fieldValidatorsPage.updateConfigs(getSourceDocumentHTML().getBytes(Charsets.UTF_8));
                }
            });

            addPageChangedListener(event -> {
                if (event.getSelectedPage() == fieldValidatorsPage.getControl() && isDirty()) {
                    fieldValidatorsPage.updateConfigs(getSourceDocumentHTML().getBytes(Charsets.UTF_8));
                }
            });

        } catch (PartInitException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (isDirty()) {
            formNode.setDirty();
        }
        if (jsEditor != null && fieldValidatorsPage != null && globalValidatorsPage != null) {
            jsEditor.doSave(monitor);
            wizard.performFinish();
        }
        super.doSave(monitor);
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public boolean isDirty() {
        if (jsEditor != null && fieldValidatorsPage != null && globalValidatorsPage != null) {
            return super.isDirty() || jsEditor.isDirty() || fieldValidatorsPage.isDirty() || globalValidatorsPage.isDirty();
        } else {
            return false;
        }
    }

    private void setDirty(boolean dirty) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    @Override
    public void dispose() {
        fieldValidatorsPage.dispose();
        globalValidatorsPage.dispose();
        super.dispose();
        wizard.dispose();
        boolean rewriteFormsXml = false;
        if (!formFile.exists()) {
            formNode.setFormFileNameSoftly("");
            rewriteFormsXml = true;
        }
        if (!validationFile.exists()) {
            formNode.setValidationFileNameSoftly("");
            rewriteFormsXml = true;
        }
        if (!((IFileEditorInput) jsEditor.getEditorInput()).exists()) {
            formNode.setScriptFileNameSoftly(null);
            rewriteFormsXml = true;
        }
        if (rewriteFormsXml) {
            IOUtils.saveFormsXml(formNode, formFile);
        }
    }

}
