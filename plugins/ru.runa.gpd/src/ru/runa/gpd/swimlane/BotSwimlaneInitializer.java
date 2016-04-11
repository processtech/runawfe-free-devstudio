package ru.runa.gpd.swimlane;

import java.util.List;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class BotSwimlaneInitializer extends SwimlaneInitializer {
    public static final String BEGIN = "#";
    private String botName = "";

    public BotSwimlaneInitializer() {
    }

    public BotSwimlaneInitializer(String swimlaneConfiguration) {
        Preconditions.checkArgument(swimlaneConfiguration.startsWith(BEGIN), "Invalid configuration");
        botName = swimlaneConfiguration.substring(BEGIN.length());
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    @Override
    public boolean hasReference(Variable variable) {
        return false;
    }

    @Override
    public void onVariableRename(String variableName, String newVariableName) {
    }

    @Override
    public void validate(Swimlane swimlane, List<ValidationError> errors) {
        if (Strings.isNullOrEmpty(botName)) {
            errors.add(ValidationError.createLocalizedError(swimlane, "botInitializer.emptyName"));
        }
    }

    @Override
    public boolean isValid() {
        return !Strings.isNullOrEmpty(botName);
    }

    @Override
    public BotSwimlaneInitializer getCopy() {
        BotSwimlaneInitializer initializer = new BotSwimlaneInitializer();
        initializer.setBotName(botName);
        return initializer;
    }

    @Override
    public String toString() {
        return BEGIN + botName;
    }
}
