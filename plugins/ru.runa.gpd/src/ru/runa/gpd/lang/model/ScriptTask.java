package ru.runa.gpd.lang.model;

import java.util.List;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.LocalizationRegistry;

public class ScriptTask extends Node implements Delegable {

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
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

    @Override
    public String toString() {
        return getLabel();
    }
    
}
