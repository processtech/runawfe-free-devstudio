package ru.runa.gpd.extension;

import org.eclipse.jface.viewers.LabelProvider;

public class LocalizationLabelProvider extends LabelProvider {
    // space symbol used for alignment in properties view
    private final boolean useSpacePrefix;

    public LocalizationLabelProvider(boolean useSpacePrefix) {
        this.useSpacePrefix = useSpacePrefix;
    }

    @Override
    public String getText(Object element) {
        return (useSpacePrefix ? " " : "") + LocalizationRegistry.getLabel(super.getText(element));
    }

}
