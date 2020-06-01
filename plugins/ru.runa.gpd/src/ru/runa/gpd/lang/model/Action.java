package ru.runa.gpd.lang.model;

import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.LocalizationRegistry;

public class Action extends NamedGraphElement implements Delegable, Describable {

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    @Override
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
    protected void fillCopyCustomFields(GraphElement copy) {
        super.fillCopyCustomFields(copy);
        copy.setDelegationClassName(getDelegationClassName());
        copy.setDelegationConfiguration(getDelegationConfiguration());
    }

}
