package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.decision.GroovyTypeSupport;
import ru.runa.gpd.extension.decision.GroovyValidationModel;
import ru.runa.gpd.extension.decision.GroovyValidationModel.Expr;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.FeaturedStyledText;
import ru.runa.gpd.ui.custom.JavaHighlightTextStyling;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.ui.wizard.ValidatorWizard.ParametersComposite;
import ru.runa.gpd.ui.wizard.ValidatorWizard.ValidatorInfoControl;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;
import ru.runa.wfe.execution.dto.WfProcess;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GlobalValidatorsWizardPage extends WizardPage {
    private final FormNode formNode;
    private TableViewer validatorsTableViewer;
    private Button deleteButton;
    private ValidatorInfoControl infoGroup;
    private List<ValidatorConfig> validatorConfigs;
    private final List<Variable> variables;
    private final List<String> variableNames;
    private final List<String> contextVariableNames;
    private final ValidatorDefinition globalDefinition = ValidatorDefinitionRegistry.getGlobalDefinition();
    private Text descriptionText;
    private TabFolder tabFolderForValidatorParameters;

    protected GlobalValidatorsWizardPage(FormNode formNode) {
        super("Global validators");
        this.formNode = formNode;
        this.variables = formNode.getProcessDefinition().getVariables(true, true);
        this.variableNames = VariableUtils.getVariableNamesForScripting(variables);
        this.contextVariableNames = Lists.newArrayList(variableNames);
        this.contextVariableNames.add(WfProcess.SELECTED_TRANSITION_KEY);
        setTitle(Localization.getString("ValidatorWizardPage.globalpage.title"));
        setDescription(Localization.getString("ValidatorWizardPage.globalpage.description"));
    }

    public void init(FormNodeValidation validation) {
        this.validatorConfigs = validation.getGlobalConfigs();
        if (validatorsTableViewer != null) {
            validatorsTableViewer.setInput(validatorConfigs);
            validatorsTableViewer.refresh(true);
        }
    }

    public List<ValidatorConfig> getValidatorConfigs() {
        return validatorConfigs;
    }

    @Override
    public void createControl(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, false));

        Composite valComposite = new Composite(mainComposite, SWT.NONE);
        valComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        valComposite.setLayout(new GridLayout(2, false));

        validatorsTableViewer = new TableViewer(valComposite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 200;
        validatorsTableViewer.getControl().setLayoutData(data);
        validatorsTableViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((ValidatorConfig) element).getMessage();
            }
        });
        validatorsTableViewer.setContentProvider(new ArrayContentProvider());
        validatorsTableViewer.setInput(validatorConfigs);
        validatorsTableViewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            protected void onSelectionChanged(SelectionChangedEvent event) throws Exception {
                if (infoGroup.getConfig() != null) {
                    infoGroup.getConfig().setDescription(descriptionText.getText());
                }
                infoGroup.saveConfig();
                tabFolderForValidatorParameters.setSelection(0);
                ValidatorConfig config = (ValidatorConfig) ((IStructuredSelection) validatorsTableViewer.getSelection()).getFirstElement();
                deleteButton.setEnabled(config != null);
                infoGroup.setConfig(ValidatorConfig.GLOBAL_FIELD_ID, globalDefinition, config);
                descriptionText.setText(config.getDescription());
            }
        });

        Table table = validatorsTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(Localization.getString("GlobalValidatorsWizardPage.SelectedValidators"));
        tableColumn.setWidth(500);

        GridData btnGridData = new GridData();
        btnGridData.horizontalAlignment = SWT.LEFT;
        btnGridData.verticalAlignment = SWT.TOP;
        Composite buttonsBar = new Composite(valComposite, SWT.NONE);
        buttonsBar.setLayoutData(btnGridData);
        buttonsBar.setLayout(new GridLayout(1, true));
        addButton(buttonsBar, "button.add", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent event) throws Exception {
                ValidatorConfig config = ValidatorDefinitionRegistry.getGlobalDefinition().create();
                config.setMessage(Localization.getString("GlobalValidatorsWizardPage.defaultValidationMessage"));
                validatorConfigs.add(config);
                validatorsTableViewer.refresh(true);
                validatorsTableViewer.setSelection(new StructuredSelection(config));
            }
        });

        deleteButton = addButton(buttonsBar, "button.delete", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent event) throws Exception {
                ValidatorConfig config = (ValidatorConfig) ((IStructuredSelection) validatorsTableViewer.getSelection()).getFirstElement();
                if (config == null) {
                    infoGroup.setVisible(false);
                    return;
                }
                validatorConfigs.remove(config);
                validatorsTableViewer.refresh(true);
            }
        });
        deleteButton.setEnabled(false);

        Composite compositeForTabFolder = new Composite(mainComposite, SWT.NONE);
        compositeForTabFolder.setLayout(new GridLayout(1, false));
        compositeForTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tabFolderForValidatorParameters = new TabFolder(compositeForTabFolder, SWT.NONE);
        GridData gridDataForTabFolder = new GridData(GridData.FILL_BOTH);
        gridDataForTabFolder.minimumHeight = 500;
        tabFolderForValidatorParameters.setLayoutData(gridDataForTabFolder);

        TabItem tabItemGeneral = new TabItem(tabFolderForValidatorParameters, SWT.NONE);
        tabItemGeneral.setText(Localization.getString("ValidatorsWizardPage.GeneralInfo"));
        GridData gridDataForInfoGroup = new GridData(GridData.FILL_BOTH);
        gridDataForInfoGroup.minimumHeight = 250;
        infoGroup = new DefaultValidatorInfoControl(tabFolderForValidatorParameters);
        infoGroup.setLayoutData(gridDataForInfoGroup);
        infoGroup.setVisible(false);
        tabItemGeneral.setControl(infoGroup);

        TabItem tabItemDescription = new TabItem(tabFolderForValidatorParameters, SWT.NONE);
        tabItemDescription.setText(Localization.getString("ValidatorsWizardPage.DescriptionForUser"));
        Composite compositeForDescription = new Composite(tabFolderForValidatorParameters, SWT.NONE);
        compositeForDescription.setLayout(new GridLayout(1, false));
        descriptionText = new Text(compositeForDescription, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));
        tabItemDescription.setControl(compositeForDescription);

        mainComposite.pack(true);
        setControl(mainComposite);
    }

    protected Button addButton(Composite parent, String buttonKey, SelectionAdapter selectionListener) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Localization.getString(buttonKey));
        button.addSelectionListener(selectionListener);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return button;
    }

    public void performFinish() {
        if (infoGroup.getConfig() != null) {
            infoGroup.getConfig().setDescription(descriptionText.getText());
        }
        infoGroup.saveConfig();
        Map<String, ValidatorConfig> globalConfigsMap = new HashMap<String, ValidatorConfig>(validatorConfigs.size());
        int discrimination = 1;
        for (ValidatorConfig config : validatorConfigs) {
            globalConfigsMap.put(config.getType() + discrimination++, config);
        }
    }

    public class DefaultValidatorInfoControl extends ValidatorInfoControl {
        private final Map<String, Button> transitionButtons = Maps.newHashMap();

        public DefaultValidatorInfoControl(Composite parent) {
            super(parent, false);
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
                        }
                    });
                    transitionButtons.put(transition.getName(), button);
                }
            }

            parametersComposite = new GroovyParamsComposite(this, SWT.BORDER);
            parametersComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            parametersComposite.setLayout(new GridLayout(1, true));
            errorMessageText.addModifyListener(new LoggingModifyTextAdapter() {
                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    config.setMessage(errorMessageText.getText());
                    validatorsTableViewer.refresh(config, true);
                }
            });
        }

        @Override
        protected boolean enableUI(String variableName, ValidatorDefinition definition, ValidatorConfig config) {
            if (config != null) {
                if (config.getTransitionNames().isEmpty()) {
                    for (Transition transition : formNode.getLeavingTransitions()) {
                        config.getTransitionNames().add(transition.getName());
                    }
                }
                for (Map.Entry<String, Button> entry : transitionButtons.entrySet()) {
                    entry.getValue().setSelection(config.getTransitionNames().contains(entry.getKey()));
                }
            }
            return (config != null);
        }
    }

    public class GroovyParamsComposite extends ParametersComposite {
        private final StyledText codeText;
        private final Combo comboBoxVar1;
        private final Combo comboBoxOp;
        private final Combo comboBoxVar2;
        private final TabFolder tabFolder;

        public GroovyParamsComposite(ValidatorInfoControl parent, int style) {
            super(parent, style);

            tabFolder = new TabFolder(this, SWT.NULL);
            tabFolder.setLayout(new GridLayout(2, true));
            tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
            tabFolder.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent event) throws Exception {
                    if (tabFolder.getSelectionIndex() == 1) {
                        toCode();
                    }
                }
            });
            TabItem[] tabs = new TabItem[2];
            tabs[0] = new TabItem(tabFolder, SWT.NULL);
            tabs[0].setText(Localization.getString("GroovyEditor.title.constructor"));
            Composite constrComposite = new Composite(tabFolder, SWT.BORDER);
            constrComposite.setLayout(new GridLayout(3, false));
            constrComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            tabs[0].setControl(constrComposite);

            comboBoxVar1 = new Combo(constrComposite, SWT.READ_ONLY);
            comboBoxVar1.setLayoutData(getComboGridData());
            for (String variableName : variableNames) {
                comboBoxVar1.add(variableName);
            }
            comboBoxVar1.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent event) throws Exception {
                    String varName = comboBoxVar1.getItem(comboBoxVar1.getSelectionIndex());
                    Variable variable = VariableUtils.getVariableByScriptingName(variables, varName);
                    comboBoxVar1.setData(variable);
                    refreshCombos();
                }
            });

            comboBoxOp = new Combo(constrComposite, SWT.READ_ONLY);
            comboBoxOp.setLayoutData(getComboGridData());

            comboBoxVar2 = new Combo(constrComposite, SWT.READ_ONLY);
            comboBoxVar2.setLayoutData(getComboGridData());

            tabs[1] = new TabItem(tabFolder, SWT.NULL);
            tabs[1].setText(Localization.getString("GroovyEditor.title.code"));
            Composite codeComposite = new Composite(tabFolder, SWT.BORDER);
            codeComposite.setLayout(new GridLayout());
            codeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            tabs[1].setControl(codeComposite);
            SWTUtils.createLink(codeComposite, Localization.getString("button.insert_variable"), new LoggingHyperlinkAdapter() {
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(contextVariableNames);
                    String variableName = dialog.openDialog();
                    if (variableName != null) {
                        codeText.insert(variableName);
                        codeText.setFocus();
                        codeText.setCaretOffset(codeText.getCaretOffset() + variableName.length());
                    }
                }
            }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            codeText = new FeaturedStyledText(codeComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, EnumSet.of(
                    FeaturedStyledText.Feature.LINE_NUMBER, FeaturedStyledText.Feature.UNDO_REDO));
            codeText.setLayoutData(new GridData(GridData.FILL_BOTH));
            codeText.addLineStyleListener(new JavaHighlightTextStyling(contextVariableNames));
        }

        private GridData getComboGridData() {
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 100;
            return gridData;
        }

        private void refreshCombos() {
            Variable variable = (Variable) comboBoxVar1.getData();
            List<Operation> operations = Operation.getAll(GroovyTypeSupport.get(variable.getJavaClassName()));
            comboBoxOp.setItems(new String[0]);
            for (Operation operation : operations) {
                comboBoxOp.add(operation.getVisibleName());
            }
            comboBoxVar2.setItems(new String[0]);
            List<String> combo2Names = getCombo2VariableNames(variable);
            for (String var2Name : combo2Names) {
                comboBoxVar2.add(var2Name);
            }
        }

        private List<String> getCombo2VariableNames(Variable variable1) {
            List<String> names = new ArrayList<String>();
            GroovyTypeSupport typeSupport1 = GroovyTypeSupport.get(variable1.getJavaClassName());
            for (Variable variable : variables) {
                GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                // formats are equals, variable not selected in the first combo
                if (typeSupport1 == typeSupport && variable1 != variable && variableNames.contains(variable1.getScriptingName())) {
                    names.add(variable.getScriptingName());
                }
            }
            return names;
        }

        private void toCode() {
            if (comboBoxVar1.getText().length() > 0 && comboBoxOp.getText().length() > 0 && comboBoxVar2.getText().length() > 0) {
                Variable variable1 = (Variable) comboBoxVar1.getData();
                String operationName = comboBoxOp.getItem(comboBoxOp.getSelectionIndex());
                Variable variable2 = VariableUtils.getVariableByScriptingName(variables, comboBoxVar2.getText());
                GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
                Operation operation = Operation.getByName(operationName, typeSupport);
                String code = operation.generateCode(variable1, variable2);
                codeText.setText(code);
            } else {
                // don't change code
            }
        }

        @Override
        protected void clear() {
        }

        @Override
        protected void build(ValidatorDefinition definition, Map<String, String> configParams) {
            String textData = configParams.get(ValidatorDefinition.EXPRESSION_PARAM_NAME);
            try {
                Expr expr = GroovyValidationModel.fromCode(textData, variables);
                if (expr != null) {
                    Variable variable = expr.getVariable1();
                    if (variableNames.contains(variable.getScriptingName())) {
                        comboBoxVar1.setText(variable.getScriptingName());
                        comboBoxVar1.setData(variable);
                        refreshCombos();
                        comboBoxOp.setText(expr.getOperation().getVisibleName());
                        comboBoxVar2.setText(expr.getVariable2().getScriptingName());
                        textData = expr.generateCode();
                    }
                } else if (!Strings.isNullOrEmpty(textData)) {
                    tabFolder.setSelection(1);
                }
            } catch (Exception e) {
                tabFolder.setSelection(1);
            }
            codeText.setText(textData != null ? textData : "");
        }

        @Override
        protected void updateConfigParams(ValidatorDefinition definition, ValidatorConfig config) {
            if (tabFolder.getSelectionIndex() == 0) {
                toCode();
            }
            String textData = codeText.getText().trim();
            if (textData.length() != 0) {
                config.getParams().put(ValidatorDefinition.EXPRESSION_PARAM_NAME, textData);
            } else {
                config.getParams().remove(ValidatorDefinition.EXPRESSION_PARAM_NAME);
            }
        }
    }
}
