package ru.runa.gpd.property;

import com.google.common.base.Objects;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import ru.runa.gpd.Localization;

public class BooleanPropertyDescriptor extends ComboBoxPropertyDescriptor {
    public static final String[] LABELS = new String[Enum.values().length];
    static {
        for (int i = 0; i < Enum.values().length; i++) {
            LABELS[i] = Localization.getString(Enum.values()[i].labelKey);
        }
    }

    public BooleanPropertyDescriptor(Object id, String displayName) {
        super(id, displayName, LABELS);
    }

    public static enum Enum {
        DEFAULT(null, "default"),
        TRUE(true, "true"),
        FALSE(false, "false");

        private final Boolean value;
        private final String labelKey;

        private Enum(Boolean value, String labelKey) {
            this.value = value;
            this.labelKey = labelKey;
        }

        public Boolean getValue() {
            return value;
        }

        public static Enum getByValueNotNull(Boolean value) {
            for (Enum data : Enum.values()) {
                if (Objects.equal(data.getValue(), value)) {
                    return data;
                }
            }
            throw new RuntimeException("No enum value found by value = '" + value + "'");
        }

    }
}
