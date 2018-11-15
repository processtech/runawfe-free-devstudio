package ru.runa.gpd.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VersionInfo;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.wfe.commons.CalendarUtil;

public class VersionCommentPage extends EditorPart implements PropertyChangeListener {

    private DateTime dateControl;
    private Text authorText;
    private Text commentText;
    private Table historyTable;
    private final ProcessDefinition definition;
    private Date currentDateTime;
    private Button addButton;
    private Button saveButton;
    
    public VersionCommentPage(ProcessDefinition definition) {
        this.definition = definition;
    }
    
    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        Rectangle displayRectangle = Display.getCurrent().getPrimaryMonitor().getBounds();
        parent.setLayout(new GridLayout(1, false));

        Composite composite0 = new Composite(parent, SWT.NONE);
        Composite composite1 = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout(2, false);
        composite0.setLayout(gridLayout);
        GridData gridData0 = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite0.setLayoutData(gridData0);

        composite1.setLayout(new GridLayout(1, false));
        GridData gridData1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite1.setLayoutData(gridData1);

        Composite composite2 = new Composite(composite0, SWT.NONE);
        composite2.setLayout(new GridLayout(1, false));
        GridData gridData2 = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gridData2.widthHint = 150;
        composite2.setLayoutData(gridData2);

        Composite composite3 = new Composite(composite0, SWT.NONE);
        composite3.setLayout(new GridLayout(1, false));
        GridData gridData3 = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite3.setLayoutData(gridData3);

        Label dateLabel = new Label(composite2, SWT.None);
        dateLabel.setText(Localization.getString("VersionCommentDialog.date"));
        dateControl = new DateTime(composite2, SWT.DATE);
        GridData dateGridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        dateControl.setLayoutData(dateGridData);

        Label authorLabel = new Label(composite2, SWT.NONE);
        authorLabel.setText(Localization.getString("VersionCommentDialog.author"));
        authorText = new Text(composite2, SWT.SINGLE | SWT.BORDER);
        authorText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        Composite compositePadding = new Composite(composite2, SWT.NONE);
        compositePadding.setLayout(new GridLayout(1, false));
        GridData gridDataPadding = new GridData(SWT.FILL, SWT.NONE, false, true);
        gridDataPadding.widthHint = displayRectangle.width / 100 * 15;
        composite2.setLayoutData(gridDataPadding);

        Label commentLabel = new Label(composite3, SWT.None);
        commentLabel.setText(Localization.getString("VersionCommentDialog.comment"));
        commentText = new Text(composite3, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = commentText.getLineHeight() * 10;
        gridData.horizontalAlignment = SWT.FILL;
        commentText.setLayoutData(gridData);

        Label historyLabel = new Label(composite1, SWT.None);
        historyLabel.setText(Localization.getString("VersionCommentDialog.history"));

        historyTable = new Table(composite1, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
        historyTable.setLinesVisible(true);
        historyTable.setHeaderVisible(true);

        GridData gridDataHistoryTable = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridDataHistoryTable.horizontalAlignment = SWT.FILL;
        gridDataHistoryTable.verticalAlignment = SWT.FILL;
        historyTable.setLayoutData(gridDataHistoryTable);
        
        Composite composite4 = new Composite(parent, SWT.FILL);
        composite4.setLayout(new GridLayout(2, false));
        
        addButton = new Button(composite4, SWT.PUSH);
        addButton.setData(Integer.valueOf(IDialogConstants.NO_ID));
        addButton.setText(Localization.getString("button.add"));
        
        addButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                TableItem tableItem = new TableItem(historyTable, SWT.NONE, 0);
                tableItem.setText(0, CalendarUtil.format(new Date(), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT));
                tableItem.setText(1, System.getProperty("user.name").isEmpty() != true ? System.getProperty("user.name") : "New author");
                tableItem.setText(2, "New description.");
                setInputFieldsEnabled(true);
                historyTable.setEnabled(true);
                historyTable.setSelection(tableItem);
                fillCommentFields(tableItem);
                saveButton.setEnabled(true);
            }
        });
        
        saveButton = new Button(composite4, SWT.PUSH);
        saveButton.setData(Integer.valueOf(IDialogConstants.OK_ID));
        saveButton.setText(Localization.getString("button.save"));
        saveButton.setEnabled(false);

        saveButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                int selectionIndex = historyTable.getSelectionIndex();
                TableItem tableItem = historyTable.getItem(selectionIndex);
                VersionInfo previousVersionInfo = new VersionInfo(tableItem.getText(0), tableItem.getText(1), tableItem.getText(2));
                int indexOfPreviousVersionInfo = definition.getVersionInfoListIndex(previousVersionInfo);

                tableItem.setText(0, getVersionDateTimeAsString());
                tableItem.setText(1, getVersionAuthor());
                tableItem.setText(2, getVersionComment());

                for (int i = 0; i < historyTable.getItems().length; i++) {
                    VersionInfo versionInfo = new VersionInfo();
                    versionInfo.setDateTime(historyTable.getItems()[i].getText(0));
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
                definition.setDirty();
            }
        });
           
        String[] headers = { Localization.getString("VersionCommentDialog.history.date"),
                Localization.getString("VersionCommentDialog.history.author"), Localization.getString("VersionCommentDialog.history.comment") };

        for (String header : headers) {
            TableColumn column = new TableColumn(historyTable, SWT.NONE);
            column.setText(header);
        }

        historyTable.getColumn(0).setWidth(displayRectangle.width / 100 * 10);
        historyTable.getColumn(1).setWidth(displayRectangle.width / 100 * 10);
        historyTable.getColumn(2).setWidth(displayRectangle.width / 100 * 50);

        historyTable.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                TableItem[] selectedItems = historyTable.getSelection();
                fillCommentFields(selectedItems[0]);
                VersionInfo versionInfo = new VersionInfo();
                versionInfo.setDateTime(selectedItems[0].getText(0));
                versionInfo.setAuthor(selectedItems[0].getText(1));
                versionInfo.setComment(selectedItems[0].getText(2));
                int indexOfVersionInfo = definition.getVersionInfoListIndex(versionInfo);
                if (indexOfVersionInfo != -1 && definition.getVersionInfoList().get(indexOfVersionInfo).isSavedToFile()) {
                    dateControl.setEnabled(false);
                    authorText.setEnabled(false);
                    commentText.setEnabled(false);
                    saveButton.setEnabled(false);
                } else {
                    dateControl.setEnabled(true);
                    authorText.setEnabled(true);
                    commentText.setEnabled(true);
                    saveButton.setEnabled(true);
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
                item.setText(0, versionInfoList.get(i).getDateTimeAsString());
                item.setText(1, versionInfoList.get(i).getAuthor());
                item.setText(2, versionInfoList.get(i).getComment());
            }
            historyTable.setSelection(0);
            fillCommentFields(historyTable.getItem(0));
            currentDateTime = versionInfoList.get(versionInfoList.size() - 1).getDate().getTime();
            if (definition.getVersionInfoList().get(versionInfoList.size() - 1).isSavedToFile()) {
                dateControl.setEnabled(false);
                authorText.setEnabled(false);
                commentText.setEnabled(false);
            }
        } else {
            currentDateTime = new Date();
        }
    }

    protected void fillCommentFields(TableItem item) {
        String date = item.getText(0);
        String author = item.getText(1);
        String comment = item.getText(2);

        setVersionDate(date);
        setVersionAuthor(author);
        setVersionComment(comment);
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
    
    protected String getVersionDateTimeAsString() {
        String result = "";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, dateControl.getYear());
        cal.set(Calendar.MONTH, dateControl.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, dateControl.getDay());
        result = CalendarUtil.format(cal, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
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
    
    @Override
    public void setFocus() {
        ArrayList<VersionInfo> versionInfoList = definition.getVersionInfoList();
        if (versionInfoList.size() > 0) {
            historyTable.removeAll();
            for (int i = versionInfoList.size() - 1; i >= 0; --i) {
                TableItem item = new TableItem(historyTable, SWT.NONE);
                item.setText(0, versionInfoList.get(i).getDateTimeAsString());
                item.setText(1, versionInfoList.get(i).getAuthor());
                item.setText(2, versionInfoList.get(i).getComment());
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

}