package ru.runa.gpd.aichat.view;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.aichat.Messages;
import ru.runa.gpd.aichat.converter.BizagiBpmnImporter;
import ru.runa.gpd.aichat.api.AiBpmnExecutor;
import ru.runa.gpd.ui.view.ProcessExplorerTreeView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static ru.runa.gpd.lang.par.ParContentProvider.PROCESS_DEFINITION_FILE_NAME;

public class AiChatView extends ViewPart {

    public static final String VIEW_ID = "ru.runa.gpd.ui.view.aichatview";
    private static final int INPUT_TEXT_HEIGHT_HINT = 60;
    private static final String AI_CHAT_FILE_NAME = "aichat.txt";
    private static final String CURRENT_BPMN_XML_FILE_NAME = "current_bpmn_xml.txt";
    private static final String CURRENT_VARIABLES_XML_FILE_NAME = "current_variables_xml.txt";
    private static final String BPMN_FILE_SUFFIX = ".bpmn";
    private static final String BPMN_TEMP_FILE_NAME = "bpmn_1";
    private static final String DIALOG_HISTORY_FILE_NAME = "dialog_history.json";
    private static final String ROLE_NAME = "role";
    private static final String USER_ROLE_NAME = "user";
    private static final String ASSISTANT_ROLE_NAME = "assistant";
    private static final String CONTENT_NAME = "content";
    private static final String MESSAGES_NAME = "messages";

    private Text inputText;
    private Text outputText;
    private Button sendButton;
    private IFile aiChatFile;
    private IContainer currentContainer;
    private Button settingsButton;

    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));

        outputText = new Text(container, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
        outputText.setLayoutData(new GridData(GridData.FILL_BOTH));

        inputText = new Text(container, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
        GridData inputData = new GridData(GridData.FILL_HORIZONTAL);
        inputData.heightHint = INPUT_TEXT_HEIGHT_HINT;
        inputText.setLayoutData(inputData);

        Composite buttonContainer = new Composite(container, SWT.NONE);
        buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        buttonContainer.setLayout(new GridLayout(2, false));

        sendButton = new Button(buttonContainer, SWT.PUSH);
        sendButton.setText(Messages.getString("send_button.aichat.label"));
        sendButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        sendButton.setEnabled(false);
        sendButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    sendMessage();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        settingsButton = new Button(buttonContainer, SWT.PUSH);
        settingsButton.setText(Messages.getString("settings_button.aichat.label"));
        settingsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        settingsButton.setEnabled(true);
        settingsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    openSettingsDialog();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService()
                .addSelectionListener((part, selection) -> selectionHandler());
    }

    private void openSettingsDialog() {
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                Display.getCurrent().getActiveShell(),
                "gpd.pref.connector.aichat",
                null, //new String[] { "gpd.pref.connector.aichat" },
                null
        );

        if (dialog != null) {
            dialog.open();
        }
    }

    private void selectionHandler() {
        IContainer newContainer = getSelectedProject();
        if (newContainer == null) {
            outputText.setText("");
            sendButton.setEnabled(false);
            currentContainer = null;
            return;
        }

        sendButton.setEnabled(true);

        if (currentContainer != null && currentContainer.equals(newContainer)) {
            return;
        }

        currentContainer = newContainer;

        aiChatFile = loadChatForContainer(newContainer);
    }

    private IFile loadChatForContainer(IContainer processContainer) {
        IFile file = processContainer.getFile(new Path(AI_CHAT_FILE_NAME));
        try {
            if (!file.exists()) {
                file.create(new ByteArrayInputStream(new byte[0]), true, null);
            }

            InputStream is = file.getContents();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();

            outputText.setText("");
            outputText.append(content.toString());

        } catch (Exception e) {
            PluginLogger.logError(e.getMessage(), e);
        }
        return file;
    }

    private void sendMessage() throws Exception {
        String message = inputText.getText();
        if (message == null || message.length() == 0) {
            return;
        }

        outputText.append(Messages.getString("you.aichat.label") + ": " + message + "\n\n");
        inputText.setText("");
        outputText.append(Messages.getString("ai.aichat.label") + ": " + Messages.getString("thinking.aichat.label") + "...\n\n");
        sendButton.setEnabled(false);

        List<HashMap<String, Object>> dialogHistory = getDialogHistoryFromFile(currentContainer);
        String currentBpmnXml = getFileContent(currentContainer, CURRENT_BPMN_XML_FILE_NAME);
        String currentVariablesXml = getFileContent(currentContainer, CURRENT_VARIABLES_XML_FILE_NAME);
        PluginLogger.logInfo("currentVariablesXml " + currentVariablesXml);
        AiBpmnExecutor aiBpmnExecutor = new AiBpmnExecutor(message, dialogHistory, currentBpmnXml, currentVariablesXml, new AiBpmnExecutor.Callback() {
            public void onSuccess(String bpmnXml, String variablesXml, String llmCommands, String shortAnswer) {
                try {
                    createProcessFiles(currentContainer, bpmnXml, variablesXml);
                    showOutputMessage(shortAnswer);
                    saveDialogHistoryInFile(currentContainer, dialogHistory, message, llmCommands);
                    saveContentInProcessContainerFile(currentContainer, bpmnXml, CURRENT_BPMN_XML_FILE_NAME);
                    saveContentInProcessContainerFile(currentContainer, variablesXml, CURRENT_VARIABLES_XML_FILE_NAME);
                    currentContainer.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }

            public void onError(Exception e) {
                PluginLogger.logError(e.getMessage(), e);
                showOutputMessage(e.getMessage());
            }
        });
        aiBpmnExecutor.start();
    }

    private String getFileContent(IContainer processContainer, String fileName) throws CoreException, IOException {
        IFile file = processContainer.getFile(new Path(fileName));
        if (!file.exists()) {
            file.create(new ByteArrayInputStream(new byte[0]), true, null);
            return null;
        } else {
            InputStream is = file.getContents();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            return content.toString();
        }
    }


    private void saveContentInProcessContainerFile(IContainer processContainer, String content, String fileName) {
        IFile file = processContainer.getFile(new Path(fileName));
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            if (!file.exists()) {
                file.create(is, true, null);
            } else {
                file.setContents(is, true, false, null);
            }
        } catch (Exception e) {
            PluginLogger.logError(e.getMessage(), e);
        }
    }

    private List<HashMap<String, Object>> getDialogHistoryFromFile(IContainer processContainer) {
        List<HashMap<String, Object>> dialogHistory = new ArrayList<>();
        IFile file = processContainer.getFile(new Path(DIALOG_HISTORY_FILE_NAME));
        try {
            if (!file.exists()) {
                file.create(new ByteArrayInputStream(new byte[0]), true, null);
            } else {
                if (file.getLocation().toFile().length() == 0) {
                    return dialogHistory;
                }

                InputStream is = file.getContents();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder content = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();

                JsonFactory factory = JsonFactory.builder()
                        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                        .build();
                ObjectMapper mapper = new ObjectMapper(factory);
                JsonNode json = mapper.readTree(content.toString());
                for (JsonNode node: json.get("messages")) {
                    HashMap<String, Object> dialogElement = new HashMap<>();
                    dialogElement.put("role", node.get("role").asText());
                    dialogElement.put("content", node.get("content").asText());
                    dialogHistory.add(dialogElement);
                }

            }
        } catch (Exception e) {
            PluginLogger.logError(e.getMessage(), e);
        }
        return dialogHistory;
    }

    private void saveDialogHistoryInFile(IContainer processContainer, List<HashMap<String, Object>> dialogHistory, String message, String llmCommands) {
        IFile file = processContainer.getFile(new Path(DIALOG_HISTORY_FILE_NAME));
        try {
            HashMap<String, Object> userContent = new HashMap<>();
            userContent.put(ROLE_NAME, USER_ROLE_NAME);
            userContent.put(CONTENT_NAME, message);

            HashMap<String, Object> assistantContent = new HashMap<>();
            assistantContent.put(ROLE_NAME, ASSISTANT_ROLE_NAME);
            assistantContent.put(CONTENT_NAME, llmCommands);

            dialogHistory.addAll(List.of(userContent, assistantContent));

            HashMap<String, Object> wrapper = new HashMap<>();
            wrapper.put(MESSAGES_NAME, dialogHistory);

            JsonFactory factory = JsonFactory.builder()
                    .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                    .build();

            ObjectMapper mapper = new ObjectMapper(factory);

            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrapper);

            InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            if (!file.exists()) {
                file.create(is, true, null);
            } else {
                file.setContents(is, true, false, null);
            }
        } catch (Exception e) {
            PluginLogger.logError(e.getMessage(), e);
        }
    }

    private void createProcessFiles(final IContainer dstFolder, final String bpmnXml, final String variablesXml) throws IOException {
        Display.getDefault().asyncExec(() -> {
            try {
                final File tempFile = createTempBpmnFile(bpmnXml);
                PluginLogger.logInfo("bpmnXml: " + bpmnXml);
                PluginLogger.logInfo("variablesXml: " + variablesXml);
                if (tempFile != null && tempFile.exists()) {
                    IFolder processFolder = BizagiBpmnImporter.go(dstFolder, tempFile.getAbsolutePath(), false, false, variablesXml);
//                    if (processFolder != null && processFolder.exists()) {
//                        VariablesXmlMerger.start(processFolder, variablesXml);
//                    }
                } else {
                    PluginLogger.logError("Error: temp file " + BPMN_TEMP_FILE_NAME + BPMN_FILE_SUFFIX + " doesn't exist", new Exception());
                }
            } catch (Exception e) {
                PluginLogger.logError(e.getMessage(), e);
            }
        });
    }

    private void showOutputMessage(final String message) {
        Display.getDefault().asyncExec(() -> {
            String currentText = outputText.getText();
            String searchText = Messages.getString("ai.aichat.label") + ": " + Messages.getString("thinking.aichat.label") + "...";
            int lastNewLine = currentText.lastIndexOf(searchText);
            if (lastNewLine > 0) {
                currentText = currentText.substring(0, lastNewLine);
                outputText.setText(currentText);
            }
            outputText.append(Messages.getString("ai.aichat.label") + ": " + message + "\n\n");
            outputText.setSelection(outputText.getText().length());
            InputStream is = new ByteArrayInputStream(outputText.getText().getBytes(StandardCharsets.UTF_8));
            try {
                aiChatFile.setContents(is, true, false, null);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            sendButton.setEnabled(true);
        });
    }

    private File createTempBpmnFile(String content) throws IOException {
        File tempFile = File.createTempFile(BPMN_TEMP_FILE_NAME, BPMN_FILE_SUFFIX);

        FileWriter writer = new FileWriter(tempFile);
        writer.write(content);
        writer.close();

        tempFile.deleteOnExit();

        return tempFile;
    }

    private IContainer getSelectedProject() {
        final IContainer[] result = new IContainer[1];
        Display.getDefault().syncExec(() -> {
            try {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window == null) {
                    return;
                }

                IWorkbenchPage page = window.getActivePage();
                if (page == null) {
                    return;
                }

                IEditorPart activeEditor = page.getActiveEditor();
                if (activeEditor != null) {
                    IFile file = activeEditor.getEditorInput().getAdapter(IFile.class);
                    if (file != null) {
                        IContainer container = file.getParent();
                        if (container != null) {
                            IFile processDefFile = container.getFile(new Path(PROCESS_DEFINITION_FILE_NAME));
                            if (processDefFile.exists()) {
                                result[0] = container;
                                return;
                            }
                        }
                    }
                }

                ProcessExplorerTreeView explorerView = (ProcessExplorerTreeView) page.findView(ProcessExplorerTreeView.ID);
                if (explorerView == null) {
                    return;
                }

                IStructuredSelection selection = (IStructuredSelection) explorerView.getSite().getSelectionProvider().getSelection();
                Object element = selection.getFirstElement();

                if (element instanceof IContainer) {
                    result[0] = (IContainer) element;
                    if (result[0].getFile(new Path(AI_CHAT_FILE_NAME)).exists()) {
                        return;
                    }

                    if ((result[0].members().length == 1) && (result[0].getFile(new Path(PROCESS_DEFINITION_FILE_NAME)).exists())) {
                        return;
                    }

                    result[0] = null;
                }
            } catch (Exception e) {
                PluginLogger.logError("Error getting project", e);
            }
        });
        return result[0];
    }

    @Override
    public void setFocus() {
        if (inputText != null) {
            inputText.setFocus();
        }
    }
}