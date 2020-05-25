package ru.runa.gpd.ui.control;

import com.google.common.collect.Maps;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.control.ValidatorInfoControl.ParametersComposite;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.TimeInputDialog;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinition.Param;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;

public class FieldValidatorsPage extends Composite implements PropertyChangeListener {
    private final FormNode formNode;
    private final ProcessDefinition processDefinition;
    private TabFolder tabFolder;
    private CheckboxTableViewer variablesTableViewer;
    private CheckboxTableViewer swimlanesTableViewer;
    private Label warningLabel;
    private CheckboxTableViewer validatorsTableViewer;
    private ValidatorInfoControl infoGroup;
    private String warningMessage = "";
    private Map<String, Map<String, ValidatorConfig>> fieldConfigs;
    private boolean dirty;
    private Consumer<Boolean> dirtyCallback;

    public FieldValidatorsPage(Composite parent, FormNode formNode, FormNodeValidation validation, Consumer<Boolean> dirtyCallback) {
        super(parent, SWT.NONE);
        this.formNode = formNode;
        this.processDefinition = formNode.getProcessDefinition();
        this.fieldConfigs = validation.getFieldConfigs();
        this.dirtyCallback = dirtyCallback;
        List<String> undefinedValidators = new ArrayList<String>();
        for (String fieldName : fieldConfigs.keySet()) {
            if (!ValidatorConfig.GLOBAL_FIELD_ID.equals(fieldName)) {
                Map<String, ValidatorConfig> configs = fieldConfigs.get(fieldName);
                for (String validatorName : configs.keySet()) {
                    if (!ValidatorDefinitionRegistry.getValidatorDefinitions().containsKey(validatorName)) {
                        undefinedValidators.add(validatorName);
                    }
                }
            }
        }
        if (undefinedValidators.size() > 0) {
            warningMessage = undefinedValidators.toString();
        }
        initUi();
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String type = evt.getPropertyName();
        if (PropertyNames.PROPERTY_CHILDREN_CHANGED.equals(type)) {
            updateViewers();
        } else if (evt.getSource() instanceof Variable) {
            if (PropertyNames.PROPERTY_NAME.equals(type) || PropertyNames.PROPERTY_FORMAT.equals(type)) {
                if (evt.getSource() instanceof Swimlane) {
                    swimlanesTableViewer.refresh(evt.getSource());
                } else {
                    variablesTableViewer.refresh(evt.getSource());
                }
            }
        }
    }

    @Override
    public void dispose() {
        for (Variable variable : formNode.getVariables(false, true)) {
            variable.removePropertyChangeListener(this);
        }
        processDefinition.removePropertyChangeListener(this);
        super.dispose();
    }

    public void doSave() {
        infoGroup.saveConfig();
        setDirty(false);
    }

    public void updateConfigs(IFile formFile) {
        try {
            updateConfigs(IOUtils.readStreamAsBytes(formFile.getContents(true)));
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    boolean configsChanged = false;

    public void updateConfigs(byte[] formData) {
        try {
            configsChanged = false;
            FormType formType = FormTypeProvider.getFormType(formNode.getFormType());
            Map<String, FormVariableAccess> formVariables = formType.getFormVariableNames(formNode, formData);
            List<String> existingVariableNames = formNode.getVariableNames(true);
            formVariables.entrySet().stream().forEach(e -> {
                if (e.getValue() == FormVariableAccess.WRITE && !fieldConfigs.containsKey(e.getKey()) && existingVariableNames.contains(e.getKey())) {
                    fieldConfigs.put(e.getKey(), new HashMap<>());
                    configsChanged = true;
                }
            });
            for (Iterator<Entry<String, Map<String, ValidatorConfig>>> i = fieldConfigs.entrySet().iterator(); i.hasNext();) {
                Entry<String, Map<String, ValidatorConfig>> e = i.next();
                FormVariableAccess access = formVariables.get(e.getKey());
                if ((access == null || access == FormVariableAccess.READ) && e.getValue().isEmpty()) {
                    i.remove();
                    configsChanged = true;
                }
            }
            if (configsChanged) {
                updateVariablesTableViewerSelection();
                updateSwimlanesTableViewerSelection();
                updateVariableSelection();
                setDirty(true);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private void initUi() {
        setLayout(new GridLayout(1, false));
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        SashForm mainComposite = new SashForm(this, SWT.HORIZONTAL);
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        tabFolder = new TabFolder(mainComposite, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 300;
        tabFolder.setLayoutData(data);

        variablesTableViewer = createTableViewer(tabFolder, SWT.BORDER | SWT.FULL_SELECTION);
        variablesTableViewer.setLabelProvider(new VariableTableLabelProvider());
        variablesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateVariableSelection();
            }
        });
        variablesTableViewer.addCheckStateListener(e -> {
            String variableName = ((Variable) e.getElement()).getName();
            if (fieldConfigs.containsKey(variableName)) {
                removeField(variableName);
            } else {
                addField(variableName);
            }
            updateVariablesTableViewerSelection();
            updateVariableSelection();
            setDirty(true);
        });

        createTable(variablesTableViewer, new DataViewerComparator<>(new ValueComparator<Variable>() {
            @Override
            public int compare(Variable o1, Variable o2) {
                int resultVariable1 = 0;
                int resultVariable2 = 0;
                int result = 0;
                switch (getColumn()) {
                case 0:
                    resultVariable1 = fieldConfigs.containsKey(o1.getName()) ? -1 : 1;
                    resultVariable2 = fieldConfigs.containsKey(o2.getName()) ? -1 : 1;
                    if (resultVariable1 == resultVariable2) {
                        result = 0;
                    } else if (resultVariable1 < resultVariable2) {
                        result = -1;
                    } else {
                        result = 1;
                    }
                    break;
                case 1:
                    result = o1.getName().compareToIgnoreCase(o2.getName());
                    break;
                }

                return result;
            }
        }), new TableColumnDescription("#", 20, SWT.LEFT), new TableColumnDescription("property.name", 200, SWT.LEFT));

        TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
        tabItem1.setText(Localization.getString("FieldValidatorsWizardPage.Variables"));
        tabItem1.setControl(variablesTableViewer.getControl());

        swimlanesTableViewer = createTableViewer(tabFolder, SWT.BORDER | SWT.FULL_SELECTION);
        swimlanesTableViewer.setLabelProvider(new VariableTableLabelProvider());
        swimlanesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateVariableSelection();
            }
        });
        swimlanesTableViewer.addCheckStateListener(e -> {
            String variableName = ((Variable) e.getElement()).getName();
            if (fieldConfigs.containsKey(variableName)) {
                removeField(variableName);
            } else {
                addField(variableName);
            }
            updateSwimlanesTableViewerSelection();
            updateVariableSelection();
            setDirty(true);
        });

        createTable(swimlanesTableViewer, new DataViewerComparator<>(new ValueComparator<Swimlane>() {
            @Override
            public int compare(Swimlane o1, Swimlane o2) {
                int resultSwimlane1 = 0;
                int resultSwimlane2 = 0;
                int result = 0;

                switch (getColumn()) {
                case 0:
                    resultSwimlane1 = fieldConfigs.containsKey(o1.getName()) ? -1 : 1;
                    resultSwimlane2 = fieldConfigs.containsKey(o2.getName()) ? -1 : 1;
                    if (resultSwimlane1 == resultSwimlane2) {
                        result = 0;
                    } else if (resultSwimlane1 < resultSwimlane2) {
                        result = -1;
                    } else {
                        result = 1;
                    }
                    break;
                case 1:
                    result = o1.getName().compareToIgnoreCase(o2.getName());
                    break;
                }

                return result;
            }
        }), new TableColumnDescription("#", 20, SWT.LEFT), new TableColumnDescription("property.name", 200, SWT.LEFT));

        TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
        tabItem2.setText(Localization.getString("FieldValidatorsWizardPage.Swimlanes"));
        tabItem2.setControl(swimlanesTableViewer.getControl());
        SashForm right = new SashForm(mainComposite, SWT.VERTICAL);
        right.setLayoutData(data);

        Composite topPane = new Composite(right, SWT.NONE);
        topPane.setLayout(new GridLayout(1, false));

        Label validatorsLabel = new Label(topPane, SWT.NONE);
        validatorsLabel.setText(Localization.getString("FieldValidatorsWizardPage.Validators"));

        validatorsTableViewer = createTableViewer(topPane, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        validatorsTableViewer.setLabelProvider(new ValidatorDefinitionTableLabelProvider());

        Composite bottomPane = new Composite(right, SWT.NONE);
        bottomPane.setLayout(new GridLayout(1, false));
        bottomPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        infoGroup = new DefaultValidatorInfoControl(bottomPane);
        infoGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        infoGroup.setVisible(false);
        validatorsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateValidatorSelection();
            }
        });
        validatorsTableViewer.addCheckStateListener(e -> {
            ValidatorDefinition validatorDefinition = (ValidatorDefinition) e.getElement();
            Map<String, ValidatorConfig> configs = fieldConfigs.get(getCurrentVariableName());
            if (configs.containsKey(validatorDefinition.getName())) {
                removeFieldValidator(validatorDefinition);
            } else {
                addFieldValidator(validatorDefinition);
            }
            validatorsTableViewer.refresh(true);
            updateValidatorSelection();
            setDirty(true);
        });

        createTable(validatorsTableViewer, new DataViewerComparator<>(new ValueComparator<ValidatorDefinition>() {
            @Override
            public int compare(ValidatorDefinition o1, ValidatorDefinition o2) {
                int resultValidator1 = 0;
                int resultValidator2 = 0;
                int result = 0;

                Map<String, ValidatorConfig> configs = fieldConfigs.get(getCurrentVariableName());
                switch (getColumn()) {
                case 0:
                    resultValidator1 = configs.containsKey(o1.getName()) ? 1 : -1;
                    resultValidator2 = configs.containsKey(o2.getName()) ? 1 : -1;
                    if (resultValidator1 == resultValidator2) {
                        result = 0;
                    } else if (resultValidator1 < resultValidator2) {
                        result = 1;
                    } else {
                        result = -1;
                    }
                    break;
                case 1:
                    result = o1.getLabel().compareToIgnoreCase(o2.getLabel());
                    break;
                }

                return result;
            }
        }), new TableColumnDescription("#", 20, SWT.LEFT), new TableColumnDescription("property.name", 200, SWT.LEFT));

        updateViewers();

        warningLabel = new Label(bottomPane, SWT.NONE);
        warningLabel.setForeground(ColorConstants.red);
        warningLabel.setText(warningMessage);
        mainComposite.pack(true);

        for (Variable variable : formNode.getVariables(false, true)) {
            variable.addPropertyChangeListener(this);
        }
        processDefinition.addPropertyChangeListener(this);

        variablesTableViewer.refresh(true);
        swimlanesTableViewer.refresh(true);
        updateVariableSelection();
        validatorsTableViewer.refresh(true);
        updateValidatorSelection();
    }

    private <S> void createTable(TableViewer viewer, DataViewerComparator<S> comparator, TableColumnDescription... column) {
        viewer.setComparator(comparator);

        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        int index = 0;
        for (TableColumnDescription col : column) {
            TableColumn tableColumn = new TableColumn(table, col.style);
            tableColumn.setText(Localization.getString(col.titleKey));
            tableColumn.setWidth(col.width);
            if (col.sort) {
                tableColumn.addSelectionListener(createSelectionListener(viewer, comparator, tableColumn, index));
            }
            index++;
        }
    }

    private <S> SelectionListener createSelectionListener(final TableViewer viewer, final DataViewerComparator<S> comparator,
            final TableColumn column, final int index) {
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comparator.setColumn(index);
                viewer.getTable().setSortDirection(comparator.getDirection());
                viewer.getTable().setSortColumn(column);
                viewer.refresh();
            }
        };
        return selectionAdapter;
    }

    private CheckboxTableViewer createTableViewer(Composite parent, int style) {
        CheckboxTableViewer result = CheckboxTableViewer.newCheckList(parent, style);
        result.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        result.setContentProvider(new ArrayContentProvider());
        return result;
    }

    private void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            if (dirty) {
                dirtyCallback.accept(dirty);
            }
        }
    }

    private void updateViewers() {
        List<Variable> variables = formNode.getVariables(true, false);
        variablesTableViewer.setInput(variables);
        updateVariablesTableViewerSelection();
        List<Swimlane> swimlanes = processDefinition.getSwimlanes();
        swimlanesTableViewer.setInput(swimlanes);
        updateSwimlanesTableViewerSelection();
    }

    private List<Variable> getCheckedVariables() {
        List<Variable> checkedVariables = fieldConfigs.keySet().stream()//
                .map(s -> VariableUtils.getVariableByName(formNode, s))//
                .filter(v -> v != null).collect(Collectors.toList());
        return checkedVariables;
    }

    private void updateVariablesTableViewerSelection() {
        List<Variable> checkedVariables = getCheckedVariables();
        variablesTableViewer.setCheckedElements(checkedVariables.toArray(new Variable[checkedVariables.size()]));
        variablesTableViewer.refresh();
    }

    private void updateSwimlanesTableViewerSelection() {
        List<Variable> checkedVariables = getCheckedVariables();
        swimlanesTableViewer.setCheckedElements(checkedVariables.toArray(new Variable[checkedVariables.size()]));
        swimlanesTableViewer.refresh();
    }

    private void updateVariableSelection() {
        Map<String, ValidatorConfig> validators = getCurrentVariableName() != null ? fieldConfigs.get(getCurrentVariableName()) : null;
        validatorsTableViewer.getTable().setEnabled(validators != null);
        infoGroup.setVisible(false);
        updateValidatorsInput(getCurrentSelection());
    }

    private void updateValidatorSelection() {
        ValidatorDefinition vd = getCurrentDefinition();
        if (vd != null && fieldConfigs.containsKey(getCurrentVariableName())) {
            ValidatorConfig config = getFieldValidator(getCurrentVariableName(), vd);
            if (config == null) {
                config = vd.create();
            }
            infoGroup.setConfig(getCurrentVariableName(), vd, config);
        }
    }

    private class DefaultValidatorInfoControl extends ValidatorInfoControl {
        private final Map<String, Button> transitionButtons = Maps.newHashMap();

        public DefaultValidatorInfoControl(Composite parent) {
            super(parent, true);
            errorMessageText.addVerifyListener(new VerifyListener() {
                @Override
                public void verifyText(VerifyEvent e) {
                    if (e.keyCode != 0) {
                        setDirty(true);
                    }
                }
            });
            if (formNode.getLeavingTransitions().size() > 1) {
                Group transitionsGroup = new Group(this, SWT.BORDER);
                transitionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                transitionsGroup.setLayout(new GridLayout(3, false));
                transitionsGroup.setText(Localization.getString("FieldValidatorsWizardPage.TransitionContext"));
                for (Transition transition : formNode.getLeavingTransitions()) {
                    final Button button = new Button(transitionsGroup, SWT.CHECK);
                    button.setText(transition.getName());
                    button.addSelectionListener(new LoggingSelectionAdapter() {

                        @Override
                        protected void onSelection(SelectionEvent e) throws Exception {
                            if (button.getSelection()) {
                                config.getTransitionNames().add(button.getText());
                            } else {
                                config.getTransitionNames().remove(button.getText());
                            }
                            setDirty(true);
                        }
                    });
                    transitionButtons.put(transition.getName(), button);
                }
            }
            parametersComposite = new DefaultParamsComposite(this, SWT.NONE);
        }

        @Override
        protected boolean enableUI(String variableName, ValidatorDefinition definition, ValidatorConfig config) {
            if (config.getTransitionNames().isEmpty()) {
                for (Transition transition : formNode.getLeavingTransitions()) {
                    config.getTransitionNames().add(transition.getName());
                }
            }
            for (Map.Entry<String, Button> entry : transitionButtons.entrySet()) {
                entry.getValue().setSelection(config.getTransitionNames().contains(entry.getKey()));
            }
            return getFieldValidator(variableName, definition) != null;
        }
    }

    private Variable getCurrentSelection() {
        if (tabFolder.getSelectionIndex() == 0) {
            return (Variable) ((StructuredSelection) variablesTableViewer.getSelection()).getFirstElement();
        } else {
            return (Swimlane) ((StructuredSelection) swimlanesTableViewer.getSelection()).getFirstElement();
        }
    }

    private String getCurrentVariableName() {
        NamedGraphElement variable = getCurrentSelection();
        return variable != null ? variable.getName() : null;
    }

    private ValidatorDefinition getCurrentDefinition() {
        return (ValidatorDefinition) ((StructuredSelection) validatorsTableViewer.getSelection()).getFirstElement();
    }

    private void updateValidatorsInput(Variable variable) {
        if (variable != null) {
            validatorsTableViewer.setInput(ValidationUtil.getFieldValidatorDefinitions(variable));
            Map<String, ValidatorConfig> map = fieldConfigs.get(variable.getName());
            List<ValidatorDefinition> checkedValidatorDefinitions;
            if (map != null) {
                checkedValidatorDefinitions = map.keySet().stream()
                        .map(type -> ValidatorDefinitionRegistry.getDefinition(type))
                        .collect(Collectors.toList());
            } else {
                checkedValidatorDefinitions = new ArrayList<>();
            }
            validatorsTableViewer
                    .setCheckedElements(checkedValidatorDefinitions.toArray(new ValidatorDefinition[checkedValidatorDefinitions.size()]));
        }
    }

    private void addField(String variableName) {
        Map<String, ValidatorConfig> validators = new HashMap<String, ValidatorConfig>();
        ValidatorDefinition requiredDef = ValidatorDefinitionRegistry.getDefinition(ValidatorDefinition.REQUIRED_VALIDATOR_NAME);
        validators.put(requiredDef.getName(), requiredDef.create());
        fieldConfigs.put(variableName, validators);
    }

    private void removeField(String variableName) {
        fieldConfigs.remove(variableName);
    }

    private void addFieldValidator(ValidatorDefinition definition) {
        Map<String, ValidatorConfig> configs = fieldConfigs.get(getCurrentVariableName());
        configs.put(definition.getName(), definition.create());
    }

    private void removeFieldValidator(ValidatorDefinition definition) {
        Map<String, ValidatorConfig> configs = fieldConfigs.get(getCurrentVariableName());
        configs.remove(definition.getName());
    }

    private ValidatorConfig getFieldValidator(String variableName, ValidatorDefinition definition) {
        Map<String, ValidatorConfig> configs = fieldConfigs.get(variableName);
        return configs.get(definition.getName());
    }

    private class VariableTableLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public String getColumnText(Object element, int index) {
            switch (index) {
            case 0:
                return "";
            case 1:
                return ((Variable) element).getName();
            default:
                return "unknown " + index;
            }
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    private class ValidatorDefinitionTableLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public String getColumnText(Object element, int index) {
            switch (index) {
            case 0:
                return "";
            case 1:
                return ((ValidatorDefinition) element).getLabel();
            default:
                return "unknown " + index;
            }
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    private class DefaultParamsComposite extends ParametersComposite {
        private final Map<String, Combo> inputCombos = new HashMap<String, Combo>();

        public DefaultParamsComposite(Composite parent, int style) {
            super(parent, style);
            this.setLayoutData(new GridData(GridData.FILL_BOTH));
            this.setLayout(new GridLayout(2, true));
        }

        @Override
        protected void clear() {
            for (Control control : getChildren()) {
                control.dispose();
            }
            inputCombos.clear();
            this.pack(true);
        }

        @Override
        protected void build(ValidatorDefinition definition, Map<String, String> configParams) {
            for (Map.Entry<String, Param> entry : definition.getParams().entrySet()) {
                Label label = new Label(this, SWT.NONE);
                label.setText(entry.getValue().getLabel());
                label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                String initialValue = configParams.get(entry.getKey());
                TypedUserInputCombo combo = new TypedUserInputCombo(this, initialValue);
                // workaround for time validator
                Class<? extends UserInputDialog> userInputDialogClass = null;
                if ("time".equals(definition.getName()) && Date.class.getName().equals(entry.getValue().getType())) {
                    userInputDialogClass = TimeInputDialog.class;
                }
                if (userInputDialogClass != null) {
                    combo.setTypeClassName(entry.getValue().getType(), userInputDialogClass);
                } else {
                    combo.setTypeClassName(entry.getValue().getType());
                }
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        super.widgetSelected(e);
                        setDirty(true);
                    }
                });
                inputCombos.put(entry.getKey(), combo);
            }
            this.pack(true);
        }

        @Override
        protected void updateConfigParams(ValidatorDefinition definition, ValidatorConfig config) {
            for (Map.Entry<String, Param> entry : definition.getParams().entrySet()) {
                Combo combo = inputCombos.get(entry.getKey());
                String text = combo.getText();
                if (text.length() != 0) {
                    if (processDefinition.getVariableNames(true, true, entry.getValue().getType()).contains(text)) {
                        text = VariableUtils.wrapVariableName(text);
                    }
                    config.getParams().put(entry.getKey(), text);
                } else {
                    config.getParams().remove(entry.getKey());
                }
            }
        }
    }

    final static class DataViewerComparator<V> extends ViewerComparator {
        private int propertyIndex;
        private int direction;
        private final ValueComparator<V> comparable;

        protected DataViewerComparator(ValueComparator<V> comparable) {
            this.propertyIndex = 0;
            this.comparable = comparable;
            direction = SWT.NONE;
        }

        private int getDirection() {
            return direction;
        }

        private void setColumn(int column) {
            if (column == propertyIndex) {
                switch (direction) {
                case SWT.UP:
                    direction = SWT.DOWN;
                    break;
                case SWT.DOWN:
                    direction = SWT.NONE;
                    break;
                case SWT.NONE:
                    direction = SWT.UP;
                    break;
                }
            } else {
                propertyIndex = column;
                direction = SWT.UP;
            }
            comparable.setColumn(column);
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            int result = 0;

            switch (direction) {
            case SWT.NONE:
                break;
            case SWT.UP:
                result = comparable.compare((V) e1, (V) e2);
                break;
            case SWT.DOWN:
                result = -comparable.compare((V) e1, (V) e2);
                break;
            }

            return result;
        }
    }

    abstract static class ValueComparator<V> implements Comparator<V> {

        private int column;

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

    }

    final static class TableColumnDescription {
        private final String titleKey;
        private final int width;
        private final int style;
        private final boolean sort;

        TableColumnDescription(String titleKey, int width, int style) {
            this(titleKey, width, style, true);
        }

        TableColumnDescription(String titleKey, int width, int style, boolean sort) {
            this.titleKey = titleKey;
            this.width = width;
            this.style = style;
            this.sort = sort;
        }
    }

}
