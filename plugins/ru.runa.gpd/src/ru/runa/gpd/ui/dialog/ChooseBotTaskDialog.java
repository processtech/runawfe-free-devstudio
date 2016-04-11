package ru.runa.gpd.ui.dialog;

import java.util.List;

import ru.runa.gpd.Localization;

public class ChooseBotTaskDialog extends ChooseItemDialog<String> {

    public ChooseBotTaskDialog(List<String> botTaskNames) {
        super(Localization.getString("ChooseBotTask.title"), botTaskNames);
    }

}
