package ru.runa.gpd.extension.businessRule;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import ru.runa.gpd.extension.businessRule.BusinessRuleModel.IfExpression;
import ru.runa.gpd.extension.decision.EditorDialog;
import ru.runa.gpd.extension.decision.GroovyTypeSupport;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.extension.handler.FormulaCellEditorProvider;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.FilterBox;
import ru.runa.gpd.util.VariableUtils;

public class BusinessRuleEditorDialog extends EditorDialog<BusinessRuleModel> {
    protected static final String AND_LOGIC_EXPRESSION = "and";
    protected static final String OR_LOGIC_EXPRESSION = "or";
    protected static final String NULL_LOGIC_EXPRESSION = "null";
    protected static final String DATA_EXPRESSION_LINE = "expressionLine";
    protected static final int SIMPLE_EXPRESSION_INDEX = -1;

    private static final Image addImage = SharedImages.getImage("icons/add_obj.gif");
    private static final Image downImage = SharedImages.getImage("icons/down.png");
    private static final Image deleteImage = SharedImages.getImage("icons/delete.gif");
    private List<ExpressionLine> expressionLines = new ArrayList<>();
    private String defaultFunction;

    private static final int FIRST_VARIABLE_INDEX = 0;
    private static final int SECOND_VARIABLE_INDEX = 1;
    private static final int OPERATION_INDEX = 2;

    public BusinessRuleEditorDialog(ProcessDefinition definition, String initialValue) {
        super(definition, initialValue);
        if (this.initialValue.length() > 0) {
            try {
                initialModel = new BusinessRuleModel(initialValue, variables);
            } catch (Throwable e) {
                initialErrorMessage = e.getMessage();
                PluginLogger.logErrorWithoutDialog("", e);
            }
        }
    }

    @Override
    protected void createConstructorView() {
        createExpressionLine();
        if (initialModel != null) {
            defaultFunction = initialModel.getDefaultFunction();
            for (int i = 1; i < initialModel.getIfExpressions().size(); i++) {
                createExpressionLine();
            }
        }
        createBottomComposite();
        ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
    }

    private void createBottomComposite() {
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
    }

    public void synchronizationFilterBox(FilterBox filterBox) {
        FilterBox box = filterBox;
        FilterBox targetBox;
        int[] indexData = (int[]) box.getData(DATA_INDEX_KEY);
        ExpressionLine expressionLine = (ExpressionLine) box.getData(DATA_EXPRESSION_LINE);
        int variableNumber = indexData[1];
        if (expressionLine.getSimpleVariableBox()[variableNumber].equals(box)) {
            targetBox = expressionLine.getVariableBoxes().get(0)[variableNumber];
        } else {
            targetBox = expressionLine.getSimpleVariableBox()[variableNumber];
        }
        refresh(box);
        targetBox.setSelectedItem(box.getText());
        refresh(targetBox);
    }

    public void synchronizationCombo(Combo comboBox) {
        Combo combo = comboBox;
        Combo targetCombo;
        ExpressionLine expressionLine = (ExpressionLine) combo.getData(DATA_EXPRESSION_LINE);
        if (expressionLine.getSimpleOperationBox().equals(combo)) {
            targetCombo = expressionLine.getOperationBoxes().get(0);
        } else {
            targetCombo = expressionLine.getSimpleOperationBox();
        }
        targetCombo.select(combo.getSelectionIndex());
        refresh(combo);
        refresh(targetCombo);
    }

    private void createExpressionLine() {
        ExpressionLine expressionLine = new ExpressionLine();
        expressionLines.add(expressionLine);
    }

    @Override
    protected void initializeConstructorView() {
        List<IfExpression> ifExpressions = initialModel.getIfExpressions();
        for (int i = 0; i < ifExpressions.size(); i++) {
            IfExpression ifExpression = ifExpressions.get(i);
            if (ifExpression != null) {
                ExpressionLine expressionLine = expressionLines.get(i);
                expressionLine.getFunctionButton().setText(ifExpression.getFunction());
                for (int j = 0; j < ifExpression.getFirstVariables().size(); j++) {
                    Variable firstVariable = ifExpression.getFirstVariables().get(j);
                    expressionLine.getLogicBoxes().get(j)
                            .select(expressionLine.getLogicBoxes().get(j).indexOf(ifExpression.getLogicExpressions().get(j)));
                    expressionLine.getLogicBoxes().get(j).setData(ifExpression.getLogicExpressions().get(j));
                    int firstVariableIndex = variables.indexOf(firstVariable);
                    if (firstVariableIndex == -1) {
                        // required variable was deleted in process definition
                        continue;
                    }
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
                    int operationIndex = Operation.getAll(typeSupport).indexOf(ifExpression.getOperations().get(j));
                    if (operationIndex == -1) {
                        // required operation was deleted !!!
                        continue;
                    }
                    FilterBox firstVariableBox = expressionLine.getVariableBoxes().get(j)[0];
                    Combo operationBox = expressionLine.getOperationBoxes().get(j);
                    FilterBox secondVariableBox = expressionLine.getVariableBoxes().get(j)[1];

                    firstVariableBox.select(firstVariableIndex);
                    if(j==0) {
                        synchronizationFilterBox(firstVariableBox);
                    } else {
                        refresh(firstVariableBox);
                    }
                    operationBox.select(operationIndex);
                    if(j==0) {
                        synchronizationCombo(operationBox);
                    } else {
                        refresh(operationBox);
                    }
                    String secondVariableText = ifExpression.getSecondVariableTextValue(j);
                    int secondVariableIndex = 0;
                    boolean secondVariableIsNotUserInput = VariableUtils.getVariableByScriptingName(variables, secondVariableText) != null
                            && (VariableUtils.getVariableByScriptingName(variables, secondVariableText).getJavaClassName())
                                    .equals(firstVariable.getJavaClassName());
                    if (secondVariableIsNotUserInput) {
                        secondVariableIndex = getSecondVariableNames(firstVariable).indexOf(secondVariableText);
                    } else {
                        int predefinedIndex = typeSupport.getPredefinedValues(ifExpression.getOperations().get(j)).indexOf(secondVariableText);
                        if (predefinedIndex >= 0) {
                            secondVariableIndex = getSecondVariableNames(firstVariable).size() + predefinedIndex;
                        } else {
                            secondVariableBox.add(secondVariableText, 0);
                            secondVariableBox.setData(DATA_USER_INPUT_KEY, secondVariableText);
                        }
                    }
                    secondVariableBox.select(secondVariableIndex);                    
                    if(j==0) {
                        synchronizationFilterBox(secondVariableBox);
                    } else {
                        refresh(secondVariableBox);
                    }
                }
            }
        }
    }

    @Override
    protected void refresh(FilterBox filterBox) {
        try {
            int[] indexes = (int[]) filterBox.getData(DATA_INDEX_KEY);
            ExpressionLine expressionLine = (ExpressionLine) filterBox.getData(DATA_EXPRESSION_LINE);
            if (indexes[1] == SECOND_VARIABLE_INDEX) {
                if (TypedUserInputCombo.INPUT_VALUE.equals(filterBox.getSelectedItem())) {
                    String oldUserInput = (String) filterBox.getData(DATA_USER_INPUT_KEY);
                    Variable firstVariable = (indexes[0] == SIMPLE_EXPRESSION_INDEX)
                            ? (Variable) expressionLine.getSimpleVariableBox()[0].getData(DATA_VARIABLE_KEY)
                            : (Variable) expressionLine.getVariableBoxes().get(indexes[0])[0].getData(DATA_VARIABLE_KEY);
                    refreshFilterBox(filterBox, oldUserInput, firstVariable);
                } else {
                    Variable variable = VariableUtils.getVariableByScriptingName(variables, filterBox.getText());
                    if (variable != null) {
                        filterBox.setData(DATA_VARIABLE_KEY, variable);
                    }
                }
                return;
            }
            if (indexes[1] == FIRST_VARIABLE_INDEX) {
                FilterBox secondVariable = (indexes[0] == SIMPLE_EXPRESSION_INDEX)
                        ? expressionLine.getSimpleVariableBox()[1]
                        : expressionLine.getVariableBoxes().get(indexes[0])[1];
                secondVariable.setSelectedItem("");

                Combo operationCombo = (indexes[0] == SIMPLE_EXPRESSION_INDEX) ? expressionLine.getSimpleOperationBox()
                        : expressionLine.getOperationBoxes().get(indexes[0]);
                refreshFilterBox(filterBox, operationCombo);
            }
        } catch (RuntimeException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    protected void refresh(Combo operationCombo) {
        try {
            int[] indexes = (int[]) operationCombo.getData(DATA_INDEX_KEY);
            ExpressionLine expressionLine = (ExpressionLine) operationCombo.getData(DATA_EXPRESSION_LINE);
            Variable firstVariable = (indexes[0] == SIMPLE_EXPRESSION_INDEX)
                    ? (Variable) expressionLine.getSimpleVariableBox()[0].getData(DATA_VARIABLE_KEY)
                    : (Variable) expressionLine.getVariableBoxes().get(indexes[0])[0].getData(DATA_VARIABLE_KEY);
            if (firstVariable != null) {
                FilterBox targetCombo = (indexes[0] == SIMPLE_EXPRESSION_INDEX) ? expressionLine.getSimpleVariableBox()[1]
                        : expressionLine.getVariableBoxes().get(indexes[0])[1];
                refreshOperationCombo(operationCombo, firstVariable, targetCombo);
            }
        } catch (RuntimeException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    protected void toCode() {
        clearErrorLabelText();
        try {
            BusinessRuleModel model = new BusinessRuleModel();
            if (defaultFunction != null) {
                model.setDefaultFunction(defaultFunction);
            }
            for (ExpressionLine expressionLine : expressionLines) {
                List<Variable> firstVariables = new ArrayList<>();
                List<Object> secondVariables = new ArrayList<>();
                List<Operation> operations = new ArrayList<>();
                List<String> logicExpressions = new ArrayList<>();
                boolean emptyFieldExist = false;
                for (int i = 0; i < expressionLine.getVariableBoxes().size(); i++) {
                    emptyFieldExist = expressionLine.getVariableBoxes().get(i)[0].getText().length() == 0
                            || expressionLine.getVariableBoxes().get(i)[1].getText().length() == 0
                            || expressionLine.getOperationBoxes().get(i).getText().length() == 0
                                    && !expressionLine.getFunctionButton().getText().equals(defaultFunction);
                    if (emptyFieldExist) {
                        setErrorLabelText(Localization.getString("GroovyEditor.fillAll"));
                        if (logicExpressions.size() > 0) {
                            logicExpressions.set(logicExpressions.size() - 1, expressionLine.getLogicBoxes().get(i).getText());
                            emptyFieldExist = false;
                        }
                        continue;
                    }

                    Variable firstVariable = (Variable) expressionLine.getVariableBoxes().get(i)[0].getData(DATA_VARIABLE_KEY);
                    String operationName = expressionLine.getOperationBoxes().get(i)
                            .getItem(expressionLine.getOperationBoxes().get(i).getSelectionIndex());
                    String secondVariableText = expressionLine.getVariableBoxes().get(i)[1].getText();
                    Variable secondVariable = VariableUtils.getVariableByScriptingName(variables, secondVariableText);
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());

                    firstVariables.add(firstVariable);
                    if (secondVariable != null) {
                        secondVariables.add(secondVariable);
                    } else {
                        secondVariables.add(secondVariableText);
                    }
                    operations.add(Operation.getByName(operationName, typeSupport));
                    logicExpressions.add(expressionLine.getLogicBoxes().get(i).getText());
                }
                if (emptyFieldExist) {
                    continue;
                }
                IfExpression ifExpression = new IfExpression(expressionLine.getFunctionButton().getText(), firstVariables, secondVariables,
                        operations, logicExpressions);
                model.addIfExpression(ifExpression);
            }
            styledText.setText(model.toString());
        } catch (RuntimeException e1) {
            PluginLogger.logError(e1);
            setErrorLabelText(Localization.getString("GroovyEditor.error.construct"));
        }
    }

    private class ExpressionLine {
        private FilterBox[] simpleVariableBox;
        private Combo simpleOperationBox;
        private Composite complexExpression;
        private Button functionButton;
        private Button showComplexExpressionButton;
        private List<FilterBox[]> variableBoxes = new ArrayList<>();
        private List<Combo> operationBoxes = new ArrayList<>();
        private List<Combo> logicBoxes = new ArrayList<>();
        private int lineIndex;

        public ExpressionLine() {
            lineIndex = expressionLines.size();
            createExpression(constructor);

            functionButton = new Button(constructor, SWT.NONE);
            functionButton.setLayoutData(getGridData());
            functionButton.setText(Localization.getString("GroovyEditor.functionButton") + lineIndex);
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

            showComplexExpressionButton = new Button(constructor, SWT.TOGGLE);
            showComplexExpressionButton.setToolTipText(Localization.getString("GroovyEditor.complexCondition"));
            showComplexExpressionButton.setImage(downImage);
            showComplexExpressionButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    if (showComplexExpressionButton.getSelection()) {
                        complexExpression.setVisible(true);
                        ((GridData) complexExpression.getLayoutData()).exclude = false;
                        constructor.layout();
                        ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
                    } else {
                        complexExpression.setVisible(false);
                        ((GridData) complexExpression.getLayoutData()).exclude = true;
                        constructor.layout();
                        ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
                    }
                }
            });

            Button addLineButton = new Button(constructor, SWT.PUSH);
            addLineButton.setImage(addImage);
            addLineButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    constructor.getChildren()[constructor.getChildren().length - 1].dispose();
                    createExpressionLine();
                    createBottomComposite();
                    constructor.layout();
                    ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
                }
            });

            Button deleteLineButton = new Button(constructor, SWT.PUSH);
            deleteLineButton.setImage(deleteImage);
            deleteLineButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    expressionLines.remove(lineIndex);
                    // expression line contains 6 elements
                    for (int i = 0; i < 6; i++) {
                        constructor.getChildren()[lineIndex * 6].dispose();
                    }
                    for (ExpressionLine expressionLine : expressionLines) {
                        expressionLine.setLineIndex(expressionLines.indexOf(expressionLine));
                    }
                    // all lines removed
                    if (constructor.getChildren().length == 1) {
                        constructor.getChildren()[constructor.getChildren().length - 1].dispose();
                        initialModel = null;
                        createExpressionLine();
                        createBottomComposite();
                    }
                    constructor.layout();
                    ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
                }
            });

            createComplexExpression();
        }

        public void createComplexExpression() {
            complexExpression = new Composite(constructor, SWT.BORDER);
            complexExpression.setLayout(new GridLayout(1, false));
            complexExpression.setVisible(false);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.exclude = true;
            data.horizontalSpan = 5;
            complexExpression.setLayoutData(data);

            createComplexExpressionLine();
            if (initialModel != null) {
                List<IfExpression> ifExpressions = initialModel.getIfExpressions();
                if (ifExpressions.size() > lineIndex) {
                    IfExpression ifExpression = ifExpressions.get(lineIndex);
                    for (int complexExpressionIndex = 1; complexExpressionIndex < ifExpression.getFirstVariables().size(); complexExpressionIndex++) {
                        // Previous element duplicates simple expression
                        createComplexExpressionLine();
                    }
                }
            }
        }

        private void createComplexExpressionLine() {
            Composite complexExpressionLine = new Composite(complexExpression, SWT.NONE);
            complexExpressionLine.setLayout(new GridLayout(2, false));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 5;
            complexExpressionLine.setLayoutData(data);

            createExpression(complexExpressionLine);
            createLogicComposite(complexExpressionLine);
        }

        private void createExpression(Composite parent) {
            Composite expressionComposite = new Composite(parent, SWT.NONE);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            expressionComposite.setLayout(new GridLayout(3, true));
            data.horizontalSpan = 1;
            expressionComposite.setLayoutData(data);

            boolean isSimpleExpression = parent == constructor;
            int expressionIndex = (isSimpleExpression) ? SIMPLE_EXPRESSION_INDEX : variableBoxes.size();

            FilterBox[] expressionVariables = new FilterBox[2];
            expressionVariables[0] = new FilterBox(expressionComposite, VariableUtils.getVariableNamesForScripting(variables));
            expressionVariables[0].setData(DATA_INDEX_KEY, new int[] { expressionIndex, FIRST_VARIABLE_INDEX });
            expressionVariables[0].setData(DATA_EXPRESSION_LINE, this);
            expressionVariables[0].setSelectionListener(new FilterBoxSelectionHandler());
            expressionVariables[0].setLayoutData(getGridData());

            Combo operation = new Combo(expressionComposite, SWT.READ_ONLY);
            operation.setData(DATA_INDEX_KEY, new int[] { expressionIndex, OPERATION_INDEX });
            operation.setData(DATA_EXPRESSION_LINE, this);
            operation.addSelectionListener(new ComboSelectionHandler());
            operation.setLayoutData(getGridData());

            expressionVariables[1] = new FilterBox(expressionComposite, null);
            expressionVariables[1].setData(DATA_INDEX_KEY, new int[] { expressionIndex, SECOND_VARIABLE_INDEX });
            expressionVariables[1].setData(DATA_EXPRESSION_LINE, this);
            expressionVariables[1].setSelectionListener(new FilterBoxSelectionHandler());
            expressionVariables[1].setLayoutData(getGridData());

            if (expressionIndex == 0 || expressionIndex == SIMPLE_EXPRESSION_INDEX) {
                expressionVariables[0].setSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        synchronizationFilterBox((FilterBox) e.widget);
                    }
                });
                operation.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        synchronizationCombo((Combo) e.widget);
                    }
                });
                expressionVariables[1].setSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        synchronizationFilterBox((FilterBox) e.widget);
                    }
                });
            }

            if (isSimpleExpression) {
                simpleVariableBox = expressionVariables;
                simpleOperationBox = operation;
            } else {
                variableBoxes.add(expressionVariables);
                operationBoxes.add(operation);
            }

            if (variableBoxes.size() > 1) {
                showComplexExpressionButton.setBackground(new Color(Display.getCurrent(), 255, 219, 222));
            }
        }

        public void createLogicComposite(Composite parent) {
            Combo logicBox = new Combo(parent, SWT.READ_ONLY);
            logicBoxes.add(logicBox);
            logicBox.setItems(NULL_LOGIC_EXPRESSION, AND_LOGIC_EXPRESSION, OR_LOGIC_EXPRESSION);
            logicBox.select(0);
            logicBox.setData(logicBox.getText());
            logicBox.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    Combo combo = (Combo) e.getSource();
                    if (combo.getData().equals(NULL_LOGIC_EXPRESSION) && !combo.getText().equals(NULL_LOGIC_EXPRESSION)) {
                        combo.setData(combo.getText());
                        createComplexExpressionLine();
                        constructor.layout();
                        ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
                    } else if (combo.getText().equals(NULL_LOGIC_EXPRESSION) && !combo.getData().equals(NULL_LOGIC_EXPRESSION)) {
                        combo.setData(combo.getText());
                        int logicExpressionIndex = logicBoxes.indexOf(combo) + 1;
                        while (logicExpressionIndex < complexExpression.getChildren().length) {
                            complexExpression.getChildren()[logicExpressionIndex].dispose();
                            variableBoxes.remove(logicExpressionIndex);
                            operationBoxes.remove(logicExpressionIndex);
                            logicBoxes.remove(logicExpressionIndex);
                            constructor.layout();
                            ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
                        }
                    }
                }
            });
        }

        public Button getFunctionButton() {
            return functionButton;
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

        public FilterBox[] getSimpleVariableBox() {
            return simpleVariableBox;
        }

        public Combo getSimpleOperationBox() {
            return simpleOperationBox;
        }

        public void setLineIndex(int lineIndex) {
            this.lineIndex = lineIndex;
        }
    }

}
