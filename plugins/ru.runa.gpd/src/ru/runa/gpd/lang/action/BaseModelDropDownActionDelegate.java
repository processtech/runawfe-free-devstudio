package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ru.runa.gpd.PluginLogger;

public abstract class BaseModelDropDownActionDelegate extends BaseModelActionDelegate implements IMenuCreator {
    @Override
    public void run(IAction action) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Menu getMenu(Control parent) {
        // never called
        return null;
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        action.setMenuCreator(this);
    }

    @Override
    public Menu getMenu(Menu parent) {
        Menu menu = new Menu(parent);
        /**
         * Add listener to re-populate the menu each time it is shown because
         * MenuManager.update(boolean, boolean) doesn't dispose pull-down
         * ActionContribution items for each popup menu.
         */
        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {
                try {
                    Menu m = (Menu) e.widget;
                    MenuItem[] items = m.getItems();
                    for (int i = 0; i < items.length; i++) {
                        items[i].dispose();
                    }
                    if (getSelection() != null) {
                        fillMenu(m);
                    }
                } catch (Exception ex) {
                    PluginLogger.logError(ex);
                }
            }
        });
        return menu;
    }

    /**
     * Fills the menu with applicable launch shortcuts
     * 
     * @param menu
     *            The menu to fill
     */
    protected abstract void fillMenu(Menu menu);
}
