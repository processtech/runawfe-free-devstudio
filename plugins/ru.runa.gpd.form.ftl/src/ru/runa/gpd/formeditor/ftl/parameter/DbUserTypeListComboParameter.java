package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;
import java.util.stream.Collectors;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;

public class DbUserTypeListComboParameter extends ComboParameter {
    @Override
    protected List<ComboOption> getOptions(Component component, ComponentParameter parameter) {
        return FormEditor.getCurrent().getProcessDefinition().getVariableUserTypes().stream()
                .map(userType -> new ComboOption(userType.getName(), userType.getName())).collect(Collectors.toList());
    }

}
