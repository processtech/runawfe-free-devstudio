package ru.runa.gpd.util;

import com.google.common.base.Strings;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.DelegableUsedDataSourcesProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.ui.custom.Dialogs;

public class ModifyDataSourceAvailabilityChecker {
    private final String dataSourceName;

    public ModifyDataSourceAvailabilityChecker(String dataSourceName) {
        this.dataSourceName = IOUtils.getWithoutExtension(dataSourceName);
    }

    public boolean checkModifyAvailable() {
        final List<BotTask> botTasks = BotCache.getAllBotNames().stream().map(botName -> BotCache.getBotTasks(botName)).flatMap(List::stream)
                .filter(botTask -> !Strings.isNullOrEmpty(botTask.getDelegationClassName()))
                .filter(botTask -> !Strings.isNullOrEmpty(botTask.getDelegationConfiguration())).collect(Collectors.toList());

        final List<BotTask> botTasksUsingDataSource = new ArrayList<>(botTasks.size());
        for (BotTask botTask : botTasks) {
            final DelegableProvider provider = HandlerRegistry.getProvider(botTask.getDelegationClassName());
            if (provider instanceof DelegableUsedDataSourcesProvider) {
                final DelegableUsedDataSourcesProvider usedDataSourcesProvider = (DelegableUsedDataSourcesProvider) provider;
                if (usedDataSourcesProvider.usedDataSourceNames(botTask.getDelegationConfiguration()).contains(dataSourceName)) {
                    botTasksUsingDataSource.add(botTask);
                }
            }
        }

        if (botTasksUsingDataSource.isEmpty()) {
            return true;
        }

        showErrorMessage(botTasksUsingDataSource);
        return false;
    }

    private void showErrorMessage(List<BotTask> botTasksUsingDataSource) {
        final StringBuilder detailsMessage = new StringBuilder();
        detailsMessage.append(Localization.getString("UnableModify.dataSource.detail.message"));
        for (BotTask botTask : botTasksUsingDataSource) {
            detailsMessage.append("\n");
            detailsMessage.append(botTask);
        }
        Dialogs.error(MessageFormat.format(Localization.getString("UnableModify.dataSource.message"), dataSourceName), detailsMessage.toString());
    }

}
