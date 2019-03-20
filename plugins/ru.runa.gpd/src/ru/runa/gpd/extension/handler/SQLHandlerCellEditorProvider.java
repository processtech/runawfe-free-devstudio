package ru.runa.gpd.extension.handler;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.springframework.util.StringUtils;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.handler.SQLTasksModel.SQLQueryModel;
import ru.runa.gpd.extension.handler.SQLTasksModel.SQLQueryParameterModel;
import ru.runa.gpd.extension.handler.SQLTasksModel.SQLTaskModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.SqlHighlightTextStyling;
import ru.runa.gpd.util.DataSourceUtils;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.DataSourceType;
import ru.runa.wfe.user.Executor;

public class SQLHandlerCellEditorProvider extends XmlBasedConstructorProvider<SQLTasksModel> {
    @Override
    protected SQLTasksModel createDefault() {
        return SQLTasksModel.createDefault();
    }

    @Override
    protected SQLTasksModel fromXml(String xml) throws Exception {
        return SQLTasksModel.fromXml(xml);
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, SQLTasksModel model) {
        return new ConstructorView(parent, delegable, model);
    }

    @Override
    protected String getTitle() {
        return Localization.getString("SQLActionHandlerConfig.title");
    }

    @Override
    protected int getSelectedTabIndex(Delegable delegable, SQLTasksModel model) {
        return model.hasFields() ? 1 : 0;
    }

    private class ConstructorView extends ConstructorComposite {

        public ConstructorView(Composite parent, Delegable delegable, SQLTasksModel model) {
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
                if (model.tasks.size() > 0) {
                    addTaskSection(model.getFirstTask());
                }
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
            } catch (Throwable e) {
                PluginLogger.logError("Cannot build model", e);
            }
        }

        private Combo cbDsTypes;

        private void addTaskSection(SQLTaskModel taskModel) {
            cbDsTypes = new Combo(this, SWT.READ_ONLY);
            cbDsTypes.add(Localization.getString("label.DataSourceJndiName"));
            cbDsTypes.add(Localization.getString("label.DataSourceJndiNameVariable"));
            cbDsTypes.add(Localization.getString("label.DataSourceName"));
            cbDsTypes.add(Localization.getString("label.DataSourceNameVariable"));
            cbDsTypes.select(0);

            final Composite values = new Composite(this, SWT.FILL);
            GridData gdValues = new GridData(GridData.FILL_HORIZONTAL);
            values.setLayoutData(gdValues);
            final StackLayout stackLayout = new StackLayout();
            values.setLayout(stackLayout);

            final Text txtJndiName = new Text(values, SWT.BORDER);
            txtJndiName.setLayoutData(new GridData(200, SWT.DEFAULT));
            txtJndiName.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    model.getFirstTask().dsName = DataSourceStuff.PATH_PREFIX_JNDI_NAME + txtJndiName.getText();
                }
            });
            final Combo cbVariables = createVariableSelector(values);
            final Combo cbDataSources = createDataSourceSelector(values);

            int colonIndex = taskModel.dsName.indexOf(':');
            if (colonIndex > 0) {
                String dsName = taskModel.dsName.substring(colonIndex + 1);
                if (taskModel.dsName.startsWith(DataSourceStuff.PATH_PREFIX_JNDI_NAME_VARIABLE)) {
                    cbDsTypes.select(1);
                    cbVariables.setText(dsName);
                    stackLayout.topControl = cbVariables;
                }
                else if (taskModel.dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)) {
                    cbDsTypes.select(2);
                    cbDataSources.setText(dsName);
                    stackLayout.topControl = cbDataSources;
                }
                else if (taskModel.dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE)) {
                    cbDsTypes.select(3);
                    cbVariables.setText(dsName);
                    stackLayout.topControl = cbVariables;
                }
                else {
                    cbDsTypes.select(0);
                    txtJndiName.setText(dsName);
                    stackLayout.topControl = txtJndiName;
                }
            } else {
                cbDsTypes.select(0);
                txtJndiName.setText(taskModel.dsName);
                stackLayout.topControl = txtJndiName;
            }
            values.layout();

            cbDsTypes.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    switch (((Combo) e.widget).getSelectionIndex()) {
                    case 0:
                        stackLayout.topControl = txtJndiName;
                        break;
                    case 1:
                    case 3:
                        stackLayout.topControl = cbVariables;
                        break;
                    case 2:
                        stackLayout.topControl = cbDataSources;
                        break;
                    }
                    values.layout();
                }

            });

            SWTUtils.createLink(this, Localization.getString("button.add") + " " + Localization.getString("label.SQLQuery"),
                    new LoggingHyperlinkAdapter() {

                        @Override
                        protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                            model.getFirstTask().addQuery();
                        }
                    }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
            for (SQLQueryModel queryModel : taskModel.queries) {
                addQuerySection(queryModel, taskModel.queries.indexOf(queryModel));
            }
        }

        private Combo createVariableSelector(Composite owner) {
            final Combo cb = new Combo(owner, SWT.READ_ONLY);
            for (String variableName : delegable.getVariableNames(false, String.class.getName())) {
                cb.add(variableName);
            }
            cb.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    model.getFirstTask().dsName = (cbDsTypes.getSelectionIndex() == 1 ?
                            DataSourceStuff.PATH_PREFIX_JNDI_NAME_VARIABLE : DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE) + cb.getText();
                }
            });
            cb.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    model.getFirstTask().dsName = (cbDsTypes.getSelectionIndex() == 1 ?
                            DataSourceStuff.PATH_PREFIX_JNDI_NAME_VARIABLE : DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE) + cb.getText();
                }
            });
            return cb;
        }

        private Combo createDataSourceSelector(Composite owner) {
            final Combo cb = new Combo(owner, SWT.NONE);
            for (IFile dsFile : DataSourceUtils.getDataSourcesByType(DataSourceType.JDBC, DataSourceType.JNDI)) {
                String dsName = dsFile.getName();
                cb.add(dsName.substring(0, dsName.length() - DataSourceStuff.DATA_SOURCE_FILE_SUFFIX.length()));
            }
            cb.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    model.getFirstTask().dsName = DataSourceStuff.PATH_PREFIX_DATA_SOURCE + cb.getText();
                }
            });
            cb.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    model.getFirstTask().dsName = DataSourceStuff.PATH_PREFIX_DATA_SOURCE + cb.getText();
                }
            });
            return cb;
        }

        private void addQuerySection(SQLQueryModel queryModel, final int queryIndex) {
            Group group = new Group(this, SWT.NONE);
            group.setData(queryIndex);
            group.setText(Localization.getString("label.SQLQuery"));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            group.setLayoutData(data);
            group.setLayout(new GridLayout(2, false));
            final StyledText text = new StyledText(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
            text.setText(queryModel.query);
            GridData textData = new GridData(GridData.FILL_BOTH);
            textData.heightHint = 100;
            text.setLayoutData(textData);
            text.addLineStyleListener(new SqlHighlightTextStyling());
            Composite rightPane = new Composite(group, SWT.NONE);
            rightPane.setLayout(new GridLayout());
            rightPane.setLayoutData(new GridData(GridData.FILL_VERTICAL));

            Label wrongIcon = new Label(rightPane, SWT.NULL);
            wrongIcon.setToolTipText(Localization.getString("SQLQuery.wrong.parameters"));
            group.setData(wrongIcon);

            new Label(rightPane, SWT.NULL).setLayoutData(new GridData(SWT.NULL, SWT.NULL, false, true));

            SWTUtils.createLink(rightPane, "[X]", new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.getFirstTask().deleteQuery(queryIndex);
                }
            });
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    model.getFirstTask().queries.get(queryIndex).query = text.getText();
                    validateQuery(model.getFirstTask().queries.get(queryIndex), wrongIcon);
                }
            });
            Composite paramsComposite = createParametersComposite(group, "label.SQLParams", queryIndex, false);
            for (SQLQueryParameterModel parameterModel : queryModel.params) {
                addParamSection(paramsComposite, parameterModel, queryIndex, queryModel.params.indexOf(parameterModel), true);
            }
            Composite resultsComposite = createParametersComposite(group, "label.SQLResults", queryIndex, true);
            for (SQLQueryParameterModel parameterModel : queryModel.results) {
                addParamSection(resultsComposite, parameterModel, queryIndex, queryModel.results.indexOf(parameterModel), false);
            }
            validateQuery(model.getFirstTask().queries.get(queryIndex), wrongIcon);
        }

        private void validateQuery(SQLQueryModel query, Label wrongIcon) {
            boolean queryIsValid = (StringUtils.countOccurrencesOf(query.query, "?")
                    + (query.query.trim().toLowerCase().startsWith("select ") ? 1 : 0)) == (query.params.size() + query.results.size());
            wrongIcon.setImage(queryIsValid ? null : SharedImages.getImage("icons/validation_error.gif"));
            wrongIcon.getParent().layout(true);
        }

        private Composite createParametersComposite(Composite parent, String labelKey, final int queryIndex, final boolean result) {
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
            headerLabel.setText(Localization.getString(labelKey));
            strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            strokeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            SWTUtils.createLink(strokeComposite, Localization.getString("button.add"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.getFirstTask().addQueryParameter(queryIndex, result);
                }
            });
            return composite;
        }

        private void addParamSection(Composite parent, final SQLQueryParameterModel parameterModel, final int queryIndex, final int paramIndex,
                boolean input) {
            final Combo combo = new Combo(parent, SWT.READ_ONLY);
            for (String variableName : delegable.getVariableNames(true)) {
                combo.add(variableName);
            }
            combo.setText(parameterModel.varName);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    parameterModel.varName = combo.getText();
                    parameterModel.swimlaneVar = delegable.getVariableNames(true, Executor.class.getName()).contains(parameterModel.varName);
                }
            });
            if (paramIndex != 0) {
                SWTUtils.createLink(parent, Localization.getString("button.up"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.getFirstTask().moveUpQueryParameter(queryIndex, parameterModel.result, paramIndex);
                    }
                });
            } else {
                new Label(parent, SWT.NONE);
            }
            SWTUtils.createLink(parent, "[X]", new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    validateQuery(model.getFirstTask().queries.get(queryIndex), (Label) parent.getParent().getData());
                    model.getFirstTask().deleteQueryParameter(queryIndex, parameterModel.result, paramIndex);
                }
            });
            validateQuery(model.getFirstTask().queries.get(queryIndex), (Label) parent.getParent().getData());
        }
    }
}
