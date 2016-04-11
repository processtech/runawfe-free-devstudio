package ru.runa.gpd.formeditor.ftl;

import org.eclipse.core.runtime.IConfigurationElement;

public class ComboOption {
    private final String value;
    private final String label;

    public ComboOption(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public ComboOption(IConfigurationElement optionElement) {
        this.value = optionElement.getAttribute("value");
        this.label = optionElement.getAttribute("name");
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
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
