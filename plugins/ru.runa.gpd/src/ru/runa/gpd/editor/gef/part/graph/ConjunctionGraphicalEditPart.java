package ru.runa.gpd.editor.gef.part.graph;

import java.util.List;

import ru.runa.gpd.editor.gef.figure.ConjunctionFigure;
import ru.runa.gpd.lang.model.bpmn.Conjunction;

public class ConjunctionGraphicalEditPart extends LabeledNodeGraphicalEditPart {

    @Override
    public Conjunction getModel() {
        return (Conjunction) super.getModel();
    }

    @Override
    public ConjunctionFigure getFigure() {
        return (ConjunctionFigure) super.getFigure();
    }

    @Override
    protected void fillFigureUpdatePropertyNames(List<String> list) {
        super.fillFigureUpdatePropertyNames(list);
        list.add(PROPERTY_MINIMAZED_VIEW);
    }

}
