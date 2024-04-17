package ru.runa.gpd.quick.formeditor.ui.wizard;

import com.google.common.base.Strings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.Activator;
import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.formeditor.QuickFormComponent;
import ru.runa.gpd.quick.formeditor.QuickFormType;
import ru.runa.gpd.quick.formeditor.settings.PreferencePage;
import ru.runa.gpd.quick.tag.FreemarkerConfigurationGpdWrap;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.util.VariableUtils;

public class QuickFormVariableWizardPage extends WizardPage {
    private ComboViewer tagType;
    private final QuickFormComponent initialVariableDef;
    private List<Object> paramsValues;
    private int mainVariableIndex;
    private final FormNode formNode;
    static final ViewerComparator viewerComparator = new ViewerComparator() {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            ComponentType firstComponentType = ComponentTypeRegistry.getNotNull(((ComboOption) e1).getValue());
            ComponentType secondComponentType = ComponentTypeRegistry.getNotNull(((ComboOption) e2).getValue());
            return firstComponentType.getOrder() - secondComponentType.getOrder();
        }
    };

    protected QuickFormVariableWizardPage(FormNode formNode, QuickFormComponent variableDef) {
        super(Messages.getString("TemplatedFormVariableWizardPage.page.title"));
        setTitle(Messages.getString("TemplatedFormVariableWizardPage.page.title"));
        setDescription(Messages.getString("TemplatedFormVariableWizardPage.page.description"));
        this.formNode = formNode;
        this.initialVariableDef = variableDef;
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        composite.setLayout(layout);
        createTagTypeField(composite);
        rebuildView(composite);
    }

    private void rebuildView(Composite parent) {
        Control[] parentChildren = parent.getChildren();
        for (int i = 2; i < parentChildren.length; i++) {
            parentChildren[i].dispose(); // удаление всего кроме Combo выбора тэга и его Label
        }
        createParamField(parent);
        Dialog.applyDialogFont(parent);
        parent.layout(true, true);
    }

    private void verifyContentsValid(Component component) {
        if (tagType.getCombo().getText().length() == 0) {
            setErrorMessage(Messages.getString("TemplatedFormVariableWizardPage.error.no_tag"));
            setPageComplete(false);
        } else if (this.paramsValues.get(mainVariableIndex).toString().length() == 0) {
            setErrorMessage(Messages.getString("TemplatedFormVariableWizardPage.error.no_variable"));
            setPageComplete(false);
        } else {
            String validationError = validateParams(component);
            setPageComplete(validationError == null ? true : false);
            setErrorMessage(validationError);
        }
    }

    /**
     * Validate inputs.
     * 
     * @param
     * @return error text, if there is an error in inputs, otherwise null
     */
    private String validateParams(Component component) {
        ComponentParameter componentParameter = component.getFirstRequiredEmptyParameter();
        if (componentParameter != null) {
            return Messages.getString("TemplatedFormVariableWizardPage.error.emptyParam") + " '" + componentParameter.getName() + "'";
        }
        return null;
    }

    private void createTagTypeField(final Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("TemplatedFormVariableWizardPage.page.tag"));
        List<ComboOption> types = new ArrayList<ComboOption>();
        FreemarkerConfigurationGpdWrap freemarkerConfiguration = FreemarkerConfigurationGpdWrap.getInstance();
        ComboOption defaultItem = null;
        for (String value : freemarkerConfiguration.getTagsName()) {
            if (ComponentTypeRegistry.has(value)) {
                ComponentType tag = ComponentTypeRegistry.getNotNull(value);
                ComboOption ComboOption = new ComboOption(value, tag.getLabel());
                types.add(ComboOption);
                if (value.equals(FreemarkerConfigurationGpdWrap.getInstance().getDefaultTagName())) {
                    defaultItem = ComboOption;
                }
            }
        }
        tagType = new ComboViewer(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        tagType.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tagType.setContentProvider(ArrayContentProvider.getInstance());
        tagType.setLabelProvider(ComboOption.labelProvider);
        tagType.setComparator(viewerComparator);
        tagType.setInput(types);
        tagType.setSelection(new StructuredSelection(defaultItem)); // установка тэга по умолчанию
        String currentTagName = null;
        if (this.initialVariableDef != null) {
            currentTagName = this.initialVariableDef.getTagName();
        }
        if (currentTagName != null) {
            selectInTagTypeComboByValue(tagType, currentTagName); // установка выбранного тэга
        }
        this.mainVariableIndex = freemarkerConfiguration.getTagMainVariableIndex(this.getTagType());
        tagType.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent e) {
                paramsValues = null;
                mainVariableIndex = freemarkerConfiguration.getTagMainVariableIndex(getTagType());
                rebuildView(parent);
            }
        });
    }

    private void createParamField(Composite parent) {
        ComponentType componentType = ComponentTypeRegistry.getNotNull(this.getTagType());
        Component component = new Component(componentType, 0, this.formNode.getProcessDefinition()); // для некоторых
        // параметров нужен доступ к типам и значениям других параметров, эта информация дается через Component
        // Этот компонент никак не связан с наличием других компонентов,
        // и поэтому указываемый для него id не используется
        loadingFromVariableDefinition(component);
        List<ComponentParameter> componentParameters = componentType.getParameters();
        int parametersNumber = componentParameters.size();
        if (paramsValues == null) { // если значение paramsValues не было получено загрузкой
            paramsValues = new ArrayList<>(parametersNumber);
            for (int i = 0; i < parametersNumber; i++) {
                paramsValues.add(null); // иницилизируем paramsValues массивом из null
            }
        }
        for (int i = 0; i < parametersNumber; i++) {
            ComponentParameter componentParameter = componentParameters.get(i);
            Label label = new Label(parent, SWT.NONE);
            label.setText(componentParameter.getLabel());
            if (paramsValues.get(i) == null) {
                paramsValues.set(i, component.getParameterValue(componentParameter));
                // устанавливаем корректные пустые значения для null параметров
            }
            final int iConstantCopy = i; // для использования в listener нужно final значение
            Object editor = componentParameter.getType().createEditor(parent, component, componentParameter, paramsValues.get(i),
                    new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            Object newValue = evt.getNewValue();
                            paramsValues.set(iConstantCopy, newValue);
                            component.setParameterValue(componentParameter, newValue);
                            verifyContentsValid(component);
                        }
                    }, this.formNode.getProcessDefinition());
            if (componentType.getId().equals(QuickFormType.READ_TAG) && editor instanceof ComboViewer
                    && ((ComboViewer) editor).getSelection().isEmpty()) {
                setDefaultDisplayFormat((ComboViewer) editor);
            }
        }
        verifyContentsValid(component);
    }

    private void setDefaultDisplayFormat(ComboViewer comboViewer) {
        String defaultValue = Activator.getDefault().getPreferenceStore().getString(PreferencePage.P_FORM_DEFAULT_DISPLAY_FORMAT);
        selectInTagTypeComboByValue(comboViewer, defaultValue);
    }

    static void selectInTagTypeComboByValue(ComboViewer comboViewer, String value) {
        List<ComboOption> comboOptions = (List<ComboOption>) comboViewer.getInput();
        for (ComboOption comboOption : comboOptions) {
            if (value.equals(comboOption.getValue())) {
                comboViewer.setSelection(new StructuredSelection(comboOption));
                return;
            }
        }
    }

    private void loadingFromVariableDefinition(Component component) {
        if (initialVariableDef != null && this.getTagType().equals(initialVariableDef.getTagName())) {
            List<Object> paramsFromSaving = initialVariableDef.getParams();
            if (paramsFromSaving != null && paramsFromSaving.size() > 0) {
                paramsValues = initialVariableDef.copyParams(); // параметры копируются,
                // чтобы изменения параметров в диалоге потом не оставлялись, когда нажимается "Отмена"
                List<ComponentParameter> componentParameters = component.getType().getParameters();
                int parametersNumber = componentParameters.size();
                for (int i = 0; i < parametersNumber; i++) {
                    component.setParameterValue(componentParameters.get(i), paramsValues.get(i));
                }
            }
        }
    }

    public String getTagType() {
        return ((ComboOption) tagType.getStructuredSelection().getFirstElement()).getValue();
    }

    public Variable getVariable() {
        String variableName = paramsValues.get(this.mainVariableIndex).toString();
        if (!Strings.isNullOrEmpty(variableName)) {
            return VariableUtils.getVariableByName(formNode, variableName);
        }
        return null;
    }

    public List<Object> getParamsValues() {
        return paramsValues;
    }
}