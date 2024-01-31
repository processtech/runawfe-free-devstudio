package ru.runa.gpd.formeditor.ftl.parameter;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;

public class ComboParameter extends ParameterType {
    /**
     * Determines whether to automatically select a single ComboOption if {@link ComboParameter#getOptions} returned a single item list
     */
    private final boolean autoSelectSingleOption;
    private PropertyChangeListener propertyChangeListener;

    public ComboParameter() {
        super();
        autoSelectSingleOption = false;
    }

    public ComboParameter(boolean multiple) {
        super(multiple);
        autoSelectSingleOption = false;
    }

    public ComboParameter(boolean multiple, boolean autoSelectSingleOption) {
        super(multiple);
        this.autoSelectSingleOption = autoSelectSingleOption;
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(Component component, ComponentParameter parameter, int propertyId,
            ProcessDefinition processDefinition) {
        List<String> list = getOptionLabels(component, parameter, processDefinition);
        return new ComboBoxPropertyDescriptor(propertyId, parameter.getLabel(), list.toArray(new String[list.size()]));
    }

    @Override
    public Object fromPropertyDescriptorValue(Component component, ComponentParameter parameter, Object editorValue,
            ProcessDefinition processDefinition) {
        List<String> list = getOptionValues(component, parameter, processDefinition);
        int index = (Integer) editorValue;
        return index != -1 ? list.get(index) : "";
    }

    @Override
    public Object toPropertyDescriptorValue(Component component, ComponentParameter parameter, Object value, ProcessDefinition processDefinition) {
        List<String> list = getOptionValues(component, parameter, processDefinition);
        return list.indexOf(value);
    }

    @Override
    public Object createEditor(Composite parent, Component component, ComponentParameter parameter, final Object oldValue,
            final PropertyChangeListener listener, ProcessDefinition processDefinition) {
        this.propertyChangeListener = listener;
        final ComboViewer viewer = new ComboViewer(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        viewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(ComboOption.labelProvider);
        final List<ComboOption> options = getOptions(component, parameter, processDefinition);
        viewer.setInput(options);
        if (!Strings.isNullOrEmpty((String) oldValue)) {
            viewer.setSelection(new StructuredSelection(new ComboOption((String) oldValue, null)));
        } else if (options.size() == 1 && autoSelectSingleOption) {
            selectValue(oldValue, listener, viewer, options.get(0));
        } else {
            for (ComboOption option : parameter.getOptions()) {
                if (option.isDefault()) {
                    selectValue(oldValue, listener, viewer, option);
                    break;
                }
            }
        }
        viewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            protected void onSelectionChanged(SelectionChangedEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) e.getSelection();
                ComboOption option = (ComboOption) selection.getFirstElement();
                listener.propertyChange(new PropertyChangeEvent(viewer, PropertyNames.PROPERTY_VALUE, oldValue, option.getValue()));
            }
        });
        return viewer;
    }

    @Override
    public void updateEditor(Object ui, Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        ComboViewer viewer = (ComboViewer) ui;
        final List<ComboOption> options = getOptions(component, parameter, processDefinition);
        viewer.setInput(options);
        if (options.size() == 1 && autoSelectSingleOption) {
            selectValue(null, propertyChangeListener, viewer, options.get(0));
        }
    }

    public List<ComboOption> getOptions(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        return parameter.getOptions();
    }

    private List<String> getOptionLabels(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        return Lists.transform(getOptions(component, parameter, processDefinition), new Function<ComboOption, String>() {

            @Override
            public String apply(ComboOption option) {
                return option.getLabel();
            }
        });
    }

    private List<String> getOptionValues(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        return Lists.transform(getOptions(component, parameter, processDefinition), new Function<ComboOption, String>() {

            @Override
            public String apply(ComboOption option) {
                return option.getValue();
            }
        });
    }

    private void selectValue(final Object oldValue, final PropertyChangeListener listener, final ComboViewer viewer, ComboOption option) {
        viewer.setSelection(new StructuredSelection(option));
        if (listener != null) {
            listener.propertyChange(new PropertyChangeEvent(viewer, PropertyNames.PROPERTY_VALUE, oldValue, option.getValue()));
        }
    }

}
