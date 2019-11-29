package ru.runa.gpd.editor.graphiti;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.graphiti.datatypes.IRectangle;
import org.eclipse.graphiti.internal.contextbuttons.IContextButtonPadDeclaration;
import org.eclipse.graphiti.internal.contextbuttons.PositionedContextButton;
import org.eclipse.graphiti.tb.IContextButtonEntry;
import org.eclipse.graphiti.tb.IContextButtonPadData;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;

public class CustomContextButtonPadDeclaration implements IContextButtonPadDeclaration {

    private static final IColorConstant PAD_OUTER_LINE_COLOR = new ColorConstant(173, 191, 204);

    private static final IColorConstant PAD_MIDDLE_LINE_COLOR = new ColorConstant(255, 255, 255);

    private static final IColorConstant PAD_INNER_LINE_COLOR = new ColorConstant(245, 249, 251);

    private static final IColorConstant PAD_FILL_COLOR = new ColorConstant(235, 243, 247);

    private static final IColorConstant BUTTON_OUTER_LINE_COLOR = new ColorConstant(46, 101, 140);

    private static final IColorConstant BUTTON_MIDDLE_LINE_COLOR = new ColorConstant(255, 255, 255);

    private static final IColorConstant BUTTON_FILL_COLOR = PAD_FILL_COLOR;

    private Rectangle originalReferenceRectangle;
    private Rectangle padReferenceRectangle;
    private IContextButtonPadData contextButtonPadData;

    private List<IContextButtonEntry> collapseAndGenericButtons;
    private List<IContextButtonEntry> domainButtonsRight;

    private Rectangle top;
    private Rectangle right;

    private PadStyle topStyle = PadStyle.STANDARD;
    private PadStyle rightStyle = PadStyle.STANDARD;

    private List<PositionedContextButton> positionedButtons;
    private List<Rectangle> containmentRectangles;
    private List<Rectangle> overlappingContainmentRectangles;

    public CustomContextButtonPadDeclaration(IContextButtonPadData contextButtonPadData) {
        this.contextButtonPadData = contextButtonPadData;

        IRectangle l = contextButtonPadData.getPadLocation();
        this.originalReferenceRectangle = new Rectangle(l.getX(), l.getY(), l.getWidth(), l.getHeight());
        this.padReferenceRectangle = new Rectangle(originalReferenceRectangle);
        this.padReferenceRectangle.grow(1, 1);

        domainButtonsRight = getDomainButtons();

        this.initializeRectangle();
        this.initializeButtonPosition();
        this.initializeContainmentRectangle();
    }

    protected int getButtonSize() {
        return 20;
    }

    protected int getButtonPadding() {
        return 1;
    }

    protected int getCollapseButtonPadding() {
        return 10;
    }

    protected int getPadPaddingOutside() {
        return 10;
    }

    protected int getPadPaddingInside() {
        return 4;
    }

    protected int getPadHorizontalOverlap() {
        return 4;
    }

    protected int getPadVerticalOverlap() {
        return 4;
    }

    public int getPadAppendageLength() {
        return 8;
    }

    @Override
    public int getPadLineWidth() {
        return 1;
    }

    @Override
    public int getPadCornerRadius() {
        return 12;
    }

    @Override
    public IColorConstant getPadOuterLineColor() {
        return PAD_OUTER_LINE_COLOR;
    }

    @Override
    public IColorConstant getPadMiddleLineColor() {
        return PAD_MIDDLE_LINE_COLOR;
    }

    @Override
    public IColorConstant getPadInnerLineColor() {
        return PAD_INNER_LINE_COLOR;
    }

    @Override
    public IColorConstant getPadFillColor() {
        return PAD_FILL_COLOR;
    }

    @Override
    public double getPadDefaultOpacity() {
        return 0.9;
    }

    public PositionedContextButton createButton(IContextButtonEntry entry, Rectangle position) {
        PositionedContextButton ret = new PositionedContextButton(entry, position);
        ret.setLine(1, 4);
        ret.setColors(BUTTON_OUTER_LINE_COLOR, BUTTON_MIDDLE_LINE_COLOR, BUTTON_FILL_COLOR);
        ret.setOpacity(0.0, 0.7, 1.0);
        return ret;
    }

    @Override
    public Rectangle getTopPad() {
        return top;
    }

    @Override
    public Rectangle getRightPad() {
        return right;
    }

    @Override
    public PadStyle getTopPadStyle() {
        return topStyle;
    }

    @Override
    public PadStyle getRightPadStyle() {
        return rightStyle;
    }

    private void initializeRectangle() {
        Rectangle innerRectangle = new Rectangle(padReferenceRectangle);
        innerRectangle.height = getPadDynamicSize(domainButtonsRight.size()) - 2 * getPadVerticalOverlap();

        Point innerTop = new Point(innerRectangle.x + innerRectangle.width, innerRectangle.y);

        if (collapseAndGenericButtons().size() != 0) {
            top = new Rectangle();
            top.width = collapseAndGenericButtons().size() * 50;
            if (collapseButton() != null && genericButtons().size() > 0) {
                top.width += 10 - getButtonPadding();
            }
            top.height = getPadConstantSize();
            top.x = innerTop.x - top.width + getPadHorizontalOverlap();
            top.y = innerTop.y - top.height;
        }

        if (domainButtonsRight.size() != 0) {
            right = new Rectangle();
            right.height = (int) Math.ceil((domainButtonsRight.size() / (double) 4)) * getButtonSize() + getPadPaddingInside() * 2 + 10;
            right.width = 4 * getButtonSize() + getPadPaddingInside() * 2;
            right.x = innerTop.x;
            right.y = innerTop.y - getPadVerticalOverlap();
        }
    }

    private void initializeButtonPosition() {
        positionedButtons = new ArrayList<PositionedContextButton>();

        for (int i = 0; i < collapseAndGenericButtons().size(); i++) {
            int iBackwards = collapseAndGenericButtons().size() - 1 - i;
            int x = top.x + getPadPaddingOutside() + (iBackwards * (getButtonSize() + getButtonPadding()));
            if (i == 0 && collapseButton() != null && genericButtons().size() > 0) {
                x += 10 - getButtonPadding();
            }
            int y = top.y + getPadPaddingInside();
            Rectangle position = new Rectangle(x, y, getButtonSize(), getButtonSize());
            positionedButtons.add(createButton(collapseAndGenericButtons().get(i), position));
        }
        for (int i = 0, buttonY = 0, buttonX = 0; i < domainButtonsRight.size(); i++, buttonX++) {
            if (buttonX > 3) {
                buttonX = 0;
                buttonY++;
            }
            int y = right.y + getPadPaddingOutside() + (buttonY * (getButtonSize() + getButtonPadding()));
            int x = right.x + getPadPaddingInside() + (getButtonSize() * buttonX);

            Rectangle position = new Rectangle(x, y, getButtonSize(), getButtonSize());
            positionedButtons.add(createButton(domainButtonsRight.get(i), position));
        }
    }

    private void initializeContainmentRectangle() {
        containmentRectangles = new ArrayList<Rectangle>();
        overlappingContainmentRectangles = new ArrayList<Rectangle>();

        if (getTopPad() != null) {
            containmentRectangles.add(getTopPad());
        }
        if (getRightPad() != null) {
            containmentRectangles.add(getRightPad());
        }

        for (PositionedContextButton button : positionedButtons) {
            Rectangle position = button.getPosition();
            for (Rectangle rectangle : containmentRectangles) {
                if (rectangle.contains(position)) {
                    containmentRectangles.add(position);
                    break;
                }
            }
        }

        Rectangle r = originalReferenceRectangle;
        Point referencePoint = new Point(r.x + (r.width / 2), r.y + (r.height / 2));
        for (Rectangle rectangle : containmentRectangles) {
            Rectangle unionRectangle = rectangle.union(new Rectangle(referencePoint));
            overlappingContainmentRectangles.add(unionRectangle);
        }
        overlappingContainmentRectangles.add(originalReferenceRectangle);
    }

    private int getPadConstantSize() {
        return getPadPaddingInside() + getButtonSize() + getPadPaddingInside();
    }

    private int getPadDynamicSize(int numberOfButtons) {
        return (2 * getPadPaddingOutside()) + (numberOfButtons > 0 ? numberOfButtons * getButtonSize() : padReferenceRectangle.height)
                + (numberOfButtons > 1 ? (numberOfButtons - 1) * getButtonPadding() : 0);
    }

    private List<IContextButtonEntry> genericButtons() {
        return contextButtonPadData.getGenericContextButtons();
    }

    private List<IContextButtonEntry> collapseAndGenericButtons() {
        if (collapseAndGenericButtons == null) {
            collapseAndGenericButtons = new ArrayList<IContextButtonEntry>(genericButtons().size() + 1);
            if (collapseButton() != null) {
                collapseAndGenericButtons.add(collapseButton());
            }
            collapseAndGenericButtons.addAll(genericButtons());
        }
        return collapseAndGenericButtons;
    }

    private IContextButtonEntry collapseButton() {
        return contextButtonPadData.getCollapseContextButton();
    }

    @Override
    public List<PositionedContextButton> getPositionedContextButtons() {
        return positionedButtons;
    }

    @Override
    public List<Rectangle> getContainmentRectangles() {
        return containmentRectangles;
    }

    @Override
    public List<Rectangle> getOverlappingContainmentRectangles() {
        return overlappingContainmentRectangles;
    }

    private List<IContextButtonEntry> getDomainButtons() {
        return contextButtonPadData.getDomainSpecificContextButtons();
    }

    @Override
    public Rectangle getBottomPad() {
        return null;
    }

}
