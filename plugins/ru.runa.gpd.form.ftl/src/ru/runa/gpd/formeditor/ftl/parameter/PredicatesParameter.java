package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.util.CdataWrapUtils;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.office.store.InternalStorageOperationHandlerCellEditorProvider;

public class PredicatesParameter extends ParameterType {
    private PredicatesDelegable delegable;

    @Override
    public PropertyDescriptor createPropertyDescriptor(Component component, ComponentParameter parameter, int propertyId) {
        return new PropertyDescriptor(propertyId, parameter.getLabel());
    }

    @Override
    public Object createEditor(Composite parent, Component component, ComponentParameter parameter, Object oldValue,
            PropertyChangeListener listener) {
        final ProcessDefinition processDefinition = FormEditor.getCurrent().getProcessDefinition();
        delegable = new PredicatesDelegable(processDefinition, getVariableUserType(component, component.getType().getParameters(), processDefinition),
                (String) oldValue);

        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(1, false));

        final Button button = new Button(composite, SWT.PUSH);
        button.setText(Messages.getString("ConfigurePredicates.title"));
        button.setLayoutData(new GridData(GridData.FILL_BOTH));
        button.addSelectionListener(SelectionListener.widgetSelectedAdapter(c -> {
            final InternalStorageOperationHandlerCellEditorProvider provider = new InternalStorageOperationHandlerCellEditorProvider();
            final String xml = provider.showConfigurationDialog(delegable);
            if (xml == null) {
                return;
            }

            final String old = delegable.getFtlConfiguration();
            delegable.setDelegationConfiguration(xml);
            listener.propertyChange(new PropertyChangeEvent(button, PropertyNames.PROPERTY_VALUE, old, delegable.getFtlConfiguration()));
        }));

        return composite;
    }

    @Override
    public void updateEditor(Object ui, Component component, ComponentParameter parameter) {
        super.updateEditor(ui, component, parameter);
        final String userType = getVariableUserType(component, component.getType().getParameters(), FormEditor.getCurrent().getProcessDefinition());
        if (!Objects.equals(delegable.get(), userType)) {
            delegable.setVariableUserType(userType);
            delegable.setFtlConfiguration("");
            component.setParameterValue(parameter, delegable.getFtlConfiguration());
        }
    }

    @Override
    public Object fromPropertyDescriptorValue(Component component, ComponentParameter parameter, Object editorValue) {
        return super.fromPropertyDescriptorValue(component, parameter, editorValue);
    }

    @Override
    public Object toPropertyDescriptorValue(Component component, ComponentParameter parameter, Object value) {
        return CdataWrapUtils.unwrapCdata((String) value);
    }

    public static String getVariableUserType(Component component, List<ComponentParameter> parameters, ProcessDefinition processDefinition) {
        return parameters.stream().filter(parameter -> parameter.getType() instanceof DbUserTypeListComboParameter).findAny()
                .map(parameter -> (String) component.getParameterValue(parameter)).orElse("");
    }

    public static class PredicatesDelegable implements Delegable, StorageAware, ProcessDefinitionAware, Supplier<String> {
        private final ProcessDefinition processDefinition;
        private String variableUserType;
        private String configuration = "";

        public PredicatesDelegable(ProcessDefinition processDefinition, String variableUserType, String configuration) {
            this.processDefinition = processDefinition;
            this.variableUserType = variableUserType;
            setFtlConfiguration(configuration);
        }

        @Override
        public boolean isUseExternalStorageIn() {
            return true;
        }

        @Override
        public boolean isUseExternalStorageOut() {
            return false;
        }

        @Override
        public String getDelegationClassName() {
            return ScriptTask.INTERNAL_STORAGE_HANDLER_CLASS_NAME;
        }

        @Override
        public void setDelegationClassName(String delegateClassName) {
        }

        @Override
        public String getDelegationConfiguration() {
            return configuration;
        }

        @Override
        public void setDelegationConfiguration(String configuration) {
            this.configuration = configuration;
        }

        @Override
        public String getDelegationType() {
            return ScriptTask.INTERNAL_STORAGE_HANDLER_CLASS_NAME;
        }

        @Override
        public List<String> getVariableNames(boolean includeSwimlanes, String... typeClassNameFilters) {
            return processDefinition.getVariableNames(includeSwimlanes, typeClassNameFilters);
        }

        @Override
        public ProcessDefinition getProcessDefinition() {
            return processDefinition;
        }

        @Override
        public String get() {
            return variableUserType;
        }

        void setVariableUserType(String variableUserType) {
            this.variableUserType = variableUserType;
        }

        String getFtlConfiguration() {
            return CdataWrapUtils.wrapCdata(configuration);
        }

        void setFtlConfiguration(String configuration) {
            this.configuration = CdataWrapUtils.unwrapCdata(configuration);
        }
    }

}
