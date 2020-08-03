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
import ru.runa.gpd.Localization;

public class CustomDialog extends IconAndMessageDialog {

    private final int dialogType;
    private String title = "";
    private boolean isOkButton = true;
    private boolean isCancelButton = false;
    private int defaultButton = IDialogConstants.OK_ID;

    private boolean detailsMode = false;
    private String detailsText;
    private boolean openedByDefaultDetailsArea;
    private Text detailsTextComponent;

    private boolean actionMode = false;
    private int actionId;
    private String actionButtonTitle;

    public CustomDialog(int dialogType, String message) {
        super(Display.getCurrent().getActiveShell());
        this.dialogType = dialogType;
        this.message = message;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void buttonPressed(int id) {
        if (id == actionId) {
            setReturnCode(IDialogConstants.PROCEED_ID);
            close();
        } else if (id == IDialogConstants.DETAILS_ID) {
            toggleDetailsArea();
        } else {
            super.buttonPressed(id);
        }
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(getTitle());
    }

    @Override
    protected Image getImage() {
        switch (dialogType) {
        case MessageDialog.ERROR:
            return getErrorImage();
        case MessageDialog.INFORMATION:
            return getInfoImage();
        case MessageDialog.CONFIRM:
            return getQuestionImage();
        case MessageDialog.WARNING:
            return getWarningImage();
        }
        return getQuestionImage();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (actionMode) {
            createButton(parent, actionId, actionButtonTitle, actionId == defaultButton);
        }
        if (isOkButton) {
            createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, IDialogConstants.OK_ID == defaultButton);
        }
        if (isCancelButton) {
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, IDialogConstants.CANCEL_ID == defaultButton);
        }
        if (detailsMode && !Strings.isNullOrEmpty(detailsText)) {
            createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, IDialogConstants.DETAILS_ID == defaultButton);
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

            if (openedByDefaultDetailsArea) {
                toggleDetailsArea(getShell(), parent);
            }
        }
    }

    protected void createDropDownList(Composite parent) {
        detailsTextComponent = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        detailsTextComponent.setText(null == detailsText ? "" : detailsText);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        detailsTextComponent.setLayoutData(data);
    }

    private void toggleDetailsArea() {
        toggleDetailsArea(getShell(), (Composite) getContents());
    }

    private void toggleDetailsArea(Shell shell, Composite composite) {
        Point windowSize = shell.getSize();
        Point oldSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (isDetailsAreaCreated()) {
            detailsTextComponent.dispose();
            getButton(IDialogConstants.DETAILS_ID).setText(IDialogConstants.SHOW_DETAILS_LABEL);
        } else {
            createDropDownList(composite);
            getButton(IDialogConstants.DETAILS_ID).setText(IDialogConstants.HIDE_DETAILS_LABEL);
        }
        Point newSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        shell.setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
    }

    private boolean isDetailsAreaCreated() {
        return (detailsTextComponent != null && !detailsTextComponent.isDisposed());
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        if (Strings.isNullOrEmpty(title)) {
            switch (dialogType) {
            case MessageDialog.CONFIRM:
                title = Localization.getString("message.confirm");
                break;
            case MessageDialog.ERROR:
                title = Localization.getString("message.error");
                break;
            case MessageDialog.INFORMATION:
                title = Localization.getString("message.information");
                break;
            case MessageDialog.WARNING:
                title = Localization.getString("message.warning");
                break;
            }
        }
        return title;
    }

    public void setDetailArea(String detailsText, boolean openedByDefaultDetailsArea) {
        detailsMode = true;
        this.detailsText = detailsText;
        this.openedByDefaultDetailsArea = openedByDefaultDetailsArea;
    }

    public void setActionMode(int actionId, String actionButtonTitle) {
        actionMode = true;
        this.actionId = actionId;
        this.actionButtonTitle = actionButtonTitle;
    }

    public void setOkButton(boolean isOkButton) {
        this.isOkButton = isOkButton;
    }

    public void setCancelButton(boolean isCancelButton) {
        this.isCancelButton = isCancelButton;
    }

    public void setDefaultButton(int defaultButton) {
        this.defaultButton = defaultButton;
    }
}
