package ru.runa.gpd.formeditor.ftl;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.LabelProvider;

public class ComboOption {
    private final String value;
    private final String label;
    private final boolean defaultValue;
    public static final LabelProvider labelProvider = new LabelProvider() {
        @Override
        public String getText(Object element) {
            return ((ComboOption) element).getLabel();
        }
    };

    public ComboOption(String value, String label) {
        this(value, label, null);
    }

    public ComboOption(IConfigurationElement optionElement) {
        this(optionElement.getAttribute("value"), optionElement.getAttribute("name"), optionElement.getAttribute("default"));
    }

    public ComboOption(String value, String label, String defaultValue) {
        this.value = value;
        this.label = label;
        this.defaultValue = Boolean.parseBoolean(defaultValue);
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDefault() {
        return defaultValue;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return value.equals(((ComboOption) o).value);
    }
}
