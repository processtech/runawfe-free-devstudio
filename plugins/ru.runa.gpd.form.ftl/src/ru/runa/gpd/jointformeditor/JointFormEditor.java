package ru.runa.gpd.jointformeditor;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
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
import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.jointformeditor.resources.Messages;
import ru.runa.gpd.jseditor.JavaScriptEditor;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.control.FieldValidatorsPage;
import ru.runa.gpd.ui.control.GlobalValidatorsPage;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.TemplateUtils;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidationUtil;
import ru.runa.gpd.validation.ValidatorParser;

public class JointFormEditor extends FormEditor {
    public static final String ID = "ru.runa.gpd.jointformeditor";

    private JavaScriptEditor jsEditor;
    private IFile validationFile;
    private FormNodeValidation validation;
    private FieldValidatorsPage fieldValidatorsPage;
    private GlobalValidatorsPage globalValidatorsPage;

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        Preconditions.checkNotNull(formNode, "formNode");
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
        validationFile = IOUtils.getAdjacentFile(processDefinitionFile, formNode.getValidationFileName());
        if (!validationFile.exists()) {
            validationFile = ValidationUtil.createEmptyValidation(processDefinitionFile, formNode);
        }
        validation = ValidatorParser.parseValidation(validationFile);
    }

    @Override
    protected void createPages() {
        String selectedPage = Activator.getPrefString(PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE);
        if (PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE_FORM.equals(selectedPage)) {
            currentPageIndex = 0;
        }
        if (PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE_SCRIPT.equals(selectedPage)) {
            currentPageIndex = 2;
        }
        if (PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE_VALIDATION.equals(selectedPage)) {
            currentPageIndex = 3;
        }
        super.createPages();
        IFile definitionFile = IOUtils.getProcessDefinitionFile((IFolder) formFile.getParent());
        jsEditor = new JavaScriptEditor();
        IFile jsFile = IOUtils.getAdjacentFile(definitionFile, formNode.getScriptFileName());
        try {
            addPage(jsEditor, new FileEditorInput(jsFile));
        } catch (PartInitException e) {
            throw new RuntimeException(e);
        }
        setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.script"));

        fieldValidatorsPage = new FieldValidatorsPage(getContainer(), formNode, validation, p -> setDirty(p));
        addPage(fieldValidatorsPage);
        setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.field_validators"));

        globalValidatorsPage = new GlobalValidatorsPage(getContainer(), formNode, validation, p -> setDirty(p));
        addPage(globalValidatorsPage);
        setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.global_validators"));

        addPropertyListener((source, propId) -> {
            if (getActivePage() < 2 && propId == IEditorPart.PROP_DIRTY) {
                String html = getSourceDocumentHTML();
                if (!Strings.isNullOrEmpty(html)) {
                    fieldValidatorsPage.updateConfigs(html.getBytes(Charsets.UTF_8));
                }
            }
        });

        addPageChangedListener(event -> {
            if (event.getSelectedPage() == fieldValidatorsPage && isDirty()) {
                fieldValidatorsPage.updateConfigs(getSourceDocumentHTML().getBytes(Charsets.UTF_8));
            }
        });
        setActivePage(currentPageIndex);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (isDirty()) {
            formNode.setDirty();
        }
        super.doSave(monitor);
        jsEditor.doSave(monitor);
        fieldValidatorsPage.doSave();
        globalValidatorsPage.doSave();
        ValidationUtil.removeEmptyConfigsForDeletedVariables(validationFile, formNode, validation);
        setDirty(false);
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || jsEditor.isDirty() || fieldValidatorsPage.isDirty() || globalValidatorsPage.isDirty();
    }

    private void setDirty(boolean dirty) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    @Override
    public void dispose() {
        fieldValidatorsPage.dispose();
        globalValidatorsPage.dispose();
        super.dispose();
        boolean rewriteFormsXml = false;
        if (!formFile.exists()) {
            formNode.setFormFileNameSoftly("");
            rewriteFormsXml = true;
        }
        ValidationUtil.removeValidationIfEmpty(validationFile, validation);
        try {
            if (!validationFile.exists() || validationFile.getSessionProperty(WorkspaceOperations.PROPERTY_FILE_WILL_BE_DELETED_SHORTLY) != null) {
                formNode.setValidationFileNameSoftly("");
                rewriteFormsXml = true;
            }
            IFile script = ((IFileEditorInput) jsEditor.getEditorInput()).getFile();
            if (!script.exists() || script.getSessionProperty(WorkspaceOperations.PROPERTY_FILE_WILL_BE_DELETED_SHORTLY) != null) {
                formNode.setScriptFileNameSoftly("");
                rewriteFormsXml = true;
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
        if (rewriteFormsXml) {
            WorkspaceOperations.job("Form rewriting", (p) -> IOUtils.saveFormsXml(formNode, formFile));
        }
    }

}
