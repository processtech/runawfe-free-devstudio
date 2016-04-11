package ru.runa.gpd.formeditor.ftl.filter;

import ru.runa.gpd.formeditor.ftl.ComponentType;

public interface IComponentFilter {

    public boolean disable(ComponentType type);

}
