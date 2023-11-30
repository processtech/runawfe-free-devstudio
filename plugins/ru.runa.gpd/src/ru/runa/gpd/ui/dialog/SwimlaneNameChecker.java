package ru.runa.gpd.ui.dialog;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.VariableNameChecker;
import ru.runa.gpd.util.IOUtils;

public class SwimlaneNameChecker extends VariableNameChecker {
    public static boolean isValid(String string, ProcessDefinition processDefinition) {
        return VariableNameChecker.isValid(string) && !string.toLowerCase().startsWith(IOUtils.GLOBAL_OBJECT_PREFIX.toLowerCase());
    }
}