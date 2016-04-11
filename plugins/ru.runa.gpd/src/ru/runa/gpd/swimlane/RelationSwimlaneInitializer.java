package ru.runa.gpd.swimlane;

import java.util.List;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class RelationSwimlaneInitializer extends SwimlaneInitializer {
    private static final char RELATION_INVERSED = '!';
    public static final String RELATION_BEGIN = "@";
    private String relationName = "";
    private String relationParameterVariableName = "";
    private boolean inversed;

    public RelationSwimlaneInitializer() {
    }

    public RelationSwimlaneInitializer(String swimlaneConfiguration) {
        Preconditions.checkArgument(swimlaneConfiguration.startsWith(RELATION_BEGIN), "Invalid configuration");
        int relationNameBegin = RELATION_BEGIN.length();
        if (swimlaneConfiguration.charAt(relationNameBegin) == RELATION_INVERSED) {
            relationNameBegin += 1;
            inversed = true;
        }
        int leftBracketIndex = swimlaneConfiguration.indexOf(LEFT_BRACKET);
        if (leftBracketIndex != -1) {
            relationName = swimlaneConfiguration.substring(relationNameBegin, leftBracketIndex);
            int startIndex = relationName.length() + relationNameBegin + 1;
            relationParameterVariableName = swimlaneConfiguration.substring(startIndex, swimlaneConfiguration.length() - 1);
            if (relationParameterVariableName.contains(LEFT_BRACKET) && relationParameterVariableName.endsWith(RIGHT_BRACKET)) {
                // back compatibility
                leftBracketIndex = relationParameterVariableName.indexOf(LEFT_BRACKET);
                relationParameterVariableName = relationParameterVariableName.substring(leftBracketIndex + 3, relationParameterVariableName.length() - 2);
            }
        } else {
            relationName = swimlaneConfiguration.substring(relationNameBegin);
        }
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        String old = this.relationName;
        this.relationName = relationName;
        firePropertyChange(PROPERTY_NAME, old, relationName);
    }

    public String getRelationParameterVariableName() {
        return relationParameterVariableName;
    }

    public void setRelationParameterVariableName(String relationParameterVariableName) {
        String old = this.relationParameterVariableName;
        this.relationParameterVariableName = relationParameterVariableName;
        firePropertyChange(PROPERTY_RELATION_PARAMETER, old, relationParameterVariableName);
    }

    public boolean isInversed() {
        return inversed;
    }

    public void setInversed(boolean inversed) {
        boolean old = this.inversed;
        this.inversed = inversed;
        firePropertyChange(PROPERTY_RELATION_INVERSED, old, inversed);
    }

    @Override
    public boolean hasReference(Variable variable) {
        return Objects.equal(relationParameterVariableName, variable.getName());
    }

    @Override
    public void onVariableRename(String variableName, String newVariableName) {
        if (Objects.equal(relationParameterVariableName, variableName)) {
            relationParameterVariableName = newVariableName;
        }
    }

    @Override
    public void validate(Swimlane swimlane, List<ValidationError> errors) {
        if (Strings.isNullOrEmpty(relationName)) {
            errors.add(ValidationError.createLocalizedError(swimlane, "relation.emptyName"));
        }
        List<String> variableNames = swimlane.getVariableNames(true, Executor.class.getName());
        if (!variableNames.contains(relationParameterVariableName)) {
            errors.add(ValidationError.createLocalizedError(swimlane, "relation.variableDoesNotExist"));
        }
    }

    @Override
    public boolean isValid() {
        return !Strings.isNullOrEmpty(relationName) && !Strings.isNullOrEmpty(relationParameterVariableName);
    }
    
    @Override
    public RelationSwimlaneInitializer getCopy() {
        RelationSwimlaneInitializer initializer = new RelationSwimlaneInitializer();
        initializer.setRelationName(relationName);
        initializer.setRelationParameterVariableName(relationParameterVariableName);
        initializer.setInversed(inversed);
        return initializer;
    }
    
    @Override
    public String toString() {
        if (relationName == null) {
            // special case without initializer
            return "";
        }
        StringBuffer result = new StringBuffer();
        result.append(RELATION_BEGIN);
        if (inversed) {
            result.append(RELATION_INVERSED);
        }
        result.append(relationName);
        if (!Strings.isNullOrEmpty(relationParameterVariableName)) {
            result.append("(").append(relationParameterVariableName).append(")");
        }
        return result.toString();
    }
}
