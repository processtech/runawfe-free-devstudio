package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
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
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.model.VariableUserTypeNameAware;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.office.store.externalstorage.InternalStorageDataModel;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;

public class PredicatesParameter extends ParameterType implements DependsOnDbVariableUserType {
    private PredicatesDelegable delegable;

    @Override
    public PropertyDescriptor createPropertyDescriptor(Component component, ComponentParameter parameter, int propertyId,
            ProcessDefinition processDefinition) {
        return new PropertyDescriptor(propertyId, parameter.getLabel());
    }

    @Override
    public Object createEditor(Composite parent, Component component, ComponentParameter parameter, Object oldValue, PropertyChangeListener listener,
            ProcessDefinition processDefinition) {
        delegable = new PredicatesDelegable(processDefinition, null, (String) oldValue);

        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(1, false));

        final Button button = new Button(composite, SWT.PUSH);
        button.setText(Messages.getString("ConfigurePredicates.title"));
        button.setLayoutData(new GridData(GridData.FILL_BOTH));
        button.addSelectionListener(SelectionListener.widgetSelectedAdapter(c -> {
            final Optional<VariableUserType> userType = userType(component, processDefinition);
            if (!userType.isPresent()) {
                return;
            }

            delegable.setVariableUserType(userType.get().getName());
            final String xml = DialogEnhancement.showConfigurationDialog(delegable);
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
    public void updateEditor(Object ui, Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        super.updateEditor(ui, component, parameter, processDefinition);
        final Optional<VariableUserType> userType = userType(component, processDefinition);
        delegable.setFtlConfiguration(userType.map(type -> InternalStorageDataModel.selectWithoutReturnBy(type).toString()).orElse(""));
        component.setParameterValue(parameter, delegable.getFtlConfiguration());
    }

    @Override
    public Object toPropertyDescriptorValue(Component component, ComponentParameter parameter, Object value, ProcessDefinition processDefinition) {
        return CdataWrapUtils.unwrapCdata((String) value);
    }

    public static String getVariableUserType(Component component, List<ComponentParameter> parameters, ProcessDefinition processDefinition) {
        return parameters.stream().filter(parameter -> parameter.getType() instanceof DbUserTypeListComboParameter).findAny()
                .map(parameter -> (String) component.getParameterValue(parameter)).orElse("");
    }

    public static class PredicatesDelegable implements Delegable, StorageAware, ProcessDefinitionAware, VariableUserTypeNameAware, CdataAwareValue {
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
        public String getUserTypeName() {
            return variableUserType;
        }

        void setVariableUserType(String variableUserType) {
            this.variableUserType = variableUserType;
        }

        @Override
        public String getFtlConfiguration() {
            return CdataWrapUtils.wrapCdata(configuration);
        }

        @Override
        public void setFtlConfiguration(String configuration) {
            String unwrapped = CdataWrapUtils.unwrapCdata(configuration);
            if (StringUtils.isNotBlank(unwrapped)) {
                final int queryIndex = unwrapped.indexOf("query");
                if (queryIndex != -1) {
                    final String afterQuery = unwrapped.substring(queryIndex + "query".length() + 2);
                    final String query = afterQuery.substring(0, afterQuery.indexOf("\""));
                    String escapeXml = query.replace("<", "&lt;").replace(">", "&gt;");
                    unwrapped = unwrapped.replace(query, escapeXml);
                }
            }
            this.configuration = unwrapped;
        }
    }

}
