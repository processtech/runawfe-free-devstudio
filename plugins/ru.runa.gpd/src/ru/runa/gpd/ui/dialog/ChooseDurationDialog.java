package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.Duration;

public class ChooseDurationDialog extends ChooseItemDialog<String> {

    public ChooseDurationDialog(ProcessDefinition definition) {
        super(Localization.getString("ChooseVariable.title"), createItems(definition), false, null, true);
    }

    private static List<String> createItems(ProcessDefinition definition) {
        List<String> list = new ArrayList<String>();
        list.add(Duration.CURRENT_DATE_MESSAGE);
        for (Variable variable : definition.getVariables(true, false, Date.class.getName())) {
            list.add(variable.getName());
        }
        return list;
    }
}
