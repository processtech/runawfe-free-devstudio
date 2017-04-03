package ru.runa.gpd.lang.model.jpdl;

import java.util.List;


public interface ActionContainer {

    /**
     * @param index -1 to add at last position
     */
    public void addAction(Action action, int index);

    /**
     * @return index of child
     */
    public int removeAction(Action action);

    public List<? extends Action> getActions();
}
