package ru.runa.gpd.formeditor.ftl;

import org.eclipse.core.runtime.IConfigurationElement;

public class ComboOption {
    private final String value;
    private final String label;
    private final String _default;

    public ComboOption(String value, String label) {
        this(value, label, null);
    }

    public ComboOption(IConfigurationElement optionElement) {
        this(optionElement.getAttribute("value"), optionElement.getAttribute("name"), optionElement.getAttribute("default"));
    }

    public ComboOption(String value, String label, String _default) {
        this.value = value;
        this.label = label;
        this._default = _default;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDefault() {
        return _default != null && Boolean.parseBoolean(_default);
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
