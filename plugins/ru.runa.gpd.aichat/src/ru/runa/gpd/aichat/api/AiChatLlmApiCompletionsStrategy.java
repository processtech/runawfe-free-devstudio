package ru.runa.gpd.aichat.api;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.ChatModel;
import com.openai.models.FunctionDefinition;
import com.openai.models.ReasoningEffort;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionTool;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.openai.models.ResponseFormatJsonObject;
import com.openai.models.models.Model;
import org.eclipse.jface.preference.IPreferenceStore;
import ru.runa.gpd.aichat.Activator;
import ru.runa.gpd.aichat.bpmnelements.FormItem;
import ru.runa.gpd.aichat.utils.InsecureSslClient;
import ru.runa.gpd.aichat.sync.AiChatLlmConnectorSettings;
import ru.runa.gpd.PluginLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_ADDITIONAL_RULES_PROMPT_KEY;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_SYSTEM_PROMPT_KEY;

public class AiChatLlmApiCompletionsStrategy implements AiChatLlmApiStrategy {
    private AiChatLlmConnectorSettings settings;
    private String message;
    private List<HashMap<String, Object>> dialogHistory;

    private final String ROLE = "role";
    private final String CONTENT = "content";
    private final String USER = "user";
    private final String ASSISTANT = "assistant";
    private final int MAX_COMPLETION_TOKENS = -1;
    private final double TEMPERATURE = 0.3;

    public AiChatLlmApiCompletionsStrategy(
            AiChatLlmConnectorSettings settings,
            String message,
            List<HashMap<String, Object>> dialogHistory
    ) {
        this.settings = settings;
        this.message = message;
        this.dialogHistory = dialogHistory;
    }

    public AiChatLlmApiCompletionsStrategy(
            AiChatLlmConnectorSettings settings
    ) {
        this.settings = settings;
    }

    @Override
    public String sendRequest() throws Exception {
        OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder()
                    .apiKey(settings.getApiKey())
                    .baseUrl(settings.getUrl());
        if (settings.isAllowSslInsecure()) {
            clientBuilder = InsecureSslClient.getClient(clientBuilder);
        }
        OpenAIClient client = clientBuilder.build();

        List<ChatCompletionMessageParam> messages = new ArrayList<>();

        addSystemPromptToMessages(messages);
        addDialogHistoryToMessages(messages);
        addUserPromptToMessages(messages);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.of(settings.getModelName()))
                .messages(messages)
                .maxCompletionTokens(MAX_COMPLETION_TOKENS)
                .temperature(TEMPERATURE)
                .reasoningEffort(ReasoningEffort.NONE)
                .responseFormat(ResponseFormatJsonObject.builder().build())
                .putAdditionalBodyProperty(
                        "chat_template_kwargs",
                        JsonValue.from(Map.of("enable_thinking", false))
                )
                .build();

        ChatCompletion response = client.chat().completions().create(params);
        PluginLogger.logInfo("raw response " + response);
        return response.choices().get(0).message().content().orElse("");
    }

    private void addSystemPromptToMessages(List<ChatCompletionMessageParam> messages) throws Exception {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String systemPrompt = store.getString(P_LLM_SYSTEM_PROMPT_KEY);
        String additionalRules = store.getString(P_LLM_ADDITIONAL_RULES_PROMPT_KEY);
        messages.add(
                ChatCompletionMessageParam.ofSystem(ChatCompletionSystemMessageParam.builder()
                        .content(systemPrompt + "\n" + additionalRules)
                        .build())
        );
    }

    private void addDialogHistoryToMessages(List<ChatCompletionMessageParam> messages) throws Exception {
        for (HashMap<String, Object> entry: dialogHistory) {
            String role = (String) entry.get(ROLE);
            String content = (String) entry.get(CONTENT);

            if (USER.equals(role)) {
                messages.add(
                        ChatCompletionMessageParam.ofUser(
                                ChatCompletionUserMessageParam.builder()
                                        .content(content)
                                        .build()
                        )
                );
            } else if (ASSISTANT.equals(role)) {
                messages.add(
                        ChatCompletionMessageParam.ofAssistant(
                                ChatCompletionAssistantMessageParam.builder()
                                        .content(content)
                                        .build()
                        )
                );
            }
        }
    }

    private void addUserPromptToMessages(List<ChatCompletionMessageParam> messages) {
        messages.add(
                ChatCompletionMessageParam.ofUser(
                        ChatCompletionUserMessageParam.builder()
                                .content(message)
                                .build()
                )
        );
    }

    @Override
    public void testConnection() throws Exception {
        OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder()
                .apiKey(settings.getApiKey())
                .baseUrl(settings.getUrl());
        if (settings.isAllowSslInsecure()) {
            clientBuilder = InsecureSslClient.getClient(clientBuilder);
        }
        OpenAIClient client = clientBuilder.build();

        List<Model> models = client.models().list().items();
        boolean foundModel = models.stream().anyMatch(model -> model.id().equals(settings.getModelName()));
        if (!foundModel) {
            throw new Exception("Not found such model");
        }
    }
}
