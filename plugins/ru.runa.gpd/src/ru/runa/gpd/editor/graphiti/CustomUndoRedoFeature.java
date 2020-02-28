package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.ICustomUndoRedoFeature;
import org.eclipse.graphiti.features.context.IContext;

public interface CustomUndoRedoFeature extends ICustomUndoRedoFeature {

    @Override
    default void preUndo(IContext context) {
    }

    @Override
    default void preRedo(IContext context) {
    }

}
