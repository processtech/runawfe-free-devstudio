package ru.runa.gpd.swimlane;

import java.util.List;

import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.EventSupport;

public abstract class SwimlaneInitializer extends EventSupport implements PropertyNames {
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";

    public abstract void validate(Swimlane swimlane, List<ValidationError> errors);

    public abstract boolean hasReference(Variable variable);

    public abstract void onVariableRename(String variableName, String newVariableName);
    
    public abstract boolean isValid();
    
    public abstract SwimlaneInitializer getCopy();

}
