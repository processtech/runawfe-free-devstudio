package ru.runa.gpd.formeditor.ftl.parameter;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;

public class SwimlaneComboParameter extends ComboParameter {

    @Override
    public List<ComboOption> getOptions(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        return Lists.transform(processDefinition.getSwimlanes(), new Function<Swimlane, ComboOption>() {

            @Override
            public ComboOption apply(Swimlane swimlane) {
                return new ComboOption(swimlane.getName(), swimlane.getName());
            }
        });
    }

}
