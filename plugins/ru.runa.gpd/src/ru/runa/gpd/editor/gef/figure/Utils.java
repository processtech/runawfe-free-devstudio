package ru.runa.gpd.editor.gef.figure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.GEFConstants;

public class Utils implements GEFConstants {
    public static final int EVENT_TYPE_ICON_WIDTH = 22;

	public static void paintSurroudingBoxes(Graphics g, Dimension dim) {
        List<Rectangle> rects = new ArrayList<Rectangle>();
        rects.add(new Rectangle(-GRID_SIZE / 2, dim.height / 2 - 3 * GRID_SIZE / 2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(-GRID_SIZE / 2, dim.height / 2 - GRID_SIZE / 2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(-GRID_SIZE / 2, dim.height / 2 + GRID_SIZE / 2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(dim.width - GRID_SIZE / 2 - 1, dim.height / 2 - 3 * GRID_SIZE / 2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(dim.width - GRID_SIZE / 2 - 1, dim.height / 2 - GRID_SIZE / 2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(dim.width - GRID_SIZE / 2 - 1, dim.height / 2 + GRID_SIZE / 2, GRID_SIZE, GRID_SIZE));
        for (Rectangle rectangle : rects) {
            g.fillRectangle(rectangle);
            g.drawRectangle(rectangle);
        }
    }

    public static void paintTimer(Graphics g, Dimension dim) {
        g.fillOval(-GRID_SIZE, dim.height - GRID_SIZE, GRID_SIZE * 2, GRID_SIZE * 2);
        g.drawOval(1 - GRID_SIZE, 1 + dim.height - GRID_SIZE, GRID_SIZE * 2 - 2, GRID_SIZE * 2 - 2);
        g.drawLine(0, dim.height, 0, dim.height + 5);
        g.drawLine(0, dim.height, 5, dim.height - 5);
    }
    
    public static void paintMessage(Graphics g, Point leftUp) {
    	final int w = EVENT_TYPE_ICON_WIDTH;
    	final int h = 14;
    	PointList points = new PointList(7);
        points.addPoint(leftUp.x, leftUp.y);
        points.addPoint(leftUp.x, leftUp.y + h);
        points.addPoint(leftUp.x + w, leftUp.y + h);
        points.addPoint(leftUp.x + w, leftUp.y);
        points.addPoint(leftUp.x + w/2, leftUp.y + h/2 + 1);
        points.addPoint(leftUp.x, leftUp.y);
        points.addPoint(leftUp.x + w, leftUp.y);
        g.drawPolyline(points);
        
   }

   public static void paintSignal(Graphics g, Point leftUp) {
		final int w = 16;
    	PointList points = new PointList(4);
        points.addPoint(leftUp.x + w/2, leftUp.y);
        points.addPoint(leftUp.x + w, leftUp.y + w);
        points.addPoint(leftUp.x, leftUp.y + w);
        points.addPoint(leftUp.x + w/2, leftUp.y);
        g.drawPolyline(points);	
   }

	public static void paintCancle(Graphics g, Point leftUp) {
		final int w = GRID_SIZE;
		final int offset = 1;
        g.drawLine(leftUp.x + offset, leftUp.y + offset, leftUp.x - offset + w, leftUp.y - offset + w );
        g.drawLine(leftUp.x + w - offset, leftUp.y + offset, leftUp.x + offset, leftUp.y + w - offset);		
	}

	public static void paintError(Graphics g, Point leftUp) {
		PointList points = new PointList(7);
		points.addPoint(leftUp.x + 5, leftUp.y);		
		points.addPoint(leftUp.x + 11, leftUp.y + 7);
		points.addPoint(leftUp.x + 15, leftUp.y + 3);
		points.addPoint(leftUp.x + 11, leftUp.y + 15);
		points.addPoint(leftUp.x + 5, leftUp.y + 9);
		points.addPoint(leftUp.x, leftUp.y + 13);
		points.addPoint(leftUp.x + 5, leftUp.y);		
		g.setBackgroundColor(g.getForegroundColor());
		g.fillPolygon(points);
	}

}
