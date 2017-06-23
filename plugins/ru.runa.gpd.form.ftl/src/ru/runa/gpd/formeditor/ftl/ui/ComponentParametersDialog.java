package ru.runa.gpd.formeditor.ftl.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;

import com.google.common.collect.Maps;

public class ComponentParametersDialog extends Dialog {
    private Component component;
    private final Map<ComponentParameter, Object> parameterEditors = Maps.newHashMap();

    public ComponentParametersDialog(Component component) {
        super(Display.getDefault().getActiveShell());
        this.component = new Component(component);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("ComponentParametersDialog.title"));
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    public Component openDialog() {
        if (open() == IDialogConstants.OK_ID) {
            return component;
        }
        return null;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite parametersComposite = (Composite) super.createDialogArea(parent);
        parametersComposite.setLayout(new GridLayout());
        SWTUtils.createLabel(parametersComposite, Messages.getString("ComponentParametersDialog.component"));
        final Combo componentsCombo = new Combo(parametersComposite, SWT.READ_ONLY);
        componentsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for (ComponentType tag : ComponentTypeRegistry.getEnabled()) {
            componentsCombo.add(tag.getLabel());
        }
        componentsCombo.setText(component.getType().getLabel());
        componentsCombo.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String type = componentsCombo.getText();
                if (!component.getType().getLabel().equals(type)) {
                    for (ComponentType componentType : ComponentTypeRegistry.getEnabled()) {
                        if (componentType.getLabel().equals(type)) {
                            component = new Component(componentType, component.getId());
                            break;
                        }
                    }
                    Control[] children = parametersComposite.getChildren();
                    for (int i = 2; i < children.length; i++) {
                        children[i].dispose();
                    }
                    drawParameters(parametersComposite);
                    parametersComposite.layout(true, true);
                    getShell().pack();
                }
            }
        });
        drawParameters(parametersComposite);
        return parametersComposite;
    }

    private void drawParameters(Composite parametersComposite) {
        parameterEditors.clear();
        for (final ComponentParameter componentParameter : component.getType().getParameters()) {
            SWTUtils.createLabel(parametersComposite, componentParameter.getLabel()).setToolTipText(componentParameter.getDescription());
            Object editor = componentParameter.getType().createEditor(parametersComposite, component, componentParameter,
                    component.getParameterValue(componentParameter), new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            component.setParameterValue(componentParameter, event.getNewValue());
                            for (ComponentParameter dependentParameter : componentParameter.getDependents()) {
                                dependentParameter.getType().updateEditor(parameterEditors.get(dependentParameter), component, dependentParameter);
                            }
                        }
                    });
            parameterEditors.put(componentParameter, editor);
        }
    }

}
