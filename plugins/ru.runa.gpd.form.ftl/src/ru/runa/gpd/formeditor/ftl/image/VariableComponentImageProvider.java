package ru.runa.gpd.formeditor.ftl.image;

import ru.runa.gpd.formeditor.ftl.ComponentType;

public class VariableComponentImageProvider extends DynaComponentImageProvider {

    @Override
    protected String getLabel(ComponentType type, String[] parameters) {
        String label = super.getLabel(type, parameters);
        if (parameters.length > 0) {
            label += ": " + parameters[0];
        }
        return label;
    }

}
