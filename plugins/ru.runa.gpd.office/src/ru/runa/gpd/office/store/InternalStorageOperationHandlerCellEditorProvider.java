package ru.runa.gpd.office.store;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
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
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider.ConstructorComposite;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GraphElementAware;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.model.VariableUserTypeNameAware;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputComposite;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.externalstorage.ConstraintsCompositeBuilder;
import ru.runa.gpd.office.store.externalstorage.DeleteConstraintsComposite;
import ru.runa.gpd.office.store.externalstorage.InsertConstraintsComposite;
import ru.runa.gpd.office.store.externalstorage.InternalStorageDataModel;
import ru.runa.gpd.office.store.externalstorage.PredicateCompositeDelegateBuilder;
import ru.runa.gpd.office.store.externalstorage.ProcessDefinitionVariableProvider;
import ru.runa.gpd.office.store.externalstorage.SelectConstraintsComposite;
import ru.runa.gpd.office.store.externalstorage.UpdateConstraintsComposite;
import ru.runa.gpd.office.store.externalstorage.VariableProvider;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.util.EmbeddedFileUtils;

public class InternalStorageOperationHandlerCellEditorProvider extends XmlBasedConstructorProvider<InternalStorageDataModel> {
    public static final String INTERNAL_STORAGE_DATASOURCE_PATH = "datasource:InternalStorage";

    @Override
    public void onDelete(Delegable delegable) {
        try {
            final InternalStorageDataModel model = fromXml(delegable.getDelegationConfiguration());
            EmbeddedFileUtils.deleteProcessFile(model.getInOutModel().inputPath);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Template file deletion", e);
        }
    }

    @Override
    protected String getTitle() {
        return Messages.getString("InternalStorageHandlerConfig.title");
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, InternalStorageDataModel model) {
        final boolean isUseExternalStorageIn = (delegable instanceof StorageAware) ? ((StorageAware) delegable).isUseExternalStorageIn() : false;
        final boolean isUseExternalStorageOut = (delegable instanceof StorageAware) ? ((StorageAware) delegable).isUseExternalStorageOut() : false;

        Optional<ProcessDefinition> processDefinition = Optional.empty();
        if (delegable instanceof ProcessDefinitionAware) {
            processDefinition = Optional.ofNullable(((ProcessDefinitionAware) delegable).getProcessDefinition());
        }
        if (!processDefinition.isPresent() && delegable instanceof VariableContainer) {
            processDefinition = ((VariableContainer) delegable).getVariables(false, true).stream().map(variable -> variable.getProcessDefinition())
                    .findAny();
        }

        if (delegable instanceof StorageAware) {
            if (delegable instanceof VariableUserTypeNameAware) {
                return new ConstructorView(parent, delegable, model,
                        new ProcessDefinitionVariableProvider(
                                processDefinition.orElseThrow(() -> new IllegalStateException("process definition unavailable"))),
                        isUseExternalStorageIn, isUseExternalStorageOut,
                        new VariableUserTypeInfo(true, ((VariableUserTypeNameAware) delegable).getUserTypeName())).build();
            }
            return new ConstructorView(parent, delegable, model,
                    new ProcessDefinitionVariableProvider(
                            processDefinition.orElseThrow(() -> new IllegalStateException("process definition unavailable"))),
                    isUseExternalStorageIn, isUseExternalStorageOut).build();
        } else {
        	return new BotConstructorView(parent, delegable, model).build();
            // TODO 1506 Реализовать VariableProvider для параметров бота
            //throw new UnsupportedOperationException("VariableProvider is not realized for " + delegable.getClass().getName());
        }
    }

    @Override
    protected InternalStorageDataModel createDefault() {
        return new InternalStorageDataModel(FilesSupplierMode.BOTH);
    }

    @Override
    protected InternalStorageDataModel fromXml(String xml) throws Exception {
        return InternalStorageDataModel.fromXml(xml);
    }

    @Override
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) throws Exception {
        final String configuration = delegable.getDelegationConfiguration();
        if (configuration.trim().isEmpty()) {
            errors.add(ValidationError.createError(
                    delegable instanceof GraphElementAware ? ((GraphElementAware) delegable).getGraphElement() : ((GraphElement) delegable),
                    Messages.getString("model.validation.xlsx.constraint.variable.empty")));
            return false;
        }
        return super.validateValue(delegable, errors);
    }

    @Override
    protected boolean validateModel(Delegable delegable, InternalStorageDataModel model, List<ValidationError> errors) {
        final GraphElement graphElement = delegable instanceof GraphElementAware ? ((GraphElementAware) delegable).getGraphElement()
                : ((GraphElement) delegable);
        if (delegable instanceof GraphElementAware) {
            model.setMode(FilesSupplierMode.IN);
        }
        model.validate(graphElement, errors);
        return super.validateModel(delegable, model, errors);
    }

    @Override
    protected Point getDialogInitialSize() {
        return new Point(800, 600);
    }

    private class ConstructorView extends ConstructorComposite {
        private final VariableProvider variableProvider;
        
        private final boolean isUseExternalStorageIn;
        private final boolean isUseExternalStorageOut;

        private StorageConstraintsModel constraintsModel;
        private VariableUserTypeInfo variableUserTypeInfo = new VariableUserTypeInfo(false, "");

        private ConstraintsCompositeBuilder constraintsCompositeBuilder;
        
        public ConstructorView(Composite parent, Delegable delegable, InternalStorageDataModel model, VariableProvider variableProvider,
                boolean isUseExternalStorageIn, boolean isUseExternalStorageOut) {
            super(parent, delegable, model);
            this.variableProvider = variableProvider;
            this.isUseExternalStorageIn = isUseExternalStorageIn;
            this.isUseExternalStorageOut = isUseExternalStorageOut;
            model.getInOutModel().inputPath = INTERNAL_STORAGE_DATASOURCE_PATH;
            setLayout(new GridLayout(2, false));
        }

        public ConstructorView(Composite parent, Delegable delegable, InternalStorageDataModel model, VariableProvider variableProvider,
                boolean isUseExternalStorageIn, boolean isUseExternalStorageOut, VariableUserTypeInfo variableUserTypeInfo) {
            this(parent, delegable, model, variableProvider, isUseExternalStorageIn, isUseExternalStorageOut);
            this.variableUserTypeInfo = variableUserTypeInfo;
        }

        public ConstructorView build() {
            buildFromModel();
            return this;
        }

        @Override
        protected void buildFromModel() {
            initConstraintsModel();
            for (Control control : getChildren()) {
                control.dispose();
            }

            if (constraintsModel.getSheetName() != null && !constraintsModel.getSheetName().isEmpty()) {
                final VariableUserType userType = variableProvider.getUserType(constraintsModel.getSheetName());
                variableUserTypeInfo.setVariableTypeName(userType != null ? userType.getName() : "");

                if (variableUserTypeInfo.isImmutable() && !variableUserTypeInfo.getVariableTypeName().equals(constraintsModel.getSheetName())) {
                    constraintsModel.setQueryString("");
                }
            }

            new Label(this, SWT.NONE).setText(Messages.getString("label.ExecutionAction"));
            if (isUseExternalStorageIn) {
                SwtUtils.createLabel(this, QueryType.SELECT.name());
                constraintsModel.setQueryType(QueryType.SELECT);
                model.setMode(FilesSupplierMode.BOTH);
            } else {
                addActionCombo(isUseExternalStorageIn, isUseExternalStorageOut);
            }

            new Label(this, SWT.NONE).setText(Messages.getString("label.DataType"));
            if (variableUserTypeInfo.isImmutable()) {
                SwtUtils.createLabel(this, variableUserTypeInfo.getVariableTypeName());
                constraintsModel.setSheetName(variableUserTypeInfo.getVariableTypeName());
                constraintsModel.setVariableName(null);
                model.setMode(FilesSupplierMode.IN);
            } else {
                addDataTypeCombo();
            }

            initConstraintsCompositeBuilder();
            if (constraintsCompositeBuilder != null) {
                constraintsCompositeBuilder.clearConstraints();
                new Label(this, SWT.NONE);
                constraintsCompositeBuilder.build();
            }

            ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
            this.layout(true, true);
            this.redraw();
        }

        private void initConstraintsModel() {
            if (constraintsModel != null && model.constraints.get(0) == constraintsModel) {
                return;
            }

            if (!model.constraints.isEmpty()) {
                Preconditions.checkState(model.constraints.size() == 1, "Expected model.constraints.size() == 1, actual " + model.constraints.size());
                constraintsModel = Iterables.getOnlyElement(model.constraints);
            } else {
                constraintsModel = new StorageConstraintsModel(StorageConstraintsModel.ATTR, QueryType.SELECT);
                model.constraints.add(constraintsModel);
            }
        }

        private void initConstraintsCompositeBuilder() {
            if (constraintsModel.getQueryType() != null) {
                switch (constraintsModel.getQueryType()) {
                case INSERT:
                    constraintsCompositeBuilder = new InsertConstraintsComposite(this, SWT.NONE, constraintsModel, variableProvider,
                            variableUserTypeInfo.getVariableTypeName());
                    break;
                case SELECT:
                    constraintsCompositeBuilder = new PredicateCompositeDelegateBuilder(this, SWT.NONE, constraintsModel, variableProvider,
                            variableUserTypeInfo.getVariableTypeName(), new SelectConstraintsComposite(this, SWT.NONE, constraintsModel,
                                    variableProvider, variableUserTypeInfo, model.getInOutModel()));
                    break;
                case UPDATE:
                    constraintsCompositeBuilder = new PredicateCompositeDelegateBuilder(this, SWT.NONE, constraintsModel, variableProvider,
                            variableUserTypeInfo.getVariableTypeName(), new UpdateConstraintsComposite(this, SWT.NONE, constraintsModel,
                                    variableProvider, variableUserTypeInfo.getVariableTypeName()));
                    break;
                case DELETE:
                    constraintsCompositeBuilder = new PredicateCompositeDelegateBuilder(this, SWT.NONE, constraintsModel, variableProvider,
                            variableUserTypeInfo.getVariableTypeName(), new DeleteConstraintsComposite(this, SWT.NONE, constraintsModel,
                                    variableProvider, variableUserTypeInfo.getVariableTypeName()));
                    break;
                }
            }
        }

        private void addDataTypeCombo() {
            final Combo combo = new Combo(this, SWT.READ_ONLY);
            variableProvider.complexUserTypeNames().collect(Collectors.toSet()).forEach(combo::add);
            combo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
                final String text = combo.getText();
                if (Strings.isNullOrEmpty(text)) {
                    return;
                }
                variableUserTypeInfo.setVariableTypeName(text);
                constraintsModel.setSheetName(variableUserTypeInfo.getVariableTypeName());
                if (constraintsCompositeBuilder != null) {
                    constraintsCompositeBuilder.onChangeVariableTypeName(variableUserTypeInfo.getVariableTypeName());
                }
                buildFromModel();
            }));

            final VariableUserType userType = variableProvider.getUserType(constraintsModel.getSheetName());
            if (userType != null) {
                combo.setText(userType.getName());
                variableUserTypeInfo.setVariableTypeName(userType.getName());
            }
        }

        private void addActionCombo(boolean isUseExternalStorageIn, boolean isUseExternalStorageOut) {
            final Combo combo = new Combo(this, SWT.READ_ONLY);
            final List<QueryType> types = QueryType.byIntent(isUseExternalStorageIn, isUseExternalStorageOut);
            for (QueryType type : types) {
                combo.add(type.name());
            }

            combo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
                String text = combo.getText();
                if (Strings.isNullOrEmpty(text)) {
                    return;
                }
                constraintsModel.setQueryType(QueryType.valueOf(combo.getText()));
                model.setMode(constraintsModel.getQueryType().equals(QueryType.SELECT) ? FilesSupplierMode.BOTH : FilesSupplierMode.IN);
                model.getInOutModel().outputVariable = null;
                buildFromModel();
                if (constraintsCompositeBuilder != null) {
                    constraintsCompositeBuilder.clearConstraints();
                }
            }));

            if (constraintsModel.getQueryType() != null && types.contains(constraintsModel.getQueryType())) {
                combo.setText(constraintsModel.getQueryType().name());
            } else {
                combo.setText(types.get(0).name());
                constraintsModel.setQueryType(types.get(0));
            }
        }
    }

    private class BotConstructorView extends ConstructorComposite {
    	private List<String> operators = Lists.newArrayList("==", "!=", ">=", "<=", ">", "<", "like", "and", "or");
        private Text queryText;
        private Label warning;
        private Color darkRed = new Color(null, 128, 0, 0);

        public BotConstructorView(Composite parent, Delegable delegable, InternalStorageDataModel model) {
            super(parent, delegable, model);
            setLayout(new GridLayout(3, false));
            buildFromModel();
        }

        public BotConstructorView build() {
            buildFromModel();
            return this;
        }

		@Override
        public void dispose() {
            if (!darkRed.isDisposed()) {
                darkRed.dispose();
            }
            super.dispose();
        }

        private void validateCondition() {
            warning.setText("");
            if (queryText != null && !queryText.isDisposed()) {
                Set<Integer> indexes = Sets.newHashSet();
                String queryString = queryText.getText().toLowerCase();
                for (String operator : operators) {
                    int index = -1;
                    while ((index = queryString.indexOf(operator, index + 1)) > 0) {
                        if (!indexes.contains(index) && (queryString.charAt(index - 1) != ' '
                                || index < queryString.length() - operator.length() && queryString.charAt(index + operator.length()) != ' ')) {
                            warning.setText(Messages.getString("query.operators.warning"));
                            return;
                        }
                        indexes.add(index);
                    }
                }
            }
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
                    queryText = new Text(this, SWT.BORDER);
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
                            validateCondition();
                        }
                    });
                } else {
                    new Label(this, SWT.NONE);
                }

                SwtUtils.createLink(this, Messages.getString("label.AddVar"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        model.constraints.add(new StorageConstraintsModel(StorageConstraintsModel.ATTR, queryType));
                        buildFromModel();
                    }
                }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                new Label(this, SWT.NONE);
                warning = new Label(this, SWT.NONE);
                warning.setForeground(darkRed);
                warning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
                validateCondition();
                model.getInOutModel().canWorkWithDataSource = true;
                
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
                super(BotConstructorView.this, SWT.NONE);
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
                if (queryType.equals(QueryType.SELECT)) {
                    return super.getTypeFilters();
                }
                return null;
            }

            @Override
            public String getTitle() {
                return Messages.getString("label.Arrtibute");
            }
        }
    }
    public static class VariableUserTypeInfo {
        private final boolean isImmutable;
        private String variableTypeName;

        public VariableUserTypeInfo(boolean isConst, String variableTypeName) {
            this.isImmutable = isConst;
            this.variableTypeName = variableTypeName;
        }

        public String getVariableTypeName() {
            return variableTypeName;
        }

        public void setVariableTypeName(String variableTypeName) {
            if (isImmutable) {
                return;
            }
            this.variableTypeName = variableTypeName;
        }

        public boolean isImmutable() {
            return isImmutable;
        }
    }
}
