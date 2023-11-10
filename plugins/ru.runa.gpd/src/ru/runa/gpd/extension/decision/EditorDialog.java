package ru.runa.gpd.extension.decision;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.JavaHighlightTextStyling;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.ChooseGroovyStuffDialog;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.ui.dialog.FilterBox;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.gpd.util.GroovyStuff;
import ru.runa.gpd.util.GroovyStuff.Item;
import ru.runa.gpd.util.VariableUtils;

public abstract class EditorDialog<T extends GroovyModel> extends Dialog {
    protected static final String DATA_INDEX_KEY = "indexes";
    protected static final String DATA_VARIABLE_KEY = "variable";
    protected static final String DATA_USER_INPUT_KEY = "userInput";
    protected static final String DATA_OPERATION_KEY = "operation";

    protected TabFolder tabFolder;
    protected StyledText styledText;
    protected Composite constructor;
    protected final String initialValue;
    protected T initialModel;
    protected String initialErrorMessage;
    protected final List<Variable> variables;
    protected final List<String> variableNames;
    protected ErrorHeaderComposite constructorHeader;
    protected ErrorHeaderComposite sourceHeader;
    protected String result;

    public EditorDialog(ProcessDefinition definition, String initialValue) {
        super(Display.getCurrent().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.initialValue = initialValue;
        this.variables = definition.getVariables(true, true);
        this.variableNames = VariableUtils.getVariableNamesForScripting(variables);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(700, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setMinimumSize(getInitialSize());
        getShell().setText(Localization.getString("GroovyEditor.title"));
        tabFolder = new TabFolder(parent, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        tabFolder.addSelectionListener(new TabSelectionHandler());
        Composite constructorView = new Composite(tabFolder, SWT.NONE);
        constructorView.setLayout(new GridLayout());
        TabItem constructorTab = new TabItem(tabFolder, SWT.NONE);
        constructorTab.setText(Localization.getString("GroovyEditor.title.constructor"));
        constructorTab.setControl(constructorView);
        constructorHeader = new ErrorHeaderComposite(constructorView);
        ScrolledComposite scrolledComposite = new ScrolledComposite(constructorView, SWT.V_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        constructor = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(constructor);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 5;
        constructor.setLayout(gridLayout);
        scrolledComposite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                scrolledComposite.setMinSize(constructor.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }
        });
        constructor.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite sourceView = new Composite(tabFolder, SWT.NONE);
        sourceView.setLayout(new GridLayout());
        sourceView.setLayoutData(new GridData(GridData.FILL_BOTH));
        sourceHeader = new ErrorHeaderComposite(sourceView);
        for(GroovyStuff value: GroovyStuff.values()) {
            if (value.getAll().size() > 0) {
                createLink(value);
            }
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
        styledText.setText(this.initialValue);
        styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
        TabItem codeTab = new TabItem(tabFolder, SWT.NONE);
        codeTab.setText(Localization.getString("GroovyEditor.title.code"));
        codeTab.setControl(sourceView);
        createConstructorView();
        try {
            if (initialModel != null && initialValue.equals(initialModel.toString())) {
                initializeConstructorView();
            } else {
                if (this.initialValue.length() > 0) {
                    tabFolder.setSelection(1);
                }
                if (initialErrorMessage != null) {
                    setErrorLabelText(initialErrorMessage);
                }
            }
        } catch (RuntimeException e) {
            tabFolder.setSelection(1);
        }
        return tabFolder;
    }
    
    protected void createLink(GroovyStuff type) {
        SwtUtils.createLink(sourceHeader, Localization.getString("Insert." + type + ".link"), new LoggingHyperlinkAdapter() {
            @Override
            public void onLinkActivated(HyperlinkEvent e) {
                Item item = new ChooseGroovyStuffDialog(type).openDialog();
                if (item != null) {
                    String insert = item.getBody();
                    styledText.insert(insert);
                    styledText.setCaretOffset(styledText.getCaretOffset() + insert.length());
                    styledText.setFocus();
                }
            }
        }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    }

    protected void setErrorLabelText(String text) {
        constructorHeader.setErrorText(text);
        sourceHeader.setErrorText(text);
    }

    protected void clearErrorLabelText() {
        constructorHeader.clearErrorText();
        sourceHeader.clearErrorText();
    }

    protected void createConstructorView() {
    };

    protected void initializeConstructorView() {
    };

    protected GridData getGridData() {
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.minimumWidth = 100;
        return data;
    }

    protected void refresh(Combo operationCombo) {
    };

    protected void refreshOperationCombo(Combo operationCombo, Variable firstVariable, FilterBox targetCombo) {
        targetCombo.setItems(new String[0]);
        GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
        Operation operation = Operation.getByName(operationCombo.getText(), typeSupport);
        operationCombo.setData(DATA_OPERATION_KEY, operation);
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

    protected void refresh(FilterBox filterBox) {
    };

    protected void refreshFilterBox(FilterBox filterBox, String oldUserInput, Variable variable) {
        GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
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
    }

    protected void refreshFilterBox(FilterBox filterBox, Combo operationCombo) {
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

    protected List<String> getSecondVariableNames(Variable firstVariable) {
        List<String> names = new ArrayList<>();
        GroovyTypeSupport typeSupportFirstVariable = GroovyTypeSupport.get(firstVariable.getJavaClassName());
        for (Variable variable : variables) {
            GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
            if (typeSupportFirstVariable.getClass() == typeSupport.getClass() && firstVariable != variable) {
                names.add(variable.getScriptingName());
            }
        }
        return names;
    }

    protected class TabSelectionHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (tabFolder.getSelectionIndex() == 1) {
                toCode();
            }
        }
    }

    protected class ErrorHeaderComposite extends Composite {
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

    public class ComboSelectionHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            refresh((Combo) e.widget);
        }
    }

    public class FilterBoxSelectionHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            refresh((FilterBox) e.widget);
        }
    }

    protected void toCode() {
    };

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
