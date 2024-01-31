package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.formeditor.ftl.parameter.ComboParameter;
import ru.runa.gpd.formeditor.ftl.parameter.ParameterType;
import ru.runa.gpd.formeditor.ftl.parameter.RichComboParameter;
import ru.runa.gpd.formeditor.ftl.parameter.VariableFinderParameter;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.formeditor.QuickFormComponent;
import ru.runa.gpd.quick.tag.FreemarkerConfigurationGpdWrap;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;

class QuickFormComponentEditingSection extends Section {
    private QuickFormComponent quickFormComponent;
    private Component component;
    private ProcessDefinition processDefinition;
    private Button variableCheckbox;
    private ComboViewer tagType;
    private List<Listener> resizeListeners = new LinkedList<>();
    private List<Listener> validationListeners = new LinkedList<>();
    private String lastValidationResult;

    QuickFormComponentEditingSection(Composite parent, QuickFormComponent quickFormComponent, ProcessDefinition processDefinition) {
        super(parent, ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
        this.quickFormComponent = quickFormComponent;
        this.processDefinition = processDefinition;
        this.marginHeight = 5;
        this.marginWidth = 5;
        this.setText(quickFormComponent.getName());
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.minimumHeight = 100;
        this.setLayoutData(gridData);
        Composite clientArea = new Composite(this, SWT.NONE);
        this.setClient(clientArea);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        clientArea.setLayoutData(gridData);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        clientArea.setLayout(layout);
        variableCheckbox = new Button(clientArea, SWT.CHECK);
        gridData = new GridData(SWT.CENTER, SWT.TOP, false, false);
        variableCheckbox.setLayoutData(gridData);
        Composite componentComposite = new Composite(clientArea, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        componentComposite.setLayoutData(gridData);
        GridLayout componentCompositeLayout = new GridLayout(2, true);
        componentCompositeLayout.marginWidth = 0;
        componentCompositeLayout.marginHeight = 0;
        componentCompositeLayout.marginBottom = 5; // предотвращает баг, вызванный компонентом
        // Section, из-за которого в componentComposite не полностью влезают элементы
        componentCompositeLayout.marginTop = 0;
        componentComposite.setLayout(componentCompositeLayout);
        tagType = createTagTypeField(componentComposite);
        createClientArea(componentComposite);
        validate();
    }

    public boolean getVariableCheckboxSelection() {
        return this.variableCheckbox.getSelection();
    }

    public void setVariableCheckboxSelection(boolean selected) {
        this.variableCheckbox.setSelection(selected);
    }

    public void setTagTypeComboEnabled(boolean enabled) {
        this.tagType.getCombo().setEnabled(enabled);
    }

    public void addVariableCheckboxSelectionListener(SelectionListener listener) {
        this.variableCheckbox.addSelectionListener(listener);
    }

    public void addResizeListener(Listener listener) {
        resizeListeners.add(listener);
    }

    public void addValidationListener(Listener listener) {
        validationListeners.add(listener);
    }

    public void notifyVariableCheckboxListeners(int eventType, Event event) {
        this.variableCheckbox.notifyListeners(eventType, event);
    }

    public QuickFormComponent getQuickFormComponent() {
        return this.quickFormComponent;
    }

    /**
     * @return error text, if there is an error in inputs, otherwise null
     */
    public String validationError() {
        return this.lastValidationResult;
    }

    private void createClientArea(Composite clientArea) {
        Combo tagTypeCombo = this.tagType.getCombo();
        for (Control control : clientArea.getChildren()) {
            if (control == tagTypeCombo) {
                continue;
            }
            control.dispose();
        }
        createParamField(clientArea);
    }

    private ComboViewer createTagTypeField(final Composite parent) {
        createComponent();
        fillComponent();
        fillVariableDefinitionParamsFromComponent();
        List<ComboOption> types = new ArrayList<ComboOption>();
        FreemarkerConfigurationGpdWrap freemarkerConfiguration = FreemarkerConfigurationGpdWrap.getInstance();
        for (String value : freemarkerConfiguration.getTagsName()) {
            if (ComponentTypeRegistry.has(value)) {
                ComponentType tag = ComponentTypeRegistry.getNotNull(value);
                int mainVariableIndex = freemarkerConfiguration.getTagMainVariableIndex(value);
                ComponentParameter mainVariableComponentParameter = tag.getParameters().get(mainVariableIndex);
                ParameterType parameterType = mainVariableComponentParameter.getType();
                if (tag.isEnabled()) {
                    List<String> possibleFirstVariables = null;
                    if (parameterType instanceof ComboParameter) {
                        ComboParameter parameterComboType = (ComboParameter) parameterType;
                        List<ComboOption> comboOptions = parameterComboType.getOptions(component, mainVariableComponentParameter, processDefinition);
                        possibleFirstVariables = new LinkedList<>();
                        for (ComboOption i : comboOptions) {
                            possibleFirstVariables.add(i.getLabel());
                        }
                    } else if (parameterType instanceof VariableFinderParameter || parameterType instanceof RichComboParameter) {
                        possibleFirstVariables = parameterType.getVariableNames(mainVariableComponentParameter, processDefinition);
                    } else {
                        continue;
                    }
                    if (possibleFirstVariables.contains(quickFormComponent.getName())) {
                        // если переменная variableDef.getName() может быть главной переменной для данного тэга
                        types.add(new ComboOption(value, tag.getLabel()));
                    }
                }
            }
        }
        ComboViewer tagType = new ComboViewer(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        tagType.getCombo().setLayoutData(gridData);
        tagType.getCombo().setToolTipText(Messages.getString("TemplatedFormVariableWizardPage.page.tag"));
        tagType.setContentProvider(ArrayContentProvider.getInstance());
        tagType.setLabelProvider(ComboOption.labelProvider);
        tagType.setComparator(QuickFormVariableWizardPage.viewerComparator);
        tagType.setInput(types);
        QuickFormVariableWizardPage.selectInTagTypeComboByValue(tagType, quickFormComponent.getTagName());
        tagType.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent e) {
                IStructuredSelection selection = (IStructuredSelection) e.getSelection();
                ComboOption ComboOption = (ComboOption) selection.getFirstElement();
                quickFormComponent.setTagName(ComboOption.getValue());
                createComponent();
                createParams(freemarkerConfiguration.getTagMainVariableIndex(quickFormComponent.getTagName()));
                fillComponent();
                fillVariableDefinitionParamsFromComponent();
                createClientArea(parent);
                parent.layout(true, true);
                for (Listener i : resizeListeners) {
                    i.handleEvent(new Event());
                }
                validate();
            }
        });
        return tagType;
    }

    private void createComponent() {
        String componentLabel = quickFormComponent.getTagName();
        Map<String, ComponentType> componentTypes = ComponentTypeRegistry.getAll();
        if (componentTypes != null) {
            for (ComponentType componentType : componentTypes.values()) {
                if (componentType.getId().equals(componentLabel) && componentType.isEnabled()) {
                    component = new Component(componentType, 0, processDefinition);
                    return;
                }
            }
        }
    }

    private void createParams(int mainVariableIndex) {
        ComponentType componentType = component.getType();
        int parameterNumber = componentType.getParameters().size();
        List<Object> paramsValues = new ArrayList<>(parameterNumber);
        for (int i = 0; i < parameterNumber; i++) {
            paramsValues.add(null); // создаем массив из null
        }
        paramsValues.set(mainVariableIndex, quickFormComponent.getName());
        quickFormComponent.setParams(paramsValues);
    }

    private void fillComponent() {
        List<ComponentParameter> componentParameters = component.getType().getParameters();
        List<Object> paramsValues = quickFormComponent.getParams();
        if (paramsValues != null && paramsValues.size() > 0) {
            int parametersNumber = componentParameters.size();
            for (int i = 0; i < parametersNumber; i++) {
                Object paramValue = paramsValues.get(i);
                if (paramValue != null) {
                    component.setParameterValue(componentParameters.get(i), paramValue);
                }
            }
        }
    }

    private void fillVariableDefinitionParamsFromComponent() {
        List<Object> paramsValues = quickFormComponent.getParams();
        ComponentType componentType = component.getType();
        int parameterNumber = componentType.getParameters().size();
        for (int i = 0; i < parameterNumber; i++) {
            ComponentParameter componentParameter = componentType.getParameters().get(i);
            if (paramsValues.get(i) == null) {
                paramsValues.set(i, component.getParameterValue(componentParameter));
            }
        }
    }

    private void createParamField(Composite parent) {
        ComponentType componentType = component.getType();
        List<ComponentParameter> componentParameters = componentType.getParameters();
        int parameterNumber = componentParameters.size();
        final List<Object> paramsValuesConstantCopy = quickFormComponent.getParams();
        FreemarkerConfigurationGpdWrap freemarkerConfiguration = FreemarkerConfigurationGpdWrap.getInstance();
        int mainVariableIndex = freemarkerConfiguration.getTagMainVariableIndex(quickFormComponent.getTagName());
        for (int i = 0; i < parameterNumber; i++) {
            ComponentParameter componentParameter = componentParameters.get(i);
            if (i != mainVariableIndex) {
                final int iConstantCopy = i;
                componentParameter.getType().createEditor(parent, component, componentParameter, paramsValuesConstantCopy.get(i),
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                Object newValue = evt.getNewValue();
                                paramsValuesConstantCopy.set(iConstantCopy, newValue);
                                component.setParameterValue(componentParameter, newValue);
                                validate();
                            }
                        }, processDefinition);
                Control[] children = parent.getChildren();
                Control lastChild = children[children.length - 1];
                this.setToolTipTextRecursive(lastChild, componentParameter.getLabel());
            }
        }
    }

    private void setToolTipTextRecursive(Control control, String text) {
        control.setToolTipText(text);
        if (control instanceof Composite) {
            Composite composite = (Composite) control;
            for (Control child : composite.getChildren()) {
                this.setToolTipTextRecursive(child, text);
            }
        }
    }

    private void validate() {
        this.lastValidationResult = this.validateParams();
        notifyValidationListeners(this.lastValidationResult);
    }

    /**
     * Validate inputs.
     * 
     * @param
     * @return error text, if there is an error in inputs, otherwise null
     */
    private String validateParams() {
        ComponentParameter componentParameter = component.getFirstRequiredEmptyParameter();
        if (componentParameter != null) {
            return Messages.getString("QuickFormComponentEditingSection.emptyParam") + " '" + componentParameter.getName() + "'";
        }
        return null;
    }

    private void notifyValidationListeners(String validationResult) {
        for (Listener listener : validationListeners) {
            listener.handleEvent(new Event());
        }
    }
}