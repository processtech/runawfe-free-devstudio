package ru.runa.gpd.jointformeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.jointformeditor.resources.Messages;
import ru.runa.gpd.jseditor.JavaScriptEditor;
import ru.runa.gpd.ui.wizard.FieldValidatorsWizardPage;
import ru.runa.gpd.ui.wizard.GlobalValidatorsWizardPage;
import ru.runa.gpd.ui.wizard.ValidatorWizard;
import ru.runa.gpd.util.IOUtils;

public class JointFormEditor extends FormEditor {

    public static final String ID = "ru.runa.gpd.jointformeditor";

    private boolean dirty = false;
    private JavaScriptEditor jsEditor;
    private IFile validationFile;
    private ValidatorWizard wizard;
    private FieldValidatorsWizardPage fieldValidatorsPage;
    private GlobalValidatorsWizardPage globalValidatorsPage;

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

            addPageChangedListener(new IPageChangedListener() {
                @Override
                public void pageChanged(PageChangedEvent event) {
                    if (event.getSelectedPage() == fieldValidatorsPage.getControl() && !IOUtils.isEmpty(formFile)) {
                        fieldValidatorsPage.updateConfigs(formFile);
                    }
                }
            });

        } catch (PartInitException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
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

    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public void dispose() {
        fieldValidatorsPage.dispose();
        globalValidatorsPage.dispose();
        super.dispose();
        wizard.dispose();
        boolean rewriteFormsXml = false;
        if (!formFile.exists()) {
            formNode.setFormFileName("");
            rewriteFormsXml = true;
        }
        if (!validationFile.exists()) {
            formNode.setValidationFileName("");
            rewriteFormsXml = true;
        }
        if (!((IFileEditorInput) jsEditor.getEditorInput()).exists()) {
            formNode.setScriptFileName(null);
            rewriteFormsXml = true;
        }
        if (rewriteFormsXml) {
            IOUtils.saveFormsXml(formNode, formFile);
        }
    }

}
