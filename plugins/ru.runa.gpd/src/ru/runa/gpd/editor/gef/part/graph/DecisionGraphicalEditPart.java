package ru.runa.gpd.editor.gef.part.graph;

import java.util.List;

import ru.runa.gpd.lang.model.Decision;

public class DecisionGraphicalEditPart extends LabeledNodeGraphicalEditPart {
    @Override
    public Decision getModel() {
        return (Decision) super.getModel();
    }

    @Override
    protected void fillFigureUpdatePropertyNames(List<String> list) {
        super.fillFigureUpdatePropertyNames(list);
        list.add(PROPERTY_CONFIGURATION);
    }
}
