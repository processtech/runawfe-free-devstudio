package ru.runa.gpd.lang.model;

public class TextAnnotation extends NamedGraphElement implements Describable {

    @Override
    public String getLabel() {
        return getDescription() + " (" + getId() + ")";
    }
}
