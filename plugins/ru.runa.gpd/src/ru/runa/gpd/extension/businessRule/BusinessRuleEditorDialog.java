package ru.runa.gpd.extension.businessRule;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.businessRule.BusinessRuleModel.IfExpr;
import ru.runa.gpd.extension.decision.GroovyEditorDialogType;
import ru.runa.gpd.extension.decision.GroovyTypeSupport;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.extension.handler.FormulaCellEditorProvider;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.FilterBox;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.gpd.util.VariableUtils;

public class BusinessRuleEditorDialog extends GroovyEditorDialogType {
    private static final Image addImage = SharedImages.getImage("icons/add_obj.gif");
    private final List<String> functions = new ArrayList();
    private List<Button> functionButtons = new ArrayList();
    private List<FilterBox[]> variableBoxes = new ArrayList();
    private List<Combo> operationBoxes = new ArrayList();
    private String defaultFunction;

    public BusinessRuleEditorDialog(ProcessDefinition definition, String initValue) {
        super(definition, initValue);
        if (this.initValue.length() > 0) {
            try {
                initModel = new BusinessRuleModel(initValue, variables);
            } catch (Throwable e) {
                initErrorMessage = e.getMessage();
                PluginLogger.logErrorWithoutDialog("", e);
            }
        }
    }

    @Override
    protected void createConstructorView() {
        if (initModel != null) {
            defaultFunction = ((BusinessRuleModel) initModel).getDefaultFunction();
            for (IfExpr expr : ((BusinessRuleModel) initModel).getIfExprs()) {
                createLines(expr.getFunction());
            }
        } else {
            createLines(null);
        }

        for (int i = 0; i < variableBoxes.size(); i++) {
            variableBoxes.get(i)[0].setSize(100, 20);
            variableBoxes.get(i)[1].setSize(100, 20);
        }

        createBottomComposite();
    }

    private Composite createBottomComposite() {
        Composite bottomComposite = new Composite(constructor, SWT.NONE);
        bottomComposite.setLayout(new GridLayout(2, true));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 5;
        bottomComposite.setLayoutData(data);
        Label defaultLabel = new Label(bottomComposite, SWT.NONE);
        defaultLabel.setText(Localization.getString("GroovyEditor.allOtherCases") + ":");
        Button defaultButton = new Button(bottomComposite, SWT.NONE);
        defaultButton.setLayoutData(getGridData());
        if (defaultFunction != null) {
            defaultButton.setText(defaultFunction);
        }
        defaultButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                FormulaCellEditorProvider.ConfigurationDialog dialog = new FormulaCellEditorProvider.ConfigurationDialog(defaultButton.getText(),
                        variableNames);
                if (dialog.open() == Window.OK) {
                    defaultButton.setText(dialog.getResult());
                    defaultFunction = defaultButton.getText();
                }
            }
        });
        bottomComposite.pack();
        return bottomComposite;
    }

    private void createLines(String function) {
        FilterBox[] variable = new FilterBox[2];
        variableBoxes.add(variable);
        variable[0] = new FilterBox(constructor, VariableUtils.getVariableNamesForScripting(variables));
        variable[0].setData(DATA_INDEX_KEY, new int[] { variableBoxes.size() - 1, 0 });
        variable[0].setSelectionListener(new FilterBoxSelectionHandler());
        variable[0].setLayoutData(getGridData());

        Combo operation = new Combo(constructor, SWT.READ_ONLY);
        operation.setData(DATA_INDEX_KEY, new int[] { variableBoxes.size() - 1, 1 });
        operation.addSelectionListener(new ComboSelectionHandler());
        operation.setLayoutData(getGridData());
        operationBoxes.add(operation);

        variable[1] = new FilterBox(constructor, null);
        variable[1].setData(DATA_INDEX_KEY, new int[] { variableBoxes.size() - 1, 2 });
        variable[1].setSelectionListener(new FilterBoxSelectionHandler());
        variable[1].setLayoutData(getGridData());

        Button functionButton = new Button(constructor, SWT.NONE);
        functionButton.setLayoutData(getGridData());
        functionButtons.add(functionButton);
        functionButton.setData(functionButtons.size() - 1);
        if (function == null) {
            functionButton.setText(Localization.getString("GroovyEditor.functionButton") + functionButtons.size());
        } else {
            functionButton.setText(function);
        }
        functions.add(functionButton.getText());
        functionButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                FormulaCellEditorProvider.ConfigurationDialog dialog = new FormulaCellEditorProvider.ConfigurationDialog(functionButton.getText(),
                        variableNames);
                if (dialog.open() == Window.OK) {
                    functionButton.setText(dialog.getResult());
                }
            }
        });

        Button addButton = new Button(constructor, SWT.PUSH);
        addButton.setImage(addImage);
        addButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                constructor.getChildren()[constructor.getChildren().length - 1].dispose();
                createLines(null);
                createBottomComposite();
                constructor.layout();
            }
        });
    }

    @Override
    protected void initConstructorView() {
        for (int i = 0; i < functions.size(); i++) {
            IfExpr ifExpr = ((BusinessRuleModel) initModel).getIfExpr(functions.get(i));
            if (ifExpr != null) {
                functionButtons.get(i).setText(ifExpr.getFunction());
                Variable variable = ifExpr.getVariable1();
                int index = variables.indexOf(variable);
                if (index == -1) {
                    // required variable was deleted in process definition
                    continue;
                }
                variableBoxes.get(i)[0].select(index);
                refresh(variableBoxes.get(i)[0]);
                GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                index = Operation.getAll(typeSupport).indexOf(ifExpr.getOperation());
                if (index == -1) {
                    // required operation was deleted !!!
                    continue;
                }
                operationBoxes.get(i).select(index);
                refresh(operationBoxes.get(i));
                String lexem2Text = ifExpr.getLexem2TextValue();
                int var2index = 0;
                if (VariableUtils.getVariableByScriptingName(variables, lexem2Text) != null
                        && (VariableUtils.getVariableByScriptingName(variables, lexem2Text).getJavaClassName()).equals(variable.getJavaClassName())) {
                    var2index = getVariable2Names(variable).indexOf(lexem2Text);
                } else {
                    int predefinedIndex = typeSupport.getPredefinedValues(ifExpr.getOperation()).indexOf(lexem2Text);
                    if (predefinedIndex >= 0) {
                        var2index = getVariable2Names(variable).size() + predefinedIndex;
                    } else {
                        variableBoxes.get(i)[1].add(lexem2Text, 0);
                        variableBoxes.get(i)[1].setData(DATA_USER_INPUT_KEY, lexem2Text);
                    }
                }
                variableBoxes.get(i)[1].select(var2index);
            }
        }
    }

    @Override
    protected void refresh(FilterBox filterBox) {
        try {
            int[] indexes = (int[]) filterBox.getData(DATA_INDEX_KEY);
            if (indexes[1] == 2) {
                if (TypedUserInputCombo.INPUT_VALUE.equals(filterBox.getSelectedItem())) {
                    String oldUserInput = (String) filterBox.getData(DATA_USER_INPUT_KEY);
                    Variable variable1 = (Variable) variableBoxes.get(indexes[0])[0].getData(DATA_VARIABLE_KEY);
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
                    UserInputDialog inputDialog = typeSupport.createUserInputDialog();
                    inputDialog.setInitialValue(oldUserInput);
                    if (OK == inputDialog.open()) {
                        String userInput = inputDialog.getUserInput();
                        if (oldUserInput != null) {
                            filterBox.remove(0);
                        }
                        filterBox.setData(DATA_USER_INPUT_KEY, userInput);
                        filterBox.add(userInput, 0);
                        filterBox.select(0);
                    } else {
                        filterBox.deselectAll();
                    }
                } else {
                    Variable variable = VariableUtils.getVariableByScriptingName(variables, filterBox.getText());
                    if (variable != null) {
                        filterBox.setData(DATA_VARIABLE_KEY, variable);
                    }
                }
                return;
            }
            if (indexes[1] == 0) {
                Combo operCombo = operationBoxes.get(indexes[0]);
                operCombo.setItems(new String[0]);
                Variable variable = VariableUtils.getVariableByScriptingName(variables, filterBox.getText());
                filterBox.setData(DATA_VARIABLE_KEY, variable);
                if (variable != null) {
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                    for (Operation operation : Operation.getAll(typeSupport)) {
                        operCombo.add(operation.getVisibleName());
                    }
                }
            }
        } catch (RuntimeException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    protected void refresh(Combo operCombo) {
        try {
            int[] indexes = (int[]) operCombo.getData(DATA_INDEX_KEY);
            Variable variable1 = (Variable) variableBoxes.get(indexes[0])[0].getData(DATA_VARIABLE_KEY);
            if (variable1 != null) {
                FilterBox targetCombo = variableBoxes.get(indexes[0])[1];
                targetCombo.setItems(new String[0]);
                GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
                Operation operation = Operation.getByName(operCombo.getText(), typeSupport);
                operCombo.setData(DATA_OPERATION_KEY, operation);
                for (String variableName : getVariable2Names(variable1)) {
                    targetCombo.add(variableName);
                }
                for (String pv : typeSupport.getPredefinedValues(operation)) {
                    targetCombo.add(pv);
                }
                if (typeSupport.hasUserInputEditor()) {
                    targetCombo.add(TypedUserInputCombo.INPUT_VALUE);
                }
            }
        } catch (RuntimeException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    protected void toCode() {
        for (int i = 0; i < variableBoxes.size(); i++) {
            if (variableBoxes.get(i)[0].getText().length() == 0
                    || variableBoxes.get(i)[1].getText().length() == 0 && !functionButtons.get(i).getText().equals(defaultFunction)) {
                setErrorLabelText(Localization.getString("GroovyEditor.fillAll"));
                // we cannot construct while all data not filled
                return;
            }
        }
        clearErrorLabelText();
        try {
            BusinessRuleModel model = new BusinessRuleModel();
            if (defaultFunction != null) {
                model.setDefaultFunction(defaultFunction);
            }
            for (int i = 0; i < functions.size(); i++) {
                IfExpr ifExpr;
                Variable variable1 = (Variable) variableBoxes.get(i)[0].getData(DATA_VARIABLE_KEY);
                String operationName = operationBoxes.get(i).getItem(operationBoxes.get(i).getSelectionIndex());
                String lexem2Text = variableBoxes.get(i)[1].getText();
                Object lexem2;
                Variable variable2 = VariableUtils.getVariableByScriptingName(variables, lexem2Text);
                if (variable2 != null) {
                    if (variable2.getJavaClassName().equals(variable1.getJavaClassName())) {
                        lexem2 = variable2;
                    } else {
                        lexem2 = lexem2Text;
                    }
                } else {
                    lexem2 = lexem2Text;
                }
                GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
                ifExpr = new IfExpr(functionButtons.get(i).getText(), variable1, lexem2, Operation.getByName(operationName, typeSupport));
                model.addIfExpr(ifExpr);
            }
            styledText.setText(model.toString());
        } catch (RuntimeException e1) {
            PluginLogger.logError(e1);
            setErrorLabelText(Localization.getString("GroovyEditor.error.construct"));
        }
    }

}
