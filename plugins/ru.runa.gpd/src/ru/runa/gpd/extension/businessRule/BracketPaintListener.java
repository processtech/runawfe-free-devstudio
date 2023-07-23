package ru.runa.gpd.extension.businessRule;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class BracketPaintListener implements PaintListener {
    private List<LogicComposite> logicComposites;
    private Composite expression;
    private LogicComposite logicComposite;

    public BracketPaintListener(List<LogicComposite> logicComposites, Composite expression, LogicComposite logicComposite) {
        this.logicComposites = logicComposites;
        this.expression = expression;
        this.logicComposite = logicComposite;
    }

    @Override
    public void paintControl(PaintEvent e) {
        for (int i = 0; i < logicComposites.get(logicComposites.indexOf(logicComposite)).getBrackets()[0]; i++) {
            int leftPoint = ((GridLayout) expression.getLayout()).marginLeft - i * LogicComposite.MARGIN_LEFT_STEP;
            int topPoint = ((GridLayout) expression.getLayout()).marginTop - i * LogicComposite.MARGIN_TOP_STEP;
            int rightPoint = e.width - 30;

            e.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BORDER));
            e.gc.setLineWidth(4);
            e.gc.drawLine(leftPoint, topPoint, rightPoint, topPoint);
            e.gc.drawLine(leftPoint, topPoint, leftPoint, topPoint + 10);
            e.gc.drawLine(rightPoint, topPoint, rightPoint, topPoint + 10);
        }

        for (int i = 0; i < logicComposites.get(logicComposites.indexOf(logicComposite)).getBrackets()[1]; i++) {
            int leftPoint = ((GridLayout) expression.getLayout()).marginLeft - i * LogicComposite.MARGIN_LEFT_STEP;
            Point point = expression.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            int bottomPoint;
            int rightPoint = e.width - 30;
            if (((GridData) logicComposites.get(logicComposites.indexOf(logicComposite)).getLayoutData()).horizontalAlignment == SWT.END) {
                bottomPoint = point.y - ((GridLayout) expression.getLayout()).verticalSpacing + i * LogicComposite.MARGIN_BOTTOM_STEP - 28;
            } else {
                bottomPoint = point.y - ((GridLayout) expression.getLayout()).verticalSpacing + i * LogicComposite.MARGIN_BOTTOM_STEP;
            }

            e.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BORDER));
            e.gc.setLineWidth(4);
            e.gc.drawLine(leftPoint, bottomPoint, rightPoint, bottomPoint);
            e.gc.drawLine(leftPoint, bottomPoint, leftPoint, bottomPoint - 10);
            e.gc.drawLine(rightPoint, bottomPoint, rightPoint, bottomPoint - 10);
        }
    }
}
