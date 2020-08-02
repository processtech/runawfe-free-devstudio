package ru.runa.gpd.formeditor.ftl.ui;

import com.google.common.collect.Maps;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.formeditor.settings.PreferencePage;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.dialog.ChooseComponentLabelDialog;

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
        final Composite rootComposite = new Composite(parent, SWT.NONE);
        rootComposite.setLayout(GridLayoutFactory.fillDefaults().create());
        rootComposite.setSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        rootComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        final int initialY = component.getType().getParameters().size() < 7 ? (component.getType().getParameters().size() + 1) * 64 : 512;
        final ScrolledComposite scrolledComposite = new ScrolledComposite(rootComposite, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, initialY).create());
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        final Composite parametersComposite = (Composite) super.createDialogArea(scrolledComposite);
        parametersComposite.setLayout(new GridLayout());

        SwtUtils.createLabel(parametersComposite, Messages.getString("ComponentParametersDialog.component"));
        Composite compComposite = new Composite(parametersComposite, SWT.NONE);
        compComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        compComposite.setLayout(new GridLayout(2, false));
        final Text varName = new Text(compComposite, SWT.READ_ONLY | SWT.BORDER);
        GridData processVariableTextData = new GridData(GridData.FILL_HORIZONTAL);
        processVariableTextData.minimumWidth = 300;
        varName.setLayoutData(processVariableTextData);
        varName.setText(component.getType().getLabel());
        Button selectButton = new Button(compComposite, SWT.PUSH);
        selectButton.setText("...");
        selectButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        selectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String result = new ChooseComponentLabelDialog(
                        ComponentTypeRegistry.getEnabled().stream().map(c -> c.getLabel()).collect(Collectors.toList())).openDialog();
                if (result != null) {
                    varName.setText(result);
                    String type = varName.getText();
                    if (!component.getType().getLabel().equals(type)) {
                        for (ComponentType componentType : ComponentTypeRegistry.getEnabled()) {
                            if (componentType.getLabel().equals(type)) {
                                Component previousComponent = component;
                                component = new Component(componentType, component.getId());
                                int commonParametersCount = Math.min(previousComponent.getType().getParameters().size(),
                                        component.getType().getParameters().size());
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

                        final int initialY = component.getType().getParameters().size() < 7 ? (component.getType().getParameters().size() + 1) * 64
                                : 512;
                        scrolledComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, initialY).create());
                        scrolledComposite.setMinSize(parametersComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        getShell().pack();
                    }
                }
            }
        });

        drawParameters(parametersComposite);

        scrolledComposite.setContent(parametersComposite);
        scrolledComposite.setMinSize(parametersComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return rootComposite;
    }

    private void drawParameters(Composite parametersComposite) {
        parameterEditors.clear();
        for (final ComponentParameter componentParameter : component.getType().getParameters()) {
            SwtUtils.createLabel(parametersComposite, componentParameter.getLabel()).setToolTipText(componentParameter.getDescription());
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
            if (component.getType().getId().equals("DisplayVariable") && editor instanceof ComboViewer) {
                ComboViewer comboViewer = (ComboViewer) editor;
                if (comboViewer.getSelection().isEmpty()) {
                    setDefaultDisplayFormat(comboViewer);
                }
            }
        }
    }

    private void setDefaultDisplayFormat(ComboViewer comboViewer) {
        String defaultValue = EditorsPlugin.getDefault().getPreferenceStore().getString(PreferencePage.P_FORM_DEFAULT_DISPLAY_FORMAT);
        for (ComboOption option : (List<ComboOption>) comboViewer.getInput()) {
            if (option.getValue().equals(defaultValue)) {
                comboViewer.setSelection(new StructuredSelection(option));
                return;
            }
        }
    }

}
