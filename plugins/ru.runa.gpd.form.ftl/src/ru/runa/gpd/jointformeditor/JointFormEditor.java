package ru.runa.gpd.jointformeditor;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import ru.runa.gpd.Activator;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.jointformeditor.resources.Messages;
import ru.runa.gpd.jseditor.JavaScriptEditor;
import ru.runa.gpd.lang.par.ProcessDefinitionValidator;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.control.FieldValidatorsPage;
import ru.runa.gpd.ui.control.GlobalValidatorsPage;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidationUtil;

public class JointFormEditor extends FormEditor {
    public static final String ID = "ru.runa.gpd.jointformeditor";

    private JavaScriptEditor jsEditor;
    private FormNodeValidation validation;
    private FieldValidatorsPage fieldValidatorsPage;
    private GlobalValidatorsPage globalValidatorsPage;

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        Preconditions.checkNotNull(formNode, "formNode");
        IFile processDefinitionFile = IOUtils.getProcessDefinitionFile((IFolder) formFile.getParent());
        validation = formNode.getValidation(processDefinitionFile);
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
        IFile jsFile = IOUtils.getAdjacentFile(formFile, formNode.getScriptFileName());
        jsEditor = new JavaScriptEditor(formNode, jsFile);
        try {
            addPage(jsEditor, new FileEditorInput(jsFile));
        } catch (PartInitException e) {
            throw new RuntimeException(e);
        }
        setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.script"));

        fieldValidatorsPage = new FieldValidatorsPage(getContainer(), formNode, validation, p -> setDirty());
        addPage(fieldValidatorsPage);
        setPageText(getPageCount() - 1, Messages.getString("editor.tab_name.field_validators"));

        globalValidatorsPage = new GlobalValidatorsPage(getContainer(), formNode, validation, p -> setDirty());
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
        super.doSave(monitor);
        jsEditor.doSave(monitor);
        fieldValidatorsPage.updateConfigs(formFile);
        fieldValidatorsPage.doSave();
        globalValidatorsPage.doSave();
        ValidationUtil.rewriteValidation(formFile, formNode, validation);
        setDirty();
        ProcessDefinitionValidator.validateDefinition(formNode.getProcessDefinition());
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public boolean isDirty() {
        // rm1721, occasional NPE
        return super.isDirty() || (jsEditor != null && jsEditor.isDirty()) || (fieldValidatorsPage != null && fieldValidatorsPage.isDirty())
                || (globalValidatorsPage != null && globalValidatorsPage.isDirty());
    }

    private void setDirty() {
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    @Override
    public void dispose() {
        fieldValidatorsPage.dispose();
        globalValidatorsPage.dispose();
        super.dispose();
    }

}
