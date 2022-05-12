package ru.runa.gpd.extension.decision;

import java.util.ArrayList;
import java.util.List;
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
import ru.runa.gpd.extension.decision.GroovyDecisionModel.IfExpression;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.FilterBox;
import ru.runa.gpd.util.VariableUtils;

public class GroovyEditorDialog extends EditorDialog<GroovyDecisionModel> {
    private static final String NO_TRANSITION_BY_DEFAULT = "GroovyEditor.no.transition.byDefault";
    private static final Image upImage = SharedImages.getImage("icons/up.gif");
    private final List<String> transitionNames;
    private Label[] labels;
    private FilterBox[][] variableBoxes;
    private Combo[] operationBoxes;
    private Combo defaultTransitionCombo;

    public GroovyEditorDialog(ProcessDefinition definition, List<String> transitionNames, String initialValue) {
        super(definition, initialValue);
        this.transitionNames = transitionNames;
        if (this.initialValue.length() > 0) {
            try {
                initialModel = new GroovyDecisionModel(initialValue, variables);
            } catch (Throwable e) {
                initialErrorMessage = e.getMessage();
                PluginLogger.logErrorWithoutDialog("", e);
            }
        }
    }

    @Override
    protected void createConstructorView() {
        if (initialModel != null) {
            // reorder transitions;
            List<String> transitionNamesToAdd = new ArrayList<>(transitionNames);
            transitionNames.clear();
            for (IfExpression ifExpression : initialModel.getIfExpressions()) {
                if (transitionNamesToAdd.remove(ifExpression.getTransition())) {
                    transitionNames.add(ifExpression.getTransition());
                }
            }
            transitionNames.addAll(transitionNamesToAdd);
        }
        variableBoxes = new FilterBox[transitionNames.size()][2];
        operationBoxes = new Combo[transitionNames.size()];
        labels = new Label[transitionNames.size()];
        for (int i = 0; i < transitionNames.size(); i++) {
            labels[i] = new Label(constructor, SWT.NONE);
            labels[i].setText(transitionNames.get(i));
            labels[i].setLayoutData(getGridData());
            variableBoxes[i][0] = new FilterBox(constructor, VariableUtils.getVariableNamesForScripting(variables));
            variableBoxes[i][0].setData(DATA_INDEX_KEY, new int[] { i, 0 });
            variableBoxes[i][0].setSelectionListener(new FilterBoxSelectionHandler());
            variableBoxes[i][0].setLayoutData(getGridData());

            operationBoxes[i] = new Combo(constructor, SWT.READ_ONLY);
            operationBoxes[i].setData(DATA_INDEX_KEY, new int[] { i, 1 });
            operationBoxes[i].addSelectionListener(new ComboSelectionHandler());
            operationBoxes[i].setLayoutData(getGridData());

            variableBoxes[i][1] = new FilterBox(constructor, null);
            variableBoxes[i][1].setData(DATA_INDEX_KEY, new int[] { i, 2 });
            variableBoxes[i][1].setSelectionListener(new FilterBoxSelectionHandler());
            variableBoxes[i][1].setLayoutData(getGridData());
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
        for (int i = 0; i < variableBoxes.length; i++) {
            for (int j = 0; j < 2; j++) {
                variableBoxes[i][j].setSize(100, 20);
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
            defaultTransitionCombo.add(Localization.getString(NO_TRANSITION_BY_DEFAULT));
            defaultTransitionCombo.select(0);
            for (String trName : transitionNames) {
                defaultTransitionCombo.add(trName);
            }
            defaultTransitionCombo.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    for (int j = 0; j < labels.length; j++) {
                        boolean enabled = !labels[j].getText().equals(defaultTransitionCombo.getText());
                        variableBoxes[j][0].setEnabled(enabled);
                        operationBoxes[j].setEnabled(enabled);
                        variableBoxes[j][1].setEnabled(enabled);
                    }
                }
            });
            bottomComposite.pack();
        }
    }

    @Override
    protected void initializeConstructorView() {
        for (int i = 0; i < transitionNames.size(); i++) {
            IfExpression ifExpression = initialModel.getIfExpression(transitionNames.get(i));
            if (ifExpression != null) {
                labels[i].setText(ifExpression.getTransition());
                if (ifExpression.isByDefault()) {
                    variableBoxes[i][0].setEnabled(false);
                    operationBoxes[i].setEnabled(false);
                    variableBoxes[i][1].setEnabled(false);
                    defaultTransitionCombo.setText(ifExpression.getTransition());
                } else {
                    Variable variable = ifExpression.getFirstVariable();
                    int index = variables.indexOf(variable);
                    if (index == -1) {
                        // required variable was deleted in process definition
                        continue;
                    }
                    variableBoxes[i][0].select(index);
                    refresh(variableBoxes[i][0]);
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
                    index = Operation.getAll(typeSupport).indexOf(ifExpression.getOperation());
                    if (index == -1) {
                        // required operation was deleted !!!
                        continue;
                    }
                    operationBoxes[i].select(index);
                    refresh(operationBoxes[i]);
                    String secondVariableText = ifExpression.getSecondVariableTextValue();
                    int secondVariableIndex = 0;
                    if (VariableUtils.getVariableByScriptingName(variables, secondVariableText) != null) {
                        secondVariableIndex = getSecondVariableNames(variable).indexOf(secondVariableText);
                    } else {
                        int predefinedIndex = typeSupport.getPredefinedValues(ifExpression.getOperation()).indexOf(secondVariableText);
                        if (predefinedIndex >= 0) {
                            secondVariableIndex = getSecondVariableNames(variable).size() + predefinedIndex;
                        } else {
                            variableBoxes[i][1].add(secondVariableText, 0);
                            variableBoxes[i][1].setData(DATA_USER_INPUT_KEY, secondVariableText);
                        }
                    }
                    variableBoxes[i][1].select(secondVariableIndex);
                }
            }
        }
    }

    private void upRecord(Integer recordIndex) {
        String recordText = labels[recordIndex].getText();
        labels[recordIndex].setText(labels[recordIndex - 1].getText());
        labels[recordIndex - 1].setText(recordText);
        boolean enabledIndex = variableBoxes[recordIndex][0].getEnabled();
        boolean enabledIndexPrev = variableBoxes[recordIndex - 1][0].getEnabled();
        int firstVariableIndex = variableBoxes[recordIndex][0].getSelectionIndex();
        int operationIndex = operationBoxes[recordIndex].getSelectionIndex();
        int secondVariableIndex = variableBoxes[recordIndex][1].getSelectionIndex();
        String secondVariableUserInput = (String) variableBoxes[recordIndex][1].getData(DATA_USER_INPUT_KEY);
        variableBoxes[recordIndex][0].select(variableBoxes[recordIndex - 1][0].getSelectionIndex());
        variableBoxes[recordIndex][0].setEnabled(enabledIndexPrev);
        refresh(variableBoxes[recordIndex][0]);
        operationBoxes[recordIndex].select(operationBoxes[recordIndex - 1].getSelectionIndex());
        operationBoxes[recordIndex].setEnabled(enabledIndexPrev);
        refresh(operationBoxes[recordIndex]);
        String secondVariableUserInput2 = (String) variableBoxes[recordIndex - 1][1].getData(DATA_USER_INPUT_KEY);
        if (secondVariableUserInput2 != null) {
            variableBoxes[recordIndex][1].add(secondVariableUserInput2, 0);
            variableBoxes[recordIndex][1].setData(DATA_USER_INPUT_KEY, secondVariableUserInput2);
        }
        variableBoxes[recordIndex][1].select(variableBoxes[recordIndex - 1][1].getSelectionIndex());
        variableBoxes[recordIndex][1].setEnabled(enabledIndexPrev);
        variableBoxes[recordIndex - 1][0].select(firstVariableIndex);
        variableBoxes[recordIndex - 1][0].setEnabled(enabledIndex);
        refresh(variableBoxes[recordIndex - 1][0]);
        operationBoxes[recordIndex - 1].select(operationIndex);
        operationBoxes[recordIndex - 1].setEnabled(enabledIndex);
        refresh(operationBoxes[recordIndex - 1]);
        if (secondVariableUserInput != null) {
            variableBoxes[recordIndex - 1][1].add(secondVariableUserInput, 0);
            variableBoxes[recordIndex - 1][1].setData(DATA_USER_INPUT_KEY, secondVariableUserInput);
        }
        variableBoxes[recordIndex - 1][1].select(secondVariableIndex);
        variableBoxes[recordIndex - 1][1].setEnabled(enabledIndex);
    }

    @Override
    protected void refresh(FilterBox filterBox) {
        try {
            int[] indexes = (int[]) filterBox.getData(DATA_INDEX_KEY);
            if (indexes[1] == 2) {
                if (TypedUserInputCombo.INPUT_VALUE.equals(filterBox.getSelectedItem())) {
                    String oldUserInput = (String) filterBox.getData(DATA_USER_INPUT_KEY);
                    Variable firstVariable = (Variable) variableBoxes[indexes[0]][0].getData(DATA_VARIABLE_KEY);
                    refreshFilterBox(filterBox, oldUserInput, firstVariable);
                } else {
                    Variable variable = VariableUtils.getVariableByScriptingName(variables, filterBox.getText());
                    if (variable != null) {
                        filterBox.setData(DATA_VARIABLE_KEY, variable);
                    }
                }
                return;
            }
            if (indexes[1] == 0) {
                refreshFilterBox(filterBox, operationBoxes[indexes[0]]);
            }
        } catch (RuntimeException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    protected void refresh(Combo operationCombo) {
        try {
            int[] indexes = (int[]) operationCombo.getData(DATA_INDEX_KEY);
            Variable firstVariable = (Variable) variableBoxes[indexes[0]][0].getData(DATA_VARIABLE_KEY);
            if (firstVariable != null) {
                refreshOperationCombo(operationCombo, firstVariable, variableBoxes[indexes[0]][1]);
            }
        } catch (RuntimeException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    protected void toCode() {
        for (int i = 0; i < variableBoxes.length; i++) {
            for (int j = 0; j < 2; j++) {
                boolean emptyFieldExist = variableBoxes[i][j].getText().length() == 0
                        && !labels[i].getText().equals(defaultTransitionCombo.getText());
                if (emptyFieldExist) {
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
                IfExpression ifExpression;
                if (labels[i].getText().equals(defaultTransitionCombo.getText())) {
                    ifExpression = new IfExpression(labels[i].getText());
                } else {
                    Variable firstVariable = (Variable) variableBoxes[i][0].getData(DATA_VARIABLE_KEY);
                    String operationName = operationBoxes[i].getItem(operationBoxes[i].getSelectionIndex());
                    String secondVariableText = variableBoxes[i][1].getText();
                    Object secondVariable = VariableUtils.getVariableByScriptingName(variables, secondVariableText);
                    if (secondVariable == null) {
                        secondVariable = secondVariableText;
                    }
                    GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
                    ifExpression = new IfExpression(labels[i].getText(), firstVariable, secondVariable, Operation.getByName(operationName, typeSupport));
                }
                decisionModel.addIfExpression(ifExpression);
            }
            styledText.setText(decisionModel.toString());
        } catch (RuntimeException e1) {
            PluginLogger.logError(e1);
            setErrorLabelText(Localization.getString("GroovyEditor.error.construct"));
        }
    }

}
