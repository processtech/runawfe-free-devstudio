package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;

public abstract class MessageNodeFigure extends NodeFigure {
    @Override
    public void init() {
        super.init();
        addLabel();
    }

    // TODO: update on size change
    protected void addEmptySpace(int position, int width) {
        Figure figure = (Figure) label.getParent().getParent();
        ((GridData) getLayoutManager().getConstraint(figure)).horizontalSpan = 1;
        GridData data = new GridData(GridData.FILL_VERTICAL);
        data.widthHint = width;
        add(new Figure(), data, position);
    }
}
