package ru.runa.gpd.lang.model.bpmn;

import java.util.List;

import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class ScriptTask extends Node implements Delegable, IBoundaryEventContainer {
    private boolean isUseExternalStorage = false;

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

    public boolean isUseExternalStorage() {
        return isUseExternalStorage;
    }

    public void setUseExternalStorage(boolean isUseExternalStorage) {
        this.isUseExternalStorage = isUseExternalStorage;
        firePropertyChange(IS_USE_EXTERNAL_STORAGE, !isUseExternalStorage, isUseExternalStorage);
    }

}
