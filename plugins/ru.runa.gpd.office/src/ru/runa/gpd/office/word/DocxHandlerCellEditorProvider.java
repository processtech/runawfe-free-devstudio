package ru.runa.gpd.office.word;

import com.google.common.base.Strings;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.bot.IBotFileSupportProvider;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputComposite;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.gpd.util.XmlUtil;

public class DocxHandlerCellEditorProvider extends XmlBasedConstructorProvider<DocxModel> implements IBotFileSupportProvider {

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, DocxModel model) {
        return new ConstructorView(parent, delegable, model, null);
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, DocxModel model,
            DialogEnhancementMode dialogEnhancementMode) {
        return new ConstructorView(parent, delegable, model, dialogEnhancementMode);
    }

    @Override
    public Object getConfigurationValue(Delegable delegable, String valueId) throws Exception {
        DocxModel docxModel = fromXml(delegable.getDelegationConfiguration());
        if (0 == valueId.compareTo(DocxDialogEnhancementMode.InputPathId)) {
            return docxModel.getInOutModel().inputPath;
        }
        return null;
    }

    @Override
    protected String getTitle() {
        return Messages.getString("DocxActionHandlerConfig.title");
    }

    @Override
    protected boolean validateModel(Delegable delegable, DocxModel model, List<ValidationError> errors) {
        GraphElement graphElement = ((GraphElement) delegable);
        model.validate(graphElement, errors);
        return super.validateModel(delegable, model, errors);
    }

    @Override
    protected DocxModel createDefault() {
        DocxModel model = new DocxModel();
        return model;
    }

    @Override
    protected DocxModel fromXml(String xml) throws Exception {
        return DocxModel.fromXml(xml);
    }

    @Override
    public void onDelete(Delegable delegable) {
        try {
            DocxModel model = fromXml(delegable.getDelegationConfiguration());
            EmbeddedFileUtils.deleteProcessFile(model.getInOutModel().inputPath);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Failed to delete embedded file in " + delegable, e);
        }
    }

    @Override
    public void onCopy(IFolder sourceFolder, Delegable source, IFolder targetFolder, Delegable target) {
        super.onCopy(sourceFolder, source, targetFolder, target);
        try {
            DocxModel model = fromXml(source.getDelegationConfiguration());
            if (EmbeddedFileUtils.isProcessFile(model.getInOutModel().inputPath)) {
                model.getInOutModel().inputPath = EmbeddedFileUtils.copyProcessFile(
                        sourceFolder, source, model.getInOutModel().inputPath, targetFolder, target);
                target.setDelegationConfiguration(model.toString());
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Failed to copy embedded file for " + target, e);
        }
    }

    private class ConstructorView extends ConstructorComposite {

        final private DialogEnhancementMode dialogEnhancementMode;

        public ConstructorView(Composite parent, Delegable delegable, DocxModel model, DialogEnhancementMode dialogEnhancementMode) {
            super(parent, delegable, model);
            setLayout(new GridLayout(2, false));
            this.dialogEnhancementMode = dialogEnhancementMode;
            buildFromModel();
        }

        @Override
        protected void buildFromModel() {
            try {
                for (Control control : getChildren()) {
                    control.dispose();
                }

                if (dialogEnhancementMode != null && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
                    ((DocxDialogEnhancementMode) dialogEnhancementMode).docxModel = model;
                }

                if (null == dialogEnhancementMode || dialogEnhancementMode.isOrDefault(DocxDialogEnhancementMode.DOCX_SHOW_OUTPUT)) {
                    final Button strict = new Button(this, SWT.CHECK);
                    strict.setText(Messages.getString(null != dialogEnhancementMode ? "label.strict.dialogEnhancementMode" : "label.strict"));
                    strict.setSelection(model.isStrict());
                    strict.addSelectionListener(new LoggingSelectionAdapter() {

                        @Override
                        protected void onSelection(SelectionEvent e) throws Exception {
                            model.setStrict(strict.getSelection());

                            if (dialogEnhancementMode != null && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
                                ((DocxDialogEnhancementMode) dialogEnhancementMode).reloadBotTaskEditorXmlFromModel(model.toString(), null, null,
                                        null);
                            }
                        }
                    });
                    new Label(this, SWT.NONE);
                }

                FilesSupplierMode filesSupplierMode = FilesSupplierMode.BOTH;
                if (null == dialogEnhancementMode || (dialogEnhancementMode.isOrDefault(DocxDialogEnhancementMode.DOCX_SHOW_INPUT)
                        && dialogEnhancementMode.isOrDefault(DocxDialogEnhancementMode.DOCX_SHOW_OUTPUT))) {
                    filesSupplierMode = FilesSupplierMode.BOTH;
                } else if (dialogEnhancementMode.is(DocxDialogEnhancementMode.DOCX_SHOW_INPUT)) {
                    filesSupplierMode = FilesSupplierMode.IN;
                } else if (dialogEnhancementMode.is(DocxDialogEnhancementMode.DOCX_SHOW_OUTPUT)) {
                    filesSupplierMode = FilesSupplierMode.OUT;
                }

                new InputOutputComposite(this, delegable, model.getInOutModel(), filesSupplierMode, "docx", dialogEnhancementMode);

                int i = 0;
                for (DocxTableModel table : model.getTables()) {
                    addTableSection(table, i++);
                }

                Composite composite = getParent();

                if (composite instanceof ScrolledComposite) {
                    ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                }

                this.layout(true, true);
            } catch (Throwable e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        private void addTableSection(DocxTableModel tableModel, final int tableIndex) {
            Group group = new Group(this, SWT.NONE);
            group.setData(tableIndex);
            group.setText(Messages.getString("label.Table"));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            group.setLayoutData(data);
            group.setLayout(new GridLayout(2, false));
            final Text text = new Text(group, SWT.BORDER);
            text.setText(tableModel.getName());
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    model.getTables().get(tableIndex).setName(text.getText());
                }
            });
            SwtUtils.createLink(group, "[X]", new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.getTables().remove(tableIndex);
                    buildFromModel();
                }
            });
            Group pgroup = new Group(group, SWT.None);
            GridData pdata = new GridData(GridData.FILL_HORIZONTAL);
            pdata.horizontalSpan = 3;
            pgroup.setLayoutData(pdata);
            pgroup.setLayout(new GridLayout(2, false));
            Label l = new Label(pgroup, SWT.None);
            l.setText(Messages.getString("label.tableStyle"));
            final Text styleText = new Text(pgroup, SWT.BORDER);
            styleText.setText(tableModel.getStyleName());
            styleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            styleText.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    model.getTables().get(tableIndex).setStyleName(styleText.getText());
                }
            });
            final Button addBreak = new Button(pgroup, SWT.CHECK);
            addBreak.setText(Messages.getString("label.tableAddBreak"));
            addBreak.setSelection(model.getTables().get(tableIndex).isAddBreak());
            addBreak.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    model.getTables().get(tableIndex).setAddBreak(addBreak.getSelection());
                }
            });
            Composite columnsComposite = createParametersComposite(group, "label.DocxTableColumns", tableIndex);
            for (DocxColumnModel columnModel : tableModel.columns) {
                addColumnSection(columnsComposite, columnModel, tableIndex, tableModel.columns.indexOf(columnModel));
            }
        }

        private Composite createParametersComposite(Composite parent, String labelKey, final int tableIndex) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(3, false));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            composite.setLayoutData(data);
            Composite strokeComposite = new Composite(composite, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            strokeComposite.setLayoutData(data);
            strokeComposite.setLayout(new GridLayout(4, false));
            Label strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            data = new GridData();
            data.widthHint = 50;
            strokeLabel.setLayoutData(data);
            Label headerLabel = new Label(strokeComposite, SWT.NONE);
            headerLabel.setText(Messages.getString(labelKey));
            strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            strokeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            SwtUtils.createLink(strokeComposite, Localization.getString("button.add"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.getTables().get(tableIndex).addColumn();
                    buildFromModel();
                }
            });
            return composite;
        }

        private void addColumnSection(Composite parent, final DocxColumnModel columnModel, final int tableIndex, final int columnIndex) {
            final Combo combo = new Combo(parent, SWT.READ_ONLY);
            for (String variableName : delegable.getVariableNames(true, List.class.getName())) {
                combo.add(variableName);
            }
            combo.setText(columnModel.variable);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    columnModel.variable = combo.getText();
                }
            });
            if (columnIndex != 0) {
                SwtUtils.createLink(parent, Localization.getString("button.up"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.getTables().get(tableIndex).moveUpColumn(columnIndex);
                        buildFromModel();
                    }
                });
            } else {
                new Label(parent, SWT.NONE);
            }
            SwtUtils.createLink(parent, "[X]", new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.getTables().get(tableIndex).columns.remove(columnIndex);
                    buildFromModel();
                }
            });
        }
    }

    @Override
    public String getEmbeddedFileName(BotTask botTask) {
        String xml = botTask.getDelegationConfiguration();
        if (!XmlUtil.isXml(xml)) {
            return null;
        }
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        Element inputElement = root.element("input");
        if (inputElement != null) {
            String path = inputElement.attributeValue("path");
            if (EmbeddedFileUtils.isBotTaskFile(path)) {
                return EmbeddedFileUtils.getBotTaskFileName(path);
            }
        }
        return null;
    }

    @Override
    public void taskRenamed(BotTask botTask, String oldName, String newName) {
        String xml = botTask.getDelegationConfiguration();
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        Element inputElement = root.element("input");
        String oldPath = inputElement.attributeValue("path");
        if (!Strings.isNullOrEmpty(oldPath) && EmbeddedFileUtils.isBotTaskFile(oldPath)) {
            String oldEmbeddedFileName = EmbeddedFileUtils.getBotTaskFileName(oldPath);
            if (EmbeddedFileUtils.isBotTaskFileName(oldEmbeddedFileName, botTask.getName())) {
                oldName = EmbeddedFileUtils.generateBotTaskEmbeddedFileName(oldName);
                String newEmbeddedFileName = EmbeddedFileUtils.generateBotTaskEmbeddedFileName(newName)
                        + oldEmbeddedFileName.substring(oldName.length());
                String newPath = EmbeddedFileUtils.getBotTaskFilePath(newEmbeddedFileName);
                inputElement.addAttribute("path", newPath);
            }
        }
        String newXml = XmlUtil.toString(document); // Debug Here.
        botTask.setDelegationConfiguration(newXml);
    }

}
