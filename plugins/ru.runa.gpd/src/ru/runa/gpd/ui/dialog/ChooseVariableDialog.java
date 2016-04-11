package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Variable;

public class ChooseVariableDialog extends ChooseItemDialog<Variable> {

    public ChooseVariableDialog(List<Variable> variables) {
        super(Localization.getString("ChooseVariable.title"), variables, true, Localization.getString("ChooseVariable.message"), true);
        setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Variable) element).getName();
            }
        });
    }

}
