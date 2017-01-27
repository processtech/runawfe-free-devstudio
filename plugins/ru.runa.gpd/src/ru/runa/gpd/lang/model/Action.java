package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.LocalizationRegistry;

public class Action extends GraphElement implements Delegable, Describable {
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
        Action copy = (Action) super.getCopy(parent);
        copy.setDelegationClassName(getDelegationClassName());
        copy.setDelegationConfiguration(getDelegationConfiguration());
        return copy;
    }

}
