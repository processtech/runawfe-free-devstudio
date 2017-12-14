package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.lang.model.Describable;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class TextAnnotation extends NamedGraphElement implements Describable {

    public TextAnnotation() {
        super();
        this.font = P_BPMN_TEXT_ANNOTATION_FONT;
        this.fontColor = P_BPMN_TEXT_ANNOTATION_FONT_COLOR;
    }

    @Override
    public String getLabel() {
        return getDescription() + " (" + getId() + ")";
    }

}
