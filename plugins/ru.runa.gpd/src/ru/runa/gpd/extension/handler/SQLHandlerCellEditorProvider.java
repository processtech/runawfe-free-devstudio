package ru.runa.gpd.extension.handler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.SQLTasksModel.SQLQueryModel;
import ru.runa.gpd.extension.handler.SQLTasksModel.SQLQueryParameterModel;
import ru.runa.gpd.extension.handler.SQLTasksModel.SQLTaskModel;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
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
    protected Composite createConstructorComposite(Composite parent, IDelegable iDelegable, SQLTasksModel model) {
        return new ConstructorView(parent, iDelegable, model);
    }

    @Override
    protected String getTitle() {
        return Localization.getString("SQLActionHandlerConfig.title");
    }

    @Override
    protected int getSelectedTabIndex(IDelegable iDelegable, SQLTasksModel model) {
        return model.hasFields() ? 1 : 0;
    }

    private class ConstructorView extends ConstructorComposite {

        public ConstructorView(Composite parent, IDelegable iDelegable, SQLTasksModel model) {
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
                if (model.tasks.size() > 0) {
                    addTaskSection(model.getFirstTask());
                }
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
            } catch (Throwable e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        private void addTaskSection(SQLTaskModel taskModel) {
            Label label = new Label(this, SWT.NONE);
            label.setText(Localization.getString("label.DataSourceName"));
            final Text text = new Text(this, SWT.BORDER);
            text.setText(taskModel.dsName);
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    model.getFirstTask().dsName = text.getText();
                }
            });
            GridData data = new GridData();
            data.widthHint = 200;
            text.setLayoutData(data);
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

        private void addQuerySection(SQLQueryModel queryModel, final int queryIndex) {
            Group group = new Group(this, SWT.NONE);
            group.setData(queryIndex);
            group.setText(Localization.getString("label.SQLQuery"));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            group.setLayoutData(data);
            group.setLayout(new GridLayout(2, false));
            final Text text = new Text(group, SWT.BORDER);
            text.setText(queryModel.query);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    model.getFirstTask().queries.get(queryIndex).query = text.getText();
                }
            });
            SWTUtils.createLink(group, "[X]", new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.getFirstTask().deleteQuery(queryIndex);
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
            for (String variableName : iDelegable.getVariableNames(true)) {
                combo.add(variableName);
            }
            combo.setText(parameterModel.varName);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    parameterModel.varName = combo.getText();
                    parameterModel.swimlaneVar = iDelegable.getVariableNames(true, Executor.class.getName()).contains(parameterModel.varName);
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
                    model.getFirstTask().deleteQueryParameter(queryIndex, parameterModel.result, paramIndex);
                }
            });
        }
    }
}
