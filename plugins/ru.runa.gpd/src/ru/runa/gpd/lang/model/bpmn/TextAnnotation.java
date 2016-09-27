package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.lang.model.Describable;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class TextAnnotation extends NamedGraphElement implements Describable {

    @Override
    public String getLabel() {
        return getDescription() + " (" + getId() + ")";
    }

}
