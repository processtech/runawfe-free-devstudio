package ru.runa.gpd.lang.action;

import com.google.common.collect.Lists;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.dialog.MultipleSelectionDialog;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SelectionItem;

public class DeleteFormFilesAction extends BaseModelActionDelegate {

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        FormNode formNode = getSelection();
        if (formNode != null) {
            if (formNode.isFormEditorOpened()) {
                action.setEnabled(false);
            } else {
                action.setEnabled(formNode.hasForm() || formNode.hasFormValidation() || formNode.hasFormScript());
            }
        }
    }

    @Override
    public void run(IAction action) {
        FormNode formNode = getSelection();
        List<SelectionItem> items = Lists.newArrayList();
        SelectionItem deleteFormFile = null;
        if (formNode.hasForm()) {
            deleteFormFile = new SelectionItem(true, Localization.getString("form.file"));
            items.add(deleteFormFile);
        }
        SelectionItem deleteValidationFile = null;
        if (formNode.hasFormValidation()) {
            deleteValidationFile = new SelectionItem(false, Localization.getString("form.validationFile"));
            items.add(deleteValidationFile);
        }
        SelectionItem deleteScriptFile = null;
        if (formNode.hasFormScript()) {
            deleteScriptFile = new SelectionItem(true, Localization.getString("form.scriptFile"));
            items.add(deleteScriptFile);
        }
        MultipleSelectionDialog dialog = new MultipleSelectionDialog(Localization.getString("DeleteFormFilesAction.title"), items);
        if (dialog.open() == IDialogConstants.OK_ID) {
            try {
                if (deleteFormFile != null && deleteFormFile.isEnabled()) {
                    formNode.setFormType(FormNode.EMPTY);
                    formNode.setFormFileName(FormNode.EMPTY);
                    formNode.setTemplateFileName(FormNode.EMPTY);
                }
                if (deleteValidationFile != null && deleteValidationFile.isEnabled()) {
                    IOUtils.markAsDeleted(IOUtils.getAdjacentFile(formNode.getProcessDefinition().getFile(), formNode.getValidationFileName()));
                    formNode.setDirty();
                }
                if (deleteScriptFile != null && deleteScriptFile.isEnabled()) {
                    IOUtils.markAsDeleted(IOUtils.getAdjacentFile(formNode.getProcessDefinition().getFile(), formNode.getScriptFileName()));
                    formNode.setDirty();
                }
            } catch (CoreException e) {
                PluginLogger.logError(e);
            }
        }
    }
}
