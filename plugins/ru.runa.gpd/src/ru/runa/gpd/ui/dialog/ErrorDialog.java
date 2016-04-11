package ru.runa.gpd.ui.dialog;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.ThrowableCauseExtractor;

public class ErrorDialog extends IconAndMessageDialog {
    private Button detailsButton;
    private final String details;
    private Text text;

    public ErrorDialog(String message, String details) {
        super(Display.getCurrent() != null ? Display.getCurrent().getActiveShell() : null);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.message = message;
        this.details = details;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Localization.getString("error"));
    }

    @Override
    protected void buttonPressed(int id) {
        if (id == IDialogConstants.DETAILS_ID) {
            // was the details button pressed?
            toggleDetailsArea();
        }
        super.buttonPressed(id);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
        if (details != null) {
            detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        return createMessageArea(parent);
    }

    @Override
    protected Image getImage() {
        return getErrorImage();
    }

    protected void createDetailsArea(Composite parent) {
        text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        text.setText(details);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        data.heightHint = 100;
        text.setLayoutData(data);
    }

    protected void disposeDetailsArea() {
        text.dispose();
        text = null;
    }

    private void toggleDetailsArea() {
        String buttonLabel;
        if (text != null) {
            disposeDetailsArea();
            buttonLabel = IDialogConstants.SHOW_DETAILS_LABEL;
        } else {
            createDetailsArea((Composite) getContents());
            buttonLabel = IDialogConstants.HIDE_DETAILS_LABEL;
        }
        detailsButton.setText(buttonLabel);
        Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        getShell().setSize(new Point(getShell().getSize().x, newSize.y));
    }

    public static void open(Throwable th) {
        open(null, th);
    }

    public static void open(String message) {
        open(message, null);
    }

    public static void open(String message, Throwable th) {
        StringWriter writer = new StringWriter();
        if (th != null) {
            ThrowableCauseExtractor causeExtractor = new ThrowableCauseExtractor(th);
            causeExtractor.runWhile();
            if (message == null) {
                message = causeExtractor.cause.getMessage();
            }
            if (message == null) {
                message = causeExtractor.cause.getClass().getName();
            }
            causeExtractor.cause.printStackTrace(new PrintWriter(writer));
        }
        new ErrorDialog(message, writer.toString()).open();
    }

}
