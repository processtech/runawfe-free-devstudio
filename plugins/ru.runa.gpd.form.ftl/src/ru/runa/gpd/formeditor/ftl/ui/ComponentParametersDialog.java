package ru.runa.gpd.formeditor.ftl.ui;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

public class ComponentParametersDialog extends Dialog {

    private static final String downPointingTriangle = "\u25bc";

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

        final String categoryTitle = downPointingTriangle + " {0} " + Strings.repeat("\u2501", 20);
        List<ComponentType> basicComponents = ComponentTypeRegistry.getEnabled(true);
        List<ComponentType> additionalComponents = ComponentTypeRegistry.getEnabled(false);
        boolean bothCategoriesExist = basicComponents.size() > 0 && additionalComponents.size() > 0;
        if (bothCategoriesExist) {
            componentsCombo.add(MessageFormat.format(categoryTitle, Messages.getString(FormComponentsView.FORM_COMPONENT_CATEGORY_BASIC_KEY)));
        }
        for (ComponentType tag : basicComponents) {
            componentsCombo.add(tag.getLabel());
        }
        if (bothCategoriesExist) {
            componentsCombo.add(MessageFormat.format(categoryTitle, Messages.getString(FormComponentsView.FORM_COMPONENT_CATEGORY_ADDITIONAL_KEY)));
        }
        for (ComponentType tag : additionalComponents) {
            componentsCombo.add(tag.getLabel());
        }

        componentsCombo.setText(component.getType().getLabel());
        componentsCombo.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String type = componentsCombo.getText();
                if (type.startsWith(downPointingTriangle)) {
                    componentsCombo.setText(component.getType().getLabel());
                    e.doit = false;
                    return;
                }
                if (!component.getType().getLabel().equals(type)) {
                    for (ComponentType componentType : ComponentTypeRegistry.getEnabled()) {
                        if (componentType.getLabel().equals(type)) {
                            Component previousComponent = component;
                            component = new Component(componentType, component.getId());
                            int commonParametersCount = Math.min(previousComponent.getType().getParameters().size(), component.getType().getParameters().size());
                            for (int i = 0; i < commonParametersCount; i++) {
                                ComponentParameter previousParameter = previousComponent.getType().getParameters().get(i);
                                ComponentParameter parameter = component.getType().getParameters().get(i);
                                if (Objects.equals(previousParameter.getLabel(), parameter.getLabel())) {
                                    component.setParameterValue(parameter, previousComponent.getParameterValue(previousParameter));
                                    }
                            }
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
