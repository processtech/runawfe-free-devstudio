package ru.runa.gpd.extension.businessRule;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
    private static final int FIRST_VARIABLE_INDEX = 0;
    private static final int SECOND_VARIABLE_INDEX = 1;
    private static final int OPERATION_INDEX = 2;
    
    protected static final String AND_LOGIC_EXPRESSION = "and";
    protected static final String OR_LOGIC_EXPRESSION = "or";
    protected static final String NULL_LOGIC_EXPRESSION = "null";
    
    private static final Image addImage = SharedImages.getImage("icons/add_obj.gif");
    private static final Image downImage = SharedImages.getImage("icons/down.png");
    private List<ExpressionLine> expressionLines = new ArrayList();
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

    protected void createConstructorView() {
        constructor.setLayout(new GridLayout(6, false));
        if (initModel != null) {
            defaultFunction = ((BusinessRuleModel) initModel).getDefaultFunction();
            for (int i = 0; i < ((BusinessRuleModel) initModel).getIfExprs().size(); i++) {
                createExpressionLine();
            }
        } else {
            createExpressionLine();
        }

        createBottomComposite();
    }

    private Composite createBottomComposite() {
        Composite bottomComposite = new Composite(constructor, SWT.NONE);
        bottomComposite.setLayout(new GridLayout(2, true));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 6;
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

    public class SyncFilterBoxHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            FilterBox box = ((FilterBox) e.widget);
            FilterBox targetBox;
            int[] indexData = (int[]) box.getData(DATA_INDEX_KEY);
            int lineNumber = indexData[0];
            int variableNumber = indexData[2];
            if (expressionLines.get(lineNumber).getVariableBoxes().get(0)[variableNumber].equals(box)) {
                targetBox = expressionLines.get(lineNumber).getVariableBoxes().get(1)[variableNumber];
            } else {
                targetBox = expressionLines.get(lineNumber).getVariableBoxes().get(0)[variableNumber];
            }
            refresh(box);
            targetBox.setSelectedItem(box.getText());
            refresh(targetBox);
        }
    }

    public class SyncComboHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            Combo combo = ((Combo) e.widget);
            Combo targetCombo;
            int[] indexData = (int[]) combo.getData(DATA_INDEX_KEY);
            int lineNumber = indexData[0];
            if (expressionLines.get(lineNumber).getOperationBoxes().get(0).equals(combo)) {
                targetCombo = expressionLines.get(lineNumber).getOperationBoxes().get(1);
            } else {
                targetCombo = expressionLines.get(lineNumber).getOperationBoxes().get(0);
            }
            targetCombo.select(combo.getSelectionIndex());
            refresh(combo);
            refresh(targetCombo);
        }
    }

    private void createExpression(Composite parent, int indexLine) {
        ExpressionLine expressionLine = expressionLines.get(indexLine);

        Composite expressionComposite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        if (parent == constructor) {
            expressionComposite.setLayout(new GridLayout(3, true));
            data.horizontalSpan = 3;
        } else {
            expressionComposite.setLayout(new GridLayout(4, true));
            data.horizontalSpan = 4;
        }
        expressionComposite.setLayoutData(data);

        FilterBox[] variable = new FilterBox[2];
        expressionLine.setVariableBox(variable);
        int expressionIndex = expressionLine.getVariableBoxes().indexOf(variable);
        variable[0] = new FilterBox(expressionComposite, VariableUtils.getVariableNamesForScripting(variables));
        variable[0].setData(DATA_INDEX_KEY, new int[] { indexLine, expressionIndex, FIRST_VARIABLE_INDEX });
        variable[0].setSelectionListener(new FilterBoxSelectionHandler());
        variable[0].setLayoutData(getGridData());
        variable[0].setSize(100, 20);

        Combo operation = new Combo(expressionComposite, SWT.READ_ONLY);
        expressionLine.setOperationBox(operation);
        operation.setData(DATA_INDEX_KEY, new int[] { indexLine, expressionIndex, OPERATION_INDEX });
        operation.addSelectionListener(new ComboSelectionHandler());
        operation.setLayoutData(getGridData());

        variable[1] = new FilterBox(expressionComposite, null);
        variable[1].setData(DATA_INDEX_KEY, new int[] { indexLine, expressionIndex, SECOND_VARIABLE_INDEX });
        variable[1].setSelectionListener(new FilterBoxSelectionHandler());
        variable[1].setLayoutData(getGridData());
        variable[1].setSize(100, 20);

        if (expressionIndex == 0 || expressionIndex == 1) {
            variable[0].setSelectionListener(new SyncFilterBoxHandler());
            operation.addSelectionListener(new SyncComboHandler());
            variable[1].setSelectionListener(new SyncFilterBoxHandler());
        }

        Combo logicBox = new Combo(expressionComposite, SWT.READ_ONLY);
        expressionLine.setLogicBox(logicBox);
        logicBox.setItems(NULL_LOGIC_EXPRESSION, AND_LOGIC_EXPRESSION, OR_LOGIC_EXPRESSION);
        logicBox.setLayoutData(getGridData());
        logicBox.select(0);
        logicBox.setData(logicBox.getText());
        logicBox.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                Combo combo = (Combo) e.getSource();
                if (combo.getData().equals(NULL_LOGIC_EXPRESSION) && !combo.getText().equals(NULL_LOGIC_EXPRESSION)) {
                    combo.setData(combo.getText());
                    createExpression(parent, indexLine);
                    constructor.layout();
                } else if (combo.getText().equals(NULL_LOGIC_EXPRESSION) && !combo.getData().equals(NULL_LOGIC_EXPRESSION)) {
                    combo.setData(combo.getText());
                    int logicExpressionIndex = expressionLine.getLogicBoxes().indexOf(combo);
                    while (logicExpressionIndex < parent.getChildren().length) {
                        parent.getChildren()[logicExpressionIndex].dispose();
                        expressionLine.getVariableBoxes().remove(logicExpressionIndex + 1);
                        expressionLine.getOperationBoxes().remove(logicExpressionIndex + 1);
                        expressionLine.getLogicBoxes().remove(logicExpressionIndex + 1);
                        constructor.layout();
                    }
                }
            }
        });
        if (parent == constructor) {
            logicBox.setVisible(false);
            ((GridData) logicBox.getLayoutData()).exclude = true;
        }
        if (expressionLine.getVariableBoxes().size() > 2) {
            expressionLine.getExpressionButton().setBackground(new Color(Display.getCurrent(), 255, 219, 222));
        }

        expressionComposite.pack();
    }

    private void createExpressionLine() {
        ExpressionLine expressionLine = new ExpressionLine();
        expressionLines.add(expressionLine);
        createExpression(constructor, expressionLines.size() - 1);

        Button functionButton = new Button(constructor, SWT.NONE);
        expressionLine.setFunctionButton(functionButton);
        functionButton.setLayoutData(getGridData());
        functionButton.setData(expressionLines.size() - 1);
        functionButton.setText(Localization.getString("GroovyEditor.functionButton") + expressionLines.size());
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

        Button addFunctionButton = new Button(constructor, SWT.PUSH);
        addFunctionButton.setImage(addImage);
        addFunctionButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                constructor.getChildren()[constructor.getChildren().length - 1].dispose();
                createExpressionLine();
                createBottomComposite();
                constructor.layout();
            }
        });

        Button addExpressionButton = new Button(constructor, SWT.TOGGLE);
        expressionLine.setExpressionButton(addExpressionButton);
        addExpressionButton.moveAbove(addFunctionButton);
        addExpressionButton.setToolTipText(Localization.getString("GroovyEditor.complexCondition"));
        addExpressionButton.setImage(downImage);
        addExpressionButton.setData(expressionLines.size() - 1);
        addExpressionButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                if (addExpressionButton.getSelection()) {
                    Composite complexExpression = expressionLines.get((int) addExpressionButton.getData()).getComplexExpression();
                    complexExpression.setVisible(true);
                    ((GridData) complexExpression.getLayoutData()).exclude = false;
                    complexExpression.moveBelow(addFunctionButton);
                    constructor.layout();
                } else {
                    Composite complexExpression = expressionLines.get((int) addExpressionButton.getData()).getComplexExpression();
                    complexExpression.setVisible(false);
                    ((GridData) complexExpression.getLayoutData()).exclude = true;
                    constructor.layout();
                }
            }
        });

        expressionLine.createComplexExpressionBody();
    }

    @Override
    protected void initConstructorView() {
        List<IfExpr> ifExprs = ((BusinessRuleModel) initModel).getIfExprs();
        for (int i = 0; i < ifExprs.size(); i++) {
            IfExpr ifExpr = ifExprs.get(i);
            if (ifExpr != null) {
                ExpressionLine expressionLine = expressionLines.get(i);
                expressionLine.getFunctionButton().setText(ifExpr.getFunction());
                for (int j = 0; j < ifExpr.getFirstVariables().size(); j++) {
                    Variable firstVariable = ifExpr.getFirstVariables().get(j);
                    expressionLine.getLogicBoxes().get(j).select(ifExpr.getLogicExpressions().indexOf(ifExpr.getLogicExpressions().get(j)));
                    expressionLine.getLogicBoxes().get(j).setData(ifExpr.getLogicExpressions().get(j));
                    int firstVariableIndex = variables.indexOf(firstVariable);
                    if (firstVariableIndex == -1) {
                        // required variable was deleted in process definition
                        continue;
                    }
                    expressionLine.getVariableBoxes().get(j)[0].select(firstVariableIndex);
                    refresh(expressionLine.getVariableBoxes().get(j)[0]);
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
                    int operationIndex = Operation.getAll(typeSupport).indexOf(ifExpr.getOperations().get(j));
                    if (operationIndex == -1) {
                        // required operation was deleted !!!
                        continue;
                    }
                    expressionLine.getOperationBoxes().get(j).select(operationIndex);
                    refresh(expressionLine.getOperationBoxes().get(j));
                    String secondVariableText = ifExpr.getSecondVariableTextValue(j);
                    int secondVariableIndex = 0;
                    boolean secondVariableIsNotUserInput = VariableUtils.getVariableByScriptingName(variables, secondVariableText) != null
                            && (VariableUtils.getVariableByScriptingName(variables, secondVariableText).getJavaClassName())
                            .equals(firstVariable.getJavaClassName());
                    if (secondVariableIsNotUserInput) {
                        secondVariableIndex = getSecondVariableNames(firstVariable).indexOf(secondVariableText);
                    } else {
                        int predefinedIndex = typeSupport.getPredefinedValues(ifExpr.getOperations().get(j)).indexOf(secondVariableText);
                        if (predefinedIndex >= 0) {
                            secondVariableIndex = getSecondVariableNames(firstVariable).size() + predefinedIndex;
                        } else {
                            expressionLine.getVariableBoxes().get(j)[1].add(secondVariableText, 0);
                            expressionLine.getVariableBoxes().get(j)[1].setData(DATA_USER_INPUT_KEY, secondVariableText);
                        }
                    }
                    expressionLine.getVariableBoxes().get(j)[1].select(secondVariableIndex);
                }
            }
        }
    }

    @Override
    protected void refresh(FilterBox filterBox) {
        try {
            int[] indexes = (int[]) filterBox.getData(DATA_INDEX_KEY);
            if (indexes[2] == SECOND_VARIABLE_INDEX) {
                if (TypedUserInputCombo.INPUT_VALUE.equals(filterBox.getSelectedItem())) {
                    String oldUserInput = (String) filterBox.getData(DATA_USER_INPUT_KEY);
                    Variable firstVariable = (Variable) expressionLines.get(indexes[0]).getVariableBoxes().get(indexes[1])[0]
                            .getData(DATA_VARIABLE_KEY);
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
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
            if (indexes[2] == FIRST_VARIABLE_INDEX) {
                Combo operationCombo = expressionLines.get(indexes[0]).getOperationBoxes().get(indexes[1]);
                operationCombo.setItems(new String[0]);
                Variable variable = VariableUtils.getVariableByScriptingName(variables, filterBox.getText());
                filterBox.setData(DATA_VARIABLE_KEY, variable);
                if (variable != null) {
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                    for (Operation operation : Operation.getAll(typeSupport)) {
                        operationCombo.add(operation.getVisibleName());
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
            Variable firstVariable = (Variable) expressionLines.get(indexes[0]).getVariableBoxes().get(indexes[1])[0].getData(DATA_VARIABLE_KEY);
            if (firstVariable != null) {
                FilterBox targetCombo = expressionLines.get(indexes[0]).getVariableBoxes().get(indexes[1])[1];
                targetCombo.setItems(new String[0]);
                GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
                Operation operation = Operation.getByName(operCombo.getText(), typeSupport);
                operCombo.setData(DATA_OPERATION_KEY, operation);
                for (String variableName : getSecondVariableNames(firstVariable)) {
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
        for (int i = 0; i < expressionLines.size(); i++) {
            ExpressionLine expressionLine = expressionLines.get(i);
            for (int j = 0; j < expressionLine.getVariableBoxes().size(); j++) {
                boolean emptyFieldExist = expressionLine.getVariableBoxes().get(j)[0].getText().length() == 0
                        || expressionLine.getVariableBoxes().get(j)[1].getText().length() == 0
                                && !expressionLine.getFunctionButton().getText().equals(defaultFunction);
                if (emptyFieldExist) {
                    setErrorLabelText(Localization.getString("GroovyEditor.fillAll"));
                    // we cannot construct while all data not filled
                    return;
                }
            }
        }
        clearErrorLabelText();
        try {
            BusinessRuleModel model = new BusinessRuleModel();
            if (defaultFunction != null) {
                model.setDefaultFunction(defaultFunction);
            }
            for (int i = 0; i < expressionLines.size(); i++) {
                ExpressionLine expressionLine = expressionLines.get(i);
                IfExpr ifExpr;
                List<Variable> firstVariables = new ArrayList();
                List<Object> secondVariables = new ArrayList();
                List<Operation> operations = new ArrayList();
                List<String> logicExprsessions = new ArrayList();
                for (int j = 0; j < expressionLine.getVariableBoxes().size(); j++) {
                    Variable firstVariable = (Variable) expressionLine.getVariableBoxes().get(j)[0].getData(DATA_VARIABLE_KEY);
                    String operationName = expressionLine.getOperationBoxes().get(j)
                            .getItem(expressionLine.getOperationBoxes().get(j).getSelectionIndex());
                    String secondVariableText = expressionLine.getVariableBoxes().get(j)[1].getText();
                    Variable secondVariable = VariableUtils.getVariableByScriptingName(variables, secondVariableText);
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());

                    firstVariables.add(firstVariable);
                    if (secondVariable != null && secondVariable.getJavaClassName().equals(firstVariable.getJavaClassName())) {
                        secondVariables.add(secondVariable);
                    } else {
                        secondVariables.add(secondVariableText);
                    }
                    operations.add(Operation.getByName(operationName, typeSupport));
                    logicExprsessions.add(expressionLine.getLogicBoxes().get(j).getText());
                }
                ifExpr = new IfExpr(expressionLine.getFunctionButton().getText(), firstVariables, secondVariables, operations, logicExprsessions);
                model.addIfExpr(ifExpr);
            }
            styledText.setText(model.toString());
        } catch (RuntimeException e1) {
            PluginLogger.logError(e1);
            setErrorLabelText(Localization.getString("GroovyEditor.error.construct"));
        }
    }

    private class ExpressionLine {
        private Composite complexExpression;
        private Button functionButton;
        private Button addExpressionButton;
        private List<FilterBox[]> variableBoxes = new ArrayList();
        private List<Combo> operationBoxes = new ArrayList();
        private List<Combo> logicBoxes = new ArrayList();
        private int lineIndex;

        public ExpressionLine() {
            complexExpression = new Composite(constructor, SWT.BORDER);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 6;
            complexExpression.setLayoutData(data);
            complexExpression.setLayout(new GridLayout(1, true));
            complexExpression.setVisible(false);
            data.exclude = true;
        }

        public void createComplexExpressionBody() {
            lineIndex = expressionLines.indexOf(this);
            createExpression(complexExpression, lineIndex);
            if (initModel != null) {
                List<IfExpr> ifExprs = ((BusinessRuleModel) initModel).getIfExprs();
                if (ifExprs.size() > lineIndex) {
                    IfExpr ifExpr = ifExprs.get(lineIndex);
                    for (int j = 2; j < ifExpr.getFirstVariables().size(); j++) {
                        createExpression(complexExpression, lineIndex);
                    }
                }
            }
        }

        public Composite getComplexExpression() {
            return complexExpression;
        }

        public Button getFunctionButton() {
            return functionButton;
        }

        public void setFunctionButton(Button functionButton) {
            this.functionButton = functionButton;
        }

        public Button getExpressionButton() {
            return addExpressionButton;
        }

        public void setExpressionButton(Button button) {
            addExpressionButton = button;
        }

        public void setVariableBox(FilterBox[] filterBox) {
            variableBoxes.add(filterBox);
        }

        public void setOperationBox(Combo operationCombo) {
            operationBoxes.add(operationCombo);
        }

        public void setLogicBox(Combo logicBox) {
            logicBoxes.add(logicBox);
        }

        public List<FilterBox[]> getVariableBoxes() {
            return variableBoxes;
        }

        public List<Combo> getOperationBoxes() {
            return operationBoxes;
        }

        public List<Combo> getLogicBoxes() {
            return logicBoxes;
        }

    }

}
