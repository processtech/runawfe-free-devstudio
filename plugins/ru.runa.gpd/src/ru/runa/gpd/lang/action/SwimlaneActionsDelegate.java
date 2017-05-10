package ru.runa.gpd.lang.action;

import java.util.List;

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
import ru.runa.gpd.editor.gef.command.EnableReassignSwimlaneToInitializerCommand;
import ru.runa.gpd.editor.gef.command.EnableReassignSwimlaneToTaskPerformerCommand;
import ru.runa.gpd.editor.gef.command.IgnoreSubstitutionCommand;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.swimlane.BotSwimlaneInitializer;
import ru.runa.gpd.ui.dialog.UpdateSwimlaneNameDialog;
import ru.runa.gpd.util.SwimlaneDisplayMode;

import com.google.common.base.Objects;

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
            if (Objects.equal(selectedSwimlane, swimlane)) {
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
        if (swimlanedNode instanceof StartState && selectedSwimlane == null && definition.getSwimlaneDisplayMode() == SwimlaneDisplayMode.none) {
            action = new CreateSwimlaneAction();
            action.setEnabled(!(definition instanceof SubprocessDefinition));
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        if (swimlanedNode instanceof TaskState) {
            if (selectedSwimlane != null) {
                action = new EnableReassignSwimlaneToInitializerAction();
                action.setChecked(((TaskState) swimlanedNode).isReassignSwimlaneToInitializerValue());
                item = new ActionContributionItem(action);
                item.fill(menu, -1);
                action = new EnableReassignSwimlaneToTaskPerformerAction();
                action.setChecked(((TaskState) swimlanedNode).isReassignSwimlaneToTaskPerformer());
                item = new ActionContributionItem(action);
                item.fill(menu, -1);
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
            swimlanedNode.setSwimlane(swimlane);
            if (swimlane.getDelegationConfiguration().startsWith(BotSwimlaneInitializer.BEGIN)) {
                swimlanedNode.getNodeRegulationsProperties().setEnabled(false);
            }
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

    private class EnableReassignSwimlaneToInitializerAction extends Action {

        public EnableReassignSwimlaneToInitializerAction() {
            setText(Localization.getString("Swimlane.reassignSwimlaneToInitializerValue"));
        }

        @Override
        public void run() {
            executeCommand(new EnableReassignSwimlaneToInitializerCommand((TaskState) swimlanedNode));
        }
    }

    private class EnableReassignSwimlaneToTaskPerformerAction extends Action {

        public EnableReassignSwimlaneToTaskPerformerAction() {
            setText(Localization.getString("Swimlane.reassignSwimlaneToTaskPerformer"));
        }

        @Override
        public void run() {
            executeCommand(new EnableReassignSwimlaneToTaskPerformerCommand((TaskState) swimlanedNode));
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
