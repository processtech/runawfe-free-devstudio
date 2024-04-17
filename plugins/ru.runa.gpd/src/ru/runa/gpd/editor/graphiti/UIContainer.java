package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.mm.pictograms.PictogramElement;

public interface UIContainer {
    void pack();

    void update();

    PictogramElement getOwner();

    String getName();
}
