package ru.runa.gpd.aichat.api;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.aichat.Messages;
import ru.runa.gpd.aichat.sync.AiChatLlmConnectorSettings;
import ru.runa.gpd.aichat.sync.LlmApiType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AiBpmnExecutor extends Thread {
    private static final ObjectMapper OBJECT_MAPPER;

    private String processDescription;
    private Callback callback;
    private List<HashMap<String, Object>> dialogHistory;
    private String currentBpmnXml;
    private String currentVariablesXml;

    static {
        JsonFactory factory = JsonFactory.builder()
                .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                .build();
        OBJECT_MAPPER = new ObjectMapper(factory);
    }

    public interface Callback {
        void onSuccess(String bpmnXml, String variablesXml, String llmCommands, String shortAnswer);
        void onError(Exception e);
    }

    public AiBpmnExecutor(String processDescription, List<HashMap<String, Object>> dialogHistory, String currentBpmnXml, String currentVariablesXml, Callback callback) {
        this.processDescription = processDescription;
        this.dialogHistory = dialogHistory;
        this.currentBpmnXml = currentBpmnXml;
        this.currentVariablesXml = currentVariablesXml;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            JsonNode commands = sendRequestToLlm(this.processDescription, this.dialogHistory);
            BpmnResult bpmnResult = getBpmnFromCommands(commands);
            Display.getDefault().asyncExec(
                    () -> callback.onSuccess(
                            bpmnResult.bpmnXml,
                            bpmnResult.variablesXml,
                            commands.toPrettyString(),
                            bpmnResult.shortAnswer
                    )
            );
        } catch (Exception exc) {
            Display.getDefault().asyncExec(() -> callback.onError(exc));
        }
    }

    private JsonNode sendRequestToLlm(String message, List<HashMap<String, Object>> dialogHistory) throws Exception {
        AiChatLlmConnectorSettings settings = AiChatLlmConnectorSettings.loadSelected();
        AiChatLlmApi api = new AiChatLlmApi();
        if (Objects.equals(settings.getApiType(), LlmApiType.COMPLETIONS.getName())) {
            api.setStrategy(new AiChatLlmApiCompletionsStrategy(settings, message, dialogHistory));
        } else if (Objects.equals(settings.getApiType(), LlmApiType.RESPONSES.getName())) {
            api.setStrategy(new AiChatLlmApiResponsesStrategy());
        }
        String response = api.sendRequest();
        PluginLogger.logInfo("response " + response);
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        String json = response.substring(start, end + 1);
        return OBJECT_MAPPER.readTree(json);
    }

    private BpmnResult getBpmnFromCommands(JsonNode commandsObject) throws Exception {
        if (commandsObject.get("instructions").isEmpty()) {
            throw new Exception(Messages.getString("misunderstood_prompt.aichat.error"));
        }
        IBpmnBuilder mcpBpmnClient = new BpmnBuilder();
        parseDialogHistory(mcpBpmnClient);
        List<JsonNode> commands = sortCommandsByExecuteOrder(commandsObject);
        executeCommands(commands, mcpBpmnClient);

        mcpBpmnClient.autoLayout();

        String variablesXmlContent = mcpBpmnClient.getVariablesXml();
        if (variablesXmlContent == null) {
            throw new Exception("Failed to get variables xml");
        }

        String bpmnXmlContent = mcpBpmnClient.exportXml();
        if (bpmnXmlContent == null) {
            throw new Exception("Failed to get bpmn xml");
        }

        String shortAnswer = commandsObject.get("message").asText();
        BpmnResult bpmnResult = new BpmnResult(bpmnXmlContent, variablesXmlContent, shortAnswer);
        return bpmnResult;
    }

    private void parseDialogHistory(IBpmnBuilder mcpBpmnClient) throws Exception {
        String diagramName = UUID.randomUUID().toString();
        JsonNode params = OBJECT_MAPPER.createObjectNode()
                .put("name", diagramName)
                .put("type", "process");
        mcpBpmnClient.newBpmn(params);
        for (HashMap<String, Object> dialog: dialogHistory) {
            if (!dialog.get("role").equals("assistant")) {
                continue;
            }
            String content = (String) dialog.get("content");
            JsonNode commandsObject = OBJECT_MAPPER.readTree(content);
            List<JsonNode> commands = sortCommandsByExecuteOrder(commandsObject);
            executeCommands(commands, mcpBpmnClient);
        }
    }

    private List<JsonNode> sortCommandsByExecuteOrder(JsonNode commandsObject) {
        List<JsonNode> commands = new ArrayList<>();
        for (JsonNode command: commandsObject.get("instructions")) {
            commands.add(command);
        }
        commands.sort(
                (command1, command2) -> {
                    String commandName1 = command1.get("command").asText();
                    String commandName2 = command2.get("command").asText();
                    BpmnCommand aiCommand1 = BpmnCommand.fromString(commandName1)
                            .orElseThrow(() -> new IllegalArgumentException("No such command: " + commandName1));
                    BpmnCommand aiCommand2 = BpmnCommand.fromString(commandName2)
                            .orElseThrow(() -> new IllegalArgumentException("No such command: " + commandName2));

                    return Integer.compare(aiCommand1.getExecuteOrder(), aiCommand2.getExecuteOrder());
                });
        return commands;
    }

    private void executeCommands(List<JsonNode> commands, IBpmnBuilder client) throws Exception {
        for (JsonNode command: commands) {
            String commandName = command.get("command").asText();
            JsonNode params = command.get("params");
            Optional<BpmnCommand> aiCommand = BpmnCommand.fromString(commandName);
            if (aiCommand.isEmpty()) {
                PluginLogger.logError("No such command: " + commandName, new Exception());
                continue;
            }
            aiCommand.get().execute(client, params);
        }
    }

    private static class BpmnResult {
        String bpmnXml;
        String variablesXml;
        String shortAnswer;

        BpmnResult(String bpmnXml, String variablesXml, String shortAnswer) {
            this.bpmnXml = bpmnXml;
            this.variablesXml = variablesXml;
            this.shortAnswer = shortAnswer;
        }
    }
}