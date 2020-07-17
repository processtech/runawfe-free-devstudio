package ru.runa.gpd.ui.dialog;

import com.google.common.base.Strings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InfoWithDetailsDialog extends IconAndMessageDialog {
    private final int dialogType;
    private final boolean showDetails;
    private String title;
    private String details;
    private Text detailsText;

    public InfoWithDetailsDialog(int dialogType, String dialogTitle, String infoMessage, String details, boolean showDetails) {
        super(Display.getCurrent().getActiveShell());
        this.dialogType = dialogType;
        this.title = dialogTitle;
        this.message = infoMessage;
        this.details = details;
        this.showDetails = showDetails;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void buttonPressed(int id) {
        if (id == IDialogConstants.DETAILS_ID) {
            // was the details button pressed?
            toggleDetailsArea();
        } else {
            super.buttonPressed(id);
        }
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }

    @Override
    protected Image getImage() {
        switch (dialogType) {
        case MessageDialog.ERROR: {
            return getErrorImage();
        }
        case MessageDialog.INFORMATION: {
            return getInfoImage();
        }
        case MessageDialog.CONFIRM: {
            return getQuestionImage();
        }
        case MessageDialog.WARNING: {
            return getWarningImage();
        }
        }
        return getQuestionImage();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
        if (dialogType != MessageDialog.ERROR && dialogType != MessageDialog.INFORMATION) {
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
        }
        if (!Strings.isNullOrEmpty(details)) {
            createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        createMessageArea(parent);
        // create a composite with standard margins and spacing
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.numColumns = 2;
        composite.setLayout(layout);
        GridData childData = new GridData(GridData.FILL_BOTH);
        childData.horizontalSpan = 2;
        composite.setLayoutData(childData);
        composite.setFont(parent.getFont());
        return composite;
    }

    @Override
    protected void createDialogAndButtonArea(Composite parent) {
        super.createDialogAndButtonArea(parent);
        if (this.dialogArea instanceof Composite) {
            // Create a label if there are no children to force a smaller layout
            Composite dialogComposite = (Composite) dialogArea;
            if (dialogComposite.getChildren().length == 0) {
                new Label(dialogComposite, SWT.NULL);
            }

            if (showDetails) {
                toggleDetailsArea(getShell(), parent);
            }
        }
    }

    protected void createDropDownList(Composite parent) {
        detailsText = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        detailsText.setText(null == details ? "" : details);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        detailsText.setLayoutData(data);
    }

    private void toggleDetailsArea() {
        toggleDetailsArea(getShell(), (Composite) getContents());
    }

    private void toggleDetailsArea(Shell shell, Composite composite) {
        Point windowSize = shell.getSize();
        Point oldSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (isDetailsAreaCreated()) {
            detailsText.dispose();
            getButton(IDialogConstants.DETAILS_ID).setText(IDialogConstants.SHOW_DETAILS_LABEL);
        } else {
            createDropDownList(composite);
            getButton(IDialogConstants.DETAILS_ID).setText(IDialogConstants.HIDE_DETAILS_LABEL);
        }
        Point newSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        shell.setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
    }

    private boolean isDetailsAreaCreated() {
        return (detailsText != null && !detailsText.isDisposed());
    }

}
