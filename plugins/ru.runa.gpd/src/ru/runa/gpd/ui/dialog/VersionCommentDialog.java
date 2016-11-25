package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VersionInfo;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.wfe.commons.CalendarUtil;

public class VersionCommentDialog extends Dialog {

    private Button addButton;
    private Button saveButton;
    private Button cancelButton;
    private DateTime dateControl;
    private Text authorText;
    private Text commentText;
    private Table historyTable;
    private final ProcessDefinition definition;

    public VersionCommentDialog(ProcessDefinition definition) {
        super(Display.getCurrent().getActiveShell());
        this.definition = definition;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Localization.getString("label.menu.versionCommentAction"));
        shell.setSize(720, 400);
        Rectangle displayRectangle = shell.getDisplay().getPrimaryMonitor().getBounds();
        shell.setLocation((displayRectangle.width - shell.getBounds().width) / 2, (displayRectangle.height - shell.getBounds().height) / 2);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        addButton = createButton(parent, IDialogConstants.NO_ID, Localization.getString("button.add"), false);

        addButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                TableItem tableItem = new TableItem(historyTable, SWT.NONE, 0);
                tableItem.setText(0, CalendarUtil.format(new Date(), CalendarUtil.DATE_WITHOUT_TIME_FORMAT));
                tableItem.setText(1, System.getProperty("user.name").isEmpty() != true ? System.getProperty("user.name") : "New author");
                tableItem.setText(2, "New description.");
                setInputFieldsEnabled(true);
                historyTable.setEnabled(true);
                historyTable.setSelection(tableItem);
                fillDialogFields(tableItem);
                getButton(IDialogConstants.OK_ID).setEnabled(true);
                setDefaultButton(saveButton);
            }
        });

        super.createButtonsForButtonBar(parent);

        saveButton = getButton(IDialogConstants.OK_ID);
        saveButton.setText(Localization.getString("button.save"));
        setButtonLayoutData(saveButton);

        cancelButton = getButton(IDialogConstants.CANCEL_ID);
        cancelButton.setText(Localization.getString("button.cancel"));
        setButtonLayoutData(cancelButton);

        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setDate(this.getVersionDateAsString());
        versionInfo.setAuthor(this.getVersionAuthor());
        versionInfo.setComment(this.getVersionComment());
        int indexOfVersionInfo = definition.getVersionInfoListIndex(versionInfo);
        if (indexOfVersionInfo != -1 && definition.getVersionInfoList().get(indexOfVersionInfo).isSavedToFile() || historyTable.getItemCount() == 0) {
            saveButton.setEnabled(false);
            parent.getShell().setDefaultButton(cancelButton);
        } else {
            saveButton.setEnabled(true);
            parent.getShell().setDefaultButton(saveButton);
        }

    }

    protected void setDefaultButton(Button button) {
        Display.getCurrent().getActiveShell().setDefaultButton(button);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent = (Composite) super.createDialogArea(parent);
        parent.setLayout(new RowLayout(2));

        Composite composite = new Composite(parent, SWT.NONE);
        Composite compositeTable = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        composite.setLayout(gridLayout);
        composite.setLayoutData(new RowData(200, 300));
        GridLayout gridLayoutTable = new GridLayout(1, false);
        compositeTable.setLayout(gridLayoutTable);
        compositeTable.setLayoutData(new RowData(500, 300));

        Label dateLabel = new Label(composite, SWT.None);
        dateLabel.setText(Localization.getString("VersionCommentDialog.date"));
        dateControl = new DateTime(composite, SWT.DATE | SWT.BORDER);
        dateControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label authorLabel = new Label(composite, SWT.NONE);
        authorLabel.setText(Localization.getString("VersionCommentDialog.author"));
        authorText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        authorText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label commentLabel = new Label(composite, SWT.None);
        commentLabel.setText(Localization.getString("VersionCommentDialog.comment"));
        commentText = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 169;
        gridData.horizontalAlignment = SWT.FILL;
        commentText.setLayoutData(gridData);

        Label historyLabel = new Label(compositeTable, SWT.None);
        historyLabel.setText(Localization.getString("VersionCommentDialog.history"));

        historyTable = new Table(compositeTable, SWT.BORDER | SWT.FULL_SELECTION);
        historyTable.setLinesVisible(true);
        historyTable.setHeaderVisible(true);
        GridData gridDataHistoryTable = new GridData(SWT.FILL, SWT.CENTER, true, true);
        gridDataHistoryTable.horizontalAlignment = SWT.FILL;
        gridDataHistoryTable.verticalAlignment = SWT.FILL;
        historyTable.setLayoutData(gridDataHistoryTable);

        String[] headers = { Localization.getString("VersionCommentDialog.history.date"),
                Localization.getString("VersionCommentDialog.history.author"), Localization.getString("VersionCommentDialog.history.comment") };

        for (String header : headers) {
            TableColumn column = new TableColumn(historyTable, SWT.NONE);
            column.setText(header);
        }

        historyTable.getColumn(0).setWidth(80);
        historyTable.getColumn(1).setWidth(120);
        historyTable.getColumn(2).setWidth(280);

        historyTable.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                TableItem[] selectedItems = historyTable.getSelection();
                fillDialogFields(selectedItems[0]);
                VersionInfo versionInfo = new VersionInfo();
                versionInfo.setDate(selectedItems[0].getText(0));
                versionInfo.setAuthor(selectedItems[0].getText(1));
                versionInfo.setComment(selectedItems[0].getText(2));

                int indexOfVersionInfo = definition.getVersionInfoListIndex(versionInfo);
                if (indexOfVersionInfo != -1 && definition.getVersionInfoList().get(indexOfVersionInfo).isSavedToFile()) {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(true);

                }
            }
        });

        setInputFieldsEnabled(false);
        historyTable.setEnabled(false);

        ArrayList<VersionInfo> versionInfoList = definition.getVersionInfoList();
        if (versionInfoList.size() > 0) {
            setInputFieldsEnabled(true);
            historyTable.setEnabled(true);
            for (int i = versionInfoList.size() - 1; i >= 0; --i) {
                TableItem item = new TableItem(historyTable, SWT.NONE);
                item.setText(0, versionInfoList.get(i).getDateAsString());
                item.setText(1, versionInfoList.get(i).getAuthor());
                item.setText(2, versionInfoList.get(i).getComment());
            }
            historyTable.setSelection(0);
            fillDialogFields(historyTable.getItem(0));

        } else {
            setDefaultButton(getButton(IDialogConstants.CANCEL_ID));
        }

        return parent;

    }

    protected void fillDialogFields(TableItem item) {
        String date = item.getText(0);
        String author = item.getText(1);
        String comment = item.getText(2);

        setVersionDate(date);
        setVersionAuthor(author);
        setVersionComment(comment);
    }

    @Override
    protected void okPressed() {
        saveTableItemsToDefinition();
        definition.setDirty();
        this.close();
    }

    protected void saveTableItemsToDefinition() {
        int selectionIndex = historyTable.getSelectionIndex();
        TableItem tableItem = historyTable.getItem(selectionIndex);
        VersionInfo previousVersionInfo = new VersionInfo(tableItem.getText(0), tableItem.getText(1), tableItem.getText(2));
        int indexOfPreviousVersionInfo = definition.getVersionInfoListIndex(previousVersionInfo);

        tableItem.setText(0, this.getVersionDateAsString());
        tableItem.setText(1, this.getVersionAuthor());
        tableItem.setText(2, this.getVersionComment());

        for (int i = 0; i < historyTable.getItems().length; i++) {
            VersionInfo versionInfo = new VersionInfo();
            versionInfo.setDate(historyTable.getItems()[i].getText(0));
            versionInfo.setAuthor(historyTable.getItems()[i].getText(1));
            versionInfo.setComment(historyTable.getItems()[i].getText(2));

            if (indexOfPreviousVersionInfo != -1 && i == selectionIndex) {
                definition.setVersionInfoByIndex(indexOfPreviousVersionInfo, versionInfo);
            } else {
                if (definition.getVersionInfoListIndex(versionInfo) == -1) {
                    definition.addToVersionInfoList(versionInfo);
                }
            }
        }
    }

    protected void setVersionDate(Calendar cal) {
        dateControl.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    protected void setVersionDate(String stringCal) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(CalendarUtil.convertToDate(stringCal, CalendarUtil.DATE_WITHOUT_TIME_FORMAT));
        dateControl.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    protected Calendar getVersionDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, dateControl.getYear());
        cal.set(Calendar.MONTH, dateControl.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, dateControl.getDay());

        return cal;
    }

    protected String getVersionDateAsString() {
        String result = "";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, dateControl.getYear());
        cal.set(Calendar.MONTH, dateControl.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, dateControl.getDay());
        result = CalendarUtil.format(cal, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
        return result;
    }

    protected void setVersionAuthor(String author) {
        authorText.setText(author);
    }

    protected String getVersionAuthor() {
        return authorText.getText();
    }

    protected void setVersionComment(String comment) {
        commentText.setText(comment);
    }

    protected String getVersionComment() {
        return commentText.getText();
    }

    protected void setInputFieldsEnabled(boolean isEnabled) {
        dateControl.setEnabled(isEnabled);
        authorText.setEnabled(isEnabled);
        commentText.setEnabled(isEnabled);
    }

}