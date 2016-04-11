package ru.runa.gpd.ui.custom;

import java.util.List;

public abstract class DragAndDropAdapter<T extends Object> {

    public void onDrop(T beforeElement, List<T> elements) {
        for (T element : elements) {
            onDropElement(beforeElement, element);
        }
    }
    
    public void onDropElement(T beforeElement, T element) {
        
    }

}
