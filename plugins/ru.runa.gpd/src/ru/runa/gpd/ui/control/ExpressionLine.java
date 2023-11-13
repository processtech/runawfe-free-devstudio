package ru.runa.gpd.ui.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.decision.GroovyTypeSupport;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.FilterBox;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.gpd.util.UserFriendlyException;
import ru.runa.gpd.util.VariableUtils;

public class ExpressionLine extends Composite {
    private int variableWidthHint = 100; // предпочитаемая ширина для виджетов,
    // представляющих выбор 1 переменной и ее отображение, это из исходника
    // (исходник - код аналогичной функциональности для бизнес правил)
    private static final Image addImage = SharedImages.getImage("icons/add_obj.gif"); // зеленый плюсик
    private static final Image deleteImage = SharedImages.getImage("icons/delete.gif"); // красный крестик

    private List<Variable> variables;

    private FilterBox[] lineVariables;
    private Combo operationCombo;
    private Button deleteButton;
    private Button addButton;
    private CloseBracketButtonAndLogicOperationComboAndOpenBracketButton logicComposite;
    private GridLayout expressionLayout;
    private Composite mainPart;
    Composite mainPartAndBrackets; // в нем скобки и mainPart

    private String secondVariableComboLastInput = null; // последнее, что было введено
    // в lineVariables[1] с помощью ввода значения с помощью "Введите значение"
    private int tabSize = 20; // размер сдвига вправо для каждой единицы глубины вложенности
    private int bracketConst = 3; // настраивает рисование скобок
    private PaintListener openBracketPaintListener = new PaintListener() {

        @Override
        public void paintControl(PaintEvent e) {
            Canvas canvas = (Canvas) e.widget;
            Rectangle clientArea = canvas.getClientArea();
            Display display = getDisplay();
            e.gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
            e.gc.fillPolygon(new int[] { 0, clientArea.height, 0, 0, clientArea.width, 0, clientArea.width, clientArea.height,
                    clientArea.width - clientArea.height / bracketConst, clientArea.height, clientArea.width - clientArea.height / bracketConst,
                    clientArea.height / bracketConst, clientArea.height / bracketConst, clientArea.height / bracketConst,
                    clientArea.height / bracketConst, clientArea.height });
        }
    };
    private PaintListener closeBracketPaintListener = new PaintListener() {

        @Override
        public void paintControl(PaintEvent e) {
            Canvas canvas = (Canvas) e.widget;
            Rectangle clientArea = canvas.getClientArea();
            Display display = getDisplay();
            e.gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
            e.gc.fillPolygon(new int[] { 0, clientArea.height, 0, 0, clientArea.height / bracketConst, 0, clientArea.height / bracketConst,
                    clientArea.height - clientArea.height / bracketConst, clientArea.width - clientArea.height / bracketConst,
                    clientArea.height - clientArea.height / bracketConst, clientArea.width - clientArea.height / bracketConst, 0, clientArea.width, 0,
                    clientArea.width, clientArea.height });
        }
    };
    private Point anotherExpressionLineMainPartSize = null;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     * @param variables
     */
    public ExpressionLine(Composite parent, int style, List<Variable> variables) {
        super(parent, SWT.NONE);
        this.variables = variables;
        GridLayout globalLayout = new GridLayout(1, false);
        globalLayout.marginWidth = 0;
        globalLayout.marginHeight = 0;
        globalLayout.verticalSpacing = 20;
        this.setLayout(globalLayout);
        mainPartAndBrackets = new Composite(this, SWT.NONE);
        mainPartAndBrackets.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout mainPartAndBracketsLayout = new GridLayout(1, false);
        mainPartAndBracketsLayout.marginWidth = 0;
        mainPartAndBracketsLayout.marginHeight = 0;
        mainPartAndBracketsLayout.verticalSpacing = 0;
        mainPartAndBrackets.setLayout(mainPartAndBracketsLayout);
        mainPart = new Composite(mainPartAndBrackets, SWT.NONE);
        mainPart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        expressionLayout = new GridLayout(5, false);
        expressionLayout.marginWidth = 0;
        expressionLayout.marginHeight = 0;
        expressionLayout.verticalSpacing = 0; // из исходника
        mainPart.setLayout(expressionLayout);

        lineVariables = new FilterBox[2];
        // далее делаем виджеты ввода первой переменной - ее выбор и то, где отображается выбранное
        lineVariables[0] = new FilterBox(mainPart, VariableUtils.getVariableNamesForScripting(variables));
        GridData variable1LayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        variable1LayoutData.widthHint = variableWidthHint;
        lineVariables[0].setLayoutData(variable1LayoutData);
        lineVariables[0].setMarginHeight(0);
        lineVariables[0].setMarginWidth(0);
        lineVariables[0].setMarginRight(5);
        lineVariables[0].setSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setOperationComboItems();
            }
        });
        // далее делаем combo box для выбора операции
        operationCombo = new Combo(mainPart, SWT.READ_ONLY); // SWT.READ_ONLY, чтобы на Ubuntu
        // горизонтальные границы этого Combo не вылезали за скругленные углы при малом размере виджета
        GridData operationLayoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        operationLayoutData.widthHint = 135; // предполагая, что, в отличие от переменных,
        // названия операций не делаются произвольно большими, устанавливаем их ширину так,
        // чтобы влезло самое длинное на текущий момент название операции - "меньше или равно"
        operationCombo.setLayoutData(operationLayoutData);
        operationCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setSecondVariableComboItems();
            }
        });
        // далее делаем виджеты ввода второго операнда
        lineVariables[1] = new FilterBox(mainPart, null); // null - как в исходнике
        GridData variable2LayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        variable2LayoutData.widthHint = variableWidthHint;
        lineVariables[1].setLayoutData(variable2LayoutData);
        lineVariables[1].setMarginHeight(0);
        lineVariables[1].setSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (lineVariables[1].getSelectedItem().equals(TypedUserInputCombo.INPUT_VALUE)) { // если из списка выбрано "Введите значение"
                    Variable firstVariable = VariableUtils.getVariableByScriptingName(variables, lineVariables[0].getText());
                    GroovyTypeSupport firstVariableTypeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
                    UserInputDialog inputDialog = firstVariableTypeSupport.createUserInputDialog(); // диалог для ввода значения
                    inputDialog.setInitialValue(secondVariableComboLastInput);
                    if (inputDialog.open() == org.eclipse.jface.window.Window.OK) {
                        String userInput = inputDialog.getUserInput();
                        if (secondVariableComboLastInput != null) {
                            lineVariables[1].remove(0); // удаляем из списка значение предыдущего ввода значения
                        }
                        secondVariableComboLastInput = userInput;
                        lineVariables[1].add(userInput, 0);
                        lineVariables[1].select(0);

                    } else {
                        lineVariables[1].deselectAll();
                    }
                }
            }
        });
        // далее создание виджетов - кнопки-скобки и логическая операция
        logicComposite = new CloseBracketButtonAndLogicOperationComboAndOpenBracketButton(mainPart, SWT.NONE);
        logicComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        // добавление кнопок "крестик" и "плюсик"
        Composite buttonsComposite = new Composite(mainPart, SWT.NONE);
        GridData buttonsLayoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        buttonsLayoutData.minimumWidth = 100; // из исходника
        buttonsComposite.setLayoutData(buttonsLayoutData);
        GridLayout buttonsLayout = new GridLayout(2, true);
        buttonsLayout.marginWidth = 0; // из исходника
        buttonsLayout.marginHeight = 0;
        buttonsLayout.marginLeft = 5; // чтобы обособить кнопки от того, что слева
        buttonsComposite.setLayout(buttonsLayout);
        addButton = new Button(buttonsComposite, SWT.PUSH); // добавление кнопки "плюсик"
        addButton.setImage(addImage);
        deleteButton = new Button(buttonsComposite, SWT.PUSH); // добавление кнопки "крестик"
        deleteButton.setImage(deleteImage);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    private void setOperationComboItems() // выставляет items в operationCombo на основании типа первой переменной
    {
        operationCombo.removeAll();
        Variable firstVariable = VariableUtils.getVariableByScriptingName(variables, lineVariables[0].getText());
        GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
        for (Operation operation : Operation.getAll(typeSupport)) {
            operationCombo.add(operation.getVisibleName());
        }
    }

    private void setSecondVariableComboItems() // выставляет items в combo второго операнда
    { // на основании первой переменной и операции
        lineVariables[1].setItems(new String[0]); // удалить все items
        lineVariables[1].deselectAll(); // даже без items текст остается, его надо убрать
        Variable firstVariable = VariableUtils.getVariableByScriptingName(variables, lineVariables[0].getText());
        GroovyTypeSupport firstVariableTypeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
        for (Variable variable : this.variables) {
            GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
            if (firstVariableTypeSupport.getClass() == typeSupport.getClass() && firstVariable != variable) {
                // добавление в Items переменных того же класса, что и первая переменная
                lineVariables[1].add(variable.getScriptingName());
            }
        }
        Operation operation = Operation.getByName(operationCombo.getText(), firstVariableTypeSupport);
        for (String predefinedValue : firstVariableTypeSupport.getPredefinedValues(operation)) {
            lineVariables[1].add(predefinedValue); // добавление предопределенных значений, возможных для данного типа операции
        }
        if (firstVariableTypeSupport.hasUserInputEditor()) { // из исходника, может добавиться Item "Введите значение"
            lineVariables[1].add(TypedUserInputCombo.INPUT_VALUE);
        }
    }

    public void addDeleteButtonSelectionListener(SelectionListener listener) {
        deleteButton.addSelectionListener(listener);
    }

    public void addAddButtonSelectionListener(SelectionListener listener) {
        addButton.addSelectionListener(listener);
    }

    public void addCloseButtonSelectionListener(SelectionListener listener) {
        logicComposite.addCloseButtonSelectionListener(listener);
    }

    public void addOpenButtonSelectionListener(SelectionListener listener) {
        logicComposite.addOpenButtonSelectionListener(listener);
    }

    public void addComboBoxSelectionListener(SelectionListener listener) {
        logicComposite.addComboBoxSelectionListener(listener);
    }

    public boolean isOpenButtonSelected() {
        return logicComposite.isOpenButtonSelected();
    }

    public boolean isCloseButtonSelected() {
        return logicComposite.isCloseButtonSelected();
    }

    public void setLogicCompositeVisible(boolean visible) { // устанавливает видимость logicComposite
        logicComposite.setVisible(visible);
    }

    public void setTabNumber(int number) {
        this.expressionLayout.marginLeft = number * this.tabSize;
        if (this.expressionLayout2 != null) {
            this.expressionLayout2.marginLeft = number * this.tabSize;
        }
    }

    public void setOpenBrackets(int count) { // устанавливает в GUI count открывающих скобок выше линии выражения
        for (Control i : mainPartAndBrackets.getChildren()) { // удаление всех открывающих скобок
            if (i == mainPart) {
                break;
            }
            i.dispose();
        }
        if (count > 0) {
            this.addOpenBrackets(count);
        }
    }

    private void addOpenBrackets(int count) {
        for (int i = 0; i < count; i++) {
            Canvas openBracket = new Canvas(mainPartAndBrackets, SWT.NONE);
            GridData openBracketLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
            openBracketLayoutData.heightHint = this.getBracketSize().y;
            openBracket.setLayoutData(openBracketLayoutData);
            openBracket.moveAbove(mainPart);
            openBracket.addPaintListener(openBracketPaintListener);
        }
    }

    public void setCloseBrackets(int count) { // устанавливает в GUI count закрываюших скобок ниже линии выражения
        Control[] subControls = mainPartAndBrackets.getChildren();
        for (int i = subControls.length - 1; i >= 0; i--) { // удаление всех закрывающих скобок
            if (subControls[i] == mainPart) {
                break;
            }
            subControls[i].dispose();
        }
        if (count > 0) {
            this.addCloseBrackets(count);
        }
    }

    private void addCloseBrackets(int count) {
        for (int i = 0; i < count; i++) {
            Canvas closeBracket = new Canvas(mainPartAndBrackets, SWT.NONE);
            GridData closeBracketLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
            closeBracketLayoutData.heightHint = this.getBracketSize().y;
            closeBracket.setLayoutData(closeBracketLayoutData);
            closeBracket.addPaintListener(closeBracketPaintListener);
        }
    }

    private Point getBracketSize() { // возвращает размер виджета, представляющего скобку, рисующуюся вверху или внизу линии выражения
        Point mainPartSize = mainPart.getSize();
        if (mainPartSize.x == 0 && mainPartSize.y == 0 && this.anotherExpressionLineMainPartSize != null) {
            mainPartSize = this.anotherExpressionLineMainPartSize;
        }
        return new Point(mainPartSize.x, mainPartSize.y / 2);
    }

    public void setOpenBracketSelection(boolean selected) {
        logicComposite.setOpenButtonSelection(selected);
    }

    public void setCloseBracketSelection(boolean selected) {
        logicComposite.setCloseButtonSelection(selected);
    }

    public void setTabSize(int size) {
        this.tabSize = size;
    }

    public void setWidgetTabNumber(int widgetNumber, int tabNumber) {
        // для элемента ExpressionLine (элементы - открывающие скобки, основная часть линии
        // выражения, закрывающие скобки) с номером widgetNumber устанавливает сдвиг в tabNumber
        // tabs. Нумерация идет с 0, самая верхняя открывающая скобка имеет номер 0, самая
        // нижняя закрывающая скобка имеет наибольший номер
        Control widget = mainPartAndBrackets.getChildren()[widgetNumber];
        if (widget == this.mainPart) {
            this.setTabNumber(tabNumber);
        } else {
            GridData gridData = (GridData) widget.getLayoutData();
            gridData.horizontalIndent = tabNumber * this.tabSize;
        }
    }

    public int getWidgetNumber() {
        return mainPartAndBrackets.getChildren().length;
    }

    public Point getMainPartSize() {
        return this.mainPart.getSize();
    }

    public void giveSourceForSizing(ExpressionLine expressionLine) {
        // так как размер скобок вычисляется на основе размера основной части, размер основной
        // части должен быть установлен, что делается, только когда виджет уже нарисован в GUI,
        // иначе размер основной части - (0,0). Если основная часть не нарисована, то можно
        // сообщить о другой подобной части в другом виджете. Этот метод позволяет взять из другой
        // ExpressionLine, уже нарисованной, необходимую информацию о ее размерах, чтобы на ее
        // основе подсчитать размеры своих скобок
        this.anotherExpressionLineMainPartSize = expressionLine.getMainPartSize();
    }

    public void setLogicPuttedOut(boolean puttedOut) {
        boolean logicAlreadyPuttedOut = this.getChildren().length > 1;
        if (puttedOut && !logicAlreadyPuttedOut) {
            this.putOutLogic();
        } else if (!puttedOut && logicAlreadyPuttedOut) {
            this.returnLogicBack();
        }
    }

    private Composite mainPartAndBrackets2;
    private GridLayout expressionLayout2;

    private void putOutLogic() {
        // для того, чтобы вынести логическую операцию за нарисованные скобки, и разместить ее там
        // под местом, откуда она выносится, делаем там аналогичные виджеты, как в линии выражения,
        // но невидимые, чтобы вынесенная логическая операция имела аналогичное смещение по x
        mainPartAndBrackets2 = new Composite(this, SWT.NONE);
        mainPartAndBrackets2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout mainPartAndBracketsLayout2 = new GridLayout(1, false);
        mainPartAndBracketsLayout2.verticalSpacing = 0;
        mainPartAndBracketsLayout2.marginHeight = 0;
        mainPartAndBracketsLayout2.marginWidth = 0;
        mainPartAndBrackets2.setLayout(mainPartAndBracketsLayout2);
        Composite mainPart2 = new Composite(mainPartAndBrackets2, SWT.NONE);
        mainPart2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        expressionLayout2 = new GridLayout(5, false);
        expressionLayout2.verticalSpacing = 0; // из исходника
        expressionLayout2.marginLeft = expressionLayout.marginLeft;
        expressionLayout2.marginWidth = 0;
        expressionLayout2.marginHeight = 0;
        mainPart2.setLayout(expressionLayout2);

        FilterBox[] lineVariables2 = new FilterBox[2];
        lineVariables2[0] = new FilterBox(mainPart2, VariableUtils.getVariableNamesForScripting(variables));
        GridData variable1LayoutData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
        variable1LayoutData2.widthHint = variableWidthHint;
        lineVariables2[0].setLayoutData(variable1LayoutData2);
        lineVariables2[0].setMarginHeight(0);
        lineVariables2[0].setMarginWidth(0);
        lineVariables2[0].setMarginRight(5);
        lineVariables2[0].setVisible(false);

        Combo operationCombo2 = new Combo(mainPart2, SWT.NONE);
        GridData operationLayoutData2 = new GridData(SWT.FILL, SWT.CENTER, false, false);
        operationLayoutData2.widthHint = 135;
        operationCombo2.setLayoutData(operationLayoutData2);
        operationCombo2.setVisible(false);

        lineVariables2[1] = new FilterBox(mainPart2, null); // null - как в исходнике
        GridData variable2LayoutData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
        variable2LayoutData2.widthHint = variableWidthHint;
        lineVariables2[1].setLayoutData(variable2LayoutData2);
        lineVariables2[1].setMarginHeight(0);

        lineVariables2[1].setVisible(false);
        CloseBracketButtonAndLogicOperationComboAndOpenBracketButton logicComposite2 = new CloseBracketButtonAndLogicOperationComboAndOpenBracketButton(
                mainPart2, SWT.NONE);
        logicComposite2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        logicComposite2.replaceLastPart(logicComposite.getCombo(), logicComposite.getOpenButton());
        logicComposite.addLastPartSpace();
        logicComposite2.setCloseButtonVisible(false);
        // добавление кнопок "крестик" и "плюсик"
        Composite buttonsComposite2 = new Composite(mainPart2, SWT.NONE);
        GridData buttonsLayoutData2 = new GridData(SWT.FILL, SWT.CENTER, false, false);
        buttonsLayoutData2.minimumWidth = 100; // из исходника
        buttonsComposite2.setLayoutData(buttonsLayoutData2);
        GridLayout buttonsLayout2 = new GridLayout(2, true);
        buttonsLayout2.marginWidth = 0; // из исходника
        buttonsLayout2.marginHeight = 0;
        buttonsLayout2.marginLeft = 5;
        buttonsComposite2.setLayout(buttonsLayout2);
        Button addButton2 = new Button(buttonsComposite2, SWT.PUSH); // добавление кнопки "плюсик"
        addButton2.setImage(addImage);
        Button deleteButton2 = new Button(buttonsComposite2, SWT.PUSH); // добавление кнопки "крестик"
        deleteButton2.setImage(deleteImage);
        buttonsComposite2.setVisible(false);
    }

    private void returnLogicBack() {
        logicComposite.removeLastPartSpace();
        logicComposite.returnLastPart();
        mainPartAndBrackets2.dispose();
    }

    public String getCodeGroovy() throws UserFriendlyException {
        // возвращает код, представляющий то, что введено в виджетах. Добавляется только то, что в
        // mainPart (скобки добавляются только те, которые взяты из нажатий кнопок скобок)
        Variable firstVariable = VariableUtils.getVariableByScriptingName(variables, lineVariables[0].getText());
        if (firstVariable == null) {
            throw this.exceptionFromGetCodeGroovy(firstVariableParamName);
        }
        GroovyTypeSupport firstVariableTypeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
        Operation operation = Operation.getByName(operationCombo.getText(), firstVariableTypeSupport);
        if (operation == null) {
            throw this.exceptionFromGetCodeGroovy(operationParamName);
        }
        String secondComboText = lineVariables[1].getText();
        if (secondComboText.equals("")) {
            throw this.exceptionFromGetCodeGroovy(secondOperandParamName);
        }
        Variable secondVariable = VariableUtils.getVariableByScriptingName(variables, secondComboText);
        Object secondComboInput;
        if (secondVariable != null) { // если в Combo была выбрана переменная
            secondComboInput = secondVariable;
        } else { // если в Combo была выбрана переменная
            secondComboInput = secondComboText;
        }
        String operationCode = operation.generateCode(firstVariable, secondComboInput);
        String logicCompositeCode = "";
        if (logicComposite.isVisible()) {
            if (logicComposite.isCloseButtonSelected()) {
                logicCompositeCode += ")";
            }
            String comboText = logicComposite.getCombo().getText();
            if (comboText.equals(CloseBracketButtonAndLogicOperationComboAndOpenBracketButton.AND_LOGIC_EXPRESSION)) {
                logicCompositeCode += " && ";
            } else if (comboText.equals(CloseBracketButtonAndLogicOperationComboAndOpenBracketButton.OR_LOGIC_EXPRESSION)) {
                logicCompositeCode += " || ";
            } else if (comboText.equals("")) {
                throw this.exceptionFromGetCodeGroovy(logicOperationParamName);
            }
            if (logicComposite.isOpenButtonSelected()) {
                logicCompositeCode += "(";
            }
        }
        return operationCode + logicCompositeCode;
    }

    private UserFriendlyException exceptionFromGetCodeGroovy(String missingInput) {
        UserFriendlyException exception = new UserFriendlyException("element with id '" + missingInput + "' is not set");
        exception.setLocalizedMessage(Localization.getString("ExpressionLine.getCodeGroovy.errorMessage",
                Localization.getString("ExpressionLine.getCodeGroovy." + missingInput)));
        return exception;
    }

    private static final String firstVariableParamName = "firstVariable";
    private static final String operationParamName = "operation";
    private static final String secondOperandParamName = "secondOperand";
    private static final String secondOperandTypeParamName = "secondOperandType";
    private static final String secondOperandIsStringValueParamName = "secondOperandIsStringValue";
    private static final String isCloseButtonSelectedParamName = "isCloseButtonSelected";
    private static final String logicOperationParamName = "logicOperation";
    private static final String isOpenButtonSelectedParamName = "isOpenButtonSelected";
    private static Set<String> paramsNames = new HashSet<>();
    {
        paramsNames.add(firstVariableParamName);
        paramsNames.add(operationParamName);
        paramsNames.add(secondOperandParamName);
        paramsNames.add(secondOperandTypeParamName);
        paramsNames.add(secondOperandIsStringValueParamName);
        paramsNames.add(isCloseButtonSelectedParamName);
        paramsNames.add(logicOperationParamName);
        paramsNames.add(isOpenButtonSelectedParamName);
    }

    public Set<String> getParamsNames() {
        return paramsNames;
    }

    private static final String operandTypeVariable = "variable";
    private static final String operandTypePredefinedValue = "predefinedValue";
    private static final String operandTypeValue = "value";
    private static final String operandTypeValueOrPredefinedValue = "valueOrPredefinedValue";

    public Map<String, String> getParamsForSaving() {
        // возвращает то, что введено, в виде отображения Map<String, String>
        // для operationCombo используется getText, в предположении, что этот метод не будет
        // выполняться, когда то, что введено - некорректно
        Map<String, String> paramsForSaving = new HashMap<>();
        paramsForSaving.put(firstVariableParamName, lineVariables[0].getText());
        paramsForSaving.put(secondOperandParamName, lineVariables[1].getText());
        paramsForSaving.put(secondOperandIsStringValueParamName, String.valueOf(false));
        paramsForSaving.put(isCloseButtonSelectedParamName, String.valueOf(logicComposite.isCloseButtonSelected()));
        paramsForSaving.put(logicOperationParamName, logicComposite.getCombo().getText());
        paramsForSaving.put(isOpenButtonSelectedParamName, String.valueOf(logicComposite.isOpenButtonSelected()));
        // Далее помещение типа второго операнда. Это нужно, например, когда при загрузке из
        // сохранения второй операнд - строка, и тогда неизвестно, это значение-строка, или
        // ранее была переменная с таким именем, но она была удалена. Метод Operation.generateCode,
        // используемый для генерирования кода, не умеет отличать null и строку "null". Поэтому
        // тут тоже отличие не находится, так как неясно, как задать "null"
        // Predefined value отличается от просто value тем, что при его загрузке
        // в combo второго операнда не помещается item
        Variable firstVariable = VariableUtils.getVariableByScriptingName(variables, lineVariables[0].getText());
        GroovyTypeSupport firstVariableTypeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
        boolean secondOperandTypeFound = false;
        for (Variable variable : this.variables) {
            GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
            if (firstVariableTypeSupport.getClass() == typeSupport.getClass() && firstVariable != variable) {
                if (lineVariables[1].getText().equals(variable.getScriptingName())) {
                    secondOperandTypeFound = true;
                    paramsForSaving.put(secondOperandTypeParamName, operandTypeVariable);
                    break;
                }
            }
        }
        // операцию получаем вне следующего if, так как она понадобится дальше вне этого if
        Operation operation = Operation.getByName(operationCombo.getText(), firstVariableTypeSupport);
        if (!secondOperandTypeFound) {
            for (String predefinedValue : firstVariableTypeSupport.getPredefinedValues(operation)) {
                if (lineVariables[1].getText().equals(predefinedValue)) {
                    secondOperandTypeFound = true;
                    paramsForSaving.put(secondOperandTypeParamName, operandTypePredefinedValue);
                    break;
                }
            }
            if (!secondOperandTypeFound) {
                paramsForSaving.put(secondOperandTypeParamName, operandTypeValue);
                if (firstVariableTypeSupport instanceof GroovyTypeSupport.StringType) {
                    paramsForSaving.put(secondOperandIsStringValueParamName, String.valueOf(true));
                }
            }
        }
        // далее помещаем операцию. Помещается operator, а не visible name, так как visible name
        // меняется в зависимости от выбранного языка, что создает трудности с восстановлением
        // операции из ее имени на другом языке, а это бывает необходимо, если в программе был
        // сменен язык до загрузки операции из сохранения
        paramsForSaving.put(operationParamName, operation.getOperator());
        return paramsForSaving;
    }

    public void initializeFromSaving(Map<String, String> params) throws UserFriendlyException, Exception {
        // далее заполнение информации о logicComposite
        logicComposite.setCloseButtonSelection(Boolean.parseBoolean(params.get(isCloseButtonSelectedParamName)));
        Combo logicCompositeCombo = logicComposite.getCombo();
        logicCompositeCombo.select(logicCompositeCombo.indexOf(params.get(logicOperationParamName)));
        logicComposite.setOpenButtonSelection(Boolean.parseBoolean(params.get(isOpenButtonSelectedParamName)));

        // далее заполнение информации об операции и операндах.
        // Для операции и операндов, если какая-то часть сохранения некорректна,
        // например из-за удаления переменной, то она не вводится, а остальное вводится

        // для combo первой переменной уже установлены items
        String firstVariableName = params.get(firstVariableParamName);
        Variable firstVariable = VariableUtils.getVariableByScriptingName(variables, firstVariableName);
        if (firstVariable == null) {
            throw this.exceptionFromInitializeFromSaving("firstVariableNotFound", firstVariableName);
        } else {
            lineVariables[0].setSelectedItem(firstVariableName);
            // сначала нужно установить items для combo операции
            operationCombo.removeAll();
            GroovyTypeSupport firstVariableTypeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
            for (Operation op : Operation.getAll(firstVariableTypeSupport)) {
                operationCombo.add(op.getVisibleName());
            }
            String operationCode = params.get(operationParamName); // имя операции на Groovy, например "!="
            Operation operation;
            try {
                operation = Operation.getByOperator(operationCode, firstVariableTypeSupport);
            } catch (RuntimeException e) {
                if (e.getMessage().equals("Operation not found for operator: " + operationCode)) {
                    throw this.exceptionFromInitializeFromSaving("operationNotFoundForOperator", operationCode);
                } else {
                    throw e;
                }
            }
            operationCombo.select(operationCombo.indexOf(operation.getVisibleName()));
            // сначала нужно установить items для combo второго операнда
            this.setSecondVariableComboItems();
            String secondComboText = params.get(secondOperandParamName);
            String secondOperandType = params.get(secondOperandTypeParamName);
            if (secondOperandType == null) {
                throw new Exception("second operand type is null");
            } else if (secondOperandType.equals(operandTypeVariable)) {
                boolean secondOperandVariableWithSuitableTypeFoundAndIsNotFirstVariable = false;
                boolean secondOperandVariableWithSuitableTypeFound = false;
                boolean secondOperandVariableFound = false;
                // далее проверка, что вторая переменная подходит по типу, и проверка, что эта переменная есть
                for (Variable variable : this.variables) {
                    if (secondComboText.equals(variable.getScriptingName())) {
                        secondOperandVariableFound = true;
                        GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                        if (firstVariableTypeSupport.getClass() == typeSupport.getClass()) {
                            secondOperandVariableWithSuitableTypeFound = true;
                            if (firstVariable != variable) {
                                secondOperandVariableWithSuitableTypeFoundAndIsNotFirstVariable = true;
                                break;
                            }
                        }
                    }
                }
                if (secondOperandVariableWithSuitableTypeFoundAndIsNotFirstVariable) {
                    lineVariables[1].setSelectedItem(secondComboText);
                } else if (secondOperandVariableWithSuitableTypeFound) {
                    throw this.exceptionFromInitializeFromSaving("secondVariableIsFirstVariable", secondComboText);
                } else if (secondOperandVariableFound) {
                    throw this.exceptionFromInitializeFromSaving("secondVariableNotSuitableByType", secondComboText);
                } else {
                    throw this.exceptionFromInitializeFromSaving("secondVariableNotFound", secondComboText);
                }
            } else if (secondOperandType.equals(operandTypePredefinedValue)) {
                boolean secondOperandPredefinedValueFound = false;
                for (String predefinedValue : firstVariableTypeSupport.getPredefinedValues(operation)) {
                    if (secondComboText.equals(predefinedValue)) {
                        secondOperandPredefinedValueFound = true;
                        break;
                    }
                }
                if (secondOperandPredefinedValueFound) {
                    lineVariables[1].setSelectedItem(secondComboText);
                } else {
                    throw new Exception("cannot find predefined value '" + secondComboText + "'");
                }
            } else if (secondOperandType.equals(operandTypeValue)) {
                // загружается значение, которое отсутствует в combo во втором FilterBox
                boolean isStringValue = Boolean.parseBoolean(params.get(secondOperandIsStringValueParamName));
                if (isValueTypeSuitable(firstVariableTypeSupport, secondComboText, isStringValue)) {
                    lineVariables[1].add(secondComboText);
                    lineVariables[1].setSelectedItem(secondComboText);
                } else {
                    throw this.exceptionFromInitializeFromSaving("ValueTypeIsNotSuitable");
                }

            } else if (secondOperandType.equals(operandTypeValueOrPredefinedValue)) {
                // если второй операнд - значение, но неизвестно, predefined ли оно.
                // Такое появляется при парсинге выражения
                boolean secondOperandPredefinedValueFound = false;
                for (String predefinedValue : firstVariableTypeSupport.getPredefinedValues(operation)) {
                    if (secondComboText.equals(predefinedValue)) {
                        secondOperandPredefinedValueFound = true;
                        break;
                    }
                }
                if (!secondOperandPredefinedValueFound) {
                    boolean isStringValue = Boolean.parseBoolean(params.get(secondOperandIsStringValueParamName));
                    if (isValueTypeSuitable(firstVariableTypeSupport, secondComboText, isStringValue)) {
                        // проверка, что значение второго операнда подходит по типу
                        lineVariables[1].add(secondComboText);
                    } else {
                        throw this.exceptionFromInitializeFromSaving("ValueTypeIsNotPredefinedAndIsNotSuitable");
                    }
                }
                lineVariables[1].setSelectedItem(secondComboText);
            } else {
                throw new Exception("second operand type wrong");
            }
        }
    }

    private UserFriendlyException exceptionFromInitializeFromSaving(String errorDescription) {
        UserFriendlyException exception = new UserFriendlyException("error id: " + errorDescription);
        exception.setLocalizedMessage(Localization.getString("ExpressionLine.initializeFromSaving." + errorDescription));
        return exception;
    }

    private UserFriendlyException exceptionFromInitializeFromSaving(String errorDescription, String errorSource) {
        UserFriendlyException exception = new UserFriendlyException("error id: " + errorDescription + " error object: " + errorSource);
        exception.setLocalizedMessage(
                Localization.getString("ExpressionLine.initializeFromSaving." + errorDescription, Localization.getString(errorSource)));
        return exception;
    }

    boolean isValueTypeSuitable(GroovyTypeSupport firstVariableTypeSupport, String value, boolean isString) {
        if (firstVariableTypeSupport instanceof GroovyTypeSupport.StringType && !isString) {
            return false;
        }
        if (firstVariableTypeSupport.hasUserInputEditor()) {
            return firstVariableTypeSupport.createUserInputDialog().getValidationFunction().test(value);
        } else {
            return false;
        }
    }

    public static class ExpressionLineModel {
        // модель с данными ExpressionLine, используется для загрузки из сохранения
        private Map<String, String> params = null;

        public ExpressionLineModel() {
            this.params = new HashMap<>();
        }

        public Map<String, String> getParamsMap() {
            return this.params;
        }

        public void setOpenBracketExist(boolean exist) {
            this.params.put(isOpenButtonSelectedParamName, String.valueOf(exist));
        }

        public void setCloseBracketExist(boolean exist) {
            this.params.put(isCloseButtonSelectedParamName, String.valueOf(exist));
        }

        public void setLogicOperationGroovy(String operation) {// на входе - логическая операция на Groovy
            if (operation.equals("&&")) {
                this.params.put(logicOperationParamName, CloseBracketButtonAndLogicOperationComboAndOpenBracketButton.AND_LOGIC_EXPRESSION);
            } else if (operation.equals("||")) {
                this.params.put(logicOperationParamName, CloseBracketButtonAndLogicOperationComboAndOpenBracketButton.OR_LOGIC_EXPRESSION);
            }
            // если подана неизвестная операция, она игнорируется
        }

        public void setFirstOperand(String operand) {
            this.params.put(firstVariableParamName, operand);
        }

        public void setSecondOperand(String operand) {
            this.params.put(secondOperandParamName, operand);
        }

        public void setOperation(String operation) {
            this.params.put(operationParamName, operation);
        }

        public void setSecondOperandTypeVariable() {
            this.params.put(secondOperandTypeParamName, operandTypeVariable);
        }

        public void setSecondOperandTypePredefinedValue() {
            this.params.put(secondOperandTypeParamName, operandTypePredefinedValue);
        }

        public void setSecondOperandTypeValue() {
            this.params.put(secondOperandTypeParamName, operandTypeValue);
        }

        public void setSecondOperandTypeValueOrPredefinedValue() {
            this.params.put(secondOperandTypeParamName, operandTypeValueOrPredefinedValue);
        }

        public void setSecondOperandIsStringValue(boolean isString) {
            this.params.put(secondOperandIsStringValueParamName, String.valueOf(isString));
        }
    }
}
