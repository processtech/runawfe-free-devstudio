package ru.runa.gpd.editor.gef.figure;

import java.util.List;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.PageFlowLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.SwimlanedNode;

public abstract class NodeFigure<T extends GraphElement> extends Figure implements GEFConstants {
    protected static final Color veryLightBlue = new Color(null, 246, 247, 255);
    protected static final Color lightBlue = new Color(null, 3, 104, 154);
    protected TextFlow swimlaneLabel;
    protected TextFlow label;
    private static final Border TOOL_TIP_BORDER = new MarginBorder(0, 2, 0, 2);
    private static final Border LABEL_BORDER = new MarginBorder(0, GRID_SIZE / 2, 0, GRID_SIZE / 2);
    protected ActionsContainer actionsContainer;
    protected ConnectionAnchor connectionAnchor = null;
    protected T model;

    public void init() {
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        setLayoutManager(layout);
    }

    public void setModel(T model) {
        this.model = model;
    }

    public ActionsContainer getActionsContainer() {
        return actionsContainer;
    }

    public ConnectionAnchor getLeavingConnectionAnchor() {
        return connectionAnchor;
    }

    public ConnectionAnchor getArrivingConnectionAnchor() {
        return connectionAnchor;
    }

    public TextFlow getLabel() {
        return label;
    }

    public Dimension getDefaultSize() {
        return model.getTypeDefinition().getGefEntry().getDefaultSize(model).getCopy();
    }

    @Override
    public void setBounds(Rectangle rect) {
        if (!isResizeable() || rect.width == 0 || rect.height == 0) {
            rect.setSize(getDefaultSize());
        } else {
            if (model instanceof Node && !((Node) model).isMinimizedView()) {
                if (rect.width < getDefaultSize().width) {
                    rect.width = getDefaultSize().width;
                }
                if (rect.height < getDefaultSize().height) {
                    rect.height = getDefaultSize().height;
                }
            }
        }
        super.setBounds(rect);
    }

    /**
     * 
     * @return rectangle for anchor
     */
    protected Rectangle getBox() {
        return getBounds();
    }

    protected void addSwimlaneLabel() {
        swimlaneLabel = new TextFlow();
        FlowPage fp = new FlowPage();
        fp.setHorizontalAligment(PositionConstants.CENTER);
        fp.add(swimlaneLabel);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        add(fp, gridData);
    }

    protected void addLabel() {
        Figure figure = new Figure();
        CenteredFlowLayout layout = new CenteredFlowLayout();
        layout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
        figure.setLayoutManager(layout);
        figure.setOpaque(false);
        label = new TextFlow();
        FlowPage fp = new FlowPage();
        fp.setLayoutManager(new PageFlowLayout(fp));
        fp.setHorizontalAligment(PositionConstants.CENTER);
        fp.add(label);
        fp.setBorder(LABEL_BORDER);
        figure.add(fp, FlowLayout.ALIGN_CENTER);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        add(figure, gridData);
    }

    protected void addActionsContainer() {
        actionsContainer = new ActionsContainer();
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 2;
        add(actionsContainer, gridData);
    }

    @Override
    public void add(IFigure figure, Object constraint, int index) {
        if (figure instanceof ActionFigure) {
            actionsContainer.addSafely(figure, constraint, index);
        } else {
            super.add(figure, constraint, index);
        }
    }

    @Override
    public void remove(IFigure figure) {
        if (figure instanceof ActionFigure) {
            actionsContainer.removeSafely(figure);
        } else {
            super.remove(figure);
        }
    }

    // Find action (which doesn't have layout constraint)
    @SuppressWarnings("unchecked")
    @Override
    public IFigure findFigureAt(int x, int y, TreeSearch search) {
        for (IFigure figure : (List<IFigure>) getChildren()) {
            if (figure instanceof ActionFigure && figure.getBounds().contains(x, y)) {
                return figure;
            }
        }
        return super.findFigureAt(x, y, search);
    }

    protected String getTooltipMessage() {
        return null;
    }

    public void update() {
        if (label != null && model instanceof NamedGraphElement) {
            label.setText(((NamedGraphElement) model).getName());
        }
        if (swimlaneLabel != null && model instanceof SwimlanedNode) {
            swimlaneLabel.setText(((SwimlanedNode) model).getSwimlaneLabel());
        }
        repaint();
        // update tooltip
        String tooltipMessage = getTooltipMessage();
        if (tooltipMessage == null || tooltipMessage.length() == 0) {
            setToolTip(null);
            return;
        }
        if (getToolTip() == null) {
            Label tooltip = new Label();
            tooltip.setBorder(TOOL_TIP_BORDER);
            setToolTip(tooltip);
        }
        ((Label) getToolTip()).setText(tooltipMessage);
    }

    @Override
    public void paint(Graphics graphics) {
        if (!Activator.getDefault().getDialogSettings().getBoolean(PluginConstants.DISABLE_ANTIALIASING)) {
            graphics.setTextAntialias(SWT.ON);
            graphics.setAntialias(SWT.ON);
        }
        super.paint(graphics);
    }

    protected void paintFigure(Graphics g, Dimension dim) {
    }

    @Override
    protected final void paintFigure(Graphics graphics) {
        Rectangle r = getClientArea().getCopy();
        graphics.translate(r.getLocation());
        paintFigure(graphics, r.getSize());
    }

    public boolean isResizeable() {
        return true;
    }
}
