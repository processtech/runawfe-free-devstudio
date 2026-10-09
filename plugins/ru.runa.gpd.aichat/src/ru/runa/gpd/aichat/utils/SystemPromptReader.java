package ru.runa.gpd.aichat.utils;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import ru.runa.gpd.PluginLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SystemPromptReader {
    private static final String PROMPTS_PACKAGE = "prompts/";
    private static final String SYSTEM_PROMPT_FILE_NAME = "system_prompt.txt";
    private static final String FORMULA_ACTION_PACKAGE = "ru/runa/gpd/extension/handler/";
    private static final String FORMULA_ACTION_FILE_NAME = "FormulaCellEditorProvider.help_ru";
    private static final String ADDITIONAL_RULES_FILE_NAME = "additional_rules.txt";

    public static String getSystemPrompt() throws Exception {
        StringBuilder sb = new StringBuilder();
        Bundle aichatBundle = FrameworkUtil.getBundle(SystemPromptReader.class);
        String systemPrompt = readFile(PROMPTS_PACKAGE + SYSTEM_PROMPT_FILE_NAME, aichatBundle);
        Bundle gpdBundle = FrameworkUtil.getBundle(PluginLogger.class);
        String formulaActionHandlerHelp = readFile(FORMULA_ACTION_PACKAGE + FORMULA_ACTION_FILE_NAME, gpdBundle);
        systemPrompt = systemPrompt.replace("{{FormulaCellEditorProvider.help}}", formulaActionHandlerHelp);
        sb.append(systemPrompt).append("\n");
        return sb.toString();
    }

    public static String getAdditionalRules() throws Exception {
        Bundle bundle = FrameworkUtil.getBundle(SystemPromptReader.class);
        return readFile(PROMPTS_PACKAGE + ADDITIONAL_RULES_FILE_NAME, bundle);
    }

    private static String readFile(String fileName, Bundle bundle) throws Exception {
        if (bundle == null) {
            Exception exception = new RuntimeException("Failed to get bundle");
            PluginLogger.logError(exception);
            throw exception;
        }

        URL url = bundle.getEntry(fileName);
        if (url == null) {
            Exception exception = new RuntimeException("Failed to get url with " + fileName);
            PluginLogger.logError(exception);
            throw exception;
        }
        String content;
        try {
            try (InputStream is = url.openStream()) {
                content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }
}
