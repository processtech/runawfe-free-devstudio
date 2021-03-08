package ru.runa.gpd.ui.control;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.extension.decision.GroovyTypeSupport;
import ru.runa.gpd.extension.decision.GroovyValidationModel;
import ru.runa.gpd.extension.decision.GroovyValidationModel.Expr;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.control.ValidatorInfoControl.ParametersComposite;
import ru.runa.gpd.ui.custom.FeaturedStyledText;
import ru.runa.gpd.ui.custom.JavaHighlightTextStyling;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.dialog.ChooseGroovyStuffDialog;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.util.GroovyStuff;
import ru.runa.gpd.util.GroovyStuff.Item;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;
import ru.runa.wfe.execution.dto.WfProcess;

public class GlobalValidatorsPage extends Composite implements PropertyChangeListener {
    private final FormNode formNode;
    private TableViewer validatorsTableViewer;
    private Button deleteButton;
    private ValidatorInfoControl infoGroup;
    private List<ValidatorConfig> validatorConfigs;
    private List<Variable> variables;
    private List<String> variableNames;
    private List<String> contextVariableNames;
    private final ValidatorDefinition globalDefinition = ValidatorDefinitionRegistry.getGlobalDefinition();
    private boolean dirty;
    private Consumer<Boolean> dirtyCallback;
    private boolean validatorChanging;

    public GlobalValidatorsPage(Composite parent, FormNode formNode, FormNodeValidation validation, Consumer<Boolean> dirtyCallback) {
        super(parent, SWT.NONE);
        this.formNode = formNode;
        updateVariableNames();
        this.validatorConfigs = validation.getGlobalConfigs();
        this.dirtyCallback = dirtyCallback;
        initUi();
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyNames.PROPERTY_CHILDREN_CHANGED.equals(evt.getPropertyName())) {
            updateVariableNames();
        }
    }

    @Override
    public void dispose() {
        formNode.getProcessDefinition().removePropertyChangeListener(this);
        super.dispose();
    }

    public void doSave() {
        infoGroup.saveConfig();
        Map<String, ValidatorConfig> globalConfigsMap = new HashMap<String, ValidatorConfig>(validatorConfigs.size());
        int discrimination = 1;
        for (ValidatorConfig config : validatorConfigs) {
            globalConfigsMap.put(config.getType() + discrimination++, config);
        }
        setDirty(false);
    }

    private void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            if (dirty) {
                dirtyCallback.accept(dirty);
            }
        }
    }

    private void updateVariableNames() {
        variables = formNode.getVariables(true, true);
        variableNames = VariableUtils.getVariableNamesForScripting(variables);
        contextVariableNames = Lists.newArrayList(variableNames);
        contextVariableNames.add(WfProcess.SELECTED_TRANSITION_KEY);
    }

    private void initUi() {
        setLayout(new GridLayout(1, false));
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        SashForm mainComposite = new SashForm(this, SWT.VERTICAL);
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite valComposite = new Composite(mainComposite, SWT.NONE);
        GridData valGridData = new GridData(GridData.FILL_BOTH);
        valComposite.setLayoutData(valGridData);
        valComposite.setLayout(new GridLayout(2, false));

        validatorsTableViewer = new TableViewer(valComposite, SWT.BORDER | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 100;
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
                try {
                    validatorChanging = true;
                    ValidatorConfig config = (ValidatorConfig) ((IStructuredSelection) validatorsTableViewer.getSelection()).getFirstElement();
                    deleteButton.setEnabled(config != null);
                    infoGroup.setConfig(ValidatorConfig.GLOBAL_FIELD_ID, globalDefinition, config);
                } finally {
                    validatorChanging = false;
                }
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
                setDirty(true);
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
                setDirty(true);
            }
        });
        deleteButton.setEnabled(false);

        Composite bottomComposite = new Composite(mainComposite, SWT.NONE);
        bottomComposite.setLayout(new GridLayout(1, false));
        bottomComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridData infoGridData = new GridData(GridData.FILL_BOTH);
        infoGroup = new DefaultValidatorInfoControl(bottomComposite);
        infoGroup.setLayoutData(infoGridData);

        mainComposite.setWeights(new int[] { 1, 2 });
        mainComposite.pack(true);

        infoGroup.setVisible(false);
        formNode.getProcessDefinition().addPropertyChangeListener(this);

        validatorsTableViewer.setInput(validatorConfigs);
        validatorsTableViewer.refresh(true);
    }

    private Button addButton(Composite parent, String buttonKey, SelectionAdapter selectionListener) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Localization.getString(buttonKey));
        button.addSelectionListener(selectionListener);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return button;
    }

    public class DefaultValidatorInfoControl extends ValidatorInfoControl {
        private final Map<String, Button> transitionButtons = Maps.newHashMap();

        public DefaultValidatorInfoControl(Composite parent) {
            super(parent, false);
            errorMessageText.addVerifyListener(new VerifyListener() {
                @Override
                public void verifyText(VerifyEvent e) {
                    if (!isConfiguring(errorMessageText)) {
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
        private Variable variable1;
        private final Combo comboBoxOp;
        private List<String> varNames = new ArrayList<>();
        private String varName2;
        private final TabFolder tabFolder;

        private Text txtVarName1;
        private Text txtVarName2;

        public GroovyParamsComposite(ValidatorInfoControl parent, int style) {
            super(parent, style);

            tabFolder = new TabFolder(this, SWT.NULL);
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

            Composite varComposite1 = new Composite(constrComposite, SWT.NONE);
            varComposite1.setLayoutData(getComboGridData());
            varComposite1.setLayout(new GridLayout(2, false));
            txtVarName1 = new Text(varComposite1, SWT.READ_ONLY | SWT.BORDER);
            txtVarName1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Button selectButton1 = new Button(varComposite1, SWT.PUSH);
            selectButton1.setText("...");
            selectButton1.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
            selectButton1.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String result = new ChooseVariableNameDialog(variableNames).openDialog();
                    if (result != null) {
                        variable1 = VariableUtils.getVariableByScriptingName(variables, result);
                        txtVarName1.setText(variable1.getScriptingName());
                        refreshCombos();
                        setDirty(true);
                    }
                }
            });

            comboBoxOp = new Combo(constrComposite, SWT.READ_ONLY);
            comboBoxOp.setLayoutData(getComboGridData());
            comboBoxOp.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    if (!validatorChanging) {
                        setDirty(true);
                    }
                }
            });

            Composite varComposite2 = new Composite(constrComposite, SWT.NONE);
            varComposite2.setLayoutData(getComboGridData());
            varComposite2.setLayout(new GridLayout(2, false));
            txtVarName2 = new Text(varComposite2, SWT.READ_ONLY | SWT.BORDER);
            txtVarName2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Button selectButton2 = new Button(varComposite2, SWT.PUSH);
            selectButton2.setText("...");
            selectButton2.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
            selectButton2.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String result = new ChooseVariableNameDialog(varNames).openDialog();
                    if (result != null) {
                        varName2 = result;
                        txtVarName2.setText(varName2);
                        setDirty(true);
                    }
                }
            });

            tabs[1] = new TabItem(tabFolder, SWT.NULL);
            tabs[1].setText(Localization.getString("GroovyEditor.title.code"));
            Composite codeComposite = new Composite(tabFolder, SWT.BORDER);
            codeComposite.setLayout(new GridLayout(5, false));
            codeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            tabs[1].setControl(codeComposite);
            if (GroovyStuff.TYPE.getAll().size() > 0) {
                SwtUtils.createLink(codeComposite, Localization.getString("Insert.TYPE.link"), new LoggingHyperlinkAdapter() {
                    @Override
                    public void onLinkActivated(HyperlinkEvent e) {
                        Item item = new ChooseGroovyStuffDialog(GroovyStuff.TYPE).openDialog();
                        if (item != null) {
                            String insert = item.getBody();
                            codeText.insert(insert);
                            codeText.setCaretOffset(codeText.getCaretOffset() + insert.length());
                            codeText.setFocus();
                        }
                    }
                }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            }
            if (GroovyStuff.CONSTANT.getAll().size() > 0) {
                SwtUtils.createLink(codeComposite, Localization.getString("Insert.CONSTANT.link"), new LoggingHyperlinkAdapter() {
                    @Override
                    public void onLinkActivated(HyperlinkEvent e) {
                        Item item = new ChooseGroovyStuffDialog(GroovyStuff.CONSTANT).openDialog();
                        if (item != null) {
                            String insert = item.getBody();
                            codeText.insert(insert);
                            codeText.setCaretOffset(codeText.getCaretOffset() + insert.length());
                            codeText.setFocus();
                        }
                    }
                }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            }
            if (GroovyStuff.STATEMENT.getAll().size() > 0) {
                SwtUtils.createLink(codeComposite, Localization.getString("Insert.STATEMENT.link"), new LoggingHyperlinkAdapter() {
                    @Override
                    public void onLinkActivated(HyperlinkEvent e) {
                        Item item = new ChooseGroovyStuffDialog(GroovyStuff.STATEMENT).openDialog();
                        if (item != null) {
                            String insert = item.getBody();
                            codeText.insert(insert);
                            codeText.setCaretOffset(codeText.getCaretOffset() + insert.length());
                            codeText.setFocus();
                        }
                    }
                }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            }
            if (GroovyStuff.METHOD.getAll().size() > 0) {
                SwtUtils.createLink(codeComposite, Localization.getString("Insert.METHOD.link"), new LoggingHyperlinkAdapter() {
                    @Override
                    public void onLinkActivated(HyperlinkEvent e) {
                        Item item = new ChooseGroovyStuffDialog(GroovyStuff.METHOD).openDialog();
                        if (item != null) {
                            String insert = item.getBody();
                            codeText.insert(insert);
                            codeText.setCaretOffset(codeText.getCaretOffset() + insert.length());
                            codeText.setFocus();
                        }
                    }
                }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            }
            SwtUtils.createLink(codeComposite, Localization.getString("button.insert_variable"), new LoggingHyperlinkAdapter() {
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
            }).setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));

            codeText = new FeaturedStyledText(codeComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, EnumSet.of(
                    FeaturedStyledText.Feature.LINE_NUMBER, FeaturedStyledText.Feature.UNDO_REDO));
            codeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
            codeText.addLineStyleListener(new JavaHighlightTextStyling(contextVariableNames));
            codeText.addVerifyKeyListener(new VerifyKeyListener() {
                @Override
                public void verifyKey(VerifyEvent e) {
                    if (e.keyCode != 0) {
                        setDirty(true);
                    }
                }
            });
        }

        private GridData getComboGridData() {
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 100;
            return gridData;
        }

        private void refreshCombos() {
            List<Operation> operations = Operation.getAll(GroovyTypeSupport.get(variable1.getJavaClassName()));
            comboBoxOp.setItems(new String[0]);
            for (Operation operation : operations) {
                comboBoxOp.add(operation.getVisibleName());
            }
            varNames = getCombo2VariableNames(variable1);
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
            if (variable1 != null && comboBoxOp.getText().length() > 0 && varName2 != null && varName2.length() > 0) {
                String operationName = comboBoxOp.getItem(comboBoxOp.getSelectionIndex());
                Variable variable2 = VariableUtils.getVariableByScriptingName(variables, varName2);
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
                        variable1 = variable;
                        txtVarName1.setText(variable.getScriptingName());
                        refreshCombos();
                        comboBoxOp.setText(expr.getOperation().getVisibleName());
                        varName2 = expr.getVariable2().getScriptingName();
                        txtVarName2.setText(varName2);
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
