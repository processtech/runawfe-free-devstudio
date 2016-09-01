package ru.runa.gpd.util;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class TransitionOrderNumCellEditorValidator implements ICellEditorValidator {
    private static final int MIN_ORDER_NUM = 1;

    private final int maxValue;

    public TransitionOrderNumCellEditorValidator(int maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public String isValid(Object value) {
        try {
            if (value instanceof String) {
                int intValue = Integer.parseInt((String) value);
                if (intValue < MIN_ORDER_NUM) {
                    return "value less then " + Integer.toString(MIN_ORDER_NUM);
                } else if (intValue > maxValue) {
                    return "value greater then " + Integer.toString(maxValue);
                } else {
                    return null;
                }
            } else {
                return "value is not a String";
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }
}
