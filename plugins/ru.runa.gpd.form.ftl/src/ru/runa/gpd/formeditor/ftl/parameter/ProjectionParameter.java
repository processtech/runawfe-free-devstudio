package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;
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
import ru.runa.gpd.formeditor.ftl.ui.dialog.projection.ProjectionDataModel;
import ru.runa.gpd.formeditor.ftl.util.CdataWrapUtils;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;

public class ProjectionParameter extends ParameterType implements DependsOnDbVariableUserType {
    private ProjectionDelegable delegable;

    @Override
    public PropertyDescriptor createPropertyDescriptor(Component component, ComponentParameter parameter, int propertyId,
            ProcessDefinition processDefinition) {
        return new PropertyDescriptor(propertyId, parameter.getLabel());
    }

    @Override
    public Object createEditor(Composite parent, Component component, ComponentParameter parameter, Object oldValue, PropertyChangeListener listener,
            ProcessDefinition processDefinition) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(1, false));

        delegable = new ProjectionDelegable(processDefinition, (String) oldValue);

        final Button button = new Button(composite, SWT.PUSH);
        button.setText(Messages.getString("ConfigureProjections.title"));
        button.setLayoutData(new GridData(GridData.FILL_BOTH));
        button.addSelectionListener(SelectionListener.widgetSelectedAdapter(c -> {
            final Optional<VariableUserType> userType = userType(component, processDefinition);
            if (!userType.isPresent()) {
                return;
            }

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
        delegable.setFtlConfiguration(userType.map(type -> ProjectionDataModel.by(type).toString()).orElse(""));
        component.setParameterValue(parameter, delegable.getFtlConfiguration());
    }

    @Override
    public Object toPropertyDescriptorValue(Component component, ComponentParameter parameter, Object value, ProcessDefinition processDefinition) {
        return CdataWrapUtils.unwrapCdata((String) value);
    }

    public static class ProjectionDelegable implements Delegable, CdataAwareValue {
        private final ProcessDefinition processDefinition;
        private String configuration;

        public ProjectionDelegable(ProcessDefinition processDefinition, String configuration) {
            this.processDefinition = processDefinition;
            setFtlConfiguration(configuration);
        }

        @Override
        public String getDelegationClassName() {
            return null;
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
            return null;
        }

        @Override
        public List<String> getVariableNames(boolean includeSwimlanes, String... typeClassNameFilters) {
            return processDefinition.getVariableNames(includeSwimlanes, typeClassNameFilters);
        }

        @Override
        public void setFtlConfiguration(String configuration) {
            this.configuration = CdataWrapUtils.unwrapCdata(configuration);
        }

        @Override
        public String getFtlConfiguration() {
            return CdataWrapUtils.wrapCdata(configuration);
        }

    }

}
