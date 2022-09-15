package ru.runa.gpd.ui.view;

import org.eclipse.jface.viewers.LabelProvider;

public class SearchResultLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        return ((SearchResultItem)element).getLabel();
    }

}
