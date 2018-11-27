package ru.runa.gpd.formeditor.ftl.validation;

import java.text.MessageFormat;
import java.util.List;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Localization;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;

import com.google.common.collect.Lists;

public class DefaultComponentValidator implements IComponentValidator {

    @Override
    public List<ValidationError> validate(FormNode formNode, Component component, byte[] formData) {
        List<ValidationError> list = Lists.newArrayList();

        String componentParameters = new String(formData);
        if (!componentParameters.contains(component.toString())) {
            String excessiveParametersMessage = MessageFormat.format(Localization.getString("ExcessiveParameters_2"), component.toString());
            list.add(ValidationError.createError(formNode, excessiveParametersMessage));
            MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    Localization.getString("ExcessiveParameters_1"), excessiveParametersMessage);
        }
        for (ComponentParameter parameter : component.getType().getParameters()) {
            list.addAll(parameter.getType().getValidator().validate(formNode, component, parameter));
        }
        return list;
    }

}
