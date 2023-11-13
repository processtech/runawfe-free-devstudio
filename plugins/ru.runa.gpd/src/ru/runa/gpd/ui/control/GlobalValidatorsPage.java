package ru.runa.gpd.ui.control;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.editor.clipboard.GlobalValidatorTransfer;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.control.ValidatorInfoControl.ParametersComposite;
import ru.runa.gpd.ui.custom.FeaturedStyledText;
import ru.runa.gpd.ui.custom.JavaHighlightTextStyling;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.dialog.ChooseGroovyStuffDialog;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.ui.dialog.GlobalValidatorExpressionConstructorDialog;
import ru.runa.gpd.util.GroovyStuff;
import ru.runa.gpd.util.GroovyStuff.Item;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;
import ru.runa.wfe.execution.dto.WfProcess;

public class GlobalValidatorsPage extends Composite implements PropertyChangeListener {
    private static final Log log = LogFactory.getLog(GlobalValidatorsPage.class);

    private final FormNode formNode;
    private TableViewer validatorsTableViewer;
    private Button deleteButton;
    private Button copyButton;
    private ValidatorInfoControl infoGroup;
    private List<ValidatorConfig> validatorConfigs;
    private List<Variable> variables;
    private List<String> variableNames;
    private List<String> contextVariableNames;
    private final ValidatorDefinition globalDefinition = ValidatorDefinitionRegistry.getGlobalDefinition();
    private boolean dirty;
    private Consumer<Boolean> dirtyCallback;

    public GlobalValidatorsPage(Composite parent, FormNode formNode, FormNodeValidation validation, Consumer<Boolean> dirtyCallback) {
        super(parent, SWT.NONE);
        this.formNode = formNode;
        updateVariableNames();
        this.validatorConfigs = validation.getGlobalConfigs();
        this.dirtyCallback = dirtyCallback;
        initUi();
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyNames.PROPERTY_CHILDREN_CHANGED.equals(evt.getPropertyName())) {
            updateVariableNames();
        }
    }

    @Override
    public void dispose() {
        formNode.getProcessDefinition().removePropertyChangeListener(this);
        super.dispose();
    }

    public void doSave() {
        infoGroup.saveConfig();
        Map<String, ValidatorConfig> globalConfigsMap = new HashMap<String, ValidatorConfig>(validatorConfigs.size());
        int discrimination = 1;
        for (ValidatorConfig config : validatorConfigs) {
            globalConfigsMap.put(config.getType() + discrimination++, config);
        }
        setDirty(false);
    }

    private void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            if (dirty) {
                dirtyCallback.accept(dirty);
            }
        }
    }

    private void updateVariableNames() {
        variables = formNode.getVariables(true, true);
        variableNames = VariableUtils.getVariableNamesForScripting(variables);
        contextVariableNames = Lists.newArrayList(variableNames);
        contextVariableNames.add(WfProcess.SELECTED_TRANSITION_KEY);
    }

    private void initUi() {
        setLayout(new GridLayout(1, false));
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        SashForm mainComposite = new SashForm(this, SWT.VERTICAL);
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite valComposite = new Composite(mainComposite, SWT.NONE);
        GridData valGridData = new GridData(GridData.FILL_BOTH);
        valComposite.setLayoutData(valGridData);
        valComposite.setLayout(new GridLayout(2, false));

        validatorsTableViewer = new TableViewer(valComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 100;
        validatorsTableViewer.getControl().setLayoutData(data);
        validatorsTableViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((ValidatorConfig) element).getMessage();
            }
        });
        validatorsTableViewer.setContentProvider(new ArrayContentProvider());
        validatorsTableViewer.setInput(validatorConfigs);
        validatorsTableViewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            protected void onSelectionChanged(SelectionChangedEvent event) throws Exception {
                try {
                    ValidatorConfig config = (ValidatorConfig) ((IStructuredSelection) validatorsTableViewer.getSelection()).getFirstElement();
                    deleteButton.setEnabled(config != null);
                    copyButton.setEnabled(config != null);
                    infoGroup.setConfig(ValidatorConfig.GLOBAL_FIELD_ID, globalDefinition, config);
                } finally {
                }
            }
        });

        Table table = validatorsTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(Localization.getString("GlobalValidatorsWizardPage.SelectedValidators"));
        tableColumn.setWidth(500);

        GridData btnGridData = new GridData();
        btnGridData.horizontalAlignment = SWT.LEFT;
        btnGridData.verticalAlignment = SWT.TOP;
        Composite buttonsBar = new Composite(valComposite, SWT.NONE);
        buttonsBar.setLayoutData(btnGridData);
        buttonsBar.setLayout(new GridLayout(1, true));
        addButton(buttonsBar, "button.add", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent event) throws Exception {
                ValidatorConfig config = ValidatorDefinitionRegistry.getGlobalDefinition().create();
                config.setMessage(Localization.getString("GlobalValidatorsWizardPage.defaultValidationMessage"));
                validatorConfigs.add(config);
                validatorsTableViewer.refresh(true);
                validatorsTableViewer.setSelection(new StructuredSelection(config));
                setDirty(true);
            }
        });

        deleteButton = addButton(buttonsBar, "button.delete", new LoggingSelectionAdapter() {
            @Override
            @SuppressWarnings({ "unchecked" })
            protected void onSelection(SelectionEvent event) throws Exception {
                List<ValidatorConfig> selected = ((IStructuredSelection) validatorsTableViewer.getSelection()).toList();
                if (selected.isEmpty()) {
                    infoGroup.setVisible(false);
                    return;
                }
                for (ValidatorConfig config : selected) {
                    validatorConfigs.remove(config);
                }
                validatorsTableViewer.refresh(true);
                setDirty(true);
            }
        });
        copyButton = addButton(buttonsBar, "button.copy", new CopySelectionListener());
        addButton(buttonsBar, "button.paste", new PasteSelectionListener());

        deleteButton.setEnabled(false);
        copyButton.setEnabled(false);

        Composite bottomComposite = new Composite(mainComposite, SWT.NONE);
        bottomComposite.setLayout(new GridLayout(1, false));
        bottomComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridData infoGridData = new GridData(GridData.FILL_BOTH);
        infoGroup = new DefaultValidatorInfoControl(bottomComposite);
        infoGroup.setLayoutData(infoGridData);

        mainComposite.setWeights(new int[] { 1, 2 });
        mainComposite.pack(true);

        infoGroup.setVisible(false);
        formNode.getProcessDefinition().addPropertyChangeListener(this);

        validatorsTableViewer.setInput(validatorConfigs);
        validatorsTableViewer.refresh(true);
    }

    private Button addButton(Composite parent, String buttonKey, SelectionAdapter selectionListener) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Localization.getString(buttonKey));
        button.addSelectionListener(selectionListener);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return button;
    }

    public class DefaultValidatorInfoControl extends ValidatorInfoControl {
        private final Map<String, Button> transitionButtons = Maps.newHashMap();

        public DefaultValidatorInfoControl(Composite parent) {
            super(parent, false);
            errorMessageText.addVerifyListener(new VerifyListener() {
                @Override
                public void verifyText(VerifyEvent e) {
                    if (!isConfiguring(errorMessageText)) {
                        setDirty(true);
                    }
                }
            });
            if (formNode.getLeavingTransitions().size() > 1) {
                Group transitionsGroup = new Group(this, SWT.BORDER);
                transitionsGroup.setLayout(new GridLayout(1, false));
                transitionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                transitionsGroup.setText(Localization.getString("FieldValidatorsWizardPage.TransitionContext"));

                ScrolledComposite scrolledComposite = new ScrolledComposite(transitionsGroup, SWT.H_SCROLL);
                scrolledComposite.setExpandHorizontal(true);
                scrolledComposite.setLayout(new GridLayout(1, false));
                scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

                Group transitionChoisesGroup = new Group(scrolledComposite, SWT.BORDER);
                transitionChoisesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
                transitionChoisesGroup.setLayout(new GridLayout(3, false));

                for (Transition transition : formNode.getLeavingTransitions()) {
                    final Button button = new Button(transitionChoisesGroup, SWT.CHECK);
                    button.setText(transition.getName());
                    button.addSelectionListener(new LoggingSelectionAdapter() {

                        @Override
                        protected void onSelection(SelectionEvent e) throws Exception {
                            if (button.getSelection()) {
                                config.getTransitionNames().add(button.getText());
                            } else {
                                config.getTransitionNames().remove(button.getText());
                            }
                            setDirty(true);
                        }
                    });
                    transitionButtons.put(transition.getName(), button);
                }
                transitionChoisesGroup.setSize(transitionChoisesGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                scrolledComposite.setContent(transitionChoisesGroup);
                scrolledComposite.setMinSize(transitionChoisesGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }

            parametersComposite = new GroovyParamsComposite(this, SWT.BORDER);
            parametersComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            errorMessageText.addModifyListener(new LoggingModifyTextAdapter() {
                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    config.setMessage(errorMessageText.getText());
                    validatorsTableViewer.refresh(config, true);
                }
            });
        }

        @Override
        protected boolean enableUI(String variableName, ValidatorDefinition definition, ValidatorConfig config) {
            if (config != null) {
                adjustTransitions(config);
                for (Map.Entry<String, Button> entry : transitionButtons.entrySet()) {
                    entry.getValue().setSelection(config.getTransitionNames().contains(entry.getKey()));
                }
            }
            return (config != null);
        }
    }

    public class GroovyParamsComposite extends ParametersComposite {
        private final StyledText codeText;

        public GroovyParamsComposite(ValidatorInfoControl parent, int style) {
            super(parent, style);
            GridLayout layout = new GridLayout(1, true);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            this.setLayout(layout);
            Composite codeComposite = new Composite(this, SWT.NONE);
            codeComposite.setLayout(new GridLayout(6, false));
            codeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            SwtUtils.createLink(codeComposite, Localization.getString("GroovyEditor.title.constructor"), new LoggingHyperlinkAdapter() {
                @Override
                public void onLinkActivated(HyperlinkEvent e) {
                    GlobalValidatorExpressionConstructorDialog expressionConstructorDialog = new GlobalValidatorExpressionConstructorDialog(
                            getShell(), variables);
                    expressionConstructorDialog.initializeFromExpression(codeText.getText());
                    if (expressionConstructorDialog.open() == org.eclipse.jface.window.Window.OK) {
                        if (expressionConstructorDialog.isDirty()) {
                            setDirty(true);
                            codeText.setText(expressionConstructorDialog.getSavingAsExpression());
                        }
                    }
                }
            }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            if (GroovyStuff.TYPE.getAll().size() > 0) {
                SwtUtils.createLink(codeComposite, Localization.getString("Insert.TYPE.link"), new LoggingHyperlinkAdapter() {
                    @Override
                    public void onLinkActivated(HyperlinkEvent e) {
                        Item item = new ChooseGroovyStuffDialog(GroovyStuff.TYPE).openDialog();
                        if (item != null) {
                            String insert = item.getBody();
                            codeText.insert(insert);
                            codeText.setCaretOffset(codeText.getCaretOffset() + insert.length());
                            codeText.setFocus();
                        }
                    }
                }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            }
            if (GroovyStuff.CONSTANT.getAll().size() > 0) {
                SwtUtils.createLink(codeComposite, Localization.getString("Insert.CONSTANT.link"), new LoggingHyperlinkAdapter() {
                    @Override
                    public void onLinkActivated(HyperlinkEvent e) {
                        Item item = new ChooseGroovyStuffDialog(GroovyStuff.CONSTANT).openDialog();
                        if (item != null) {
                            String insert = item.getBody();
                            codeText.insert(insert);
                            codeText.setCaretOffset(codeText.getCaretOffset() + insert.length());
                            codeText.setFocus();
                        }
                    }
                }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            }
            if (GroovyStuff.STATEMENT.getAll().size() > 0) {
                SwtUtils.createLink(codeComposite, Localization.getString("Insert.STATEMENT.link"), new LoggingHyperlinkAdapter() {
                    @Override
                    public void onLinkActivated(HyperlinkEvent e) {
                        Item item = new ChooseGroovyStuffDialog(GroovyStuff.STATEMENT).openDialog();
                        if (item != null) {
                            String insert = item.getBody();
                            codeText.insert(insert);
                            codeText.setCaretOffset(codeText.getCaretOffset() + insert.length());
                            codeText.setFocus();
                        }
                    }
                }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            }
            if (GroovyStuff.METHOD.getAll().size() > 0) {
                SwtUtils.createLink(codeComposite, Localization.getString("Insert.METHOD.link"), new LoggingHyperlinkAdapter() {
                    @Override
                    public void onLinkActivated(HyperlinkEvent e) {
                        Item item = new ChooseGroovyStuffDialog(GroovyStuff.METHOD).openDialog();
                        if (item != null) {
                            String insert = item.getBody();
                            codeText.insert(insert);
                            codeText.setCaretOffset(codeText.getCaretOffset() + insert.length());
                            codeText.setFocus();
                        }
                    }
                }).setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            }
            SwtUtils.createLink(codeComposite, Localization.getString("button.insert_variable"), new LoggingHyperlinkAdapter() {
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(contextVariableNames);
                    String variableName = dialog.openDialog();
                    if (variableName != null) {
                        codeText.insert(variableName);
                        codeText.setFocus();
                        codeText.setCaretOffset(codeText.getCaretOffset() + variableName.length());
                    }
                }
            }).setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));

            codeText = new FeaturedStyledText(codeComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL,
                    EnumSet.of(FeaturedStyledText.Feature.LINE_NUMBER, FeaturedStyledText.Feature.UNDO_REDO));
            codeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
            codeText.addLineStyleListener(new JavaHighlightTextStyling(contextVariableNames));
            codeText.addVerifyKeyListener(new VerifyKeyListener() {
                @Override
                public void verifyKey(VerifyEvent e) {
                    if (e.keyCode != 0) {
                        setDirty(true);
                    }
                }
            });
        }

        @Override
        protected void clear() {
        }

        @Override
        protected void build(ValidatorDefinition definition, Map<String, String> configParams) {
            String textData = configParams.get(ValidatorDefinition.EXPRESSION_PARAM_NAME);
            codeText.setText(textData != null ? textData : "");
        }

        @Override
        protected void updateConfigParams(ValidatorDefinition definition, ValidatorConfig config) {
            String textData = codeText.getText().trim();
            if (textData.length() != 0) {
                config.getParams().put(ValidatorDefinition.EXPRESSION_PARAM_NAME, textData);
            } else {
                config.getParams().remove(ValidatorDefinition.EXPRESSION_PARAM_NAME);
            }
        }
    }

    private class CopySelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            Clipboard clipboard = new Clipboard(getDisplay());
            @SuppressWarnings("unchecked")
            List<ValidatorConfig> list = ((IStructuredSelection) validatorsTableViewer.getSelection()).toList();
            clipboard.setContents(new Object[] { list }, new Transfer[] { GlobalValidatorTransfer.getInstance() });
            clipboard.dispose();
        }
    }

    private class PasteSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            Clipboard clipboard = new Clipboard(getDisplay());
            @SuppressWarnings("unchecked")
            List<ValidatorConfig> data = (List<ValidatorConfig>) clipboard.getContents(GlobalValidatorTransfer.getInstance());
            clipboard.dispose();
            if (data != null) {
                for (ValidatorConfig config : data) {
                    config.getTransitionNames().clear();
                    adjustTransitions(config);
                    validatorConfigs.add(config);
                }
                validatorsTableViewer.refresh(true);
                setDirty(true);
            }
        }
    }

    private void adjustTransitions(ValidatorConfig config) {
        List<String> transitions = new ArrayList<>();
        List<String> configTransitions = config.getTransitionNames();
        Set<String> allTransitions = new HashSet<>();
        for (Transition t : formNode.getLeavingTransitions()) {
            allTransitions.add(t.getName());
        }

        for (String t : configTransitions) {
            if (allTransitions.contains(t)) {
                transitions.add(t);
            } else {
                log.info(String.format("Transition '%s' does not exists", t));
            }
        }

        if (transitions.isEmpty()) {
            for (Transition transition : formNode.getLeavingTransitions()) {
                transitions.add(transition.getName());
            }
        }
        configTransitions.clear();
        configTransitions.addAll(transitions);

    }

}
