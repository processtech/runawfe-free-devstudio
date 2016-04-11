package ru.runa.gpd.formeditor.ftl.parameter.interfaces;

import ru.runa.gpd.formeditor.ftl.ComponentParameter;

public interface IParameterChangeConsumer {

    public void onParameterChange(IParameterChangeCustomer customer, ComponentParameter parameter);

}
