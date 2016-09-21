package ru.runa.gpd.lang.model.jpdl;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.LocalizationRegistry;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.lang.model.IDescribable;
import ru.runa.gpd.lang.model.GraphElement;

public class Action extends GraphElement implements IDelegable, IDescribable {
    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    public String getLabel() {
        String className = getDelegationClassName();
        if (className == null || className.length() == 0) {
            className = Localization.getString("label.new");
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
