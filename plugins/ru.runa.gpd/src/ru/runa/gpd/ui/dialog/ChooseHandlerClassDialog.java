package ru.runa.gpd.ui.dialog;

import java.util.Comparator;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.LocalizationLabelProvider;
import ru.runa.gpd.extension.LocalizationRegistry;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ChooseHandlerClassDialog extends ChooseItemDialog<String> {

    public ChooseHandlerClassDialog(String type, String defaultClassName) {
        super(Localization.getString("ChooseClass.title"), Lists.transform(HandlerRegistry.getInstance().getAll(type, true), new Function<HandlerArtifact, String>() {

            @Override
            public String apply(HandlerArtifact artifact) {
                return artifact.getName();
            }
        }), true, Localization.getString("ChooseClass.message"), true);
        setLabelProvider(new LocalizationLabelProvider(false));
        setComparator(new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                String l1 = LocalizationRegistry.getLabel(s1);
                String l2 = LocalizationRegistry.getLabel(s2);
                return l1.compareTo(l2);
            }
        });
        setSelectedItem(defaultClassName);
    }

}
