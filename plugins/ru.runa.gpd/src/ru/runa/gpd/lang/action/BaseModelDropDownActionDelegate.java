package ru.runa.gpd.lang.action;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionDelegate2;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.GraphElement;

public abstract class BaseModelDropDownActionDelegate extends BaseModelActionDelegate implements IActionDelegate2 {
    /**
     * Вызывалось ли dispose()
     */
    private boolean disposed = false;
    /**
     * Для каждого класса делегатов содержит not disposed экземпляр делегата данного класса, или null, если такого нет.
     */
    private static Map<Class<? extends BaseModelDropDownActionDelegate>, BaseModelDropDownActionDelegate> delegates = new HashMap<>();
    private MenuCreator menuCreator;

    public BaseModelDropDownActionDelegate() { // вызывается в неявных конструкторах подклассов,
        // которые вызываются за счет создания executable extension
        delegates.put(this.getClass(), this);
        this.menuCreator = new MenuCreator();
    }

    @Override
    public void run(IAction action) {
    }

    @Override
    public void runWithEvent(IAction action, Event event) {
        this.run(action);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        action.setMenuCreator(this.menuCreator);
    }

    /**
     * Fills the menu with applicable launch shortcuts
     * 
     * @param menu
     *            The menu to fill
     */
    protected abstract void fillMenu(Menu menu);

    @Override
    protected <T extends GraphElement> T getSelection() {
        if (this.disposed) {
            // когда делегат disposed, он больше не используется и его selection не обновляется,
            // и надо использовать selection из преемника этого делегата
            BaseModelDropDownActionDelegate successor = this.getSuccessor(); // null, когда он еще не создан
            return successor == null ? null : successor.getSelection();
        } else {
            return super.getSelection();
        }
    }

    /**
     * Возвращает делегат, который является преемником этого. Когда редактор какого-либо процесса закрывается, для делегата, наследующего
     * BaseModelDropDownActionDelegate и использованного в этом процессе, selection становится пустым. Для делегата вызывается dispose, и он далее не
     * используется в других открытых редакторах процессов: вместо него создается новый экземпляр делегата. Этот новый экзепляр - преемник исходного
     * делегата, он возвращается в этом методе.
     */
    private BaseModelDropDownActionDelegate getSuccessor() {
        return delegates.get(this.getClass());
    }

    private void fillMenuInternal(Menu menu) {
        if (this.disposed) {
            BaseModelDropDownActionDelegate successor = this.getSuccessor();
            if (successor == null) {
                this.fillMenu(menu);
            } else {
                successor.fillMenu(menu);
            }
        } else {
            this.fillMenu(menu);
        }
    }

    @Override
    public void init(IAction action) {
    }

    @Override
    public void dispose() {
        disposed = true;
        delegates.remove(this.getClass());
    }

    private class MenuCreator implements IMenuCreator {
        @Override
        public Menu getMenu(Control parent) {
            // never called
            return null;
        }

        @Override
        public Menu getMenu(Menu parent) {
            Menu menu = new Menu(parent);
            /**
             * Add listener to re-populate the menu each time it is shown because MenuManager.update(boolean, boolean) doesn't dispose pull-down
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
                            fillMenuInternal(m);
                        }
                    } catch (Exception ex) {
                        PluginLogger.logError(ex);
                    }
                }
            });
            return menu;
        }

        @Override
        public void dispose() {
        }
    }
}
