package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.swimlane.RelationComposite;
import ru.runa.gpd.ui.custom.InsertVariableTextMenuDetectListener;
import ru.runa.gpd.ui.custom.JavaHighlightTextStyling;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.util.MultiinstanceParameters;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.lang.MultiTaskCreationMode;
import ru.runa.wfe.user.Group;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MultiinstanceComposite extends Composite {
    private final ProcessDefinition processDefinition;
    private final MultiinstanceParameters parameters;
    private Combo variableCombo;
    private Composite createSubprocessesByDiscriminatorComposite;
    private Combo swimlaneCombo;
    private StyledText conditionText;

    public MultiinstanceComposite(Composite parent, GraphElement graphElement, MultiinstanceParameters parameters) {
        super(parent, SWT.NONE);
        this.processDefinition = graphElement.getProcessDefinition();
        this.parameters = parameters;

        setLayout(new GridLayout());
        CTabFolder tabFolder = createTabFolder(this);
        tabFolder.setToolTipText(Localization.getString("Multiinstance.TypeMultiInstance"));
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        String variableLabelText;
        String groupLabelText;
        if (graphElement instanceof MultiTaskState) {
            variableLabelText = Localization.getString("Multiinstance.TasksVariableName");
            groupLabelText = Localization.getString("Multiinstance.TasksGroupName");
        } else {
            variableLabelText = Localization.getString("Multiinstance.SubprocessesVariableName");
            groupLabelText = Localization.getString("Multiinstance.SubprocessesGroupName");
        }
        Composite variableTabComposite = createTabVariable(tabFolder, variableLabelText);
        if (graphElement instanceof MultiTaskState) {
            createTaskCreationSettingsComposite(variableTabComposite);
        } else { // MultiSubprocess
            createSubprocessCreationSettingsComposite(variableTabComposite);
        }
        createTabGroup(tabFolder, groupLabelText);
        createTabRelation(tabFolder);
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        selectInitialTab(tabFolder);
    }

    private void createTaskCreationSettingsComposite(Composite parent) {
        Composite createTasksByExecutorsComposite = new Composite(parent, SWT.NONE);
        createTasksByExecutorsComposite.setLayout(new GridLayout(2, false));
        createTasksByExecutorsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        SWTUtils.createLabel(createTasksByExecutorsComposite, Localization.getString("MultiTask.property.creationMode"));
        final Combo creationCombo = new Combo(createTasksByExecutorsComposite, SWT.READ_ONLY);
        for (MultiTaskCreationMode creationMode : MultiTaskCreationMode.values()) {
            creationCombo.add(Localization.getString("MultiTask.property.creationMode." + creationMode));
        }
        creationCombo.setText(Localization.getString("MultiTask.property.creationMode." + parameters.getCreationMode()));
        creationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        creationCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                parameters.setCreationMode(MultiTaskCreationMode.values()[creationCombo.getSelectionIndex()]);
                if (parameters.getCreationMode() == MultiTaskCreationMode.BY_EXECUTORS) {
                    parameters.setSwimlaneName(null);
                }
                updateTaskCreationSettingsComposite();
            }
        });

        createSubprocessesByDiscriminatorComposite = new Composite(parent, SWT.NONE);
        createSubprocessesByDiscriminatorComposite.setLayout(new GridLayout(2, false));
        createSubprocessesByDiscriminatorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        SWTUtils.createLabel(createSubprocessesByDiscriminatorComposite, Localization.getString("SwimlanedNode.property.swimlane"));
        swimlaneCombo = new Combo(createSubprocessesByDiscriminatorComposite, SWT.READ_ONLY);
        for (Swimlane swimlane : processDefinition.getSwimlanes()) {
            swimlaneCombo.add(swimlane.getName());
        }
        swimlaneCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        swimlaneCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                parameters.setSwimlaneName(swimlaneCombo.getText());
            }
        });

        SWTUtils.createLabel(createSubprocessesByDiscriminatorComposite, Localization.getString("MultiTask.property.discriminatorCondition"));
        SWTUtils.createLink(createSubprocessesByDiscriminatorComposite, Localization.getString("button.insert_variable"),
                new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(getConditionVariableNames());
                        String variableName = dialog.openDialog();
                        if (variableName != null) {
                            conditionText.insert(variableName);
                            conditionText.setFocus();
                            conditionText.setCaretOffset(conditionText.getCaretOffset() + variableName.length());
                        }
                    }
                }).setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
        conditionText = new StyledText(createSubprocessesByDiscriminatorComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData conditionGridData = new GridData(GridData.FILL_BOTH);
        conditionGridData.horizontalSpan = 2;
        conditionText.setLayoutData(conditionGridData);
        conditionText.setLineSpacing(2);
        conditionText.addLineStyleListener(new JavaHighlightTextStyling(getConditionVariableNames()));
        conditionText.addModifyListener(new LoggingModifyTextAdapter() {

            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                parameters.setDiscriminatorCondition(conditionText.getText());
            }
        });
        updateTaskCreationSettingsComposite();
    }

    private void createSubprocessCreationSettingsComposite(Composite parent) {
        createSubprocessesByDiscriminatorComposite = new Composite(parent, SWT.NONE);
        createSubprocessesByDiscriminatorComposite.setLayout(new GridLayout(2, false));
        createSubprocessesByDiscriminatorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        SWTUtils.createLabel(createSubprocessesByDiscriminatorComposite, Localization.getString("MultiSubprocess.property.discriminatorCondition"));
        SWTUtils.createLink(createSubprocessesByDiscriminatorComposite, Localization.getString("button.insert_variable"),
                new LoggingHyperlinkAdapter() {
                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(getConditionVariableNames());
                        String variableName = dialog.openDialog();
                        if (variableName != null) {
                            conditionText.insert(variableName);
                            conditionText.setFocus();
                            conditionText.setCaretOffset(conditionText.getCaretOffset() + variableName.length());
                        }
                    }
                }).setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
        conditionText = new StyledText(createSubprocessesByDiscriminatorComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData conditionGridData = new GridData(GridData.FILL_BOTH);
        conditionGridData.horizontalSpan = 2;
        conditionText.setLayoutData(conditionGridData);
        conditionText.setLineSpacing(2);
        conditionText.addLineStyleListener(new JavaHighlightTextStyling(getConditionVariableNames()));
        conditionText.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                parameters.setDiscriminatorCondition(conditionText.getText());
            }
        });
        updateSubprocessCreationSettingsComposite();
    }

    private List<String> getConditionVariableNames() {
        List<String> conditionVariableNames = Lists.newArrayList("index", "item");
        Variable variable = VariableUtils.getVariableByName(processDefinition, parameters.getDiscriminatorVariableName());
        if (variable != null) {
            VariableUserType userType = processDefinition.getVariableUserType(variable.getFormatComponentClassNames()[0]);
            if (userType != null) {
                Variable itemVariable = new Variable("item", "item", variable);
                Variable userTypeVariable = new Variable("", "", userType.getName(), userType);
                for (Variable expanded : VariableUtils.expandComplexVariable(itemVariable, userTypeVariable)) {
                    conditionVariableNames.add(expanded.getScriptingName());
                }
            }
        }
        return conditionVariableNames;
    }

    private void updateTaskCreationSettingsComposite() {
        if (parameters.getCreationMode() == MultiTaskCreationMode.BY_EXECUTORS) {
            createSubprocessesByDiscriminatorComposite.setVisible(false);
        } else {
            if (!Strings.isNullOrEmpty(parameters.getSwimlaneName())) {
                swimlaneCombo.setText(parameters.getSwimlaneName());
            }
            conditionText.setText(parameters.getDiscriminatorCondition() != null ? parameters.getDiscriminatorCondition() : "");
            createSubprocessesByDiscriminatorComposite.setVisible(true);
        }
        getParent().layout(true);
    }

    private void updateSubprocessCreationSettingsComposite() {
        conditionText.setText(parameters.getDiscriminatorCondition() != null ? parameters.getDiscriminatorCondition() : "");
    }

    private Composite createTabVariable(CTabFolder parent, String variableLabelText) {
        Composite variableTabComposite = new Composite(parent, SWT.NONE);
        variableTabComposite.setLayout(new GridLayout());

        CTabItem tabItem1 = new CTabItem(parent, SWT.NONE);
        tabItem1.setText(Localization.getString("Multiinstance.tab.variable"));
        tabItem1.setControl(variableTabComposite);

        variableTabComposite.setLayout(new GridLayout());
        Label variableLabel = new Label(variableTabComposite, SWT.READ_ONLY);
        variableLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        variableLabel.setText(variableLabelText);
        List<String> variableNames = getDiscriminatorVariableNames();

        variableCombo = new Combo(variableTabComposite, SWT.READ_ONLY);
        variableCombo.setItems(variableNames.toArray(new String[variableNames.size()]));
        variableCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (parameters.getDiscriminatorVariableName() != null) {
            variableCombo.setText(parameters.getDiscriminatorVariableName());
        }
        variableCombo.addModifyListener(new LoggingModifyTextAdapter() {

            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                parameters.setDiscriminatorVariableName(variableCombo.getText());
            }
        });
        return variableTabComposite;
    }

    private List<String> getDiscriminatorVariableNames() {
        return processDefinition.getVariableNames(false, List.class.getName());
    }

    private void createTabGroup(CTabFolder parent, String groupLabelText) {
        Composite composite1 = new Composite(parent, SWT.NONE);
        composite1.setLayout(new GridLayout());
        CTabItem tabItem1 = new CTabItem(parent, SWT.NONE);
        tabItem1.setText(Localization.getString("Multiinstance.tab.group"));
        tabItem1.setControl(composite1);

        composite1.setLayout(new GridLayout());
        Label groupLabel = new Label(composite1, SWT.READ_ONLY);
        groupLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupLabel.setText(groupLabelText);
        final List<String> groupVariableNames = getDiscriminatorGroupNames();
        String lastUserInputValue = parameters.isDiscriminatorGroupInputAsText() ? parameters.getDiscriminatorGroup() : null;
        final TypedUserInputCombo groupCombo = new TypedUserInputCombo(composite1, lastUserInputValue);
        groupCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupCombo.setShowEmptyValue(false);
        for (String variableName : groupVariableNames) {
            groupCombo.add(variableName);
        }
        groupCombo.setTypeClassName(String.class.getName());
        groupCombo.setText(parameters.getDiscriminatorGroup());
        groupCombo.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String selected = groupCombo.getText();
                if (!TypedUserInputCombo.INPUT_VALUE.equals(selected)) {
                    parameters.setDiscriminatorGroupInputAsText(!groupVariableNames.contains(selected));
                    parameters.setDiscriminatorGroup(selected);
                }
            }
        });
    }

    private List<String> getDiscriminatorGroupNames() {
        return processDefinition.getVariableNames(true, String.class.getName(), Group.class.getName());
    }

    private void selectInitialTab(CTabFolder tabFolder) {

        if (VariableMapping.USAGE_DISCRIMINATOR_VARIABLE.equals(parameters.getDiscriminatorType())) {
            tabFolder.setSelection(0);
            // gridData.heightHint = 100;
        } else if (VariableMapping.USAGE_DISCRIMINATOR_GROUP.equals(parameters.getDiscriminatorType())) {
            tabFolder.setSelection(1);
            // gridData.heightHint = 100;
        } else if (VariableMapping.USAGE_DISCRIMINATOR_RELATION.equals(parameters.getDiscriminatorType())) {
            tabFolder.setSelection(2);
            // gridData.heightHint = 170;
        } else {
            throw new RuntimeException("Unexpected type value = " + parameters.getDiscriminatorType());
        }
    }

    private CTabFolder createTabFolder(Composite parent) {

        final CTabFolder tabFolder = new CTabFolder(this, SWT.TOP | SWT.BORDER);
        tabFolder.setToolTipText(Localization.getString("Multiinstance.TypeMultiInstance"));
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        tabFolder.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {

                int newPageIndex = tabFolder.indexOf((CTabItem) e.item);

                // GridData gridData = (GridData) getLayoutData();
                if (newPageIndex == 0) {
                    parameters.setDiscriminatorType(VariableMapping.USAGE_DISCRIMINATOR_VARIABLE);
                    // gridData.heightHint = 100;
                } else if (newPageIndex == 1) {
                    parameters.setDiscriminatorType(VariableMapping.USAGE_DISCRIMINATOR_GROUP);
                    // gridData.heightHint = 100;
                } else if (newPageIndex == 2) {
                    parameters.setDiscriminatorType(VariableMapping.USAGE_DISCRIMINATOR_RELATION);
                    // gridData.heightHint = 170;
                }
                getParent().layout(true);
            }
        });

        return tabFolder;
    }

    private void createTabRelation(CTabFolder parent) {

        Composite composite1 = new Composite(parent, SWT.NONE);
        composite1.setLayout(new GridLayout());
        CTabItem tabItem1 = new CTabItem(parent, SWT.NONE);
        tabItem1.setText(Localization.getString("Multiinstance.tab.relation"));
        tabItem1.setControl(composite1);

        composite1.setLayout(new GridLayout());
        RelationEditor relationEditor = new RelationEditor(composite1);
        relationEditor.init(parameters.getDiscriminatorRelation());
    }

    private class RelationEditor extends RelationComposite {

        public RelationEditor(Composite parent) {
            super(parent, true, processDefinition);
            new InsertVariableTextMenuDetectListener(relationNameText, processDefinition.getVariableNames(false, String.class.getName()));
        }

    }

}
