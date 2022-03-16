package ru.runa.gpd.ui.dialog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.dom4j.Document;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import com.google.common.base.Strings;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VersionInfo;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.lang.par.VersionCommentXmlContentProvider;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.commons.CalendarUtil;

public class VersionCommentDialog extends Dialog {

    private Button addButton;
    private Button editButton;
    private Button saveButton;
    private Button cancelButton;
    private DateTime dateControl;
    private Text authorText;
    private Text commentText;
    private Table historyTable;
    private final ProcessDefinition definition;
    private Date currentDateTime;
    private final IWorkbenchPage workbenchPage;

    public VersionCommentDialog(ProcessDefinition definition, IWorkbenchPage workbenchPage) {
        super(Display.getCurrent().getActiveShell());
        this.definition = definition;
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.workbenchPage = workbenchPage;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Localization.getString("label.menu.versionCommentAction"));
        Rectangle displayRectangle = shell.getDisplay().getPrimaryMonitor().getBounds();
        shell.setSize(displayRectangle.width - (displayRectangle.width / 100 * 33), displayRectangle.height - (displayRectangle.height / 100 * 25));
        shell.setLocation((displayRectangle.width - shell.getBounds().width) / 2, (displayRectangle.height - shell.getBounds().height) / 2);
        shell.setMinimumSize(640, 480);

    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        addButton = createButton(parent, IDialogConstants.NO_ID, Localization.getString("button.add"), false);

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
                fillDialogFields(tableItem);
                getButton(IDialogConstants.OK_ID).setEnabled(true);
                setDefaultButton(parent, saveButton);
            }
        });

        if (CommonPreferencePage.isEditingCommentHistoryXmlEnabled()) {
        	createXmlEditButton(parent);
        }
        
        super.createButtonsForButtonBar(parent);

        saveButton = getButton(IDialogConstants.OK_ID);
        saveButton.setText(Localization.getString("button.save"));
        setButtonLayoutData(saveButton);

        cancelButton = getButton(IDialogConstants.CANCEL_ID);
        cancelButton.setText(Localization.getString("button.cancel"));
        setButtonLayoutData(cancelButton);

        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setDateTime(currentDateTime);
        versionInfo.setAuthor(this.getVersionAuthor());
        versionInfo.setComment(this.getVersionComment());
        int indexOfVersionInfo = definition.getVersionInfoListIndex(versionInfo);
        if (indexOfVersionInfo != -1 && definition.getVersionInfoList().get(indexOfVersionInfo).isSavedToFile() || historyTable.getItemCount() == 0) {
            saveButton.setEnabled(false);
            setDefaultButton(parent, cancelButton);
        } else {
            saveButton.setEnabled(true);
            setDefaultButton(parent, saveButton);
        }

    }

    private void createXmlEditButton(Composite parent) {
        editButton = createButton(parent, IDialogConstants.NO_ID, Localization.getString("button.edit"), false);

        editButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                cancelButton.notifyListeners(SWT.Selection, new Event());
                initAndOpenVersionCommentXmlEditor();
            }
            
        });
    }
    
    private void initAndOpenVersionCommentXmlEditor() throws PartInitException {
        IFile file = IOUtils.getAdjacentFile(definition.getFile(), ParContentProvider.COMMENTS_FILE_NAME);
        IEditorPart editor = IDE.openEditor(workbenchPage, file, true);
        editor.addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(Object source, int propId) {
                if (propId == IEditorPart.PROP_DIRTY && !editor.isDirty()) {
                    definition.getVersionInfoList().clear();
                    // если при сохранении файл невалидный - то он перезапишется пустым
                    try {
                        ParContentProvider.rewriteVersionCommentXml(definition.getFile(), definition);
                    } catch (Exception e) {
                        VersionCommentXmlContentProvider contentProvider = new VersionCommentXmlContentProvider();
                        InputStream contentStream = null;
                        try {
                            Document document = contentProvider.save(definition);
                            byte[] contentBytes = XmlUtil.writeXml(document);
                            contentStream = new ByteArrayInputStream(contentBytes);
                            IOUtils.createOrUpdateFile(file, contentStream);
                        } catch (Exception ex) {
                            PluginLogger.logErrorWithoutDialog("Error saving blank version comment xml.", ex);
                        } finally {
                            if (contentStream != null) {
                                try {
                                    contentStream.close();
                                } catch (Exception ex) {
                                    PluginLogger.logErrorWithoutDialog("Error closing input stream.", ex); 
                                }
                            }
                        }
                        PluginLogger.logError("Error parsing version comment xml. " + file, e);
                    }
                    if (definition.getVersionInfoList().stream()
                            .anyMatch(item -> Strings.isNullOrEmpty(item.getAuthor()) || Strings.isNullOrEmpty(item.getComment()))) {
                        PluginLogger.logError("Error parsing version comment xml. One or more fields is empty " + file, new Exception());
                    }
                }
            }
        });
    }
    
    protected void setDefaultButton(Composite parent, Button button) {
        parent.getShell().setDefaultButton(button);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Rectangle displayRectangle = Display.getCurrent().getPrimaryMonitor().getBounds();
        parent = (Composite) super.createDialogArea(parent);
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
                fillDialogFields(selectedItems[0]);
                VersionInfo versionInfo = new VersionInfo();
                versionInfo.setDateTime(selectedItems[0].getText(0));
                versionInfo.setAuthor(selectedItems[0].getText(1));
                versionInfo.setComment(selectedItems[0].getText(2));
                int indexOfVersionInfo = definition.getVersionInfoListIndex(versionInfo);
                if (indexOfVersionInfo != -1 && definition.getVersionInfoList().get(indexOfVersionInfo).isSavedToFile()) {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                    dateControl.setEnabled(false);
                    authorText.setEnabled(false);
                    commentText.setEnabled(false);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                    dateControl.setEnabled(true);
                    authorText.setEnabled(true);
                    commentText.setEnabled(true);

                }
            }
        });

        setInputFieldsEnabled(false);
        historyTable.setEnabled(false);

        ArrayList<VersionInfo> versionInfoList = definition.getVersionInfoList();
        if (versionInfoList.size() > 0) {
            setInputFieldsEnabled(true);
            historyTable.setEnabled(true);
            try {
                for (int i = versionInfoList.size() - 1; i >= 0; --i) {
                    TableItem item = new TableItem(historyTable, SWT.NONE);
                    item.setText(0, versionInfoList.get(i).getDateTimeAsString());
                    item.setText(1, versionInfoList.get(i).getAuthor());
                    item.setText(2, versionInfoList.get(i).getComment());
                }
            } catch (IllegalArgumentException e) {
                try {
                    if (CommonPreferencePage.isEditingCommentHistoryXmlEnabled()) {
                        initAndOpenVersionCommentXmlEditor();
                    }
                } catch (Exception ex) {
                    PluginLogger.logErrorWithoutDialog("Error opening version comment xml editor ", ex);
                } finally {
                    PluginLogger.logError("Error parsing version comment xml " + definition.getName(), e);
                }
            }
            historyTable.setSelection(0);
            fillDialogFields(historyTable.getItem(0));
            currentDateTime = versionInfoList.get(versionInfoList.size() - 1).getDate().getTime();
            if (definition.getVersionInfoList().get(versionInfoList.size() - 1).isSavedToFile()) {
                dateControl.setEnabled(false);
                authorText.setEnabled(false);
                commentText.setEnabled(false);
            }
        } else {
            setDefaultButton(parent, getButton(IDialogConstants.CANCEL_ID));
            currentDateTime = new Date();
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

        tableItem.setText(0, this.getVersionDateTimeAsString());
        tableItem.setText(1, this.getVersionAuthor());
        tableItem.setText(2, this.getVersionComment());

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

}