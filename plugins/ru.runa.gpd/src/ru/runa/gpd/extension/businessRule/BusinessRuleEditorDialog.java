package ru.runa.gpd.extension.businessRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.DelegableConfigurationDialog;
import ru.runa.gpd.extension.businessRule.BusinessRuleModel.IfExpr;
import ru.runa.gpd.extension.decision.GroovyTypeSupport;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.extension.handler.FormulaCellEditorProvider;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.HelpDialog;
import ru.runa.gpd.ui.custom.HighlightTextStyling;
import ru.runa.gpd.ui.custom.JavaHighlightTextStyling;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.ChooseGroovyStuffDialog;
import ru.runa.gpd.ui.dialog.ChooseItemDialog;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.ui.dialog.FilterBox;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.gpd.util.GroovyStuff;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.util.GroovyStuff.Item;

public class BusinessRuleEditorDialog extends Dialog {
    private static final Image addImage = SharedImages.getImage("icons/add_obj.gif");
    private TabFolder tabFolder;
    private StyledText styledText;
    private Composite constructor;
    private final String initValue;
    private BusinessRuleModel initModel;
    private String initErrorMessage;
    private final List<String> functions = new ArrayList();
    private final List<Variable> variables;
    private final List<String> variableNames;
    private ErrorHeaderComposite constructorHeader;
    private ErrorHeaderComposite sourceHeader;
    private List<Button> buttons = new ArrayList();
    private List<FilterBox[]> varBoxes = new ArrayList();
    private List<Combo> operBoxes = new ArrayList();
    private String defaultFunction;
    private String result;

    public BusinessRuleEditorDialog(ProcessDefinition definition, String initValue) {
        super(Display.getCurrent().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.initValue = initValue;
        this.variables = definition.getVariables(true, true);
        this.variableNames = VariableUtils.getVariableNamesForScripting(variables);
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
    protected Point getInitialSize() {
        return new Point(700, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Localization.getString("GroovyEditor.title"));
        tabFolder = new TabFolder(parent, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        tabFolder.addSelectionListener(new TabSelectionHandler());
        Composite constructorView = new Composite(tabFolder, SWT.NONE);
        constructorView.setLayout(new GridLayout());
        TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
        tabItem1.setText(Localization.getString("GroovyEditor.title.constructor"));
        tabItem1.setControl(constructorView);
        constructorHeader = new ErrorHeaderComposite(constructorView);
        ScrolledComposite scrolledComposite = new ScrolledComposite(constructorView, SWT.V_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinHeight(200);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        constructor = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(constructor);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 5;
        constructor.setLayout(gridLayout);
        constructor.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite sourceView = new Composite(tabFolder, SWT.NONE);
        sourceView.setLayout(new GridLayout());
        sourceView.setLayoutData(new GridData(GridData.FILL_BOTH));
        sourceHeader = new ErrorHeaderComposite(sourceView);
        if (GroovyStuff.TYPE.getAll().size() > 0) {
            SwtUtils.createLink(sourceHeader, Localization.getString("Insert.TYPE.link"), new LoggingHyperlinkAdapter() {
                @Override
                public void onLinkActivated(HyperlinkEvent e) {
                    Item item = new ChooseGroovyStuffDialog(GroovyStuff.TYPE).openDialog();
                    if (item != null) {
                        String insert = item.getBody();
                        styledText.insert(insert);
                        styledText.setCaretOffset(styledText.getCaretOffset() + insert.length());
                        styledText.setFocus();
                    }
                }
            }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        }
        if (GroovyStuff.CONSTANT.getAll().size() > 0) {
            SwtUtils.createLink(sourceHeader, Localization.getString("Insert.CONSTANT.link"), new LoggingHyperlinkAdapter() {
                @Override
                public void onLinkActivated(HyperlinkEvent e) {
                    Item item = new ChooseGroovyStuffDialog(GroovyStuff.CONSTANT).openDialog();
                    if (item != null) {
                        String insert = item.getBody();
                        styledText.insert(insert);
                        styledText.setCaretOffset(styledText.getCaretOffset() + insert.length());
                        styledText.setFocus();
                    }
                }
            }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        }
        if (GroovyStuff.STATEMENT.getAll().size() > 0) {
            SwtUtils.createLink(sourceHeader, Localization.getString("Insert.STATEMENT.link"), new LoggingHyperlinkAdapter() {
                @Override
                public void onLinkActivated(HyperlinkEvent e) {
                    Item item = new ChooseGroovyStuffDialog(GroovyStuff.STATEMENT).openDialog();
                    if (item != null) {
                        String insert = item.getBody();
                        styledText.insert(insert);
                        styledText.setCaretOffset(styledText.getCaretOffset() + insert.length());
                        styledText.setFocus();
                    }
                }
            }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        }
        if (GroovyStuff.METHOD.getAll().size() > 0) {
            SwtUtils.createLink(sourceHeader, Localization.getString("Insert.METHOD.link"), new LoggingHyperlinkAdapter() {
                @Override
                public void onLinkActivated(HyperlinkEvent e) {
                    Item item = new ChooseGroovyStuffDialog(GroovyStuff.METHOD).openDialog();
                    if (item != null) {
                        String insert = item.getBody();
                        styledText.insert(insert);
                        styledText.setCaretOffset(styledText.getCaretOffset() + insert.length());
                        styledText.setFocus();
                    }
                }
            }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        }
        SwtUtils.createLink(sourceHeader, Localization.getString("button.insert_variable"), new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(variableNames);
                String variableName = dialog.openDialog();
                if (variableName != null) {
                    styledText.insert(variableName);
                    styledText.setFocus();
                    styledText.setCaretOffset(styledText.getCaretOffset() + variableName.length());
                }
            }
        }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        styledText = new StyledText(sourceView, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.addLineStyleListener(new JavaHighlightTextStyling(variableNames));
        styledText.setText(this.initValue);
        styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
        TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
        tabItem2.setText(Localization.getString("GroovyEditor.title.code"));
        tabItem2.setControl(sourceView);
        createConstructorView();
        try {
            if (initModel != null && initValue.equals(initModel.toString())) {
                initConstructorView();
            } else {
                if (this.initValue.length() > 0) {
                    tabFolder.setSelection(1);
                }
                if (initErrorMessage != null) {
                    setErrorLabelText(initErrorMessage);
                }
            }
        } catch (RuntimeException e) {
            // Activate source view if custom code found
            tabFolder.setSelection(1);
        }
        return tabFolder;
    }

    private void setErrorLabelText(String text) {
        constructorHeader.setErrorText(text);
        sourceHeader.setErrorText(text);
    }

    private void clearErrorLabelText() {
        constructorHeader.clearErrorText();
        sourceHeader.clearErrorText();
    }

    private void createConstructorView() {
        if (initModel != null) {
            defaultFunction = initModel.getDefaultFunction();
            for (IfExpr expr : initModel.getIfExprs()) {
                createLines(expr.getFunction());
            }
        } else {
            createLines(null);
        }

        for (int i = 0; i < varBoxes.size(); i++) {
            varBoxes.get(i)[0].setSize(100, 20);
            varBoxes.get(i)[1].setSize(100, 20);
        }

        createButtomComposite();

    }

    private Composite createButtomComposite() {
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
        FilterBox[] var = new FilterBox[2];
        varBoxes.add(var);
        var[0] = new FilterBox(constructor, VariableUtils.getVariableNamesForScripting(variables));
        var[0].setData(DATA_INDEX_KEY, new int[] { varBoxes.size() - 1, 0 });
        var[0].setSelectionListener(new FilterBoxSelectionHandler());
        var[0].setLayoutData(getGridData());

        Combo oper = new Combo(constructor, SWT.READ_ONLY);
        oper.setData(DATA_INDEX_KEY, new int[] { varBoxes.size() - 1, 1 });
        oper.addSelectionListener(new ComboSelectionHandler());
        oper.setLayoutData(getGridData());
        operBoxes.add(oper);

        var[1] = new FilterBox(constructor, null);
        var[1].setData(DATA_INDEX_KEY, new int[] { varBoxes.size() - 1, 2 });
        var[1].setSelectionListener(new FilterBoxSelectionHandler());
        var[1].setLayoutData(getGridData());

        Button functionButton = new Button(constructor, SWT.NONE);
        functionButton.setLayoutData(getGridData());
        buttons.add(functionButton);
        functionButton.setData(buttons.size() - 1);
        if (function == null) {
            functionButton.setText(Localization.getString("GroovyEditor.functionButton") + buttons.size());
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
                createButtomComposite();
                constructor.layout();
            }
        });
    }

    private void initConstructorView() {
        for (int i = 0; i < functions.size(); i++) {
            IfExpr ifExpr = initModel.getIfExpr(functions.get(i));
            if (ifExpr != null) {
                buttons.get(i).setText(ifExpr.getFunction());
                Variable variable = ifExpr.getVariable1();
                int index = variables.indexOf(variable);
                if (index == -1) {
                    // required variable was deleted in process definition
                    continue;
                }
                varBoxes.get(i)[0].select(index);
                refresh(varBoxes.get(i)[0]);
                GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                index = Operation.getAll(typeSupport).indexOf(ifExpr.getOperation());
                if (index == -1) {
                    // required operation was deleted !!!
                    continue;
                }
                operBoxes.get(i).select(index);
                refresh(operBoxes.get(i));
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
                        varBoxes.get(i)[1].add(lexem2Text, 0);
                        varBoxes.get(i)[1].setData(DATA_USER_INPUT_KEY, lexem2Text);
                    }
                }
                varBoxes.get(i)[1].select(var2index);
            }
        }
    }

    static final String DATA_INDEX_KEY = "indexes";
    static final String DATA_VARIABLE_KEY = "variable";
    static final String DATA_USER_INPUT_KEY = "userInput";
    static final String DATA_OPERATION_KEY = "operation";

    private GridData getGridData() {
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.minimumWidth = 100;
        return data;
    }

    private void refresh(FilterBox filterBox) {
        try {
            int[] indexes = (int[]) filterBox.getData(DATA_INDEX_KEY);
            if (indexes[1] == 2) {
                if (TypedUserInputCombo.INPUT_VALUE.equals(filterBox.getSelectedItem())) {
                    String oldUserInput = (String) filterBox.getData(DATA_USER_INPUT_KEY);
                    Variable variable1 = (Variable) varBoxes.get(indexes[0])[0].getData(DATA_VARIABLE_KEY);
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
                Combo operCombo = operBoxes.get(indexes[0]);
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

    private void refresh(Combo operCombo) {
        try {
            int[] indexes = (int[]) operCombo.getData(DATA_INDEX_KEY);
            Variable variable1 = (Variable) varBoxes.get(indexes[0])[0].getData(DATA_VARIABLE_KEY);
            if (variable1 != null) {
                FilterBox targetCombo = varBoxes.get(indexes[0])[1];
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

    private List<String> getVariable2Names(Variable variable1) {
        List<String> names = new ArrayList<String>();
        GroovyTypeSupport typeSupport1 = GroovyTypeSupport.get(variable1.getJavaClassName());
        for (Variable variable : variables) {
            GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
            if (typeSupport1.getClass() == typeSupport.getClass() && variable1 != variable) {
                names.add(variable.getScriptingName());
            }
        }
        return names;
    }

    private class TabSelectionHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (tabFolder.getSelectionIndex() == 1) {
                toCode();
            }
        }
    }

    private class ErrorHeaderComposite extends Composite {
        private final Label errorLabel;

        public ErrorHeaderComposite(Composite parent) {
            super(parent, SWT.NONE);
            setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            setLayout(new GridLayout(6, false));
            errorLabel = new Label(this, SWT.NONE);
            errorLabel.setForeground(ColorConstants.red);
            errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        public void setErrorText(String text) {
            errorLabel.setText(text);
        }

        public void clearErrorText() {
            setErrorText("");
        }
    }

    private class ComboSelectionHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            refresh((Combo) e.widget);
        }
    }

    private class FilterBoxSelectionHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            refresh((FilterBox) e.widget);
        }
    }

    private void toCode() {
        for (int i = 0; i < varBoxes.size(); i++) {
            if (varBoxes.get(i)[0].getText().length() == 0
                    || varBoxes.get(i)[1].getText().length() == 0 && !buttons.get(i).getText().equals(defaultFunction)) {
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
                Variable variable1 = (Variable) varBoxes.get(i)[0].getData(DATA_VARIABLE_KEY);
                String operationName = operBoxes.get(i).getItem(operBoxes.get(i).getSelectionIndex());
                String lexem2Text = varBoxes.get(i)[1].getText();
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
                ifExpr = new IfExpr(buttons.get(i).getText(), variable1, lexem2, Operation.getByName(operationName, typeSupport));
                model.addIfExpr(ifExpr);
            }
            styledText.setText(model.toString());
        } catch (RuntimeException e1) {
            PluginLogger.logError(e1);
            setErrorLabelText(Localization.getString("GroovyEditor.error.construct"));
        }
    }

    @Override
    protected void okPressed() {
        if (tabFolder.getSelectionIndex() == 0) {
            toCode();
        }
        this.result = styledText.getText();
        super.okPressed();
    }

    public String getResult() {
        return result;
    }

}
