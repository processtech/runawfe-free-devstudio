package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.Activator;
import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;
import ru.runa.gpd.quick.formeditor.QuickFormType;
import ru.runa.gpd.quick.formeditor.settings.PreferencePage;
import ru.runa.gpd.quick.formeditor.ui.wizard.QuickFormVariableWizardPage.SelectItem;
import ru.runa.gpd.quick.tag.FreemarkerConfigurationGpdWrap;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.util.VariableUtils;

public class QuickFormVariabliesToDisplayWizardPage extends WizardPage {
    private final FormNode formNode;
    private Button checkAllCheckbox;
    private final List<QuickFormGpdVariable> selectedVariables = new ArrayList<QuickFormGpdVariable>();
    private final List<QuickFormGpdVariable> initialVariables = new ArrayList<QuickFormGpdVariable>();
    private final List<Button> checkboxes = new ArrayList<Button>();
    private final Map<String, Boolean> sectionState = new HashMap<String, Boolean>();
    private ScrolledComposite scrolledComposite;

    protected QuickFormVariabliesToDisplayWizardPage(FormNode formNode, List<QuickFormGpdVariable> quickFormVariableDefs) {
        super(Messages.getString("QuickFormVariabliesToDisplayWizardPage.page.title"));
        setTitle(Messages.getString("QuickFormVariabliesToDisplayWizardPage.page.title"));
        setDescription(Messages.getString("QuickFormVariabliesToDisplayWizardPage.page.description"));
        this.formNode = formNode;
        if (quickFormVariableDefs != null && quickFormVariableDefs.size() > 0) {
            for (QuickFormGpdVariable variable : quickFormVariableDefs) {
                selectedVariables.add(variable);
                initialVariables.add(variable);
            }
        }
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        createView(parent);
    }

    private void createView(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createCheckAllVariables(composite);
        createVariableCheckboxes(composite);
        checkAllCheckbox.setSelection(selectedVariables.size() == checkboxes.size());
        checkAllCheckbox.setEnabled(selectedVariables.size() != checkboxes.size());

        setControl(composite);
        Dialog.applyDialogFont(composite);
        parent.layout(true, true);
        setPageComplete(false);
    }

    private void createCheckAllVariables(final Composite parent) {
        checkAllCheckbox = new Button(parent, SWT.CHECK);
        checkAllCheckbox.setText(Messages.getString("QuickFormVariabliesToDisplayWizardPage.selectall.label"));
        checkAllCheckbox.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                for (Button checkbox : checkboxes) {
                    if (!checkbox.getSelection()) {
                        checkbox.setSelection(true);
                        checkbox.notifyListeners(SWT.Selection, new Event());
                    }
                }
                setPageComplete(true);
            }
        });
    }

    private void createVariableCheckboxes(final Composite parent) {
        scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        final Composite checkboxesArea = new Composite(scrolledComposite, SWT.NONE);

        checkboxesArea.setLayout(new GridLayout(1, true));
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        checkboxesArea.setLayoutData(gridData);

        List<String> names = formNode.getVariableNames(true);

        for (String name : names) {
            QuickFormGpdVariable paramVariable = null;
            for (QuickFormGpdVariable variable : initialVariables) {
                if (name.equals(variable.getName())) {
                    paramVariable = variable;
                    break;
                }
            }
            if (paramVariable == null) {
                paramVariable = new QuickFormGpdVariable();
                Variable variable = VariableUtils.getVariableByName(formNode, name);
                paramVariable.setTagName(QuickFormType.READ_TAG);
                paramVariable.setName(variable.getName());
                paramVariable.setScriptingName(variable.getScriptingName());
                paramVariable.setDescription(variable.getDescription());
                paramVariable.setFormatLabel(variable.getFormatLabel());
                paramVariable.setParams(
                        new String[] { Activator.getDefault().getPreferenceStore().getString(PreferencePage.P_FORM_DEFAULT_DISPLAY_FORMAT) });
            }
            createCheckbox(checkboxesArea, paramVariable);
        }

        scrolledComposite.setContent(checkboxesArea);
        scrolledComposite.setMinSize(checkboxesArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void createCheckbox(final Composite parent, final QuickFormGpdVariable variableDef) {

        Section section = new Section(parent, ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
        section.marginHeight = 5;
        section.marginWidth = 5;
        section.setText(variableDef.getName());
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.minimumHeight = 100;
        section.setLayoutData(gridData);
        Composite clientArea = new Composite(section, SWT.NONE);
        section.setClient(clientArea);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        clientArea.setLayoutData(gridData);
        GridLayout layout = new GridLayout(3, false);
        layout.marginBottom = 2;
        clientArea.setLayout(layout);
        section.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                sectionState.put(variableDef.getName(), e.getState());
                scrolledComposite.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }
        });

        final Button variableCheckbox = new Button(clientArea, SWT.CHECK);
        variableCheckbox.setSelection(selectedVariables.contains(variableDef));
        // variableCheckbox.setText(name);
        variableCheckbox.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                if (selectedVariables.contains(variableDef)) {
                    selectedVariables.remove(variableDef);
                } else {
                    selectedVariables.add(variableDef);
                }
                checkAllCheckbox.setSelection(selectedVariables.size() == checkboxes.size());
                checkAllCheckbox.setEnabled(selectedVariables.size() != checkboxes.size());
                setPageComplete(true);
            }
        });
        checkboxes.add(variableCheckbox);

        createClientArea(clientArea, variableDef);
    }

    private void createClientArea(Composite clientArea, final QuickFormGpdVariable variableDef) {
        for (Control control : clientArea.getChildren()) {
            if (control instanceof Button && checkboxes.contains(control)) {
                continue;
            }
            control.dispose();
        }

        ComboViewer tagType = createTagTypeField(clientArea, variableDef);
        tagType.getCombo().setEnabled(!initialVariables.contains(variableDef));

        String paramValue = "";
        if (variableDef != null && variableDef.getParams() != null && variableDef.getParams().length > 0) {
            paramValue = variableDef.getParams()[0];
        }

        createParamField(clientArea, tagType, paramValue, variableDef);

        clientArea.layout(true, true);
    }

    private ComboViewer createTagTypeField(final Composite parent, final QuickFormGpdVariable variableDef) {
        List<SelectItem> types = new ArrayList<SelectItem>();
        FreemarkerConfigurationGpdWrap freemarkerConfiguration = FreemarkerConfigurationGpdWrap.getInstance();

        for (String value : freemarkerConfiguration.getTagsName()) {
            if (ComponentTypeRegistry.has(value)) {
                ComponentType tag = ComponentTypeRegistry.getNotNull(value);
                types.add(new SelectItem(tag.getLabel(), value));
                continue;
            }
        }

        ComboViewer tagType = new ComboViewer(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);

        tagType.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tagType.setContentProvider(ArrayContentProvider.getInstance());
        tagType.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof SelectItem) {
                    return ((SelectItem) element).getLabel();
                }
                return "";
            }
        });
        tagType.setInput(types.toArray(new SelectItem[types.size()]));
        tagType.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent e) {
                IStructuredSelection selection = (IStructuredSelection) e.getSelection();
                SelectItem selectItem = (SelectItem) selection.getFirstElement();
                variableDef.setTagName(selectItem.getValue().toString());
                variableDef.setParams("InputVariable".equals(selectItem.getValue()) ? new String[0] : new String[] { "false" });
                createClientArea(parent, variableDef);
            }
        });

        if (variableDef != null && variableDef.getTagName() != null) {
            SelectItem[] selectItems = (SelectItem[]) tagType.getInput();
            for (SelectItem selectItem : selectItems) {
                if (variableDef.getTagName().equals(selectItem.getValue())) {
                    tagType.getCombo().setText(selectItem.getLabel());
                    break;
                }
            }
        }

        return tagType;
    }

    private void createParamField(Composite parent, ComboViewer tagType, String paramValue, final QuickFormGpdVariable variableDef) {
        Map<String, ComponentType> componentTypes = ComponentTypeRegistry.getAll();
        if (componentTypes != null) {
            for (ComponentType componentType : componentTypes.values()) {
                if (componentType.getLabel().equals(tagType.getCombo().getText())) {
                    for (int i = 1; i < componentType.getParameters().size(); i++) {
                        ComponentParameter componentParameter = componentType.getParameters().get(i);
                        componentParameter.getType().createEditor(parent, null, componentParameter, paramValue, new PropertyChangeListener() {

                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                List<String> param = new ArrayList<String>();
                                param.add(evt.getNewValue().toString());
                                variableDef.setParams(param.toArray(new String[0]));
                            }
                        });
                        // parameterComposite.setEnabled(!initialVariables.contains(variableDef));
                    }
                }
            }
        }
    }

    public List<QuickFormGpdVariable> getSelectedVariables() {
        return selectedVariables;
    }
}
