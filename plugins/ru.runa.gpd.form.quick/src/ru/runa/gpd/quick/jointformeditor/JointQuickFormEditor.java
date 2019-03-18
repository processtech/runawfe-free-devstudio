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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.jointformeditor.resources.Messages;
import ru.runa.gpd.jseditor.JavaScriptEditor;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.lang.par.ProcessDefinitionValidator;
import ru.runa.gpd.quick.formeditor.QuickFormEditor;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.control.FieldValidatorsPage;
import ru.runa.gpd.ui.control.GlobalValidatorsPage;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidationUtil;

public class JointQuickFormEditor extends MultiPageEditorPart {

    public static final String ID = "ru.runa.gpd.quickjointformeditor";

    protected FormNode formNode;
    protected IFile formFile;
    protected IFolder definitionFolder;

    private boolean dirty = false;
    private QuickFormEditor quickEditor;
    private JavaScriptEditor jsEditor;
    private FormNodeValidation validation;
    private FieldValidatorsPage fieldValidatorsPage;
    private GlobalValidatorsPage globalValidatorsPage;

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
        validation = formNode.getValidation(formFile);
    }

    @Override
    protected void createPages() {
        IFile definitionFile = IOUtils.getProcessDefinitionFile((IFolder) formFile.getParent());
        quickEditor = new QuickFormEditor();
        IFile qfFile = IOUtils.getAdjacentFile(definitionFile, formNode.getFormFileName());
        try {
            addPage(quickEditor, new FileEditorInput(qfFile));
        } catch (PartInitException e) {
            throw new RuntimeException(e);
        }
        setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.template"));

        IFile jsFile = IOUtils.getAdjacentFile(definitionFile, formNode.getScriptFileName());
        jsEditor = new JavaScriptEditor(formNode, jsFile);
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
            if (getActivePage() == 0 && propId == IEditorPart.PROP_DIRTY && !quickEditor.isEmpty()) {
                fieldValidatorsPage.updateConfigs(formFile);
            }
        });

        addPageChangedListener(event -> {
            if (event.getSelectedPage() == fieldValidatorsPage && !quickEditor.isEmpty()) {
                try {
                    fieldValidatorsPage.updateConfigs(quickEditor.getFormData());
                } catch (UnsupportedEncodingException | CoreException e) {
                    PluginLogger.logError(e);
                }
            }
        });
        String selectedPage = Activator.getPrefString(PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE);
        if (PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE_FORM.equals(selectedPage)) {
            setActivePage(0);
        }
        if (PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE_SCRIPT.equals(selectedPage)) {
            setActivePage(1);
        }
        if (PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE_VALIDATION.equals(selectedPage)) {
            setActivePage(2);
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        quickEditor.doSave(monitor);
        jsEditor.doSave(monitor);
        fieldValidatorsPage.updateConfigs(formFile);
        fieldValidatorsPage.doSave();
        globalValidatorsPage.doSave();
        ValidationUtil.rewriteValidation(formFile, formNode, validation);
        setDirty(false);
        ProcessDefinitionValidator.validateDefinition(formNode.getProcessDefinition());
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
        return super.isDirty() || fieldValidatorsPage.isDirty() || globalValidatorsPage.isDirty();
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
    }

}
