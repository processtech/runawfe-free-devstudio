package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Menu;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.wfe.lang.MultiTaskSynchronizationMode;

import com.google.common.base.Objects;

public class SelectMultiTaskSynchronizationModeAction extends BaseModelDropDownActionDelegate {
    private MultiTaskSynchronizationMode selectedMode;
    private MultiTaskState multiTaskState;

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        multiTaskState = getSelection();
        if (multiTaskState != null) {
            selectedMode = multiTaskState.getSynchronizationMode();
        }
    }

    /**
     * Fills the menu with applicable launch shortcuts
     * 
     * @param menu
     *            The menu to fill
     */
    @Override
    protected void fillMenu(Menu menu) {
        for (MultiTaskSynchronizationMode mode : MultiTaskSynchronizationMode.values()) {
            SetModeAction action = new SetModeAction(mode);
            if (Objects.equal(selectedMode, mode)) {
                action.setChecked(true);
            }
            ActionContributionItem item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
    }

    public class SetModeAction extends Action {
        private final MultiTaskSynchronizationMode mode;

        public SetModeAction(MultiTaskSynchronizationMode mode) {
            this.mode = mode;
            setText(Localization.getString("MultiTask.property.synchronizationMode." + mode.name()));
        }

        @Override
        public void run() {
            multiTaskState.setSynchronizationMode(mode);
        }
    }
}
