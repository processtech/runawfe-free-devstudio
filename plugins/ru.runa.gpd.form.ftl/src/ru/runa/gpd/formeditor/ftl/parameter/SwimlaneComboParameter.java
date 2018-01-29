package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;

import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.Swimlane;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class SwimlaneComboParameter extends ComboParameter {

    @Override
    protected List<ComboOption> getOptions(Component component, ComponentParameter parameter) {
        return Lists.transform(FormEditor.getCurrent().getProcessDefinition().getSwimlanes(), new Function<Swimlane, ComboOption>() {

            @Override
            public ComboOption apply(Swimlane swimlane) {
                return new ComboOption(swimlane.getName(), swimlane.getName());
            }
        });
    }

}
