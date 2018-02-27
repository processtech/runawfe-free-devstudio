package ru.runa.gpd.office.store;

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
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputComposite;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.util.EmbeddedFileUtils;

import com.google.common.base.Strings;

public abstract class BaseCommonStorageHandlerCellEditorProvider extends XmlBasedConstructorProvider<DataModel> {
    protected abstract FilesSupplierMode getMode();

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, DataModel model) {
        return new ConstructorView(parent, delegable, model);
    }

    @Override
    protected boolean validateModel(Delegable delegable, DataModel model, List<ValidationError> errors) {
        GraphElement graphElement = ((GraphElement) delegable);
        model.validate(graphElement, errors);
        return super.validateModel(delegable, model, errors);
    }

    @Override
    protected DataModel createDefault() {
        return new DataModel(getMode());
    }

    @Override
    protected DataModel fromXml(String xml) throws Exception {
        return DataModel.fromXml(xml, getMode());
    }

    @Override
    public void onDelete(Delegable delegable) {
        try {
            DataModel model = fromXml(delegable.getDelegationConfiguration());
            EmbeddedFileUtils.deleteProcessFile(model.getInOutModel().inputPath);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Template file deletion", e);
        }
    }

    private class ConstructorView extends ConstructorComposite {

        public ConstructorView(Composite parent, Delegable delegable, DataModel model) {
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
                Label l = new Label(this, SWT.NONE);
                l.setText(Messages.getString("label.ExecutionAction"));
                addActionCombo();
                new Label(this, SWT.NONE);
                if (queryType != null && !queryType.equals(QueryType.INSERT)) {
                    l = new Label(this, SWT.NONE);
                    l.setText(Messages.getString("label.Query"));
                    final Text queryText = new Text(this, SWT.BORDER);
                    if (model != null && model.constraints != null && model.constraints.size() > 0) {
                        queryString = model.constraints.get(0).getQueryString();
                    }
                    if (!Strings.isNullOrEmpty(queryString)) {
                        queryText.setText(queryString);
                    }
                    queryText.addModifyListener(new ModifyListener() {

                        @Override
                        public void modifyText(ModifyEvent e) {
                            for (StorageConstraintsModel m : model.constraints) {
                                m.setQueryString(queryText.getText());
                            }
                        }
                    });
                } else {
                    new Label(this, SWT.NONE);
                }

                SWTUtils.createLink(this, Messages.getString("label.AddVar"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.add(new StorageConstraintsModel(StorageConstraintsModel.ATTR, queryType));
                        buildFromModel();
                    }
                }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                model.getInOutModel().canWorkWithDataSource = true;
                new InputOutputComposite(this, delegable, model.getInOutModel(), getMode(), "xlsx");
                for (StorageConstraintsModel c : model.constraints) {
                    new ArrtibuteComposite(c);
                }
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
                this.redraw();
            } catch (Throwable e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        private QueryType queryType;

        private String queryString;

        private void addActionCombo() {
            final Combo combo = new Combo(this, SWT.READ_ONLY);
            for (QueryType action : QueryType.values()) {
                combo.add(action.toString());
            }
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String text = combo.getText();
                    if (Strings.isNullOrEmpty(text)) {
                        return;
                    }
                    // if
                    // (!QueryType.valueOf(combo.getText()).equals(queryType)) {
                    // for (StorageConstraintsModel m : model.constraints) {
                    // if (m.getConditionModel() != null) {
                    // m.getConditionModel().getConditions().clear();
                    // }
                    // }
                    // }
                    queryType = QueryType.valueOf(combo.getText());
                    for (StorageConstraintsModel m : model.constraints) {
                        m.setQueryType(queryType);
                    }
                    buildFromModel();
                }
            });
            if (queryType == null) {
                for (StorageConstraintsModel m : model.constraints) {
                    queryType = m.getQueryType();
                    break;
                }
            }
            if (queryType != null) {
                combo.setText(queryType.toString());
            }
        }

        public abstract class ConstraintsComposite extends Composite {
            public final StorageConstraintsModel cmodel;

            ConstraintsComposite(StorageConstraintsModel m) {
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
                for (String variableName : delegable.getVariableNames(true, getTypeFilters())) {
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
        }

        public class ArrtibuteComposite extends ConstraintsComposite {
            ArrtibuteComposite(StorageConstraintsModel m) {
                super(m);
            }

            @Override
            protected String[] getTypeFilters() {
                return null;
            }

            @Override
            public String getTitle() {
                return Messages.getString("label.Arrtibute");
            }
        }
    }
}
