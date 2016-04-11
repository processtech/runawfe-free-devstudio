package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.SnapToGrid;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class GridSupportLayer extends FreeformLayer implements GEFConstants {

    protected int gridX = GRID_SIZE;

    protected int gridY = GRID_SIZE;

    /**
     * Field indicating what the grid origin is. This is used simply to
     * determine the offset from 0,0.
     */
    protected Point origin = new Point();
    
    private ProcessDefinition definition;

    /**
     * Constructor Sets the default grid color: ColorConstants.lightGray
     */
    public GridSupportLayer() {
        setForegroundColor(ColorConstants.black);
        setLayoutManager(new FreeformLayout());
        setBorder(new LineBorder(1));
    }

    public void setDefinition(ProcessDefinition definition) {
        this.definition = definition;
    }
    
    /**
     * Overridden to indicate no preferred size. The grid layer should not
     * affect the size of the layered pane in which it is placed.
     * 
     * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
     */
    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        return new Dimension();
    }

    @Override
    protected void paintFigure(Graphics graphics) {
        graphics.setForegroundColor(ColorConstants.black);
        super.paintFigure(graphics);
        if (definition != null && definition.isShowGrid()) {
            graphics.setForegroundColor(ColorConstants.lightGray);
            paintGrid(graphics);
        }
    }

    /**
     * Paints the grid. Sub-classes can override to customize the grid's look.
     * If this layer is being used with SnapToGrid, this method will only be
     * invoked when the {@link SnapToGrid#PROPERTY_GRID_VISIBLE visibility}
     * property is set to true.
     * 
     * @param g
     *            The Graphics object to be used to do the painting
     * @see FigureUtilities#paintGrid(Graphics, IFigure, Point, int, int)
     */
    protected void paintGrid(Graphics g) {
        FigureUtilities.paintGrid(g, this, origin, gridX, gridY);
    }

    /**
     * Sets the origin of the grid. The origin is used only to determine the
     * offset from 0,0.
     * 
     * @param p
     *            the origin
     */
    public void setOrigin(Point p) {
        if (p == null)
            p = new Point();
        if (!origin.equals(p)) {
            origin = p;
            repaint();
        }
    }

    public void setSpacing(Dimension spacing) {
        if (spacing == null)
            spacing = new Dimension(GRID_SIZE, GRID_SIZE);
        if (!spacing.equals(gridX, gridY)) {
            gridX = spacing.width != 0 ? spacing.width : gridX;
            gridY = spacing.height != 0 ? spacing.height : gridY;
            repaint();
        }
    }
}
