package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ComboParameter extends ParameterType {

    protected List<String> getOptionLabels(ComponentParameter parameter) {
        return Lists.transform(parameter.getOptions(), new Function<ComboOption, String>() {

            @Override
            public String apply(ComboOption option) {
                return option.getLabel();
            }
        });
    }

    protected List<String> getOptionValues(ComponentParameter parameter) {
        return Lists.transform(parameter.getOptions(), new Function<ComboOption, String>() {

            @Override
            public String apply(ComboOption option) {
                return option.getValue();
            }
        });
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(ComponentParameter parameter, int propertyId) {
        List<String> list = getOptionLabels(parameter);
        return new ComboBoxPropertyDescriptor(propertyId, parameter.getLabel(), list.toArray(new String[list.size()]));
    }

    @Override
    public Object fromPropertyDescriptorValue(ComponentParameter parameter, Object editorValue) {
        List<String> list = getOptionValues(parameter);
        int index = (Integer) editorValue;
        return index != -1 ? list.get(index) : "";
    }

    @Override
    public Object toPropertyDescriptorValue(ComponentParameter parameter, Object value) {
        List<String> list = getOptionValues(parameter);
        return list.indexOf(value);
    }

    @Override
    public Composite createEditor(Composite parent, ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        final ComboViewer viewer = new ComboViewer(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        viewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((ComboOption) element).getLabel();
            }
        });
        viewer.setInput(parameter.getOptions());
        if (oldValue != null) {
            viewer.setSelection(new StructuredSelection(new ComboOption((String) oldValue, null)));
        }
        if (listener != null) {
            viewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
                @Override
                protected void onSelectionChanged(SelectionChangedEvent e) throws Exception {
                    IStructuredSelection selection = (IStructuredSelection) e.getSelection();
                    ComboOption option = (ComboOption) selection.getFirstElement();
                    listener.propertyChange(new PropertyChangeEvent(viewer, PropertyNames.PROPERTY_VALUE, oldValue, option.getValue()));
                }
            });
        }
        return viewer.getCombo();
    }
}
