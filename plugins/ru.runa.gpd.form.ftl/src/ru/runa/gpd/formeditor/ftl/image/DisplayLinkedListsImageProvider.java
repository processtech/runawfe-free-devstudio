package ru.runa.gpd.formeditor.ftl.image;

import ru.runa.gpd.formeditor.ftl.ComponentType;

public class DisplayLinkedListsImageProvider extends DynaComponentImageProvider {

    @Override
    protected String getLabel(ComponentType type, String[] parameters) {
        String label = super.getLabel(type, parameters);
        if (parameters.length > 1) {
            label += ": ";
            for (int i = 1; i < parameters.length; i++) {
                if (i != 1) {
                    label += ", ";
                }
                label += parameters[i];
            }
        }
        return label;
    }

}
