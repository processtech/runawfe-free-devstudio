package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;

import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

// use VariableFinderParameter
@Deprecated
public class VariableComboParameter extends ComboParameter {

    @Override
    protected List<ComboOption> getOptions(Component component, ComponentParameter parameter) {
        return Lists.transform(FormEditor.getCurrent().getVariableNames(parameter.getVariableTypeFilter()), new Function<String, ComboOption>() {

            @Override
            public ComboOption apply(String string) {
                return new ComboOption(string, string);
            }
        });
    }

}
