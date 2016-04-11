package ru.runa.gpd.util;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;

public class LabelCellEditorLocator implements CellEditorLocator {

    private TextFlow label;

    public LabelCellEditorLocator(TextFlow label) {
        this.label = label;
    }

    public void relocate(CellEditor celleditor) {
        Text text = (Text) celleditor.getControl();
        Rectangle rect = label.getClientArea().getCopy();
        label.translateToAbsolute(rect);
        org.eclipse.swt.graphics.Rectangle trim = text.computeTrim(0, 0, 0, 0);
        rect.translate(trim.x, trim.y);
        rect.width += trim.width;
        rect.height += trim.height;
        text.setBounds(rect.x, rect.y, rect.width, rect.height);
    }
}
