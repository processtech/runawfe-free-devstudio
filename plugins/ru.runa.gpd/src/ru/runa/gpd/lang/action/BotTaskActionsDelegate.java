package ru.runa.gpd.lang.action;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.swt.widgets.Menu;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.BotTaskLink;
import ru.runa.gpd.lang.model.BotTaskType;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.ui.dialog.ChooseBotTaskDialog;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.WorkspaceOperations;

import com.google.common.collect.Lists;

public class BotTaskActionsDelegate extends BaseModelDropDownActionDelegate {
    
    private TaskState getTaskStateNotNull() {
        return getSelectionNotNull();
    }

    @Override
    protected void fillMenu(Menu menu) {
        Action action;
        ActionContributionItem item;
        if (getTaskStateNotNull().getBotTaskLink() == null) {
            BotTask botTask = BotCache.getBotTask(getTaskStateNotNull().getSwimlaneBotName(), getTaskStateNotNull().getName());
            if (botTask != null && botTask.getType() == BotTaskType.SIMPLE) {
                // we are supposing that bot task bound by name
                action = new OpenBotTaskAction(getTaskStateNotNull().getName());
                item = new ActionContributionItem(action);
                item.fill(menu, -1);
            } else {
                action = new BindBotTaskWithNodeAction();
                item = new ActionContributionItem(action);
                item.fill(menu, -1);
            }
        } else {
            action = new EditBotTaskWithNodeAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
            action = new UnbindBotTaskFromNodeAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
            action = new OpenBotTaskAction(getTaskStateNotNull().getBotTaskLink().getBotTaskName());
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
    }

    public class BindBotTaskWithNodeAction extends Action {
        public BindBotTaskWithNodeAction() {
            setText(Localization.getString("BotTaskActionsDelegate.bind"));
        }

        @Override
        public void run() {
            try {
                String botTaskName = chooseBotTask();
                if (botTaskName != null) {
                    BotTaskLink botTaskLink = new BotTaskLink();
                    botTaskLink.setBotTaskName(botTaskName);
                    linkWithBotTask(botTaskLink);
                }
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }

    public class UnbindBotTaskFromNodeAction extends Action {
        public UnbindBotTaskFromNodeAction() {
            setText(Localization.getString("BotTaskActionsDelegate.unbind"));
        }

        @Override
        public void run() {
            try {
                linkWithBotTask(null);
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }

    public class EditBotTaskWithNodeAction extends Action {
        public EditBotTaskWithNodeAction() {
            setText(Localization.getString("BotTaskActionsDelegate.edit"));
        }

        @Override
        public void run() {
            try {
                linkWithBotTask(getTaskStateNotNull().getBotTaskLink());
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }

    public class OpenBotTaskAction extends Action {
        private final String botTaskName;

        public OpenBotTaskAction(String botTaskName) {
            this.botTaskName = botTaskName;
            setText(Localization.getString("BotTaskActionsDelegate.gotobottask", botTaskName));
        }

        @Override
        public void run() {
            BotTask botTask = BotCache.getBotTask(getTaskStateNotNull().getSwimlaneBotName(), botTaskName);
            WorkspaceOperations.openBotTask(BotCache.getBotTaskFile(botTask));
        }
    }

    private String chooseBotTask() throws CoreException, IOException {
        List<BotTask> botTasks = BotCache.getBotTasks(getTaskStateNotNull().getSwimlaneBotName());
        List<String> botTaskNames = Lists.newArrayList();
        for (BotTask botTask : botTasks) {
            if (botTask.getType() != BotTaskType.SIMPLE) {
                botTaskNames.add(botTask.getName());
            }
        }
        ChooseBotTaskDialog dialog = new ChooseBotTaskDialog(botTaskNames);
        return dialog.openDialog();
    }

    private void linkWithBotTask(BotTaskLink botTaskLink) {
        getTaskStateNotNull().setBotTaskLink(botTaskLink);
        if (botTaskLink != null) {
            BotTaskUtils.editBotTaskLinkConfiguration(getTaskStateNotNull());
        }
    }
}
