package ru.runa.gpd.ui.wizard;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.Maps;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.DynaContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.UserTypeFormat;

public class VariableFormatPage extends DynaContentWizardPage {
    private final VariableContainer variableContainer;
    private VariableFormatArtifact type;
    private VariableUserType userType;
    private String[] componentClassNames;
    private final ProcessDefinition processDefinition;
    private final boolean editFormat;
    private static Map<String, String[]> containerFormats = Maps.newHashMap();
    static {
        containerFormats.put(ListFormat.class.getName(), new String[] { Localization.getString("VariableFormatPage.components.list.value") });
        containerFormats.put(MapFormat.class.getName(), new String[] { Localization.getString("VariableFormatPage.components.map.key"),
                Localization.getString("VariableFormatPage.components.map.value") });
    }

    public VariableFormatPage(ProcessDefinition processDefinition, VariableContainer variableContainer, Variable variable, boolean editFormat) {
        this.processDefinition = processDefinition;
        this.variableContainer = variableContainer;
        if (variable != null) {
            if (variable.getUserType() != null) {
                this.userType = variable.getUserType();
                setTypeByFormatClassName(UserTypeFormat.class.getName());
            } else {
                setTypeByFormatClassName(variable.getFormatClassName());
            }
            componentClassNames = variable.getFormatComponentClassNames();
            if (containerFormats.containsKey(type.getName()) && componentClassNames.length != containerFormats.get(type.getName()).length) {
                createDefaultComponentClassNames();
            }
        } else {
            setTypeByFormatClassName(StringFormat.class.getName());
            componentClassNames = new String[0];
        }
        this.editFormat = editFormat;
    }

    private void setTypeByFormatClassName(String formatClassName) {
        this.type = VariableFormatRegistry.getInstance().getArtifactNotNull(formatClassName);
    }

    @Override
    protected int getGridLayoutColumns() {
        return 1;
    }

    @Override
    protected void createContent(Composite composite) {
        final Combo combo = createTypeCombo(composite, false, false);
        combo.setEnabled(editFormat);
        combo.setText(userType != null ? userType.getName() : type.getLabel());
        combo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String label = combo.getText();
                userType = processDefinition.getVariableUserType(label);
                if (userType != null) {
                    type = VariableFormatRegistry.getInstance().getArtifactNotNull(UserTypeFormat.class.getName());
                } else {
                    type = VariableFormatRegistry.getInstance().getArtifactNotNullByLabel(label);
                }
                createDefaultComponentClassNames();
                updateContent();
            }
        });
        dynaComposite = new Composite(composite, SWT.NONE);
        dynaComposite.setLayout(new GridLayout(2, false));
        dynaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private void createDefaultComponentClassNames() {
        String[] labels = containerFormats.get(type.getName());
        componentClassNames = new String[labels != null ? labels.length : 0];
        for (int i = 0; i < componentClassNames.length; i++) {
            componentClassNames[i] = StringFormat.class.getName();
        }
    }

    private Combo createTypeCombo(Composite composite, boolean disableListFormat, boolean disableMapFormat) {
        final Combo combo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        for (VariableFormatArtifact artifact : VariableFormatRegistry.getInstance().getAll()) {
            if (UserTypeFormat.class.getName().equals(artifact.getName())
                    || disableListFormat && ListFormat.class.getName().equals(artifact.getName())
                    || disableMapFormat && MapFormat.class.getName().equals(artifact.getName())) {
                continue;
            }

            if (artifact.isEnabled()) {
                combo.add(artifact.getLabel());
            }
        }
        for (VariableUserType userType : processDefinition.getVariableUserTypes()) {
            if (!(variableContainer instanceof VariableUserType) || ((VariableUserType) variableContainer).canUseAsAttributeType(userType)) {
                combo.add(userType.getName());
            }
        }
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return combo;
    }

    @Override
    protected void createDynaContent() {
        String[] labels = containerFormats.get(type.getName());
        if (labels != null) {
            GridData strokeData = new GridData(GridData.FILL_HORIZONTAL);
            strokeData.horizontalSpan = 2;
            SwtUtils.createStrokeComposite(dynaComposite, strokeData, Localization.getString("VariableFormatPage.components.label"), 3);
            for (int i = 0; i < labels.length; i++) {
                Label label = new Label(dynaComposite, SWT.NONE);
                label.setText(labels[i]);
                final Combo combo = createTypeCombo(dynaComposite, true, true);
                combo.setData(i);
                VariableFormatArtifact artifact = VariableFormatRegistry.getInstance().getArtifact(componentClassNames[i]);
                combo.setText(artifact != null ? artifact.getLabel() : componentClassNames[i]);
                combo.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        int index = (Integer) combo.getData();
                        String label = combo.getText();
                        VariableUserType userType = processDefinition.getVariableUserType(label);
                        if (userType != null) {
                            componentClassNames[index] = label;
                        } else {
                            componentClassNames[index] = VariableFormatRegistry.getInstance().getArtifactNotNullByLabel(label).getName();
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void verifyContentIsValid() {
    }

    public VariableUserType getUserType() {
        return userType;
    }

    public VariableFormatArtifact getType() {
        return type;
    }

    public String[] getComponentClassNames() {
        return componentClassNames;
    }
}
