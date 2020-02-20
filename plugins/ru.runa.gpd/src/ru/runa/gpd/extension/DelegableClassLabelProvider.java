package ru.runa.gpd.extension;

import org.eclipse.jface.viewers.LabelProvider;

public class DelegableClassLabelProvider extends LabelProvider {
    // space symbol used for alignment in properties view
    private final boolean useSpacePrefix;

    public DelegableClassLabelProvider(boolean useSpacePrefix) {
        this.useSpacePrefix = useSpacePrefix;
    }

    @Override
    public String getText(Object element) {
        String className = super.getText(element);
        String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
        return (useSpacePrefix ? " " : "") + LocalizationRegistry.getLabel(className) + " (" + simpleClassName + ")";
    }

}
