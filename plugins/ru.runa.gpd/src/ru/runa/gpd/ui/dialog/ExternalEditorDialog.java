package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Localization;

public class ExternalEditorDialog extends Dialog {

    private Text text;

    private String path;

    public ExternalEditorDialog(Shell parent, String path) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.path = path;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        text = new Text(composite, SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = 300;
        text.setLayoutData(gridData);
        text.setText(path);
        text.setEditable(false);

        Button button = new Button(composite, SWT.PUSH);
        button.setLayoutData(new GridData(GridData.FILL_BOTH));
        button.setText(Localization.getString("button.choose"));
        button.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                FileDialog fd = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.OPEN);
                String selectedPath = fd.open();

                if (selectedPath != null) {
                    text.setText(selectedPath);
                    refreshOk();
                }
            }
        });

        composite.pack();
        getShell().setText(Localization.getString("EditorSelectionDialog.title"));
        return composite;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control control = super.createButtonBar(parent);
        this.getButton(IDialogConstants.OK_ID).setEnabled(false);
        return control;
    }

    private void refreshOk() {
        this.path = text.getText();
        this.getButton(IDialogConstants.OK_ID).setEnabled(path.length() > 0);
    }

    public String getPath() {
        return path;
    }

}
