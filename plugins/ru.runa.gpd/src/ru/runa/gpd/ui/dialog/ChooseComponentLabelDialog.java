package ru.runa.gpd.ui.dialog;

import java.util.List;
import org.eclipse.jface.viewers.LabelProvider;
import ru.runa.gpd.Localization;

public class ChooseComponentLabelDialog extends ChooseItemDialog<String> {

    public ChooseComponentLabelDialog(List<String> componentNames) {
        super(Localization.getString("ChooseComponent.title"), componentNames, false, Localization.getString("ChooseComponent.message"), true);
        setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return super.getText(element);
            }
        });
    }

}
