package ru.runa.gpd.form;

/**
 * Defines how variables are used in forms.
 * 
 * @since 4.1.0
 * @author Dofs
 */
public enum FormVariableAccess {
    /**
     * Variable used in form as read-only
     */
    READ,
    /**
     * Variable used in edit mode in form
     */
    WRITE,
    /**
     * Variable is used in form but its access cannot be determined definitely
     */
    DOUBTFUL;
}
