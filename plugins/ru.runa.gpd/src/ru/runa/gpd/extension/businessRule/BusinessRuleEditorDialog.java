package ru.runa.gpd.extension.businessRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.businessRule.BusinessRuleModel.IfExpression;
import ru.runa.gpd.extension.decision.EditorDialog;
import ru.runa.gpd.extension.decision.GroovyCodeParser;
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
    private static final String DATA_EXPRESSION_LINE = "expressionLine";

    private static final int FIRST_VARIABLE_INDEX = 0;
    private static final int SECOND_VARIABLE_INDEX = 1;
    private static final int OPERATION_INDEX = 2;

    private static final Image addImage = SharedImages.getImage("icons/add_obj.gif");
    private static final Image deleteImage = SharedImages.getImage("icons/delete.gif");
    private List<ExpressionLine> expressionLines = new ArrayList<>();
    private Button defaultButton;
    private BusinessRuleModel tempModel;

    public BusinessRuleEditorDialog(ProcessDefinition definition, String initialValue) {
        super(definition, initialValue);
        if (this.initialValue.length() > 0) {
            Optional<BusinessRuleModel> optionalModel = GroovyCodeParser.parseBusinessRuleModel(initialValue, variables);
            if (optionalModel.isPresent()) {
                initialModel = optionalModel.get();
            } else {
                initialErrorMessage = "failed to parse";
            }
        }
    }

    @Override
    protected void createConstructorView() {
        if (initialModel == null) {
            createExpressionLine(0, null);
        } else {
            for (int i = 0; i < initialModel.getIfExpressions().size(); i++) {
                createExpressionLine(i, initialModel);
            }
        }
        createBottomComposite();
    }

    private ExpressionLine createExpressionLine(int index, BusinessRuleModel model) {
        ExpressionLine expressionLine = new ExpressionLine(index, model);
        expressionLines.add(index, expressionLine);
        return expressionLine;
    }

    private void createBottomComposite() {
        Composite bottomComposite = new Composite(constructor, SWT.NONE);
        bottomComposite.setLayout(new GridLayout(2, false));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 5;
        bottomComposite.setLayoutData(data);

        Label defaultLabel = new Label(bottomComposite, SWT.NONE);
        defaultLabel.setText(Localization.getString("GroovyEditor.allOtherCases") + ":");

        defaultButton = new Button(bottomComposite, SWT.NONE);
        defaultButton.setLayoutData(getGridData());
        defaultButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                FormulaCellEditorProvider.ConfigurationDialog dialog = new FormulaCellEditorProvider.ConfigurationDialog(defaultButton.getText(),
                        variableNames);
                if (dialog.open() == Window.OK) {
                    defaultButton.setText(dialog.getResult());
                }
            }
        });
    }

    private void initialize(IfExpression ifExpression, ExpressionLine expressionLine) {
        expressionLine.getFunctionButton().setText(ifExpression.getFunction());
        int bracketsCount = 0;
        for (int i = 0; i < ifExpression.getFirstVariables().size(); i++) {
            Variable firstVariable = ifExpression.getFirstVariables().get(i);
            Combo logicBox = expressionLine.getLogicComposites().get(i).getLogicBox();
            logicBox.select(logicBox.indexOf(ifExpression.getLogicExpressions().get(i)));
            expressionLine.getLogicComposites().get(i).setBrackets(ifExpression.getBrackets().get(i));
            int firstVariableIndex = variables.indexOf(firstVariable);
            if (firstVariableIndex == -1) {
                // required variable was deleted in process definition
                continue;
            }
            GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
            int operationIndex = Operation.getAll(typeSupport).indexOf(ifExpression.getOperations().get(i));
            if (operationIndex == -1) {
                // required operation was deleted !!!
                continue;
            }
            FilterBox firstVariableBox = expressionLine.getVariableBoxes().get(i)[0];
            firstVariableBox.select(firstVariableIndex);
            refresh(firstVariableBox);
            Combo operationBox = expressionLine.getOperationBoxes().get(i);
            operationBox.select(operationIndex);
            refresh(operationBox);
            FilterBox secondVariableBox = expressionLine.getVariableBoxes().get(i)[1];
            String secondVariableText = ifExpression.getSecondVariableTextValue(i);
            int secondVariableIndex = 0;
            boolean secondVariableIsNotUserInput = VariableUtils.getVariableByScriptingName(variables, secondVariableText) != null
                    && (VariableUtils.getVariableByScriptingName(variables, secondVariableText).getJavaClassName())
                            .equals(firstVariable.getJavaClassName());
            if (secondVariableIsNotUserInput) {
                secondVariableIndex = getSecondVariableNames(firstVariable).indexOf(secondVariableText);
            } else {
                int predefinedIndex = typeSupport.getPredefinedValues(ifExpression.getOperations().get(i)).indexOf(secondVariableText);
                if (predefinedIndex >= 0) {
                    secondVariableIndex = getSecondVariableNames(firstVariable).size() + predefinedIndex;
                } else {
                    secondVariableBox.add(secondVariableText, 0);
                    secondVariableBox.setData(DATA_USER_INPUT_KEY, secondVariableText);
                }
            }
            secondVariableBox.select(secondVariableIndex);

            expressionLine.getLogicComposites().get(i).updateVerticalMargin(i);
            bracketsCount += expressionLine.getLogicComposites().get(i).getBrackets()[0];
            ((GridLayout) ((Composite) expressionLine.getExpressionComposite().getChildren()[i]).getLayout()).marginLeft = bracketsCount
                    * LogicComposite.MARGIN_LEFT_STEP;
            bracketsCount -= expressionLine.getLogicComposites().get(i).getBrackets()[1];
        }
    }

    @Override
    protected void initializeConstructorView() {
        List<IfExpression> ifExpressions = initialModel.getIfExpressions();
        for (int i = 0; i < ifExpressions.size(); i++) {
            IfExpression ifExpression = ifExpressions.get(i);
            if (ifExpression != null) {
                ExpressionLine expressionLine = expressionLines.get(i);
                initialize(ifExpression, expressionLine);
            }
        }
        if (initialModel.getDefaultFunction() != null) {
            defaultButton.setText(initialModel.getDefaultFunction());
        }
        ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
    }

    @Override
    protected void refresh(FilterBox filterBox) {
        try {
            int[] indexes = (int[]) filterBox.getData(DATA_INDEX_KEY);
            ExpressionLine expressionLine = (ExpressionLine) filterBox.getData(DATA_EXPRESSION_LINE);
            if (indexes[1] == SECOND_VARIABLE_INDEX) {
                if (TypedUserInputCombo.INPUT_VALUE.equals(filterBox.getSelectedItem())) {
                    String oldUserInput = (String) filterBox.getData(DATA_USER_INPUT_KEY);
                    Variable firstVariable = (Variable) expressionLine.getVariableBoxes().get(indexes[0])[0].getData(DATA_VARIABLE_KEY);
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
                FilterBox secondVariable = expressionLine.getVariableBoxes().get(indexes[0])[1];
                secondVariable.setSelectedItem("");

                Combo operationCombo = expressionLine.getOperationBoxes().get(indexes[0]);
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
            Variable firstVariable = (Variable) expressionLine.getVariableBoxes().get(indexes[0])[0].getData(DATA_VARIABLE_KEY);
            if (firstVariable != null) {
                FilterBox targetCombo = expressionLine.getVariableBoxes().get(indexes[0])[1];
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
            tempModel = new BusinessRuleModel();
            BusinessRuleModel model = new BusinessRuleModel();
            for (ExpressionLine expressionLine : expressionLines) {
                List<Variable> firstVariables = new ArrayList<>();
                List<Object> secondVariables = new ArrayList<>();
                List<Operation> operations = new ArrayList<>();
                List<String> logicExpressions = new ArrayList<>();
                List<int[]> brackets = new ArrayList<>();
                boolean emptyFieldExist = false;
                for (int i = 0; i < expressionLine.getVariableBoxes().size(); i++) {
                    int[] bracket = expressionLine.getLogicComposites().get(i).getBrackets().clone();
                    brackets.add(bracket);

                    emptyFieldExist = expressionLine.getVariableBoxes().get(i)[0].getText().length() == 0
                            || expressionLine.getVariableBoxes().get(i)[1].getText().length() == 0
                            || expressionLine.getOperationBoxes().get(i).getText().length() == 0
                                    && !expressionLine.getFunctionButton().getText().equals(defaultButton.getText());
                    if (emptyFieldExist) {
                        setErrorLabelText(Localization.getString("GroovyEditor.fillAll"));
                        if (logicExpressions.size() > 0) {
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
                    if (i == expressionLine.getVariableBoxes().size() - 1) {
                        logicExpressions.add(LogicComposite.NULL_LOGIC_EXPRESSION);
                    } else {
                        logicExpressions.add(expressionLine.getLogicComposites().get(i).getLogicBox().getText());
                    }
                }
                IfExpression ifExpression = new IfExpression(expressionLine.getFunctionButton().getText(), firstVariables, secondVariables,
                        operations, logicExpressions, brackets);
                tempModel.addIfExpression(ifExpression);
                if (emptyFieldExist) {
                    continue;
                }
                model.setDefaultFunction(defaultButton.getText());
                model.addIfExpression(ifExpression);
            }
            styledText.setText(model.toString());
        } catch (RuntimeException e1) {
            PluginLogger.logError(e1);
            setErrorLabelText(Localization.getString("GroovyEditor.error.construct"));
        }
    }

    protected class ExpressionLine extends Composite {
        private static final int ADD_DELETE_COMPOSITE_INDEX = 4;

        private Composite expressionsComposite = new Composite(this, SWT.NONE);
        private Button complexExpressionButton;
        private Button functionButton;
        private List<FilterBox[]> variableBoxes = new ArrayList<>();
        private List<Combo> operationBoxes = new ArrayList<>();
        private List<LogicComposite> logicComposites = new ArrayList<>();
        private int lineIndex;

        public ExpressionLine(int index, BusinessRuleModel model) {
            super(constructor, SWT.NONE);
            lineIndex = index;
            setLayout(new GridLayout(4, false));
            GridData expressionLineData = new GridData(GridData.FILL_HORIZONTAL);
            expressionLineData.horizontalSpan = 5;
            setLayoutData(expressionLineData);

            expressionsComposite.setLayout(new GridLayout(1, false));
            expressionsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            complexExpressionButton = new Button(this, SWT.NONE);
            GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
            buttonData.minimumWidth = 420;
            buttonData.horizontalIndent = 15;
            complexExpressionButton.setLayoutData(buttonData);
            complexExpressionButton.setText(Localization.getString("GroovyEditor.complexExpressionButton"));

            complexExpressionButton.addMouseTrackListener(new MouseTrackListener() {
                @Override
                public void mouseHover(MouseEvent e) {
                    toCode();
                    complexExpressionButton.setToolTipText(tempModel.getIfExpressions().get(lineIndex).generateCode());
                }

                @Override
                public void mouseExit(MouseEvent e) {
                }

                @Override
                public void mouseEnter(MouseEvent e) {
                }
            });

            complexExpressionButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    Dialog dialog = new Dialog(Display.getCurrent().getActiveShell()) {
                        ExpressionLine line;
                        Composite composite;

                        {
                            setShellStyle(getShellStyle() | SWT.RESIZE);
                        }

                        @Override
                        protected Point getInitialSize() {
                            return new Point(700, 400);
                        }

                        @Override
                        protected Control createDialogArea(Composite parent) {
                            getShell().setMinimumSize(getInitialSize());
                            getShell().setText(Localization.getString("GroovyEditor.title"));

                            ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
                            scrolledComposite.setExpandHorizontal(true);
                            scrolledComposite.setExpandVertical(true);
                            scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

                            try {
                                toCode();
                                clearErrorLabelText();
                                line = new ExpressionLine(lineIndex, tempModel);
                                line.swapToComplex();

                                if (tempModel.getIfExpressions().size() > lineIndex) {
                                    initialize(tempModel.getIfExpressions().get(lineIndex), line);
                                }

                                composite = line.getExpressionComposite();
                                composite.setParent(scrolledComposite);
                                scrolledComposite.setContent(composite);
                                composite.setVisible(true);
                                ((GridData) composite.getLayoutData()).exclude = false;
                                composite.layout();
                                scrolledComposite.setMinSize(expressionsComposite.computeSize(SWT.MIN, SWT.DEFAULT));
                            } catch (Throwable e) {
                                initialErrorMessage = e.getMessage();
                                PluginLogger.logErrorWithoutDialog("", e);
                            }
                            return scrolledComposite;
                        }

                        @Override
                        protected void okPressed() {
                            for (int i = line.getVariableBoxes().size() - 1; i >= 0; i--) {
                                boolean emptyFieldExist = line.getVariableBoxes().get(i)[0].getText().length() == 0
                                        || line.getVariableBoxes().get(i)[1].getText().length() == 0
                                        || line.getOperationBoxes().get(i).getText().length() == 0;
                                if (emptyFieldExist) {
                                    line.dellExpression(i);
                                }
                            }

                            composite.setParent(line.getExpressionLine());
                            composite.moveAbove(line.getExpressionLine().getChildren()[0]);
                            composite.setVisible(false);
                            ((GridData) composite.getLayoutData()).exclude = true;

                            expressionLines.set(lineIndex, line);
                            line.getExpressionLine().moveAbove(complexExpressionButton.getParent());
                            complexExpressionButton.getParent().dispose();
                            constructor.layout();
                            super.okPressed();
                        }

                        @Override
                        protected void cancelPressed() {
                            line.getExpressionLine().dispose();
                            super.cancelPressed();
                        }
                    };

                    dialog.open();
                }
            });

            if (model == null || model.getIfExpressions().size() <= lineIndex) {
                createExpression(0);
                swapToSimple();
            } else {
                List<IfExpression> ifExpressions = model.getIfExpressions();
                IfExpression ifExpression = ifExpressions.get(lineIndex);
                createExpression(0);
                for (int i = 1; i < ifExpression.getFirstVariables().size(); i++) {
                    createExpression(i);
                }
                if (ifExpression.getFirstVariables().size() == 1) {
                    swapToSimple();
                } else {
                    swapToComplex();
                }
            }

            functionButton = new Button(this, SWT.NONE);
            functionButton.setLayoutData(getVariableGridData());
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

            Button addChangeButton = new Button(this, SWT.PUSH);
            addChangeButton.setImage(addImage);
            addChangeButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    Menu menu = new Menu(Display.getCurrent().getActiveShell(), SWT.POP_UP);

                    MenuItem add = new MenuItem(menu, SWT.PUSH);
                    add.setText(Localization.getString("GroovyEditor.addChangeButtonMenu.add"));
                    MenuItem addComplex = new MenuItem(menu, SWT.PUSH);
                    addComplex.setText(Localization.getString("GroovyEditor.addChangeButtonMenu.addComplex"));
                    MenuItem change = new MenuItem(menu, SWT.PUSH);
                    change.setText(Localization.getString("GroovyEditor.addChangeButtonMenu.change"));

                    Point loc = addChangeButton.getLocation();
                    Rectangle rect = addChangeButton.getBounds();
                    Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
                    menu.setLocation(Display.getCurrent().map(addChangeButton.getParent(), null, mLoc));
                    menu.setVisible(true);

                    add.addSelectionListener(new LoggingSelectionAdapter() {
                        @Override
                        protected void onSelection(SelectionEvent e) throws Exception {
                            ExpressionLine line = createExpressionLine(lineIndex + 1, null);
                            line.swapToSimple();
                            line.getExpressionLine().moveBelow(addChangeButton.getParent());
                            for (ExpressionLine expressionLine : expressionLines) {
                                expressionLine.setLineIndex(expressionLines.indexOf(expressionLine));
                            }
                            constructor.layout();
                            ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
                        }
                    });

                    addComplex.addSelectionListener(new LoggingSelectionAdapter() {
                        @Override
                        protected void onSelection(SelectionEvent e) throws Exception {
                            ExpressionLine line = createExpressionLine(lineIndex + 1, null);
                            line.swapToComplex();
                            line.getExpressionLine().moveBelow(addChangeButton.getParent());
                            for (ExpressionLine expressionLine : expressionLines) {
                                expressionLine.setLineIndex(expressionLines.indexOf(expressionLine));
                            }
                            constructor.layout();
                            ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
                        }
                    });

                    change.addSelectionListener(new LoggingSelectionAdapter() {
                        @Override
                        protected void onSelection(SelectionEvent e) throws Exception {
                            if (complexExpressionButton.getVisible()) {
                                logicComposites.get(0).getBrackets()[0] = 0;
                                logicComposites.get(0).getBrackets()[1] = 0;
                                logicComposites.get(0).updateVerticalMargin(0);
                                while (logicComposites.size() > 1) {
                                    expressionsComposite.getChildren()[1].dispose();
                                    variableBoxes.remove(1);
                                    operationBoxes.remove(1);
                                    logicComposites.remove(1);
                                    expressionsComposite.layout();
                                }
                                swapToSimple();
                                constructor.layout();
                            } else {
                                swapToComplex();
                                constructor.layout();
                            }
                        }
                    });
                }
            });

            Button deleteLineButton = new Button(this, SWT.PUSH);
            deleteLineButton.setImage(deleteImage);
            deleteLineButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    expressionLines.remove(lineIndex);
                    ((Button) e.getSource()).getParent().dispose();
                    for (ExpressionLine expressionLine : expressionLines) {
                        expressionLine.setLineIndex(expressionLines.indexOf(expressionLine));
                    }
                    if (constructor.getChildren().length == 1) {
                        initialModel = null;
                        ExpressionLine line = createExpressionLine(expressionLines.size(), null);
                        line.getExpressionLine().moveAbove(constructor.getChildren()[0]);
                    }
                    constructor.layout();
                    ((ScrolledComposite) constructor.getParent()).setMinSize(constructor.computeSize(SWT.MIN, SWT.DEFAULT));
                }
            });
        }

        protected void swapToComplex() {
            Composite expression = (Composite) expressionsComposite.getChildren()[0];
            ((GridLayout) expression.getLayout()).numColumns = 5;
            expression.getChildren()[ADD_DELETE_COMPOSITE_INDEX].setVisible(true);
            ((GridData) expression.getChildren()[ADD_DELETE_COMPOSITE_INDEX].getLayoutData()).exclude = false;
            ((GridData) logicComposites.get(0).getLayoutData()).exclude = false;
            expressionsComposite.setVisible(false);
            ((GridData) expressionsComposite.getLayoutData()).exclude = true;
            complexExpressionButton.setVisible(true);
            ((GridData) complexExpressionButton.getLayoutData()).exclude = false;
            expressionsComposite.layout();
        }

        protected void swapToSimple() {
            Composite expression = (Composite) expressionsComposite.getChildren()[0];
            ((GridLayout) expression.getLayout()).numColumns = 3;
            ((GridLayout) expression.getLayout()).marginLeft = 0;
            expression.getChildren()[ADD_DELETE_COMPOSITE_INDEX].setVisible(false);
            ((GridData) expression.getChildren()[ADD_DELETE_COMPOSITE_INDEX].getLayoutData()).exclude = true;
            logicComposites.get(0).setVisible(false);
            ((GridData) logicComposites.get(0).getLayoutData()).exclude = true;
            expressionsComposite.setVisible(true);
            ((GridData) expressionsComposite.getLayoutData()).exclude = false;
            complexExpressionButton.setVisible(false);
            ((GridData) complexExpressionButton.getLayoutData()).exclude = true;
            expressionsComposite.layout();
        }

        private void createExpression(int index) {
            Composite expression = new Composite(expressionsComposite, SWT.NONE);
            GridLayout expressionLayout = new GridLayout(5, true);
            expressionLayout.verticalSpacing = 0;
            expression.setLayout(expressionLayout);
            expression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            FilterBox[] expressionVariables = new FilterBox[2];
            expressionVariables[0] = new FilterBox(expression, VariableUtils.getVariableNamesForScripting(variables));
            expressionVariables[0].setData(DATA_INDEX_KEY, new int[] { index, FIRST_VARIABLE_INDEX });
            expressionVariables[0].setData(DATA_EXPRESSION_LINE, this);
            expressionVariables[0].setSelectionListener(new FilterBoxSelectionHandler());
            expressionVariables[0].setLayoutData(getVariableGridData());

            Combo operation = new Combo(expression, SWT.READ_ONLY);
            operation.setData(DATA_INDEX_KEY, new int[] { index, OPERATION_INDEX });
            operation.setData(DATA_EXPRESSION_LINE, this);
            operation.addSelectionListener(new ComboSelectionHandler());
            operation.setLayoutData(getVariableGridData());

            expressionVariables[1] = new FilterBox(expression, null);
            expressionVariables[1].setData(DATA_INDEX_KEY, new int[] { index, SECOND_VARIABLE_INDEX });
            expressionVariables[1].setData(DATA_EXPRESSION_LINE, this);
            expressionVariables[1].setSelectionListener(new FilterBoxSelectionHandler());
            expressionVariables[1].setLayoutData(getVariableGridData());

            LogicComposite logicComposite = new LogicComposite(expression, logicComposites);

            variableBoxes.add(index, expressionVariables);
            operationBoxes.add(index, operation);
            logicComposites.add(index, logicComposite);

            if (index == logicComposites.size() - 1) {
                logicComposite.setVisible(false);
                if (logicComposites.size() > 1) {
                    logicComposites.get(logicComposites.indexOf(logicComposite) - 1).setVisible(true);
                }
            } else {
                expression.moveAbove(expressionsComposite.getChildren()[index]);
            }

            Composite addDeleteComposite = new Composite(expression, SWT.NONE);
            GridLayout layout = new GridLayout(2, true);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            addDeleteComposite.setLayout(layout);
            addDeleteComposite.setLayoutData(getGridData());

            Button deleteButton = new Button(addDeleteComposite, SWT.PUSH);
            deleteButton.setImage(deleteImage);
            deleteButton.setLayoutData(new GridData());
            deleteButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    dellExpression(logicComposites.indexOf(logicComposite));
                }
            });

            Button addButton = new Button(addDeleteComposite, SWT.PUSH);
            addButton.setImage(addImage);
            addButton.setLayoutData(new GridData());
            addButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    int newLogicCompositeIndex = logicComposites.indexOf(logicComposite) + 1;
                    createExpression(newLogicCompositeIndex);

                    if (logicComposite.getCloseButton().getSelection()) {
                        if (newLogicCompositeIndex != logicComposites.size() - 1 && !logicComposite.getOpenButton().getSelection()) {
                            ((GridLayout) logicComposites.get(newLogicCompositeIndex).getParent()
                                    .getLayout()).marginLeft = ((GridLayout) logicComposites.get(newLogicCompositeIndex + 1).getParent()
                                            .getLayout()).marginLeft;
                        }
                    } else {
                        ((GridLayout) logicComposites.get(newLogicCompositeIndex).getParent().getLayout()).marginLeft = ((GridLayout) expression
                                .getLayout()).marginLeft;
                    }
                    if (logicComposite.getOpenButton().getSelection()) {
                        Button newOpenButton = logicComposites.get(newLogicCompositeIndex).getOpenButton();
                        logicComposite.getOpenButton().setSelection(false);
                        newOpenButton.setSelection(true);
                    }

                    for (int i = 0; i < logicComposites.size(); i++) {
                        variableBoxes.get(i)[0].setData(DATA_INDEX_KEY, new int[] { i, FIRST_VARIABLE_INDEX });
                        operationBoxes.get(i).setData(DATA_INDEX_KEY, new int[] { i, OPERATION_INDEX });
                        variableBoxes.get(i)[1].setData(DATA_INDEX_KEY, new int[] { i, SECOND_VARIABLE_INDEX });
                    }
                    expressionsComposite.layout();
                    ((ScrolledComposite) expressionsComposite.getParent()).setMinSize(expressionsComposite.computeSize(SWT.MIN, SWT.DEFAULT));
                }
            });
            expression.addPaintListener(new BracketPaintListener(logicComposites, expression, logicComposite));
        }

        private void dellExpression(int index) {
            if (logicComposites.size() > 1) {
                logicComposites.get(index).updateBeforeDeletion();

                expressionsComposite.getChildren()[index].dispose();
                variableBoxes.remove(index);
                operationBoxes.remove(index);
                logicComposites.remove(index);
                expressionsComposite.layout();

                for (int i = 0; i < logicComposites.size(); i++) {
                    variableBoxes.get(i)[0].setData(DATA_INDEX_KEY, new int[] { i, FIRST_VARIABLE_INDEX });
                    operationBoxes.get(i).setData(DATA_INDEX_KEY, new int[] { i, OPERATION_INDEX });
                    variableBoxes.get(i)[1].setData(DATA_INDEX_KEY, new int[] { i, SECOND_VARIABLE_INDEX });
                }
                logicComposites.get(0).updateAfterDeletion();
            }
        }

        private GridData getVariableGridData() {
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.widthHint = 120;
            return data;
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

        public void setLineIndex(int lineIndex) {
            this.lineIndex = lineIndex;
        }

        public Composite getExpressionComposite() {
            return expressionsComposite;
        }

        public Composite getExpressionLine() {
            return this;
        }

        public List<LogicComposite> getLogicComposites() {
            return logicComposites;
        }

        public Button getComplexExpressionButton() {
            return complexExpressionButton;
        }
    }
}