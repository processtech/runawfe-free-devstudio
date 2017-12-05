package ru.runa.gpd.extension.decision;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.decision.GroovyDecisionModel.IfExpr;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.JavaHighlightTextStyling;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.ChooseGroovyStuffDialog;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.gpd.util.GroovyStuff;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.util.GroovyStuff.Item;

public class GroovyEditorDialog extends Dialog {
    private static final Image upImage = SharedImages.getImage("icons/up.gif");
    private TabFolder tabFolder;
    private StyledText styledText;
    private Composite constructor;
    private final String initValue;
    private GroovyDecisionModel initModel;
    private String initErrorMessage;
    private final List<String> transitionNames;
    private final List<Variable> variables;
    private final List<String> variableNames;
    private ErrorHeaderComposite constructorHeader;
    private ErrorHeaderComposite sourceHeader;
    private Label[] labels;
    private Combo[][] comboBoxes;
    private Combo defaultTransitionCombo;
    private String result;

    public GroovyEditorDialog(ProcessDefinition definition, List<String> transitionNames, String initValue) {
        super(Display.getCurrent().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.initValue = initValue;
        this.transitionNames = transitionNames;
        this.variables = definition.getVariables(true, true);
        this.variableNames = VariableUtils.getVariableNamesForScripting(variables);
        if (this.initValue.length() > 0) {
            try {
                initModel = new GroovyDecisionModel(initValue, variables);
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
            SWTUtils.createLink(sourceHeader, Localization.getString("Insert.TYPE.link"), new LoggingHyperlinkAdapter() {
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
            SWTUtils.createLink(sourceHeader, Localization.getString("Insert.CONSTANT.link"), new LoggingHyperlinkAdapter() {
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
            SWTUtils.createLink(sourceHeader, Localization.getString("Insert.STATEMENT.link"), new LoggingHyperlinkAdapter() {
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
            SWTUtils.createLink(sourceHeader, Localization.getString("Insert.METHOD.link"), new LoggingHyperlinkAdapter() {
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
        SWTUtils.createLink(sourceHeader, Localization.getString("button.insert_variable"), new LoggingHyperlinkAdapter() {
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
            // reorder transitions;
            List<String> tmp = new ArrayList<String>(transitionNames);
            transitionNames.clear();
            for (IfExpr expr : initModel.getIfExprs()) {
                if (tmp.remove(expr.getTransition())) {
                    transitionNames.add(expr.getTransition());
                }
            }
            for (String newTransition : tmp) {
                transitionNames.add(newTransition);
            }
        }
        comboBoxes = new Combo[transitionNames.size()][3];
        labels = new Label[transitionNames.size()];
        for (int i = 0; i < transitionNames.size(); i++) {
            labels[i] = new Label(constructor, SWT.NONE);
            labels[i].setText(transitionNames.get(i));
            labels[i].setLayoutData(getGridData());
            comboBoxes[i][0] = new Combo(constructor, SWT.READ_ONLY);
            for (Variable variable : variables) {
                comboBoxes[i][0].add(variable.getScriptingName());
            }
            comboBoxes[i][0].setData(DATA_INDEX_KEY, new int[] { i, 0 });
            comboBoxes[i][0].addSelectionListener(new ComboSelectionHandler());
            comboBoxes[i][0].setLayoutData(getGridData());
            comboBoxes[i][1] = new Combo(constructor, SWT.READ_ONLY);
            comboBoxes[i][1].setData(DATA_INDEX_KEY, new int[] { i, 1 });
            comboBoxes[i][1].addSelectionListener(new ComboSelectionHandler());
            comboBoxes[i][1].setLayoutData(getGridData());
            comboBoxes[i][2] = new Combo(constructor, SWT.READ_ONLY);
            comboBoxes[i][2].setData(DATA_INDEX_KEY, new int[] { i, 2 });
            comboBoxes[i][2].addSelectionListener(new ComboSelectionHandler());
            comboBoxes[i][2].setLayoutData(getGridData());
            if (i != 0) {
                Button upButton = new Button(constructor, SWT.PUSH);
                upButton.setImage(upImage);
                upButton.setData(i);
                upButton.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        upRecord((Integer) e.widget.getData());
                    }
                });
            } else {
                new Label(constructor, SWT.NONE);
            }
        }
        for (int i = 0; i < comboBoxes.length; i++) {
            for (int j = 0; j < 3; j++) {
                comboBoxes[i][j].setSize(100, 20);
            }
        }
        if (transitionNames.size() > 0) {
            Composite bottomComposite = new Composite(constructor, SWT.NONE);
            bottomComposite.setLayout(new GridLayout(2, true));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 5;
            bottomComposite.setLayoutData(data);
            Label defaultLabel = new Label(bottomComposite, SWT.NONE);
            defaultLabel.setText(Localization.getString("GroovyEditor.byDefault") + ":");
            defaultTransitionCombo = new Combo(bottomComposite, SWT.READ_ONLY);
            for (String trName : transitionNames) {
                defaultTransitionCombo.add(trName);
            }
            defaultTransitionCombo.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    for (int j = 0; j < labels.length; j++) {
                        boolean enabled = !labels[j].getText().equals(defaultTransitionCombo.getText());
                        comboBoxes[j][0].setEnabled(enabled);
                        comboBoxes[j][1].setEnabled(enabled);
                        comboBoxes[j][2].setEnabled(enabled);
                    }
                }
            });
            bottomComposite.pack();
        }
    }

    private void initConstructorView() {
        for (int i = 0; i < transitionNames.size(); i++) {
            IfExpr ifExpr = initModel.getIfExpr(transitionNames.get(i));
            if (ifExpr != null) {
                labels[i].setText(ifExpr.getTransition());
                if (ifExpr.isByDefault()) {
                    comboBoxes[i][0].setEnabled(false);
                    comboBoxes[i][1].setEnabled(false);
                    comboBoxes[i][2].setEnabled(false);
                    defaultTransitionCombo.setText(ifExpr.getTransition());
                } else {
                    Variable variable = ifExpr.getVariable1();
                    int index = variables.indexOf(variable);
                    if (index == -1) {
                        // required variable was deleted in process
                        // definition
                        continue;
                    }
                    comboBoxes[i][0].select(index);
                    refreshComboItems(comboBoxes[i][0]);
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                    index = Operation.getAll(typeSupport).indexOf(ifExpr.getOperation());
                    if (index == -1) {
                        // required operation was deleted !!!
                        continue;
                    }
                    comboBoxes[i][1].select(index);
                    refreshComboItems(comboBoxes[i][1]);
                    String lexem2Text = ifExpr.getLexem2TextValue();
                    int combo3index = 0;
                    if (VariableUtils.getVariableByScriptingName(variables, lexem2Text) != null) {
                        combo3index = getCombo3VariableNames(variable).indexOf(lexem2Text);
                    } else {
                        int predefinedIndex = typeSupport.getPredefinedValues(ifExpr.getOperation()).indexOf(lexem2Text);
                        if (predefinedIndex >= 0) {
                            combo3index = getCombo3VariableNames(variable).size() + predefinedIndex;
                        } else {
                            comboBoxes[i][2].add(lexem2Text, 0);
                            comboBoxes[i][2].setData(DATA_USER_INPUT_KEY, lexem2Text);
                        }
                    }
                    comboBoxes[i][2].select(combo3index);
                }
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

    private void upRecord(Integer recordIndex) {
        String recordText = labels[recordIndex].getText();
        labels[recordIndex].setText(labels[recordIndex - 1].getText());
        labels[recordIndex - 1].setText(recordText);
        boolean enabledIndex = comboBoxes[recordIndex][0].getEnabled();
        boolean enabledIndexPrev = comboBoxes[recordIndex - 1][0].getEnabled();
        int combo1Index = comboBoxes[recordIndex][0].getSelectionIndex();
        int combo2Index = comboBoxes[recordIndex][1].getSelectionIndex();
        int combo3Index = comboBoxes[recordIndex][2].getSelectionIndex();
        String combo3UserInput = (String) comboBoxes[recordIndex][2].getData(DATA_USER_INPUT_KEY);
        comboBoxes[recordIndex][0].select(comboBoxes[recordIndex - 1][0].getSelectionIndex());
        comboBoxes[recordIndex][0].setEnabled(enabledIndexPrev);
        refreshComboItems(comboBoxes[recordIndex][0]);
        comboBoxes[recordIndex][1].select(comboBoxes[recordIndex - 1][1].getSelectionIndex());
        comboBoxes[recordIndex][1].setEnabled(enabledIndexPrev);
        refreshComboItems(comboBoxes[recordIndex][1]);
        String combo3UserInput2 = (String) comboBoxes[recordIndex - 1][2].getData(DATA_USER_INPUT_KEY);
        if (combo3UserInput2 != null) {
            comboBoxes[recordIndex][2].add(combo3UserInput2, 0);
            comboBoxes[recordIndex][2].setData(DATA_USER_INPUT_KEY, combo3UserInput2);
        }
        comboBoxes[recordIndex][2].select(comboBoxes[recordIndex - 1][2].getSelectionIndex());
        comboBoxes[recordIndex][2].setEnabled(enabledIndexPrev);
        comboBoxes[recordIndex - 1][0].select(combo1Index);
        comboBoxes[recordIndex - 1][0].setEnabled(enabledIndex);
        refreshComboItems(comboBoxes[recordIndex - 1][0]);
        comboBoxes[recordIndex - 1][1].select(combo2Index);
        comboBoxes[recordIndex - 1][1].setEnabled(enabledIndex);
        refreshComboItems(comboBoxes[recordIndex - 1][1]);
        if (combo3UserInput != null) {
            comboBoxes[recordIndex - 1][2].add(combo3UserInput, 0);
            comboBoxes[recordIndex - 1][2].setData(DATA_USER_INPUT_KEY, combo3UserInput);
        }
        comboBoxes[recordIndex - 1][2].select(combo3Index);
        comboBoxes[recordIndex - 1][2].setEnabled(enabledIndex);
    }

    private void refreshComboItems(Combo combo) {
        try {
            int[] indexes = (int[]) combo.getData(DATA_INDEX_KEY);
            if (indexes[1] == 2) {
                if (TypedUserInputCombo.INPUT_VALUE.equals(combo.getText())) {
                    String oldUserInput = (String) combo.getData(DATA_USER_INPUT_KEY);
                    Variable variable1 = (Variable) comboBoxes[indexes[0]][0].getData(DATA_VARIABLE_KEY);
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
                    UserInputDialog inputDialog = typeSupport.createUserInputDialog();
                    inputDialog.setInitialValue(oldUserInput);
                    if (OK == inputDialog.open()) {
                        String userInput = inputDialog.getUserInput();
                        if (oldUserInput != null) {
                            combo.remove(0);
                        }
                        combo.setData(DATA_USER_INPUT_KEY, userInput);
                        combo.add(userInput, 0);
                        combo.select(0);
                    } else {
                        combo.deselectAll();
                    }
                } else {
                    Variable variable = VariableUtils.getVariableByScriptingName(variables, combo.getText());
                    if (variable != null) {
                        combo.setData(DATA_VARIABLE_KEY, variable);
                    }
                }
                return;
            }
            Combo targetCombo = comboBoxes[indexes[0]][indexes[1] + 1];
            targetCombo.setItems(new String[0]);
            if (indexes[1] == 0) {
                // there was changed value in first (variable) combo in 'i' row
                Variable variable = VariableUtils.getVariableByScriptingName(variables, combo.getText());
                combo.setData(DATA_VARIABLE_KEY, variable);
                if (variable != null) {
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                    for (Operation operation : Operation.getAll(typeSupport)) {
                        targetCombo.add(operation.getVisibleName());
                    }
                }
            } else if (indexes[1] == 1) {
                // there was changed value in second (operation) combo in 'i'
                // row
                Variable variable1 = (Variable) comboBoxes[indexes[0]][0].getData(DATA_VARIABLE_KEY);
                if (variable1 != null) {
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
                    Operation operation = Operation.getByName(combo.getText(), typeSupport);
                    combo.setData(DATA_OPERATION_KEY, operation);
                    for (String variableName : getCombo3VariableNames(variable1)) {
                        targetCombo.add(variableName);
                    }
                    for (String pv : typeSupport.getPredefinedValues(operation)) {
                        targetCombo.add(pv);
                    }
                    if (typeSupport.hasUserInputEditor()) {
                        targetCombo.add(TypedUserInputCombo.INPUT_VALUE);
                    }
                }
            }
        } catch (RuntimeException e) {
            PluginLogger.logError(e);
        }
    }

    private List<String> getCombo3VariableNames(Variable variable1) {
        List<String> names = new ArrayList<String>();
        GroovyTypeSupport typeSupport1 = GroovyTypeSupport.get(variable1.getJavaClassName());
        for (Variable variable : variables) {
            GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
            // formats are equals, variable not selected in the first combo
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
            refreshComboItems((Combo) e.widget);
        }
    }

    private void toCode() {
        for (int i = 0; i < comboBoxes.length; i++) {
            for (int j = 0; j < 3; j++) {
                if (comboBoxes[i][j].getText().length() == 0 && !labels[i].getText().equals(defaultTransitionCombo.getText())) {
                    setErrorLabelText(Localization.getString("GroovyEditor.fillAll"));
                    // we cannot construct while all data not filled
                    return;
                }
            }
        }
        clearErrorLabelText();
        try {
            GroovyDecisionModel decisionModel = new GroovyDecisionModel();
            for (int i = 0; i < transitionNames.size(); i++) {
                IfExpr ifExpr;
                if (labels[i].getText().equals(defaultTransitionCombo.getText())) {
                    ifExpr = new IfExpr(labels[i].getText());
                } else {
                    Variable variable1 = (Variable) comboBoxes[i][0].getData(DATA_VARIABLE_KEY);
                    String operationName = comboBoxes[i][1].getItem(comboBoxes[i][1].getSelectionIndex());
                    String lexem2Text = comboBoxes[i][2].getText();
                    Object lexem2;
                    Variable variable2 = VariableUtils.getVariableByScriptingName(variables, lexem2Text);
                    if (variable2 != null) {
                        lexem2 = variable2;
                    } else {
                        lexem2 = lexem2Text;
                    }
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
                    ifExpr = new IfExpr(labels[i].getText(), variable1, lexem2, Operation.getByName(operationName, typeSupport));
                }
                decisionModel.addIfExpr(ifExpr);
            }
            styledText.setText(decisionModel.toString());
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
