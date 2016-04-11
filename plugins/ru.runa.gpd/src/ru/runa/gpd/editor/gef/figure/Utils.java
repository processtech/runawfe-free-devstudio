package ru.runa.gpd.editor.gef.figure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.GEFConstants;

public class Utils implements GEFConstants {
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
}
