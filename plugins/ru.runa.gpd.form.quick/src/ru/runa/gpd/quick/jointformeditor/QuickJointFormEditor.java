package ru.runa.gpd.quick.jointformeditor;

import com.google.common.base.Preconditions;
import java.io.UnsupportedEncodingException;
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
import org.eclipse.ui.part.MultiPageEditorPart;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.jointformeditor.resources.Messages;
import ru.runa.gpd.jseditor.JavaScriptEditor;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.quick.formeditor.QuickFormEditor;
import ru.runa.gpd.ui.wizard.FieldValidatorsWizardPage;
import ru.runa.gpd.ui.wizard.GlobalValidatorsWizardPage;
import ru.runa.gpd.ui.wizard.ValidatorWizard;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.TemplateUtils;
import ru.runa.gpd.validation.ValidationUtil;

public class QuickJointFormEditor extends MultiPageEditorPart {

    public static final String ID = "ru.runa.gpd.quickjointformeditor";

    protected FormNode formNode;
    protected IFile formFile;
    protected IFolder definitionFolder;

    private boolean dirty = false;
    private JavaScriptEditor jsEditor;
    private IFile validationFile;
    private QuickFormEditor quickEditor;
    private ValidatorWizard wizard;
    private FieldValidatorsWizardPage fieldValidatorsPage;
    private GlobalValidatorsWizardPage globalValidatorsPage;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        formFile = ((FileEditorInput) input).getFile();
        definitionFolder = (IFolder) formFile.getParent();
        IFile definitionFile = IOUtils.getProcessDefinitionFile(definitionFolder);
        ProcessDefinition processDefinition = ProcessCache.getProcessDefinition(definitionFile);
        if (formFile.getName().startsWith(ParContentProvider.SUBPROCESS_DEFINITION_PREFIX)) {
            String subprocessId = formFile.getName().substring(0, formFile.getName().indexOf("."));
            processDefinition = processDefinition.getEmbeddedSubprocessById(subprocessId);
            Preconditions.checkNotNull(processDefinition, "embedded subpocess");
        }
        for (FormNode formNode : processDefinition.getChildren(FormNode.class)) {
            if (input.getName().equals(formNode.getFormFileName())) {
                this.formNode = formNode;
                setPartName(formNode.getName());
                break;
            }
        }
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

    @Override
    protected void createPages() {
        IFile definitionFile = IOUtils.getProcessDefinitionFile((IFolder) formFile.getParent());
        try {

            quickEditor = new QuickFormEditor();
            IFile qfFile = IOUtils.getAdjacentFile(definitionFile, formNode.getFormFileName());
            addPage(quickEditor, new FileEditorInput(qfFile));
            setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.template"));

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
                if (propId == IEditorPart.PROP_DIRTY && !quickEditor.isEmpty()) {
                    fieldValidatorsPage.updateConfigs(formFile);
                }
            });

            addPageChangedListener(event -> {
                if (event.getSelectedPage() == fieldValidatorsPage.getControl() && !quickEditor.isEmpty()) {
                    try {
                        fieldValidatorsPage.updateConfigs(quickEditor.getFormData());
                    } catch (UnsupportedEncodingException | CoreException e) {
                        PluginLogger.logError(e);
                    }
                }
            });

        } catch (PartInitException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (quickEditor != null && jsEditor != null && fieldValidatorsPage != null && globalValidatorsPage != null) {
            quickEditor.doSave(monitor);
            jsEditor.doSave(monitor);
            wizard.performFinish();
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
        // do nothing
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
        quickEditor.dispose();
        wizard.dispose();
        super.dispose();
        boolean rewriteFormsXml = false;
        if (!formFile.exists()) {
            formNode.setFormFileNameSoftly("");
            formNode.setTemplateFileNameSoftly("");
            rewriteFormsXml = true;
        }
        if (!validationFile.exists()) {
            formNode.setValidationFileNameSoftly("");
            rewriteFormsXml = true;
        }
        if (!((IFileEditorInput) jsEditor.getEditorInput()).exists()) {
            formNode.setScriptFileNameSoftly("");
            rewriteFormsXml = true;
        }
        if (rewriteFormsXml) {
            IOUtils.saveFormsXml(formNode, formFile);
        }
    }

}
