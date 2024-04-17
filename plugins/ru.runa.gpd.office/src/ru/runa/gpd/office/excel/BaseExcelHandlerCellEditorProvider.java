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
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputComposite;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.gpd.util.XmlUtil;

public abstract class BaseExcelHandlerCellEditorProvider extends XmlBasedConstructorProvider<ExcelModel> implements IBotFileSupportProvider {
    protected abstract FilesSupplierMode getMode();

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, ExcelModel model) {
        return new ConstructorView(parent, delegable, model);
    }

    @Override
    protected boolean validateModel(Delegable delegable, ExcelModel model, List<ValidationError> errors) {
        GraphElement graphElement = ((GraphElement) delegable);
        model.validate(graphElement, errors);
        return super.validateModel(delegable, model, errors);
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
        String newXml = XmlUtil.toString(document);
        botTask.setDelegationConfiguration(newXml);
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
                for (Control control : getChildren()) {
                    control.dispose();
                }
                SwtUtils.createLink(this, Messages.getString("label.AddCell"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.add(new ConstraintsModel(ConstraintsModel.CELL));
                        buildFromModel();
                    }
                }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                SwtUtils.createLink(this, Messages.getString("label.AddRow"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.add(new ConstraintsModel(ConstraintsModel.ROW));
                        buildFromModel();
                    }
                }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                SwtUtils.createLink(this, Messages.getString("label.AddColumn"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.add(new ConstraintsModel(ConstraintsModel.COLUMN));
                        buildFromModel();
                    }
                }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                new InputOutputComposite(this, delegable, model.getInOutModel(), getMode(), "xlsx", null);
                for (ConstraintsModel c : model.constraints) {
                    switch (c.type) {
                    case ConstraintsModel.CELL:
                        new CellComposite(c);
                        break;
                    case ConstraintsModel.ROW:
                        new RowComposite(c);
                        break;
                    case ConstraintsModel.COLUMN:
                        new ColumnComposite(c);
                        break;
                    }
                }
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
                this.redraw();
            } catch (Throwable e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        public abstract class ConstraintsComposite extends Composite {
            public final ConstraintsModel cmodel;

            ConstraintsComposite(ConstraintsModel m) {
                super(ConstructorView.this, SWT.NONE);
                cmodel = m;
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                data.horizontalSpan = 3;
                setLayoutData(data);
                setLayout(new FillLayout(SWT.VERTICAL));
                Group group = new Group(this, SWT.None);
                group.setLayout(new GridLayout(3, false));
                group.setText(getTitle());
                Button button = new Button(group, SWT.PUSH);
                button.setText(Messages.getString("label.variable"));
                final Text text = new Text(group, SWT.READ_ONLY | SWT.BORDER);
                text.setText(cmodel.variableName);
                text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                button.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(delegable.getVariableNames(true, getTypeFilters()));
                        dialog.setSelectedItem(cmodel.variableName);
                        String variableName = dialog.openDialog();
                        if (variableName != null) {
                            cmodel.variableName = variableName;
                            text.setText(variableName);
                        }
                    };
                });
                SwtUtils.createLink(group, "[X]", new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.remove(cmodel);
                        buildFromModel();
                    }
                });
                final Combo sheetCombo = new Combo(group, SWT.READ_ONLY);
                sheetCombo.add(Messages.getString("label.sheetByTitle"));
                sheetCombo.add(Messages.getString("label.sheetByIndex"));
                final Text sheetText = new Text(group, SWT.BORDER);
                sheetText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                if (cmodel.sheetName != null && cmodel.sheetName.length() > 0) {
                    sheetCombo.select(0);
                    sheetText.setText(cmodel.sheetName);
                } else {
                    sheetCombo.select(1);
                    sheetText.setText("" + cmodel.sheetIndex);
                }
                sheetText.addModifyListener(new LoggingModifyTextAdapter() {

                    @Override
                    protected void onTextChanged(ModifyEvent e) throws Exception {
                        updateSheet(sheetCombo, sheetText);
                    }
                });
                sheetCombo.addSelectionListener(new LoggingSelectionAdapter() {

                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        updateSheet(sheetCombo, sheetText);
                    }
                });
                new Label(group, SWT.NONE);
                Label l = new Label(group, SWT.None);
                l.setText(getXcoordMessage());
                final Text tx = new Text(group, SWT.BORDER);
                tx.setText("" + cmodel.getColumn());
                tx.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                new Label(group, SWT.NONE);
                tx.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent arg0) {
                        try {
                            int x = Integer.parseInt(tx.getText());
                            if (x > 0 && x < 65535) {
                                cmodel.setColumn(x);
                            } else {
                                tx.setText("1");
                            }
                        } catch (Exception e) {
                            tx.setText("1");
                        }
                    }
                });
                l = new Label(group, SWT.None);
                l.setText(getYcoordMessage());
                final Text ty = new Text(group, SWT.BORDER);
                ty.setText("" + cmodel.getRow());
                ty.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                new Label(group, SWT.NONE);
                ty.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent arg0) {
                        try {
                            int y = Integer.parseInt(ty.getText());
                            if (y > 0 && y < 65535) {
                                cmodel.setRow(y);
                            } else {
                                ty.setText("1");
                            }
                        } catch (Exception e) {
                            ty.setText("1");
                        }
                    }
                });
                layout(true, true);
            }

            private void updateSheet(Combo combo, Text text) {
                if (combo.getSelectionIndex() == 0) {
                    cmodel.sheetName = text.getText();
                    cmodel.sheetIndex = 1;
                } else {
                    cmodel.sheetName = "";
                    try {
                        cmodel.sheetIndex = Integer.parseInt(text.getText());
                        if (cmodel.sheetIndex <= 0) {
                            cmodel.sheetIndex = 1;
                        }
                    } catch (Exception e) {
                        cmodel.sheetIndex = 1;
                        text.setText("1");
                    }
                }
            }

            protected String[] getTypeFilters() {
                return new String[] { List.class.getName() };
            }

            public abstract String getTitle();

            public String getXcoordMessage() {
                return Messages.getString("label.xcoord");
            }

            public String getYcoordMessage() {
                return Messages.getString("label.ycoord");
            }
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
            public String getYcoordMessage() {
                return Messages.getString("label.ycoord_start");
            }
        }
    }
}
