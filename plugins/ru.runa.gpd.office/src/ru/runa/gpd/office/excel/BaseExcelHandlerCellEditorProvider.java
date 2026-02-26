package ru.runa.gpd.office.excel;

import com.google.common.base.Strings;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
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
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.bot.IBotFileSupportProvider;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputComposite;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.var.format.ListFormat;

public abstract class BaseExcelHandlerCellEditorProvider extends XmlBasedConstructorProvider<ExcelModel> implements IBotFileSupportProvider {
    protected abstract FilesSupplierMode getMode();

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, ExcelModel model) {
        return new ConstructorView(parent, delegable, model);
    }

    @Override
    protected ExcelModel createDefault() {
        return new ExcelModel(getMode());
    }

    @Override
    protected ExcelModel fromXml(String xml) throws Exception {
        return ExcelModel.fromXml(xml, getMode());
    }

    @Override
    protected boolean validateModel(Delegable delegable, ExcelModel model, List<ValidationError> errors) {
        GraphElement graphElement = (GraphElement) delegable;
        model.validate(graphElement, errors);
        return super.validateModel(delegable, model, errors);
    }

    @Override
    public void onDelete(Delegable delegable) {
        try {
            ExcelModel model = fromXml(delegable.getDelegationConfiguration());
            EmbeddedFileUtils.deleteProcessFile(model.getInOutModel().inputPath);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Template file deletion", e);
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
        if (inputElement != null) {
            String path = inputElement.attributeValue("path");
            if (!Strings.isNullOrEmpty(path) && EmbeddedFileUtils.isBotTaskFile(path)) {
                String oldFileName = EmbeddedFileUtils.getBotTaskFileName(path);
                if (EmbeddedFileUtils.isBotTaskFileName(oldFileName, botTask.getName())) {
                    String baseOldName = EmbeddedFileUtils.generateBotTaskEmbeddedFileName(oldName);
                    String newFileName = EmbeddedFileUtils.generateBotTaskEmbeddedFileName(newName) + oldFileName.substring(baseOldName.length());
                    inputElement.addAttribute("path", EmbeddedFileUtils.getBotTaskFilePath(newFileName));
                }
            }
        }
        botTask.setDelegationConfiguration(XmlUtil.toString(document));
    }

    private class ConstructorView extends ConstructorComposite {
        public ConstructorView(Composite parent, Delegable delegable, ExcelModel model) {
            super(parent, delegable, model);
            setLayout(new GridLayout(3, false));
            buildFromModel();
        }

        @Override
        protected void buildFromModel() {
            try {
                cleanComposite();
                addActionLinks();
                new InputOutputComposite(this, delegable, model.getInOutModel(), getMode(), "xlsx", null);
                addConstraintsComposites();
                refreshLayout();
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("Build model failed", e);
            }
        }

        private void cleanComposite() {
            for (Control child : getChildren()) {
                child.dispose();
            }
        }

        private void addActionLinks() {
            createTypeLink("label.AddCell", ConstraintsModel.CELL);
            createTypeLink("label.AddRow", ConstraintsModel.ROW);
            createTypeLink("label.AddColumn", ConstraintsModel.COLUMN);
        }

        private void createTypeLink(String key, final int type) {
            SwtUtils.createLink(this, Messages.getString(key), new LoggingHyperlinkAdapter() {
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.constraints.add(new ConstraintsModel(type));
                    buildFromModel();
                }
            }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
        }

        private void addConstraintsComposites() {
            for (ConstraintsModel c : model.constraints) {
                if (c.type == ConstraintsModel.CELL) {
                    new CellComposite(c);
                } else if (c.type == ConstraintsModel.ROW) {
                    new RowComposite(c);
                } else if (c.type == ConstraintsModel.COLUMN) {
                    new ColumnComposite(c);
                }
            }
        }

        private void refreshLayout() {
            if (getParent() instanceof ScrolledComposite) {
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
            }
            this.layout(true, true);
        }

        public abstract class ConstraintsComposite extends Composite {
            protected final ConstraintsModel cmodel;
            protected Button udtButton;
            protected Group group;

            ConstraintsComposite(ConstraintsModel m) {
                super(ConstructorView.this, SWT.NONE);
                this.cmodel = m;
                setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.CENTER, true, false, 3, 1));
                setLayout(new FillLayout(SWT.VERTICAL));
                group = new Group(this, SWT.None);
                group.setLayout(new GridLayout(3, false));
                group.setText(getTitle());
                initComposite();
            }

            private void initComposite() {
                createVariablePicker();
                createSheetPicker();
                createCoordinateInputs();
                if (cmodel.type != ConstraintsModel.CELL) {
                    createUdtSection();
                }
            }

            private void createVariablePicker() {
                Button btn = new Button(group, SWT.PUSH);
                btn.setText(Messages.getString("label.variable"));
                final Text txt = new Text(group, SWT.READ_ONLY | SWT.BORDER);
                txt.setText(Strings.nullToEmpty(cmodel.variableName));
                txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                btn.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(delegable.getVariableNames(true, getTypeFilters()));
                        dialog.setSelectedItem(cmodel.variableName);
                        String selected = dialog.openDialog();
                        if (selected != null) {
                            updateVariable(selected);
                            txt.setText(selected);
                        }
                    }
                });
                createDeleteLink();
            }

            private void updateVariable(String name) {
                cmodel.variableName = name;
                if (!isUdtVariable(name)) {
                    cmodel.columns.clear();
                }
                updateUIState();
            }

            private void createDeleteLink() {
                SwtUtils.createLink(group, "[X]", new LoggingHyperlinkAdapter() {
                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.remove(cmodel);
                        buildFromModel();
                    }
                });
            }

            private void createSheetPicker() {
                final Combo combo = new Combo(group, SWT.READ_ONLY);
                combo.add(Messages.getString("label.sheetByTitle"));
                combo.add(Messages.getString("label.sheetByIndex"));
                final Text text = new Text(group, SWT.BORDER);
                text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                boolean isName = !Strings.isNullOrEmpty(cmodel.sheetName);
                combo.select(isName ? 0 : 1);
                text.setText(isName ? cmodel.sheetName : String.valueOf(cmodel.sheetIndex));
                addSheetListeners(combo, text);
                new Label(group, SWT.NONE);
            }

            private void addSheetListeners(final Combo combo, final Text text) {
                text.addModifyListener(new LoggingModifyTextAdapter() {
                    @Override
                    protected void onTextChanged(ModifyEvent e) throws Exception {
                        if (combo.getSelectionIndex() == 0) {
                            cmodel.sheetName = text.getText();
                        } else {
                            cmodel.sheetName = "";
                            try {
                                cmodel.sheetIndex = Integer.parseInt(text.getText());
                            } catch (Exception ex) {
                                cmodel.sheetIndex = 1;
                            }
                        }
                    }
                });
            }

            private void createCoordinateInputs() {
                createNumericInput(getXcoordMessage(), true);
                createNumericInput(getYcoordMessage(), false);
            }

            private void createNumericInput(String label, final boolean isX) {
                new Label(group, SWT.None).setText(label);
                final Text text = new Text(group, SWT.BORDER);
                text.setText(String.valueOf(isX ? cmodel.getColumn() : cmodel.getRow()));
                text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                text.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent e) {
                        String input = text.getText();
                        if (!input.isEmpty() && input.matches("\\d+")) {
                            int val = Integer.parseInt(input);
                            if (val > 0) {
                                if (isX) {
                                    cmodel.setColumn(val);
                                } else {
                                    cmodel.setRow(val);
                                }
                            }
                        }
                    }
                });
                new Label(group, SWT.NONE);
            }

            private void createUdtSection() {
                Composite comp = new Composite(group, SWT.NONE);
                comp.setLayout(new GridLayout(1, false));
                comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.CENTER, true, false, 3, 1));
                udtButton = new Button(comp, SWT.PUSH);
                udtButton.setText(Messages.getString("label.ConfigureUdtFields"));
                udtButton.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        new ExcelColumnMappingDialog(getShell(), delegable, cmodel).open();
                    }
                });
                updateUIState();
            }

            private boolean isUdtVariable(String name) {
                if (Strings.isNullOrEmpty(name)) {
                    return false;
                }
                Variable var = VariableUtils.getVariableByName(((GraphElement) delegable).getProcessDefinition(), name);
                if (var != null && var.getFormatClassName().equals(ListFormat.class.getName())) {
                    String comp = var.getFormatComponentClassNames()[0];
                    return comp != null && ((GraphElement) delegable).getProcessDefinition().getVariableUserType(comp) != null;
                }
                return false;
            }

            protected void updateUIState() {
                if (udtButton != null) {
                    udtButton.setVisible(isUdtVariable(cmodel.variableName));
                    group.layout(true, true);
                }
            }

            protected String[] getTypeFilters() {
                return new String[] { List.class.getName() };
            }

            public abstract String getTitle();

            public abstract String getXcoordMessage();

            public abstract String getYcoordMessage();
        }

        public class CellComposite extends ConstraintsComposite {
            CellComposite(ConstraintsModel m) {
                super(m);
            }

            @Override
            protected String[] getTypeFilters() {
                return null;
            }

            @Override
            public String getTitle() {
                return Messages.getString("label.Cell");
            }

            @Override
            public String getXcoordMessage() {
                return Messages.getString("label.column");
            }

            @Override
            public String getYcoordMessage() {
                return Messages.getString("label.row");
            }
        }

        public class RowComposite extends ConstraintsComposite {
            RowComposite(ConstraintsModel m) {
                super(m);
            }

            @Override
            public String getTitle() {
                return Messages.getString("label.Row");
            }

            @Override
            public String getXcoordMessage() {
                return Messages.getString("label.xcoord_start");
            }

            @Override
            public String getYcoordMessage() {
                return Messages.getString("label.ycoord");
            }
        }

        public class ColumnComposite extends ConstraintsComposite {
            ColumnComposite(ConstraintsModel m) {
                super(m);
            }

            @Override
            public String getTitle() {
                return Messages.getString("label.Column");
            }

            @Override
            public String getXcoordMessage() {
                return Messages.getString("label.xcoord");
            }

            @Override
            public String getYcoordMessage() {
                return Messages.getString("label.ycoord_start");
            }
        }
    }
}