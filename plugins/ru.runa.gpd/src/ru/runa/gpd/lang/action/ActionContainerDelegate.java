package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.gef.command.AddActionCommand;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;

public class ActionContainerDelegate extends BaseModelDropDownActionDelegate {

    /**
     * Fills the menu with applicable launch shortcuts
     * 
     * @param menu
     *            The menu to fill
     */
    @Override
    protected void fillMenu(Menu menu) {
        ActionContainer actionContainer = (ActionContainer) getSelectionNotNull();
        boolean createSeparator = false;
        for (ru.runa.gpd.lang.model.Action action : actionContainer.getActions()) {
            Action menuAction = new ShowAction(action);
            menuAction.setText(action.getLabel());
            ActionContributionItem item = new ActionContributionItem(menuAction);
            item.fill(menu, -1);
            createSeparator = true;
        }
        if (createSeparator) {
            new MenuItem(menu, SWT.SEPARATOR);
        }
        Action menuAction = new AddActionAction(actionContainer);
        ActionContributionItem item = new ActionContributionItem(menuAction);
        item.fill(menu, -1);
    }

    public class AddActionAction extends Action {
        private final ActionContainer actionContainer;
        
        public AddActionAction(ActionContainer actionContainer) {
            this.actionContainer = actionContainer;
            setText(Localization.getString("button.create"));
        }

        @Override
        public void run() {
            AddActionCommand command = new AddActionCommand();
            command.setTarget(actionContainer);
            executeCommand(command);
            setFocus(command.getAction());
        }
    }

    private void setFocus(ru.runa.gpd.lang.model.Action action) {
        getActiveDesignerEditor().select(action);
    }

    public class ShowAction extends Action {
        private ru.runa.gpd.lang.model.Action action;

        public ShowAction(ru.runa.gpd.lang.model.Action action) {
            this.action = action;
        }

        @Override
        public void run() {
            setFocus(action);
            DelegableProvider provider = HandlerRegistry.getProvider(action.getDelegationClassName());
            String newConfig = provider.showConfigurationDialog(action);
            if (newConfig != null) {
                action.setDelegationConfiguration(newConfig);
            }
        }
    }
}
