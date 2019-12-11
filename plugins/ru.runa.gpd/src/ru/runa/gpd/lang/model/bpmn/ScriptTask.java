package ru.runa.gpd.lang.model.bpmn;

import java.util.List;

import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class ScriptTask extends Node implements Delegable, IBoundaryEventContainer {
    private boolean isUseExternalStorageOut = false;
    private boolean isUseExternalStorageIn = false;

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

    public boolean isUseExternalStorageOut() {
        return isUseExternalStorageOut;
    }

    public void setUseExternalStorageOut(boolean isUseExternalStorageOut) {
        this.isUseExternalStorageOut = isUseExternalStorageOut;
        this.isUseExternalStorageIn = !isUseExternalStorageOut;
        firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_OUT, !isUseExternalStorageOut, isUseExternalStorageOut);
        firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_IN, !isUseExternalStorageIn, isUseExternalStorageIn);
    }

    public boolean isUseExternalStorageIn() {
        return isUseExternalStorageIn;
    }

    public void setUseExternalStorageIn(boolean isUseExternalStorageIn) {
        this.isUseExternalStorageIn = isUseExternalStorageIn;
        this.isUseExternalStorageOut = !isUseExternalStorageIn;
        firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_IN, !isUseExternalStorageIn, isUseExternalStorageIn);
        firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_OUT, !isUseExternalStorageOut, isUseExternalStorageOut);
    }

}
