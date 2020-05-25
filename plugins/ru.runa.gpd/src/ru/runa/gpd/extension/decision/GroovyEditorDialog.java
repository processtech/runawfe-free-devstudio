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
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.ChooseGroovyStuffDialog;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.ui.dialog.FilterBox;
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
    private FilterBox[][] varBoxes;
    private Combo[] operBoxes;
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
        varBoxes = new FilterBox[transitionNames.size()][2];
        operBoxes = new Combo[transitionNames.size()];
        labels = new Label[transitionNames.size()];
        for (int i = 0; i < transitionNames.size(); i++) {
            labels[i] = new Label(constructor, SWT.NONE);
            labels[i].setText(transitionNames.get(i));
            labels[i].setLayoutData(getGridData());
            varBoxes[i][0] = new FilterBox(constructor, VariableUtils.getVariableNamesForScripting(variables));
            varBoxes[i][0].setData(DATA_INDEX_KEY, new int[] { i, 0 });
            varBoxes[i][0].setSelectionListener(new FilterBoxSelectionHandler());
            varBoxes[i][0].setLayoutData(getGridData());
            
            operBoxes[i] = new Combo(constructor, SWT.READ_ONLY);
            operBoxes[i].setData(DATA_INDEX_KEY, new int[] { i, 1 });
            operBoxes[i].addSelectionListener(new ComboSelectionHandler());
            operBoxes[i].setLayoutData(getGridData());
            
            varBoxes[i][1] = new FilterBox(constructor, null);
            varBoxes[i][1].setData(DATA_INDEX_KEY, new int[] { i, 2 });
            varBoxes[i][1].setSelectionListener(new FilterBoxSelectionHandler());
            varBoxes[i][1].setLayoutData(getGridData());
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
        for (int i = 0; i < varBoxes.length; i++) {
            for (int j = 0; j < 2; j++) {
                varBoxes[i][j].setSize(100, 20);
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
                        varBoxes[j][0].setEnabled(enabled);
                        operBoxes[j].setEnabled(enabled);
                        varBoxes[j][1].setEnabled(enabled);
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
                    varBoxes[i][0].setEnabled(false);
                    operBoxes[i].setEnabled(false);
                    varBoxes[i][1].setEnabled(false);
                    defaultTransitionCombo.setText(ifExpr.getTransition());
                } else {
                    Variable variable = ifExpr.getVariable1();
                    int index = variables.indexOf(variable);
                    if (index == -1) {
                        // required variable was deleted in process definition
                        continue;
                    }
                    varBoxes[i][0].select(index);
                    refresh(varBoxes[i][0]);
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                    index = Operation.getAll(typeSupport).indexOf(ifExpr.getOperation());
                    if (index == -1) {
                        // required operation was deleted !!!
                        continue;
                    }
                    operBoxes[i].select(index);
                    refresh(operBoxes[i]);
                    String lexem2Text = ifExpr.getLexem2TextValue();
                    int var2index = 0;
                    if (VariableUtils.getVariableByScriptingName(variables, lexem2Text) != null) {
                        var2index = getVariable2Names(variable).indexOf(lexem2Text);
                    } else {
                        int predefinedIndex = typeSupport.getPredefinedValues(ifExpr.getOperation()).indexOf(lexem2Text);
                        if (predefinedIndex >= 0) {
                            var2index = getVariable2Names(variable).size() + predefinedIndex;
                        } else {
                            varBoxes[i][1].add(lexem2Text, 0);
                            varBoxes[i][1].setData(DATA_USER_INPUT_KEY, lexem2Text);
                        }
                    }
                    varBoxes[i][1].select(var2index);
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
        boolean enabledIndex = varBoxes[recordIndex][0].getEnabled();
        boolean enabledIndexPrev = varBoxes[recordIndex - 1][0].getEnabled();
        int var1Index = varBoxes[recordIndex][0].getSelectionIndex();
        int operIndex = operBoxes[recordIndex].getSelectionIndex();
        int var2Index = varBoxes[recordIndex][1].getSelectionIndex();
        String var2UserInput = (String) varBoxes[recordIndex][1].getData(DATA_USER_INPUT_KEY);
        varBoxes[recordIndex][0].select(varBoxes[recordIndex - 1][0].getSelectionIndex());
        varBoxes[recordIndex][0].setEnabled(enabledIndexPrev);
        refresh(varBoxes[recordIndex][0]);
        operBoxes[recordIndex].select(operBoxes[recordIndex - 1].getSelectionIndex());
        operBoxes[recordIndex].setEnabled(enabledIndexPrev);
        refresh(operBoxes[recordIndex]);
        String var2UserInput2 = (String) varBoxes[recordIndex - 1][1].getData(DATA_USER_INPUT_KEY);
        if (var2UserInput2 != null) {
            varBoxes[recordIndex][1].add(var2UserInput2, 0);
            varBoxes[recordIndex][1].setData(DATA_USER_INPUT_KEY, var2UserInput2);
        }
        varBoxes[recordIndex][1].select(varBoxes[recordIndex - 1][1].getSelectionIndex());
        varBoxes[recordIndex][1].setEnabled(enabledIndexPrev);
        varBoxes[recordIndex - 1][0].select(var1Index);
        varBoxes[recordIndex - 1][0].setEnabled(enabledIndex);
        refresh(varBoxes[recordIndex - 1][0]);
        operBoxes[recordIndex - 1].select(operIndex);
        operBoxes[recordIndex - 1].setEnabled(enabledIndex);
        refresh(operBoxes[recordIndex - 1]);
        if (var2UserInput != null) {
            varBoxes[recordIndex - 1][1].add(var2UserInput, 0);
            varBoxes[recordIndex - 1][1].setData(DATA_USER_INPUT_KEY, var2UserInput);
        }
        varBoxes[recordIndex - 1][1].select(var2Index);
        varBoxes[recordIndex - 1][1].setEnabled(enabledIndex);
    }

    private void refresh(FilterBox filterBox) {
        try {
            int[] indexes = (int[]) filterBox.getData(DATA_INDEX_KEY);
            if (indexes[1] == 2) {
                if (TypedUserInputCombo.INPUT_VALUE.equals(filterBox.getSelectedItem())) {
                    String oldUserInput = (String) filterBox.getData(DATA_USER_INPUT_KEY);
                    Variable variable1 = (Variable) varBoxes[indexes[0]][0].getData(DATA_VARIABLE_KEY);
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
                Combo operCombo = operBoxes[indexes[0]];
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
            Variable variable1 = (Variable) varBoxes[indexes[0]][0].getData(DATA_VARIABLE_KEY);
            if (variable1 != null) {
                FilterBox targetCombo = varBoxes[indexes[0]][1];
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
        for (int i = 0; i < varBoxes.length; i++) {
            for (int j = 0; j < 2; j++) {
                if (varBoxes[i][j].getText().length() == 0 && !labels[i].getText().equals(defaultTransitionCombo.getText())) {
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
                    Variable variable1 = (Variable) varBoxes[i][0].getData(DATA_VARIABLE_KEY);
                    String operationName = operBoxes[i].getItem(operBoxes[i].getSelectionIndex());
                    String lexem2Text = varBoxes[i][1].getText();
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
