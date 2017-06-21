package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.wizard.Wizard;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;

public class VariableWizard extends Wizard {
    private VariableNamePage namePage;
    private final VariableFormatPage formatPage;
    private final VariableDefaultValuePage defaultValuePage;
    private VariableAccessPage accessPage;
    private final VariableStoreTypePage storeTypePage;
    private Variable variable;

    public VariableWizard(ProcessDefinition processDefinition, Variable variable, boolean showNamePage, boolean editFormat) {
        this(processDefinition, processDefinition, variable, showNamePage, editFormat, true);
    }

    public VariableWizard(ProcessDefinition processDefinition, VariableContainer variableContainer, Variable variable, boolean showNamePage,
            boolean editFormat, boolean showAccessPage) {
        if (showNamePage) {
            namePage = new VariableNamePage(variableContainer, variable);
        }
        formatPage = new VariableFormatPage(processDefinition, variableContainer, variable, editFormat);
        defaultValuePage = new VariableDefaultValuePage(variable);
        if (showAccessPage) {
            accessPage = new VariableAccessPage(variable);
        }
        storeTypePage = new VariableStoreTypePage(variable);
        setWindowTitle(showNamePage ? Localization.getString("VariableWizard.create") : Localization.getString("VariableWizard.edit"));
    }

    @Override
    public void addPages() {
        if (namePage != null) {
            addPage(namePage);
        }
        addPage(formatPage);
        addPage(defaultValuePage);
        if (accessPage != null) {
            addPage(accessPage);
        }
        addPage(storeTypePage);
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public boolean performFinish() {
        String name = null;
        String scriptingName = null;
        String description = null;
        if (namePage != null) {
            name = namePage.getVariableName();
            scriptingName = namePage.getScriptingVariableName();
            description = namePage.getVariableDesc();
        }
        String formatClassName = formatPage.getType().getName();
        String format = formatClassName;
        if (formatPage.getComponentClassNames().length != 0) {
            format += Variable.FORMAT_COMPONENT_TYPE_START;
            for (int i = 0; i < formatPage.getComponentClassNames().length; i++) {
                if (i != 0) {
                    format += Variable.FORMAT_COMPONENT_TYPE_CONCAT;
                }
                format += formatPage.getComponentClassNames()[i];
            }
            format += Variable.FORMAT_COMPONENT_TYPE_END;
        }
        String defaultValue = defaultValuePage.getDefaultValue();
        boolean publicVisibility = accessPage != null ? accessPage.isPublicVisibility() : false;
        variable = new Variable(name, scriptingName, format, formatPage.getUserType(), publicVisibility, defaultValue, storeTypePage.getStoreType()
                .name());
        variable.setDescription(description);
        return true;
    }
}
