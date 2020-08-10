package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.lang.model.AbstractTransition;

public class DottedTransition extends AbstractTransition {

    @Override
    public String getLabel() {
        return getName();
    }

}
