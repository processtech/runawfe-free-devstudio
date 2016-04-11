package ru.runa.gpd.ui.custom;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ru.runa.gpd.util.FontsUtils;

import com.google.common.collect.Lists;

// from https://code.google.com/p/swtsplitbutton/
public class DropDownButton extends Button {
    private final static String EMPTY_SPACE = FontsUtils.getSpaceByWidth(21);
    private final static Color COLOR_WIDGET_NORMAL_SHADOW = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
    private final static Color COLOR_WIDGET_HIGHLIGHT_SHADOW = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
    private final static Color COLOR__BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);

    private List<SelectionListener> listeners = Lists.newArrayList();

    private int x1 = -1;
    private int y1 = -1;
    private int x2 = -1;
    private int y2 = -1;
    private Menu menu;

    public DropDownButton(Composite parent) {
        super(parent, SWT.PUSH);
        setText("");
        super.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                // draw the split line and arrow
                Rectangle rect = getBounds();
                Color oldForeground = e.gc.getForeground();
                Color oldBackground = e.gc.getBackground();
                x1 = e.x + rect.width - 20;
                y1 = e.y;
                x2 = e.x + rect.width;
                y2 = e.y + rect.height;
                int dx = -e.gc.getClipping().x;
                int dy = -e.gc.getClipping().y;

                e.gc.setForeground(COLOR_WIDGET_NORMAL_SHADOW);
                e.gc.setBackground(COLOR_WIDGET_NORMAL_SHADOW);
                e.gc.setLineWidth(1);
                e.gc.drawLine(e.x + rect.width - 20 + dx, e.y + 6 + dy, e.x + rect.width - 20 + dx, e.y + rect.height - 6 + dy);

                e.gc.setForeground(COLOR_WIDGET_HIGHLIGHT_SHADOW);
                e.gc.setBackground(COLOR_WIDGET_HIGHLIGHT_SHADOW);
                e.gc.setLineWidth(1);
                e.gc.drawLine(e.x + rect.width - 19 + dx, e.y + 6 + dy, e.x + rect.width - 19 + dx, e.y + rect.height - 6 + dy);

                e.gc.setForeground(COLOR__BLACK);
                e.gc.setBackground(COLOR__BLACK);
                e.gc.fillPolygon(new int[] { e.x + rect.width - 15 + dx, e.y + rect.height / 2 - 1 + dy, e.x + rect.width - 8 + dx,
                        e.y + rect.height / 2 - 1 + dy, e.x + rect.width - 12 + dx, e.y + rect.height / 2 + 3 + dy });

                e.gc.setForeground(oldForeground);
                e.gc.setBackground(oldBackground);
            }
        });
        super.addListener(SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent(Event event) {
                if (isShowMenu(event.x, event.y)) {
                    Button button = (Button) event.widget;
                    Rectangle rect = button.getBounds();
                    Point p = button.toDisplay(rect.x, rect.y + rect.height);
                    getMenu().setLocation(p.x - rect.x, p.y - rect.y);
                    getMenu().setVisible(true);
                } else {
                    for (SelectionListener listener : listeners) {
                        listener.widgetSelected(null);
                    }
                }
            }
        });
        menu = new Menu(getShell(), SWT.POP_UP);
    }

    private boolean isShowMenu(int x, int y) {
        return x >= x1 && y >= y1 && x <= x2 && y <= y2;
    }

    @Override
    public void addSelectionListener(SelectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Override
    public void setText(String string) {
        if (string != null) {
            super.setText(string + EMPTY_SPACE);
        }
    }

    @Override
    public String getText() {
        return super.getText().trim();
    }

    public void addButton(String label, LoggingSelectionAdapter selectionAdapter) {
        MenuItem logout = new MenuItem(menu, SWT.PUSH);
        logout.setText(label);
        logout.addSelectionListener(selectionAdapter);
    }
}
