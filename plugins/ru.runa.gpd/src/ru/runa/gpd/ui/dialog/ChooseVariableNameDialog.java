package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.Localization;
import ru.runa.wfe.execution.dto.WfProcess;

public class ChooseVariableNameDialog extends ChooseItemDialog<String> {

    public ChooseVariableNameDialog(List<String> variableNames) {
        super(Localization.getString("ChooseVariable.title"), variableNames, true, Localization.getString("ChooseVariable.message"), true);
        setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (WfProcess.SELECTED_TRANSITION_KEY.equals(element)) {
                    return "* " + Localization.getString("RUNAWFE_SELECTED_TRANSITION");
                }
                return super.getText(element);
            }
        });
    }

}
