package ru.runa.gpd.lang.action;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.gef.command.IgnoreSubstitutionCommand;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.ui.dialog.UpdateSwimlaneNameDialog;
import ru.runa.gpd.util.SwimlaneDisplayMode;

public class SwimlaneActionsDelegate extends BaseModelDropDownActionDelegate {
    private Swimlane selectedSwimlane;
    private ProcessDefinition definition;
    private SwimlanedNode swimlanedNode;

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        swimlanedNode = getSelection();
        if (swimlanedNode != null) {
            selectedSwimlane = swimlanedNode.getSwimlane();
            definition = swimlanedNode.getProcessDefinition();
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
        List<Swimlane> swimlanes = definition.getSwimlanes();
        for (Swimlane swimlane : swimlanes) {
            Action action = new SetSwimlaneAction();
            action.setText(swimlane.getName());
            if (Objects.equals(selectedSwimlane, swimlane)) {
                action.setChecked(true);
            }
            action.setEnabled(definition.getSwimlaneDisplayMode() == SwimlaneDisplayMode.none);
            ActionContributionItem item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        new MenuItem(menu, SWT.SEPARATOR);
        Action action;
        ActionContributionItem item;
        action = new GotoSwimlaneAction();
        action.setEnabled(!(definition instanceof SubprocessDefinition));
        item = new ActionContributionItem(action);
        item.fill(menu, -1);
        if (swimlanedNode instanceof StartState) {
            if (selectedSwimlane == null && definition.getSwimlaneDisplayMode() == SwimlaneDisplayMode.none) {
                action = new CreateSwimlaneAction();
                action.setEnabled(!(definition instanceof SubprocessDefinition));
                item = new ActionContributionItem(action);
                item.fill(menu, -1);
            }
            if (selectedSwimlane != null) {
                Menu submenu = new Menu(menu);
                MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
                menuItem.setText(Localization.getString("Swimlane.reassignSwimlaneToTaskPerformer"));
                menuItem.setMenu(submenu);
                for (ReassignSwimlaneMode mode : ReassignSwimlaneMode.values()) {
                    action = new SetCheckedReassignSwimlaneToTaskPerformerModeAction(mode);
                    action.setChecked(mode.isChecked(((StartState) swimlanedNode).isReassignSwimlaneToTaskPerformer()));
                    item = new ActionContributionItem(action);
                    item.fill(submenu, -1);
                }
            }
        }
        if (swimlanedNode instanceof TaskState) {
            if (selectedSwimlane != null) {
                Menu submenu = new Menu(menu);
                MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
                menuItem.setText(Localization.getString("Swimlane.reassignSwimlaneToInitializer"));
                menuItem.setMenu(submenu);
                for (ReassignSwimlaneMode mode : ReassignSwimlaneMode.values()) {
                    action = new SetReassignSwimlaneToInitializerModeAction(mode);
                    action.setChecked(mode.isChecked(((TaskState) swimlanedNode).isReassignSwimlaneToInitializer()));
                    item = new ActionContributionItem(action);
                    item.fill(submenu, -1);
                }

                submenu = new Menu(menu);
                menuItem = new MenuItem(menu, SWT.CASCADE);
                menuItem.setText(Localization.getString("Swimlane.reassignSwimlaneToTaskPerformer"));
                menuItem.setMenu(submenu);
                for (ReassignSwimlaneMode mode : ReassignSwimlaneMode.values()) {
                    action = new SetCheckedReassignSwimlaneToTaskPerformerModeAction(mode);
                    action.setChecked(mode.isChecked(((TaskState) swimlanedNode).isReassignSwimlaneToTaskPerformer()));
                    item = new ActionContributionItem(action);
                    item.fill(submenu, -1);
                }
            }

            action = new IgnoreSubstitutionAction();
            action.setChecked(((TaskState) swimlanedNode).isIgnoreSubstitutionRules());
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        if (selectedSwimlane != null && definition.getSwimlaneDisplayMode() == SwimlaneDisplayMode.none) {
            action = new ClearSwimlaneAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
    }

    private void createSwimlane() {
        UpdateSwimlaneNameDialog newSwimlaneDialog = new UpdateSwimlaneNameDialog(definition, null);
        if (newSwimlaneDialog.open() == IDialogConstants.OK_ID) {
            String swimlaneName = newSwimlaneDialog.getName();
            Swimlane newSwimlane = NodeRegistry.getNodeTypeDefinition(Swimlane.class).createElement(definition, false);
            newSwimlane.setName(swimlaneName);
            newSwimlane.setScriptingName(newSwimlaneDialog.getScriptingName());
            definition.addChild(newSwimlane);
            setSwimlane(swimlaneName);
        }
    }

    private void setSwimlane(String swimlaneName) {
        if (swimlaneName != null) {
            Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
            if (swimlane == null) {
                swimlane = definition.getGlobalSwimlaneByName(swimlaneName);
            }
            swimlanedNode.setSwimlane(swimlane);
        } else {
            swimlanedNode.setSwimlane(null);
        }
    }

    private void editSwimlane() {
        ProcessEditorBase editor = getActiveDesignerEditor();
        editor.openPage(1);
        if (swimlanedNode.getSwimlane() != null) {
            editor.select(swimlanedNode.getSwimlane());
        }
    }

    private class SetSwimlaneAction extends Action {
        @Override
        public void run() {
            setSwimlane(getText());
        }
    }

    private class CreateSwimlaneAction extends Action {
        public CreateSwimlaneAction() {
            setText(Localization.getString("Swimlane.createSimpleNew"));
        }

        @Override
        public void run() {
            createSwimlane();
        }
    }

    private class ClearSwimlaneAction extends Action {
        public ClearSwimlaneAction() {
            setText(Localization.getString("Swimlane.clear"));
        }

        @Override
        public void run() {
            setSwimlane(null);
        }
    }

    private class GotoSwimlaneAction extends Action {
        public GotoSwimlaneAction() {
            setText(Localization.getString("Swimlane.gotoEdit"));
        }

        @Override
        public void run() {
            editSwimlane();
        }
    }

    public enum ReassignSwimlaneMode {
        YES(true),
        NO(false),
        DEFAULT(null);
        private final Boolean value;

        ReassignSwimlaneMode(Boolean value) {
            this.value = value;
        }

        public Boolean getValue() {
            return value;
        }

        public boolean isChecked(Boolean param) {
            return Objects.equals(value, param);
        }
    }

    public class SetReassignSwimlaneToInitializerModeAction extends Action {
        private final ReassignSwimlaneMode mode;

        public SetReassignSwimlaneToInitializerModeAction(ReassignSwimlaneMode mode) {
            this.mode = mode;
            setText(Localization.getString(mode.name().toLowerCase()));
        }

        @Override
        public void run() {
            ((TaskState) swimlanedNode).setReassignSwimlaneToInitializer(mode.getValue());
        }
    }

    public class SetCheckedReassignSwimlaneToTaskPerformerModeAction extends Action {
        private final ReassignSwimlaneMode mode;

        public SetCheckedReassignSwimlaneToTaskPerformerModeAction(ReassignSwimlaneMode mode) {
            this.mode = mode;
            setText(Localization.getString(mode.name().toLowerCase()));
        }

        @Override
        public void run() {
            ((FormNode) swimlanedNode).setReassignSwimlaneToTaskPerformer(mode.getValue());
        }
    }

    private class IgnoreSubstitutionAction extends Action {
        public IgnoreSubstitutionAction() {
            setText(Localization.getString("property.ignoreSubstitution"));
        }

        @Override
        public void run() {
            IgnoreSubstitutionCommand command = new IgnoreSubstitutionCommand((TaskState) swimlanedNode);
            executeCommand(command);
        }
    }
}
