package ru.runa.gpd.editor.gef.figure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.gef.ActionGraphUtils;

public class ActionsContainer extends Figure {
    private boolean actionsFitInFigure = true;
    private List<ActionFigure> cachedFigures = null;
    private ActionFigure multipleFigure = null;
    
    public ActionsContainer() {
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 5;
        setLayoutManager(layout);
        setOpaque(false);
    }
    
    public void reset() {
        actionsFitInFigure = true;
        cachedFigures = null;
        removeAll();
    }
    
    private ActionFigure getMultipleFigure() {
        if (multipleFigure == null) {
            multipleFigure = ActionFigure.getMultipleFigure();
        }
        return multipleFigure;
    }
    
    public void removeSafely(IFigure figure) {
        if (figure.getParent() == this) {
            remove(figure);
        } else {
            cachedFigures.remove(figure);
            checkActionsFitInFigure();
        }
    }
    
    @Override
    public void remove(IFigure figure) {
        super.remove(figure);
        adjustLayout();
    }
    
    private void adjustLayout() {
        GridLayout layout = (GridLayout) getLayoutManager();
        layout.numColumns = getChildren().size();
        layout.invalidate();
    }
    
    public void addSafely(IFigure figure, Object constraint, int index) {
        if (!actionsFitInFigure) {
            cachedFigures.add((ActionFigure) figure);
            return;
        }
        if (getChildren().size() < index) {
            index = -1;
        }
        add(figure, constraint, index);
    }

    @Override
    public void add(IFigure figure, Object constraint, int index) {
        super.add(figure, constraint, index);
        if (actionsFitInFigure) {
            checkActionsFitInFigure();
        }
        adjustLayout();
    }

    @Override
    public void setBounds(Rectangle rect) {
        super.setBounds(rect);
        checkActionsFitInFigure();
    }

    @SuppressWarnings("unchecked")
	private void checkActionsFitInFigure() {
        int actionsCount = getChildren().size();
        if (!actionsFitInFigure) {
            actionsCount = cachedFigures.size();
        }
        int preferred = actionsCount * (ActionGraphUtils.ACTION_SIZE + 7);
        int real = getParent().getBounds().width - GEFConstants.GRID_SIZE;
        boolean nowActionsFitInFigure = !(real < preferred);
        if (nowActionsFitInFigure == actionsFitInFigure) {
            return;
        }
        this.actionsFitInFigure = nowActionsFitInFigure;
        if (actionsFitInFigure) {
            remove(getMultipleFigure());
            if (cachedFigures == null) {
                throw new NullPointerException("Node.cachedFigures = null");
            }
            for (ActionFigure figure : cachedFigures) {
                figure.setVisible(true);
                add(figure);
            }
        } else {
            cachedFigures = new ArrayList<ActionFigure>(getChildren());
            for (ActionFigure figure : cachedFigures) {
                figure.setVisible(false);
                remove(figure);
            }
            add(getMultipleFigure());
        }

    }
}
