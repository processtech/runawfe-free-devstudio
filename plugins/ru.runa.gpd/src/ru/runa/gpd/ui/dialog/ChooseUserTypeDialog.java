package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.VariableUserType;

public class ChooseUserTypeDialog extends ChooseItemDialog<VariableUserType> {

    public ChooseUserTypeDialog(List<VariableUserType> userTypes) {
        super(Localization.getString("ChooseUserType.title"), userTypes, false, Localization.getString("ChooseUserType.message"), true);
        setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((VariableUserType) element).getName();
            }
        });
    }

}
