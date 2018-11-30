package ru.runa.gpd.ui.wizard;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.DynaContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.TimeInputDialog;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.gpd.ui.wizard.ValidatorWizard.ParametersComposite;
import ru.runa.gpd.ui.wizard.ValidatorWizard.ValidatorInfoControl;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinition.Param;

public class VariableRestrictionsPage extends DynaContentWizardPage {
    private final Variable variable;
    private TableViewer validatorsTableViewer;
    private ValidatorInfoControl infoGroup;
    private ProcessDefinition processDefinition;
    private Map<String, ValidatorConfig> validators;
    private final VariableFormatPage formatPage;

    public VariableRestrictionsPage(ProcessDefinition processDefinition, Variable variable, VariableFormatPage formatPage) {
        this.processDefinition = processDefinition;
        if (variable != null && variable.getValidators() != null) {
            this.variable = variable;
            this.validators = variable.getValidators();
        } else {
            this.variable = new Variable();
            this.validators = Maps.newHashMap();
        }
        this.formatPage = formatPage;
    }

    public Variable getVariable() {
        return variable;
    }
    
    @Override
    public void createContent(Composite parent) {
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.minimumHeight = 450;

        dynaComposite = new Composite(parent, SWT.NONE);
        dynaComposite.setLayoutData(data);
        dynaComposite.setLayout(new GridLayout(1, false));
    }
    
    @Override
    protected void createDynaContent() {
        String formatClassName = formatPage.getType().getName();
        String format = formatClassName;
        if (formatPage.getComponentClassNames().length != 0) {
            format += Variable.FORMAT_COMPONENT_TYPE_START;
            for (int i = 0; i < formatPage.getComponentClassNames().length; i++) {
                if (i != 0) {
                    format += Variable.FORMAT_COMPONENT_TYPE_CONCAT;
                }
                format += formatPage.getComponentClassNames()[i];
            }
            format += Variable.FORMAT_COMPONENT_TYPE_END;
        }
        // not changing user type if it has been already set after opening validators page
        if (!variable.getFormat().contains("usertype") && !format.contains("usertype")) {
            variable.setFormat(format);
        }
        Label validatorsLabel = new Label(dynaComposite, SWT.NONE);
        validatorsLabel.setText(Localization.getString("FieldValidatorsWizardPage.Validators"));
        validatorsTableViewer = createTableViewer(dynaComposite, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        validatorsTableViewer.setLabelProvider(new ValidatorDefinitionTableLabelProvider());
        validatorsTableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        
        GridData groupData = new GridData(GridData.FILL_BOTH);
        groupData.minimumHeight = 250;
        infoGroup = new DefaultValidatorInfoControl(dynaComposite);
        infoGroup.setLayoutData(groupData);
        infoGroup.setVisible(false);
        validatorsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateValidatorSelection();
            }
        });
        validatorsTableViewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                ValidatorDefinition definition = getCurrentDefinition();
                if (validators.containsKey(getCurrentDefinition().getName())) {
                    validators.remove(definition.getName());
                } else {
                    validators.put(definition.getName(), definition.create());
                }
                validatorsTableViewer.refresh(true);
                updateValidatorSelection();
            }
        });
        
       createTable(validatorsTableViewer, new TableColumnDescription("#", 20, SWT.LEFT), new TableColumnDescription("property.name", 200, SWT.LEFT));
       validatorsTableViewer.setInput(ValidationUtil.getTypeDefinedFieldValidatorDefinitions(variable));
        
        validatorsTableViewer.refresh(true);
        updateValidatorSelection();
        
    }
    
    private class DefaultValidatorInfoControl extends ValidatorInfoControl {

        public DefaultValidatorInfoControl(Composite parent) {
            super(parent, true);
            parametersComposite = new DefaultParamsComposite(this, SWT.NONE);
        }

        @Override
        protected boolean enableUI(String variableName, ValidatorDefinition definition, ValidatorConfig config) {
            return true;
        }

    }
    private <S> void createTable(TableViewer viewer, TableColumnDescription... column) {

        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        for (TableColumnDescription col : column) {
            TableColumn tableColumn = new TableColumn(table, col.style);
            tableColumn.setText(Localization.getString(col.titleKey));
            tableColumn.setWidth(col.width);
        }
    }

    private TableViewer createTableViewer(Composite parent, int style) {
        TableViewer result = new TableViewer(parent, style);
        result.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        result.setContentProvider(new ArrayContentProvider());
        result.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (variable != null) {
                    validatorsTableViewer.setInput(ValidationUtil.getTypeDefinedFieldValidatorDefinitions(variable));
                }
            }
        });

        return result;
    }
    
    private void updateValidatorSelection() {
        ValidatorDefinition vd = getCurrentDefinition();
        if (vd != null) {
            ValidatorConfig config;
            if (validators == null || !validators.containsKey(vd.getName())) {
                config = vd.create();
            } else {
                config = validators.get(vd.getName());
            }
            infoGroup.setConfig(variable != null ? variable.getName() : null, vd, config);
        }
    }

    private ValidatorDefinition getCurrentDefinition() {
        return (ValidatorDefinition) ((StructuredSelection) validatorsTableViewer.getSelection()).getFirstElement();
    }


    static final String UNCHECKED_IMG = "icons/unchecked.gif";
    static final String CHECKED_IMG = "icons/checked.gif";


    private class ValidatorDefinitionTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            String result = "";
            ValidatorDefinition variable = (ValidatorDefinition) element;

            switch (index) {
            case 0: result = "";
                    break;
            case 1: result = variable.getLabel();
                    break;
            default: result = "unknown " + index;
                    break;
            }

            return result;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            Image result = null;
            ValidatorDefinition definition = (ValidatorDefinition) element;
            String imagePath;
            if (validators != null) {
                switch (columnIndex) {
                case 0: imagePath = validators.containsKey(definition.getName()) ? CHECKED_IMG : UNCHECKED_IMG;
                        result = SharedImages.getImage(imagePath);
                        break;
                case 1: result = null;
                        break;
                }
            } else {
                switch (columnIndex) {
                case 0: imagePath = UNCHECKED_IMG;
                        result = SharedImages.getImage(imagePath);
                        break;
                case 1: result = null;
                        break;
                }
            }
            return result;
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
                // TODO: enable feature: use variables for comparison
                // if (initialValue != null &&
                // VariableUtils.isVariableNameWrapped(initialValue)) {
                // initialValue =
                // VariableUtils.unwrapVariableName(initialValue);
                // }
                TypedUserInputCombo combo = new TypedUserInputCombo(this, initialValue);
                // for (Variable variable : processDefinition.getVariables(true,
                // true, entry.getValue().getType())) {
                // combo.add(variable.getName());
                // }
                // TODO workaround for time validator
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

    final static class TableColumnDescription {
        private final String titleKey;
        private final int width;
        private final int style;

        TableColumnDescription(String titleKey, int width, int style) {
            this.titleKey = titleKey;
            this.width = width;
            this.style = style;
        }
    }
    
    public Map<String, ValidatorConfig> getValidators() {
        return validators;
    }
    
    public ValidatorInfoControl getInfoGroup() {
        return infoGroup;
    }

    @Override
    protected void verifyContentIsValid() {
    }

}