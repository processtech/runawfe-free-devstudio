package ru.runa.gpd.extension.businessRule;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import javafx.collections.ListChangeListener;
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
    private static final Image downImage = SharedImages.getImage("icons/down.png");
    private List<ExprLine> exprLines = new ArrayList();
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
                createLines();
            }
        } else {
            createLines();
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

    private void createExpr(Composite parent, int indexLine) {
        FilterBox[] variable = new FilterBox[2];
        exprLines.get(indexLine).setVariableBox(variable);
        variable[0] = new FilterBox(parent, VariableUtils.getVariableNamesForScripting(variables));
        variable[0].setData(DATA_INDEX_KEY, new int[] { indexLine, exprLines.get(indexLine).getVariableBoxes().size() - 1, 0 });
        variable[0].setSelectionListener(new FilterBoxSelectionHandler());
        variable[0].setLayoutData(getGridData());
        variable[0].setSize(100, 20);

        Combo operation = new Combo(parent, SWT.READ_ONLY);
        exprLines.get(indexLine).setOperationBox(operation);
        operation.setData(DATA_INDEX_KEY, new int[] { indexLine, exprLines.get(indexLine).getOperationBox().size() - 1, 1 });
        operation.addSelectionListener(new ComboSelectionHandler());
        operation.setLayoutData(getGridData());

        variable[1] = new FilterBox(parent, null);
        variable[1].setData(DATA_INDEX_KEY, new int[] { indexLine, exprLines.get(indexLine).getVariableBoxes().size() - 1, 2 });
        variable[1].setSelectionListener(new FilterBoxSelectionHandler());
        variable[1].setLayoutData(getGridData());
        variable[1].setSize(100, 20);
    }

    private void createLines() {
        ExprLine exprLine = new ExprLine();
        exprLines.add(exprLine);
        createExpr(constructor, exprLines.size() - 1);

        Button functionButton = new Button(constructor, SWT.NONE);
        functionButton.setLayoutData(getGridData());
        exprLine.setFunctionButton(functionButton);
        functionButton.setData(exprLines.size() - 1);
        functionButton.setText(Localization.getString("GroovyEditor.functionButton") + exprLines.size());
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

        exprLine.setFunctionButton(functionButton);

        Button addButton = new Button(constructor, SWT.PUSH);
        addButton.setImage(addImage);
        addButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                constructor.getChildren()[constructor.getChildren().length - 1].dispose();
                createLines();
                createBottomComposite();
                constructor.layout();
            }
        });

        constructor.setLayout(new GridLayout(6, false));
        Button addExprButton = new Button(constructor, SWT.TOGGLE);
        addExprButton.setImage(downImage);
        addExprButton.setData(exprLines.size() - 1);
        addExprButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                if (addExprButton.getSelection()) {
                    Composite complexExpression = exprLines.get((int) addExprButton.getData()).getComposite(constructor);
                    complexExpression.moveBelow(addExprButton);
                    constructor.layout();
                } else {
                    Composite complexExpression = exprLines.get((int) addExprButton.getData()).getComposite(constructor);
                    complexExpression.dispose();
                    constructor.layout();
                }
            }
        });
    }

    @Override
    protected void initConstructorView() {
        List<IfExpr> ifExprs = ((BusinessRuleModel) initModel).getIfExprs();
        for (int i = 0; i < ifExprs.size(); i++) {
            IfExpr ifExpr = ifExprs.get(i);
            if (ifExpr != null) {
                ExprLine exprLine = exprLines.get(i);
                exprLine.getFunctionButton().setText(ifExpr.getFunction());
                Variable variable = ifExpr.getVariable1();
                int index = variables.indexOf(variable);
                if (index == -1) {
                    // required variable was deleted in process definition
                    continue;
                }
                exprLine.getVariableBoxes().get(0)[0].select(index);
                refresh(exprLine.getVariableBoxes().get(0)[0]);
                GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                index = Operation.getAll(typeSupport).indexOf(ifExpr.getOperation());
                if (index == -1) {
                    // required operation was deleted !!!
                    continue;
                }
                exprLine.getOperationBox().get(0).select(index);
                refresh(exprLine.getOperationBox().get(0));
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
                        exprLine.getVariableBoxes().get(0)[1].add(lexem2Text, 0);
                        exprLine.getVariableBoxes().get(0)[1].setData(DATA_USER_INPUT_KEY, lexem2Text);
                    }
                }
                exprLine.getVariableBoxes().get(0)[1].select(var2index);
            }
        }
    }

    @Override
    protected void refresh(FilterBox filterBox) {
        try {
            int[] indexes = (int[]) filterBox.getData(DATA_INDEX_KEY);
            if (indexes[2] == 2) {
                // indexes[1] == 2 - second variable
                if (TypedUserInputCombo.INPUT_VALUE.equals(filterBox.getSelectedItem())) {
                    String oldUserInput = (String) filterBox.getData(DATA_USER_INPUT_KEY);
                    Variable variable1 = (Variable) exprLines.get(indexes[0]).getVariableBoxes().get(indexes[1])[0].getData(DATA_VARIABLE_KEY);
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
            if (indexes[2] == 0) {
                // indexes[1] == 0 - first variable
                Combo operCombo = exprLines.get(indexes[0]).getOperationBox().get(indexes[1]);
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
            Variable variable1 = (Variable) exprLines.get(indexes[0]).getVariableBoxes().get(indexes[1])[0].getData(DATA_VARIABLE_KEY);
            if (variable1 != null) {
                FilterBox targetCombo = exprLines.get(indexes[0]).getVariableBoxes().get(indexes[1])[1];
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
        for (int i = 0; i < exprLines.size(); i++) {
            ExprLine exprLine = exprLines.get(i);
            if (exprLine.getVariableBoxes().get(0)[0].getText().length() == 0 || exprLine.getVariableBoxes().get(0)[1].getText().length() == 0
                    && !exprLine.getFunctionButton().getText().equals(defaultFunction)) {
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
            for (int i = 0; i < exprLines.size(); i++) {
                ExprLine exprLine = exprLines.get(i);
                IfExpr ifExpr;
                Variable variable1 = (Variable) exprLine.getVariableBoxes().get(0)[0].getData(DATA_VARIABLE_KEY);
                String operationName = exprLine.getOperationBox().get(0).getItem(exprLine.getOperationBox().get(0).getSelectionIndex());
                String lexem2Text = exprLine.getVariableBoxes().get(0)[1].getText();
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
                ifExpr = new IfExpr(exprLine.getFunctionButton().getText(), variable1, lexem2, Operation.getByName(operationName, typeSupport));
                model.addIfExpr(ifExpr);
            }
            styledText.setText(model.toString());
        } catch (RuntimeException e1) {
            PluginLogger.logError(e1);
            setErrorLabelText(Localization.getString("GroovyEditor.error.construct"));
        }
    }

    private class ExprLine {
        private Composite complexExpression;
        private Button functionButton;
        private List<FilterBox[]> variableBoxes = new ArrayList();
        private List<Combo> operationBoxes = new ArrayList();
        private List<Combo> logicExpr = new ArrayList();
        private int indexLine;

        public Composite getComposite(Composite parent) {
            if (complexExpression == null || complexExpression.isDisposed()) {
                Composite complexExpr = new Composite(constructor, SWT.BORDER);
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                data.horizontalSpan = 6;
                complexExpr.setLayoutData(data);
                complexExpr.setLayout(new GridLayout(4, false));
                complexExpression = complexExpr;
                indexLine = exprLines.indexOf(this);
                createExpr(complexExpr, indexLine);
                createLogicCombo();
                return complexExpr;
            } else {
                return complexExpression;
            }
        }
        
        public void createLogicCombo() {
            Combo logicExpr = new Combo(complexExpression, SWT.READ_ONLY);
            logicExpr.setItems("null", "and", "or");
            logicExpr.select(0);    
            logicExpr.setData(logicExpr.getText());
            logicExpr.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    Combo combo = (Combo) e.getSource();
                    if(combo.getData().equals("null") && !combo.getText().equals("null")) {
                        combo.setData(combo.getText());
                        createExpr(complexExpression, indexLine);
                        createLogicCombo();
                        constructor.layout();
                    } else if(combo.getText().equals("null") && !combo.getData().equals("null")) {
                        combo.setData(combo.getText());
                        complexExpression.getChildren()[complexExpression.getChildren().length - 2].dispose();
                        complexExpression.getChildren()[complexExpression.getChildren().length - 2].dispose();
                        complexExpression.getChildren()[complexExpression.getChildren().length - 2].dispose();
                        complexExpression.getChildren()[complexExpression.getChildren().length - 2].dispose();
                        constructor.layout();
                    }
                }
            });
        }

        public Button getFunctionButton() {
            return functionButton;
        }

        public void setFunctionButton(Button functionButton) {
            this.functionButton = functionButton;
        }

        public void setVariableBox(FilterBox[] filterBox) {
            variableBoxes.add(filterBox);
        }

        public void setOperationBox(Combo operationCombo) {
            operationBoxes.add(operationCombo);
        }

        public List<FilterBox[]> getVariableBoxes() {
            return variableBoxes;
        }

        public List<Combo> getOperationBox() {
            return operationBoxes;
        }

    }

}
