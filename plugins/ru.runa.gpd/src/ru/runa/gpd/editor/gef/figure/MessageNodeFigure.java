package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.lang.model.EventNodeType;

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
    
    public void paintEventType(EventNodeType eventNodeType, Graphics g, Dimension dim, boolean toLeft) {
    	final int offset = Utils.EVENT_TYPE_ICON_WIDTH + GEFConstants.GRID_SIZE/2;
		Point envTypePoint = new Point(toLeft ? dim.width - offset : GEFConstants.GRID_SIZE/2, GEFConstants.GRID_SIZE/2);
		Utils.paintEventType(g, envTypePoint, eventNodeType);
	}
}
