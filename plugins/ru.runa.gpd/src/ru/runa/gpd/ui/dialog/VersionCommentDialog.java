package ru.runa.gpd.ui.dialog;

import java.util.Calendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class VersionCommentDialog extends Dialog {

    private Text numberText;
    private DateTime dateControl;
    private Text commentText;
    private final ProcessDefinition definition;

    public VersionCommentDialog(ProcessDefinition definition) {
        super(Display.getCurrent().getActiveShell());
        this.definition = definition;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Localization.getString("label.menu.versionCommentAction"));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        Button saveButton = getButton(IDialogConstants.OK_ID);
        saveButton.setText(Localization.getString("button.save"));
        setButtonLayoutData(saveButton);

        Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
        cancelButton.setText(Localization.getString("button.cancel"));
        setButtonLayoutData(cancelButton);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent = (Composite) super.createDialogArea(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label numberLabel = new Label(composite, SWT.NONE);
        numberLabel.setText(Localization.getString("VersionCommentDialog.number"));
        numberText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        numberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (definition.getVersionNumber().length() > 0) {
            numberText.setText(fromXMLSafeText(definition.getVersionNumber()));
        }

        Label dateLabel = new Label(composite, SWT.None);
        dateLabel.setText(Localization.getString("VersionCommentDialog.date"));
        dateControl = new DateTime(composite, SWT.DATE | SWT.BORDER);
        dateControl.setDate(definition.getVersionDate().get(Calendar.YEAR), definition.getVersionDate().get(Calendar.MONTH), definition
                .getVersionDate().get(Calendar.DAY_OF_MONTH));
        dateControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label commentLabel = new Label(composite, SWT.None);
        commentLabel.setText(Localization.getString("VersionCommentDialog.comment"));
        commentText = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.BORDER);
        if (definition.getVersionComment().length() > 0) {
            commentText.setText(fromXMLSafeText(definition.getVersionComment()));
        }
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 6 * commentText.getLineHeight();
        gridData.horizontalAlignment = SWT.FILL;
        commentText.setLayoutData(gridData);

        return parent;
    }

    @Override
    protected void okPressed() {
        if (fromXMLSafeText(definition.getVersionNumber()).equals(getVersionNumber()) != true) {
            definition.setVersionNumber(toXMLSafeText(getVersionNumber()));
            definition.setDirty();
        }
        if (definition.getVersionDate().get(Calendar.YEAR) != getVersionDate().get(Calendar.YEAR)
                || definition.getVersionDate().get(Calendar.MONTH) != getVersionDate().get(Calendar.MONTH)
                || definition.getVersionDate().get(Calendar.DAY_OF_MONTH) != getVersionDate().get(Calendar.DAY_OF_MONTH)) {
            definition.setVersionDate(getVersionDate());
            if (definition.isDirty() != true) {
                definition.setDirty();
            }
        }

        if (fromXMLSafeText(definition.getVersionComment()).equals(getVersionComment()) != true) {
            definition.setVersionComment(toXMLSafeText(getVersionComment()));
            if (definition.isDirty() != true) {
                definition.setDirty();
            }
        }

        this.close();
    }

    protected String getVersionNumber() {
        return numberText.getText();
    }

    protected Calendar getVersionDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, dateControl.getYear());
        cal.set(Calendar.MONTH, dateControl.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, dateControl.getDay());

        return cal;
    }

    protected String getVersionComment() {
        return commentText.getText();
    }

    protected String toXMLSafeText(String text) {
        return text.replace("<", "&lt;").replace(">", "&gt;").replace("\'", "&rsquo;").replace("\"", "&quot;").replace("/", "&frasl;")
                .replace("\r\n", "&013;");
    }

    protected String fromXMLSafeText(String text) {
        return text.replace("&lt;", "<").replace("&gt;", ">").replace("&rsquo;", "\'").replace("&quot;", "\"").replace("&frasl;", "/")
                .replace("&013;", "\r\n").replace("&amp;", "&");
    }

}