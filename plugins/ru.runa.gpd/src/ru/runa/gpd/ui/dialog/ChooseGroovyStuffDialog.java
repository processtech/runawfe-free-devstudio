package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.GroovyStuff;
import ru.runa.gpd.util.GroovyStuff.Item;

public class ChooseGroovyStuffDialog extends ChooseItemDialog<Item> {

    public ChooseGroovyStuffDialog(GroovyStuff type) {
        super(Localization.getString("Choose." + type.name() + ".title"), type.getAll(), true, Localization.getString("Choose." + type.name() + ".message"), false);
        setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Item) element).getLabel();
            }
        });
    }

}
