package ru.runa.gpd.ui.dialog;

import java.io.File;
import java.util.Map;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.editor.ProcessSaveHistory;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class ProcessSaveHistoryDialog extends Dialog {
    
    private IFolder definitionFolder;
    private Map<String, File> savepoints;
    private List lstSavepoints;

    public ProcessSaveHistoryDialog(IFolder definitionFolder) {
        super(Display.getDefault().getActiveShell());
        this.definitionFolder = definitionFolder;
    }

	@Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        lstSavepoints = new List(area, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 200;
        lstSavepoints.setLayoutData(gridData);
        savepoints = ProcessSaveHistory.getSavepoints(definitionFolder);
        for (String timestamp : savepoints.keySet()) {
            lstSavepoints.add(timestamp);
        }
        lstSavepoints.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                updateButtons();
            }
        });
        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Localization.getString("button.restore"), false);
        createButton(parent, IDialogConstants.FINISH_ID, Localization.getString("button.clear"), false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, true);
        updateButtons();
    }

    private void updateButtons() {
        getButton(IDialogConstants.OK_ID).setEnabled(lstSavepoints.getSelectionIndex() >= 0);
        getButton(IDialogConstants.FINISH_ID).setEnabled(lstSavepoints.getItemCount() > 0);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImage(SharedImages.getImage("icons/saveall_edit.gif"));
        newShell.setText(Localization.getString("ProcessSaveHistoryDialog.title"));
    }

    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (IDialogConstants.FINISH_ID == buttonId) {
            setReturnCode(IDialogConstants.FINISH_ID);
            if (!Activator.getPrefBoolean(PrefConstants.P_CONFIRM_DELETION) || MessageDialog.openQuestion(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Localization.getString("ClearProcessSaveHistory.title"),
                    Localization.getString("ClearProcessSaveHistory.message"))) {
                ProcessSaveHistory.clear(definitionFolder);
            }
            close();
        }
    }

    @Override
    protected void okPressed() {
        ProcessSaveHistory.restore(savepoints.get(lstSavepoints.getSelection()[0]), definitionFolder);
        super.okPressed();
    }

}
