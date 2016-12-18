package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.util.MultiinstanceParameters;
import ru.runa.gpd.util.VariableMapping;

public class MultiSubprocessDialog extends SubprocessDialog {
    private final MultiinstanceParameters parameters;
    private Combo iteratorVariableCombo;

    public MultiSubprocessDialog(MultiSubprocess multiSubprocess) {
        super(multiSubprocess);
        parameters = new MultiinstanceParameters(multiSubprocess.getVariableMappings());
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(800, 700);
    }

    @Override
    protected void onSubprocessChanged() {
        super.onSubprocessChanged();
        updateIteratorComboVariables();
    }

    @Override
    protected void createConfigurationComposite(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout());
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 240;
        group.setLayoutData(data);
        group.setText(Localization.getString("Feature.Multiinstance"));
        new MultiinstanceComposite(group, subprocess, parameters).setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite composite = new Composite(group, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label processVariableLabel = new Label(composite, SWT.NONE);
        processVariableLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        processVariableLabel.setText(Localization.getString("Subprocess.IteratorVariableName") + ":");
        iteratorVariableCombo = new Combo(composite, SWT.READ_ONLY);
        updateIteratorComboVariables();
        iteratorVariableCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        iteratorVariableCombo.setText(parameters.getIteratorVariableName());
        iteratorVariableCombo.addModifyListener(new LoggingModifyTextAdapter() {

            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                parameters.setIteratorVariableName(iteratorVariableCombo.getText());
            }
        });
    }

    private void updateIteratorComboVariables() {
        List<String> variableNames;
        ProcessDefinition definition = ProcessCache.getFirstProcessDefinition(getSubprocessName());
        if (definition != null) {
            variableNames = definition.getVariableNames(true);
        } else {
            variableNames = new ArrayList<String>();
        }
        iteratorVariableCombo.setItems(variableNames.toArray(new String[variableNames.size()]));
    }

    @Override
    public List<VariableMapping> getVariableMappings(boolean includeMetadata) {
        List<VariableMapping> variableMappings = super.getVariableMappings(includeMetadata);
        if (includeMetadata) {
            parameters.mergeTo(variableMappings);
        }
        return variableMappings;
    }

}
