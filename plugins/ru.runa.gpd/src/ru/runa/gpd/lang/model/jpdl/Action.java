package ru.runa.gpd.lang.model.jpdl;

import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.LocalizationRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Describable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class Action extends NamedGraphElement implements Delegable, Describable {

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    public String getLabel() {
        String className = getDelegationClassName();
        if (className == null || className.length() == 0) {
            return super.getLabel();
        }
        return LocalizationRegistry.getLabel(className);
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public Action getCopy(GraphElement parent) {
        return (Action) super.getCopy(parent);
    }

}
