package ru.runa.gpd.ui.dialog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.decision.GroovyCodeParser;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.control.ExpressionLine;
import ru.runa.gpd.util.UserFriendlyException;

public class GlobalValidatorExpressionConstructorDialog extends Dialog {
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("GroovyEditor.title.constructor"));
    }

    private List<Variable> variables;

    // далее части GUI, которые нужны во многих методах
    private Composite inScrolled;
    private ScrolledComposite scrolledComposite;
    private Composite errorMessageComposite;
    private Label errorMessageLabel;

    private List<ExpressionLine> expressionLinesList = new LinkedList<>();
    // необходимо хранить этот список для итерации по этим элементам, так как другой способ -
    // итерация по потомкам композита, где они помещены, не работает из за того, что не получается
    // преобразование Control к типу ExpressionLine: появляется ClassCastException,
    // вызванная различными загрузчиками классов для этих двух классов, что скорее всего из-за
    // того, как устроен OSGi

    // при нажатии некоторых кнопок может делаться сразу много изменений в GUI. Чтобы все эти
    // изменения делались за один раз, обновление GUI на основе сделанных изменений делается
    // только один раз, методом refreshGUI()

    // скобки в последней линии выражения скрываются, и если они были нажаты,
    // то при скрытии их нажатие убирается, чтобы оно точно не учитывалось
    private int globalTabSize = 20;
    private Map<String, String> initializationParams = null; // данные, из которых
    // будет заполняться конструктор сложного условия.
    private String initializationParamsString = null;
    private String initializationExpression = null;
    private Map<String, String> stateAfterInitialization = null; // состояние диалога после
    // инициализации, используется для определения dirty
    // заполняется при инициализации: При загрузке из Map сюда помещаются те из
    // initializationParams, которые использовались для заполнения ввода диалога.
    // При загрузке из String или Expression выполняется загрузка из Map,
    // и поэтому это все равно заполняется.
    private Map<String, String> paramsForSaving = null; // это заполняется содержанием виджетов
    // конструктора при нажатии OK и может быть запрошено у этого класса для сохранения
    // информации о том, что было введено в конструктор
    private boolean dirty = false; // изменилось ли выражение в диалоге после открытия диалога

    private enum InitializationType // тип инициализации - из каких данных диалог будет
    { // загружать сохранение при открытии диалога - из строки, из Map, или из строки-выражения
        MAP,
        STRING_BASE_64,
        EXPRESSION
    }

    private InitializationType initializationType = null;
    private SelectionAdapter bracketsButtonSelectionAdapter = new SelectionAdapter() {
        // один адаптер может быть использован как слушатель сразу для всех кнопок-скобок, так как
        // действия при нажатии не зависят от того, какая именно кнопка-скобка нажимается
        @Override
        public void widgetSelected(SelectionEvent e) {
            handleBracketsChanged();
            refreshGUI();
        }
    };

    /**
     * Create the dialog.
     * 
     * @param parentShell
     * @param variables
     */
    public GlobalValidatorExpressionConstructorDialog(Shell parentShell, List<Variable> variables) {
        super(parentShell);
        setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.CLOSE | SWT.MAX);
        this.variables = variables;
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) container.getLayout();
        gridLayout.marginWidth = 20;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        this.getShell().setMinimumSize(this.getInitialSize()); // из исходника
        errorMessageComposite = new Composite(container, SWT.NONE);
        errorMessageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout errorMessageCompositeLayout = new GridLayout(1, false);
        errorMessageCompositeLayout.marginWidth = 0; // чтобы текст ошибки и ExpressionLine имели одинаковое расстояние
        // до левого края
        errorMessageCompositeLayout.marginHeight = 10;
        errorMessageComposite.setLayout(errorMessageCompositeLayout);
        errorMessageLabel = new Label(errorMessageComposite, SWT.NONE);
        scrolledComposite = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL);
        // новые виджеты будут появляться внизу, поэтому нужен SWT.V_SCROLL; смещение виджетов при
        // скобках делает необходимым SWT.H_SCROLL
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true)); // чтобы он был
        // таким же широким, как его родитель, и был по всему обьему окна
        inScrolled = new Composite(scrolledComposite, SWT.NONE); // то, что внутри scrolled
        scrolledComposite.setContent(inScrolled); // обязательно вызывать для ScrolledComposite
        GridLayout inScrolledLayout = new GridLayout(1, true);
        inScrolledLayout.marginWidth = 0;
        inScrolledLayout.verticalSpacing = 20;
        inScrolledLayout.marginHeight = 0;
        inScrolledLayout.marginRight = 10;
        inScrolled.setLayout(inScrolledLayout);
        ExpressionLine firstLine = new ExpressionLine(inScrolled, SWT.NONE, variables);
        firstLine.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        expressionLinesList.add(firstLine);
        setLastLogicCompositeNotVisibleState();
        firstLine.setTabSize(globalTabSize);
        firstLine.addAddButtonSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addExpressionLine(firstLine);
                handleBracketsChanged();
                setLastLogicCompositeNotVisibleState();
                refreshGUI();
            }
        });
        firstLine.addDeleteButtonSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (inScrolled.getChildren().length > 1) {
                    deleteExpressionLine(firstLine);
                    deselectLastExpressionBrackets();
                    handleBracketsChanged();
                    setLastLogicCompositeNotVisibleState();
                    refreshGUI();
                } else {
                    // первое выражение не может удаляться, его кнопка для добавления новых дает
                    // последний шанс добавить на этом диалоге еще линии
                    addExpressionLine(firstLine);
                    setLastLogicCompositeNotVisibleState();
                    deleteExpressionLine(firstLine);
                    refreshGUI();
                }
            }
        });
        firstLine.addOpenButtonSelectionListener(bracketsButtonSelectionAdapter);
        firstLine.addCloseButtonSelectionListener(bracketsButtonSelectionAdapter);
        this.refreshGUI();
        try {
            this.initializeFromSaving(); // делается после refreshGUI, чтобы хоть раз
            // была добавлена в GUI ExpressionLine, чтобы ее размер был хоть раз определен, что нужно
            // для того, чтобы возможные новые скобки, которые создаются в этом методе,
            // могли определить свои размеры на основании размеров ExpressionLine
        } catch (UserFriendlyException e) {
            this.setErrorMessage(e.getLocalizedMessage());
            Throwable cause = e.getCause();
            if (cause != null) {
                PluginLogger.logErrorWithoutDialog("createDialogArea", cause);
            }
        } catch (Exception e) {
            this.setErrorMessage(Localization.getString("GlobalValidatorExpressionConstructorDialog.createDialogArea.initializationFailed"));
            PluginLogger.logErrorWithoutDialog("createDialogArea", e);
        }
        return container;
    }

    /**
     * Create contents of the button bar.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(800, 400);
    }

    private void addExpressionLine(ExpressionLine afterLine) { // afterLine - линия, после которой добавляется новая линия
        ExpressionLine newLine = new ExpressionLine(inScrolled, SWT.NONE, variables);
        newLine.giveSourceForSizing(afterLine);
        newLine.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        expressionLinesList.add(expressionLinesList.indexOf(afterLine) + 1, newLine);
        newLine.moveAbove(afterLine);
        newLine.moveBelow(afterLine);
        newLine.setTabSize(globalTabSize);
        newLine.addAddButtonSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addExpressionLine(newLine);
                handleBracketsChanged();
                setLastLogicCompositeNotVisibleState();
                refreshGUI();
            }
        });
        newLine.addDeleteButtonSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (inScrolled.getChildren().length > 1) {
                    deleteExpressionLine(newLine);
                    deselectLastExpressionBrackets();
                    handleBracketsChanged();
                    setLastLogicCompositeNotVisibleState();
                    refreshGUI();
                } else {
                    addExpressionLine(newLine);
                    setLastLogicCompositeNotVisibleState();
                    deleteExpressionLine(newLine);
                    refreshGUI();
                }
            }
        });
        newLine.addOpenButtonSelectionListener(bracketsButtonSelectionAdapter);
        newLine.addCloseButtonSelectionListener(bracketsButtonSelectionAdapter);
    }

    private void deleteExpressionLine(ExpressionLine expressionLine) {
        expressionLinesList.remove(expressionLine);
        expressionLine.dispose();
    }

    // для каждого элемента выражения, а также для начала и конца выражения определяется значение -
    // глубина вложенности этого элемента относительно начала выражения. Для начала и конца
    // выражения она равна 0, а для линий выражений она соответствует сдвигу вправо
    // каждого выражения в UI. Везде она >= 0. Глубина вложенности считается для элементов выражения,
    // и если для нее есть нарушения, то добавляются открывающиеся скобки в самом начали и
    // закрывающиеся скобки в конце, чтобы устранить эти нарушения.
    private void handleBracketsChanged() {
        int nesting = 0;
        int minNesting = 0;
        for (ExpressionLine i : this.expressionLinesList) {
            if (i.isCloseButtonSelected()) {
                nesting--;
                if (nesting < minNesting) {
                    minNesting = nesting;
                }
            }
            if (i.isOpenButtonSelected()) {
                nesting++;
            }
        }
        int openBracketsInBeginning = 0 - minNesting; // такое количество открываюших скобок
        // добавляется в начало, чтобы вложенность везде в выражении была >= 0
        int closeBracketsInEnd = nesting + openBracketsInBeginning; // такое количество закрывающих
        // скобок добавляется в конец, чтобы вложенность в нем была 0
        this.setBracketsInGUI(openBracketsInBeginning, closeBracketsInEnd);
    }

    private void setBracketsInGUI(int openBracketsInBeginning, int closeBracketsInEnd) {
        int nesting = openBracketsInBeginning;
        for (ExpressionLine i : this.expressionLinesList) { // удаляем из всех линий выражений все скобки
            i.setOpenBrackets(0);
            i.setCloseBrackets(0);
        }
        int openBracketsCount = openBracketsInBeginning; // количество открывающих скобок перед текущей линией выражения
        for (ExpressionLine i : this.expressionLinesList) {
            i.setTabNumber(nesting);
            i.setOpenBrackets(openBracketsCount);
            if (openBracketsCount == 1) { // для не крайних линий выражений, если есть открывающая
                // скобка, а она максимум одна, для этой скобки устанавливается смещение
                i.setWidgetTabNumber(0, nesting - 1);
            }
            if (i.isCloseButtonSelected()) {
                nesting--;
                i.setCloseBrackets(1);
                i.setWidgetTabNumber(i.getWidgetNumber() - 1, nesting);
                // для не крайних линий выражений устанавливает
                // смещение для единственной закрывающей скобки
                i.setLogicPuttedOut(true);
            } else {
                i.setLogicPuttedOut(false);
            }
            if (i.isOpenButtonSelected()) {
                nesting++;
                openBracketsCount = 1; // количество открывающих скобок для следующей линии выражения
            } else {
                openBracketsCount = 0;
            }
        }
        ExpressionLine firstLine = expressionLinesList.get(0);
        ExpressionLine lastLine = expressionLinesList.get(expressionLinesList.size() - 1);
        lastLine.setCloseBrackets(closeBracketsInEnd);
        for (int j = 0; j < openBracketsInBeginning; j++) {
            firstLine.setWidgetTabNumber(j, j); // устанавливается смещение для открывающих скобок первой линии выражения
        }
        int lastLineLastWidgetIndex = lastLine.getWidgetNumber() - 1;
        for (int j = 0; j < closeBracketsInEnd; j++) { // устанавливается смещение для закрывающих скобок последней линии выражения
            lastLine.setWidgetTabNumber(lastLineLastWidgetIndex - j, j);
        }
    }

    private void refreshGUI() {// обновляет GUI. Вызывается после изменений в GUI для их отображения
        inScrolled.layout(true, true); // чтобы новые виджеты отобразились,
        // all=true чтобы изменения происходили в дочерних виджетах
        scrolledComposite.setMinSize(inScrolled.computeSize(SWT.DEFAULT, SWT.DEFAULT));// из исходника
    }

    private void setLastLogicCompositeNotVisibleState() {
        // в исходнике логическое условие со скобками в последней линии выражения не видимо,
        // метод делает так, что это выполняется.
        // метод должен использоваться только при добавлении и удалении по 1 линии выражения
        int lastExpressionLineIndex = expressionLinesList.size() - 1;
        ExpressionLine lastExpressionLine = expressionLinesList.get(lastExpressionLineIndex);
        lastExpressionLine.setLogicCompositeVisible(false);
        if (lastExpressionLineIndex > 0) { // если есть предпоследняя линия выражения
            expressionLinesList.get(lastExpressionLineIndex - 1).setLogicCompositeVisible(true);
            // в ней до этого LogicComposite мог быть не видим
        }
    }

    private void deselectLastExpressionBrackets() {
        ExpressionLine lastExpressionLine = expressionLinesList.get(expressionLinesList.size() - 1);
        lastExpressionLine.setOpenBracketSelection(false);
        lastExpressionLine.setCloseBracketSelection(false);
    }

    private String complexExpressionGroovy;

    @Override
    protected void okPressed() {
        // перед dispose собираем ввод из GUI
        try {
            this.complexExpressionGroovy = this.getComplexExpressionGroovyFromDialog();
            this.fillParamsForSaving();
            this.refreshDirty();
            this.stateAfterInitialization = null;
            super.okPressed();
        } catch (UserFriendlyException e) {
            this.setErrorMessage(e.getLocalizedMessage());
            Throwable cause = e.getCause();
            if (cause != null) {
                PluginLogger.logErrorWithoutDialog("okPressed", cause);
            }
        }
    }

    private void setErrorMessage(String message) {
        String firstCharacterString = String.valueOf(Character.toUpperCase(message.charAt(0)));
        message = firstCharacterString.concat(message.substring(1)).concat(".");
        errorMessageLabel.setText(message);
        errorMessageComposite.layout(true, true);
    }

    private String getComplexExpressionGroovyFromDialog() throws UserFriendlyException {
        StringBuffer result = new StringBuffer();
        int nesting = 0;
        int minNesting = 0;
        for (ExpressionLine i : this.expressionLinesList) {
            if (i.isCloseButtonSelected()) {
                nesting--;
                if (nesting < minNesting) {
                    minNesting = nesting;
                }
            }
            if (i.isOpenButtonSelected()) {
                nesting++;
            }
        }
        int openBracketsInBeginning = 0 - minNesting;
        int closeBracketsInEnd = nesting + openBracketsInBeginning;
        for (int i = 0; i < openBracketsInBeginning; i++) {
            result.append("(");
        }
        for (ExpressionLine i : this.expressionLinesList) {
            try {
                result.append(i.getCodeGroovy());
            } catch (UserFriendlyException e) {
                throw exceptionFromGetComplexExpressionGroovyFromDialog(e, this.expressionLinesList.indexOf(i));
            }
        }
        for (int i = 0; i < closeBracketsInEnd; i++) {
            result.append(")");
        }
        return result.toString();
    }

    private UserFriendlyException exceptionFromGetComplexExpressionGroovyFromDialog(UserFriendlyException expressionLineException,
            int expressionLineNumber) {
        UserFriendlyException exception = new UserFriendlyException(
                expressionLineException.getMessage() + " in expression line " + (expressionLineNumber + 1), expressionLineException.getCause());
        exception.setLocalizedMessage(
                Localization.getString("GlobalValidatorExpressionConstructorDialog.getComplexExpressionGroovyFromDialog.errorMessage",
                        expressionLineNumber + 1, expressionLineException.getLocalizedMessage()));
        return exception;
    }

    public String getComplexExpressionGroovy() { // если выражение было успешно составлено, возвращает код Groovy, иначе null
        return complexExpressionGroovy;
    }

    // то, что введено в диалоге, может сохраняться в Map среди других его параметров.
    // Тогда, чтобы отличать параметры диалога от других параметров Map,
    // будет полезен prefix - первая часть названий всех параметров
    // prefix, тем не менее, будет делать строку со всеми параметрами длиннее
    private static final String paramPrefix = "ComplexExpressionConstructorDialog";
    private static final String isSavedParam = paramPrefix + "_saved";
    private static final String expressionLineNumberParam = paramPrefix + "_ExpressionLinesNumber";

    private void fillParamsForSaving() {
        this.paramsForSaving = new HashMap<>();
        this.paramsForSaving.put(isSavedParam, "true"); // этот параметр при считывании
        // параметров из Map позволяет убедиться, что в Map действительно сохранены параметры
        // этого диалога
        this.paramsForSaving.put(expressionLineNumberParam, String.valueOf(this.expressionLinesList.size())); // чтобы при считывании можно
        // было сразу создать нужное количество ExpressionLine
        int index = 0;
        for (ExpressionLine i : this.expressionLinesList) {
            for (Entry<String, String> entry : i.getParamsForSaving().entrySet()) {
                this.paramsForSaving.put(paramPrefix + "_" + String.valueOf(index) + "_" + entry.getKey(), entry.getValue());
            }
        }
        index++;
    }

    public Map<String, String> getSavingAsMap() {
        return this.paramsForSaving;
    }

    public String getSavingAsString() throws Exception { // создает строку, содержащую параметры
        String result = null;
        try (ByteArrayOutputStream tmp = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(tmp)) {
            out.writeObject(this.paramsForSaving); // преобразование к
            // сериализуемому типу HashMap
            result = Base64.getEncoder().encodeToString(tmp.toByteArray());
            // без преобразования подобного типа между String и ByteArray не работает
        } catch (Exception e) {
            throw e; // блок try catch нужен для закрытия streams, так как это try with resources
        }
        return result;
    }

    public String getSavingAsExpression() {
        return this.getComplexExpressionGroovy();
    }

    private void initializeFromSaving() throws UserFriendlyException, Exception {
        if (this.initializationType == null) {
            return; // способ инициализации не был выбран, инициализация не проводится
        }
        switch (this.initializationType) {
        case MAP:
            break;
        case STRING_BASE_64:
            this.getParamsMapFromString(); // получить параметры из строки
            break;
        case EXPRESSION:
            this.getParamsMapFromExpression();
            break;
        }
        Exception initializeFromMapSavingException = null;
        try {
            this.initializeFromMapSaving();
        } catch (Exception e) {
            initializeFromMapSavingException = e;
        }
        // initializeFromMapSaving, когда посылает исключение, все равно инициализирует
        // часть GUI, и для этой части можно расставить скобки и показать ее в GUI.
        handleBracketsChanged();
        refreshGUI();
        if (initializeFromMapSavingException != null) {
            throw initializeFromMapSavingException;
        }
    }

    private void getParamsMapFromString() throws Exception {
        if (this.initializationParamsString == null) {
            throw new Exception("initialization string is null");
        }
        byte[] initialilizationParamsByteArray;
        initialilizationParamsByteArray = Base64.getDecoder().decode(this.initializationParamsString);
        try (ByteArrayInputStream tmp = new ByteArrayInputStream(initialilizationParamsByteArray);
                ObjectInputStream in = new ObjectInputStream(tmp)) {
            this.initializationParams = (HashMap<String, String>) in.readObject();
        } catch (Exception e) {
            throw e; // блок try catch нужен для закрытия streams, так как это try with resources
        }
    }

    private void getParamsMapFromExpression() throws UserFriendlyException {
        Optional<GlobalValidatorExpressionConstructorDialog.ExpressionModel> optionalModel = GroovyCodeParser
                .parseValidationModel(this.initializationExpression, this.variables);
        if (!optionalModel.isPresent()) {
            if (this.initializationExpression.equals("")) {
                this.initializationParams = this.getEmptyMapSaving();
                return;
            }
            throw this.exceptionFromGetParamsMapFromExpression();
        }
        GlobalValidatorExpressionConstructorDialog.ExpressionModel model = optionalModel.get();
        this.initializationParams = model.getParamsMap();
    }

    private Map<String, String> getEmptyMapSaving() {
        Map<String, String> params = new HashMap<>();
        params.put(isSavedParam, "true");
        params.put(expressionLineNumberParam, "0");
        return params;
    }

    private UserFriendlyException exceptionFromGetParamsMapFromExpression() {
        UserFriendlyException exception = new UserFriendlyException("failed to parse expression");
        // парсер обрабатывает синтаксические ошибки языка Groovy и ошибки неподдерживаемого этим диалогом синтаксиса,
        // в сумме их много и сложно для каждой из них придумать user-friendly сообщение об ошибке для отображения в GUI.
        // Поэтому не уточняется, какая именно ошибка произошла, но парсер в своем классе сообщает об ошибке через PluginLogger.
        exception.setLocalizedMessage(Localization.getString("GlobalValidatorExpressionConstructorDialog.getParamsMapFromExpression.errorMessage"));
        return exception;
    }

    private void initializeFromMapSaving() throws UserFriendlyException, Exception {
        // если произошли ошибки инициализации некоторых ExpressionLine,
        // остальные ExpressionLine все равно инициализируются,
        // при этом первая из ошибок посылается
        if (initializationParams == null) {
            throw new Exception("initialization params is null");
        }
        if (!initializationParams.containsKey(isSavedParam)) {
            throw new Exception("initialization params not contain param " + isSavedParam);
        }
        if (!initializationParams.get(isSavedParam).equals("true")) {
            throw new Exception("param " + isSavedParam + " not true");
        }
        this.stateAfterInitialization = new HashMap<>();
        this.stateAfterInitialization.put(isSavedParam, initializationParams.get(isSavedParam));
        String expressionLineNumberString = initializationParams.get(expressionLineNumberParam);
        if (expressionLineNumberString == null) {
            throw new Exception("param of ExpressionLine number is null");
        }
        int expressionLineNumber = Integer.parseInt(expressionLineNumberString);
        if (expressionLineNumber < 0) {
            throw new Exception("expression line number less than 0");
        }
        this.stateAfterInitialization.put(expressionLineNumberParam, initializationParams.get(expressionLineNumberParam));
        if (expressionLineNumber == 0) {
            return;
        }
        ExpressionLine expressionLine = this.expressionLinesList.get(0);
        for (int i = 0; i < expressionLineNumber - 1; i++) {
            this.addExpressionLine(expressionLine);
        }
        setLastLogicCompositeNotVisibleState();
        if (expressionLineNumber > 1) {
            expressionLine.setLogicCompositeVisible(true); // это 1-ая линия, она скрыта, а
            // setLastLogicCompositeNotVisibleState не сделает его видимым после добавления еще > 2 линий,
            // так как работает только с последними двумя линиями.
        }
        int index = 0;
        Exception firstExpressionlineException = null;
        int firstExpressionlineExceptionIndex = 0;
        for (ExpressionLine i : this.expressionLinesList) {
            Set<String> paramsNames = i.getParamsNames();
            Map<String, String> initializationParamsForExpressionLine = new HashMap<>();
            for (String param : paramsNames) {
                String paramName = paramPrefix + "_" + String.valueOf(index) + "_" + param;
                initializationParamsForExpressionLine.put(param, initializationParams.get(paramName));
                this.stateAfterInitialization.put(paramName, initializationParams.get(paramName));
            }
            try {
                i.initializeFromSaving(initializationParamsForExpressionLine);
            } catch (Exception e) {
                if (firstExpressionlineException == null) {
                    firstExpressionlineException = e;
                    firstExpressionlineExceptionIndex = index;
                }
            }
            index++;
        }
        if (firstExpressionlineException != null) {
            if (firstExpressionlineException instanceof UserFriendlyException) {
                throw this.exceptionFromInitializeFromMapSaving((UserFriendlyException) firstExpressionlineException,
                        firstExpressionlineExceptionIndex);
            } else {
                throw this.exceptionFromInitializeFromMapSaving(firstExpressionlineException, firstExpressionlineExceptionIndex);
                // другой метод с таким же названием
            }
        }
    }

    private UserFriendlyException exceptionFromInitializeFromMapSaving(UserFriendlyException expressionLineException, int expressionLineNumber) {
        UserFriendlyException exception = new UserFriendlyException(
                expressionLineException.getMessage() + " in expression line " + (expressionLineNumber + 1), expressionLineException.getCause());
        exception.setLocalizedMessage(Localization.getString("GlobalValidatorExpressionConstructorDialog.initializeFromMapSaving.errorMessage",
                expressionLineNumber + 1, expressionLineException.getLocalizedMessage()));
        return exception;
    }

    private UserFriendlyException exceptionFromInitializeFromMapSaving(Exception expressionLineException, int expressionLineNumber) {
        UserFriendlyException exception = new UserFriendlyException("initialization error", expressionLineException);
        exception.setLocalizedMessage(
                Localization.getString("GlobalValidatorExpressionConstructorDialog.initializeFromMapSaving.ExpressionLineInitializationError"));
        return this.exceptionFromInitializeFromMapSaving(exception, expressionLineNumber);
    }

    public boolean isDirty() {
        return dirty;
    }

    private void refreshDirty() {
        if (this.stateAfterInitialization == null) {
            this.dirty = true; // this.paramsForSaving в любом случае не null,
            // и значит не равно this.stateAfterInitialization
        } else {
            this.dirty = !this.stateAfterInitialization.equals(this.paramsForSaving);
        }
    }

    public void initializeFromMap(Map<String, String> initializationParams) {
        this.initializationParams = initializationParams;
        this.initializationType = InitializationType.MAP;
    }

    public void initializeFromString(String initializationParamsString) {
        this.initializationParamsString = initializationParamsString;
        this.initializationType = InitializationType.STRING_BASE_64;
    }

    public void initializeFromExpression(String initializationExpression) {
        this.initializationExpression = initializationExpression;
        this.initializationType = InitializationType.EXPRESSION;
    }

    public static class ExpressionModel {
        // модель выражения такого типа, который задается в этом диалоге.
        // класс используется только для передачи данных о модели при загрузке из сохранения
        private Map<String, String> params = null;

        public ExpressionModel() {
            this.params = new HashMap<>();
            this.params.put(isSavedParam, "true");
            this.params.put(expressionLineNumberParam, String.valueOf(0));
        }

        // возвращает представление модели в виде Map из параметров
        public Map<String, String> getParamsMap() {
            return params;
        }

        public void addExpressionLineModel(ExpressionLine.ExpressionLineModel model) {
            String expressionLinesNumberString = this.params.get(expressionLineNumberParam);
            int expressionLinesNumber = Integer.parseInt(expressionLinesNumberString);
            for (Entry<String, String> entry : model.getParamsMap().entrySet()) {
                this.params.put(paramPrefix + "_" + expressionLinesNumberString + "_" + entry.getKey(), entry.getValue());
            }
            this.params.put(expressionLineNumberParam, String.valueOf(expressionLinesNumber + 1));
        }
    }
}
