package ru.runa.gpd.swimlane;

import com.google.common.base.Strings;

public class SwimlaneInitializerParser {
    public static SwimlaneInitializer parse(String swimlaneConfiguration) {
        if (Strings.isNullOrEmpty(swimlaneConfiguration)) {
            return new OrgFunctionSwimlaneInitializer();
        }
        if (swimlaneConfiguration.startsWith(BotSwimlaneInitializer.BEGIN)) {
            return new BotSwimlaneInitializer(swimlaneConfiguration);
        }
        if (swimlaneConfiguration.startsWith(RelationSwimlaneInitializer.RELATION_BEGIN)) {
            return new RelationSwimlaneInitializer(swimlaneConfiguration);
        }
        return new OrgFunctionSwimlaneInitializer(swimlaneConfiguration);
    }
}
