package ru.runa.gpd.office.excel;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputComposite;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.util.EmbeddedFileUtils;

public abstract class BaseExcelHandlerCellEditorProvider extends XmlBasedConstructorProvider<ExcelModel> {
    protected abstract FilesSupplierMode getMode();

    @Override
    protected Composite createConstructorComposite(Composite parent, IDelegable iDelegable, ExcelModel model) {
        return new ConstructorView(parent, iDelegable, model);
    }

    @Override
    protected boolean validateModel(IDelegable iDelegable, ExcelModel model, List<ValidationError> errors) {
        GraphElement graphElement = ((GraphElement) iDelegable);
        model.validate(graphElement, errors);
        return super.validateModel(iDelegable, model, errors);
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
    public void onDelete(IDelegable iDelegable) {
        try {
            ExcelModel model = fromXml(iDelegable.getDelegationConfiguration());
            EmbeddedFileUtils.deleteProcessFile(model.getInOutModel().inputPath);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Template file deletion", e);
        }
    }

    private class ConstructorView extends ConstructorComposite {

        public ConstructorView(Composite parent, IDelegable iDelegable, ExcelModel model) {
            super(parent, iDelegable, model);
            setLayout(new GridLayout(3, false));
            buildFromModel();
        }

        @Override
        protected void buildFromModel() {
            try {
                for (Control control : getChildren()) {
                    control.dispose();
                }
                SWTUtils.createLink(this, Messages.getString("label.AddCell"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.add(new ConstraintsModel(ConstraintsModel.CELL));
                        buildFromModel();
                    }
                }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                SWTUtils.createLink(this, Messages.getString("label.AddRow"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.add(new ConstraintsModel(ConstraintsModel.ROW));
                        buildFromModel();
                    }
                }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                SWTUtils.createLink(this, Messages.getString("label.AddColumn"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.add(new ConstraintsModel(ConstraintsModel.COLUMN));
                        buildFromModel();
                    }
                }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                new InputOutputComposite(this, iDelegable, model.getInOutModel(), getMode(), "xlsx");
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
                Label l = new Label(group, SWT.NONE);
                l.setText(Messages.getString("label.variable"));
                final Combo combo = new Combo(group, SWT.READ_ONLY);
                for (String variableName : iDelegable.getVariableNames(true, getTypeFilters())) {
                    combo.add(variableName);
                }
                combo.setText(cmodel.variableName);
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        cmodel.variableName = combo.getText();
                    }
                });
                SWTUtils.createLink(group, "[X]", new LoggingHyperlinkAdapter() {

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
                l = new Label(group, SWT.None);
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
