package ru.runa.gpd.formeditor.ftl.image;

import ru.runa.gpd.formeditor.ftl.ComponentType;

public class EditLinkedListsImageProvider extends DynaComponentImageProvider {

    @Override
    protected String getLabel(ComponentType type, String[] parameters) {
        String label = super.getLabel(type, parameters);
        if (parameters.length > 3) {
            label += ": ";
            for (int i = 3; i < parameters.length; i++) {
                if (i != 3) {
                    label += ", ";
                }
                label += parameters[i];
            }
        }
        return label;
    }

}
