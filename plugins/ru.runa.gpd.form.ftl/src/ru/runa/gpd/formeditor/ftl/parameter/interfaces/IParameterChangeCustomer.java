package ru.runa.gpd.formeditor.ftl.parameter.interfaces;

public interface IParameterChangeCustomer {

    public void addParameterChangeListener(IParameterChangeConsumer consumer);

    public void removeParameterChangeListener(IParameterChangeConsumer consumer);

}
