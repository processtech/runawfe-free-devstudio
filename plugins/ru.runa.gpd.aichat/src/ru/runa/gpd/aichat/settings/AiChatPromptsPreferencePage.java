package ru.runa.gpd.aichat.settings;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.aichat.Activator;
import ru.runa.gpd.aichat.Messages;
import ru.runa.gpd.aichat.utils.SystemPromptReader;
import ru.runa.gpd.settings.PrefConstants;

import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_ADDITIONAL_RULES_PROMPT_KEY;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_SYSTEM_PROMPT_KEY;


public class AiChatPromptsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private static final int WIDTH_HINT = 600;
    private static final int HEIGHT_HINT = 150;
    private static final int MINIMUM_WUDTH = 300;
    private static final int MINIMUM_HEIGHT = 80;

    private static String SYSTEM_PROMPT_DEFAULT;
    private static String ADDITIONAL_RULES_PROMPT_DEFAULT;

    private Text systemPromptTextField;
    private Text additionalRulesTextField;

    public AiChatPromptsPreferencePage() {
        super();
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        //setTitle(Messages.getString("pref.aichat.prompts"));
        //noDefaultButton();
    }

    public static String getAdditionalRulesPromptDefault() throws Exception {
        if (ADDITIONAL_RULES_PROMPT_DEFAULT == null) {
            ADDITIONAL_RULES_PROMPT_DEFAULT = SystemPromptReader.getAdditionalRules();
        }
        return ADDITIONAL_RULES_PROMPT_DEFAULT;
    }

    public static String getSystemPromptDefault() throws Exception {
        if (SYSTEM_PROMPT_DEFAULT == null) {
            SYSTEM_PROMPT_DEFAULT = SystemPromptReader.getSystemPrompt();
        }
        return SYSTEM_PROMPT_DEFAULT;
    }

    @Override
    protected Control createContents(Composite composite) {
        Composite container = new Composite(composite, SWT.V_SCROLL);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite textComposite = new Composite(container, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        textComposite.setLayout(layout);
        textComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSystemPromptContainer(textComposite);
        createAdditionalRulesContainer(textComposite);

        try {
            loadPrompts();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return container;
    }

    private void createSystemPromptContainer(Composite parent) {
        Label systemPromptLabel = new Label(parent, SWT.NONE);
        systemPromptLabel.setText(Messages.getString("pref.aichat.prompts.systemPrompt"));
        systemPromptLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        systemPromptTextField = new Text(
                parent,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY
        );

        GridData systemPromptData = new GridData(SWT.FILL, SWT.FILL, true, true);
        systemPromptData.widthHint = WIDTH_HINT;
        systemPromptData.heightHint = HEIGHT_HINT;
        systemPromptData.minimumWidth = MINIMUM_WUDTH;
        systemPromptData.minimumHeight = MINIMUM_HEIGHT;

        systemPromptTextField.setLayoutData(systemPromptData);
    }

    private void createAdditionalRulesContainer(Composite parent) {
        Label additionalRulesLabel = new Label(parent, SWT.NONE);
        additionalRulesLabel.setText(Messages.getString("pref.aichat.prompts.additionalRules"));
        additionalRulesLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        additionalRulesTextField = new Text(
                parent,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
        );

        GridData additionalRulesData = new GridData(SWT.FILL, SWT.FILL, true, true);
        additionalRulesData.widthHint = WIDTH_HINT;
        additionalRulesData.heightHint = HEIGHT_HINT;
        additionalRulesData.minimumWidth = MINIMUM_WUDTH;
        additionalRulesData.minimumHeight = MINIMUM_HEIGHT;

        additionalRulesTextField.setLayoutData(additionalRulesData);
    }

    @Override
    public void init(IWorkbench iWorkbench) {

    }

    @Override
    public boolean performOk() {
        getPreferenceStore().setValue(P_LLM_SYSTEM_PROMPT_KEY, systemPromptTextField.getText());
        getPreferenceStore().setValue(P_LLM_ADDITIONAL_RULES_PROMPT_KEY, additionalRulesTextField.getText());
        return true;
    }

    @Override
    protected void performDefaults() {
        try {
            systemPromptTextField.setText(getSystemPromptDefault());
            getPreferenceStore().setToDefault(P_LLM_SYSTEM_PROMPT_KEY);

            additionalRulesTextField.setText(getAdditionalRulesPromptDefault());
            getPreferenceStore().setToDefault(P_LLM_ADDITIONAL_RULES_PROMPT_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadPrompts() {
        String systemPrompt = getPreferenceStore().getString(P_LLM_SYSTEM_PROMPT_KEY);
        systemPromptTextField.setText(systemPrompt);

        String additionalRules = getPreferenceStore().getString(P_LLM_ADDITIONAL_RULES_PROMPT_KEY);
        additionalRulesTextField.setText(additionalRules);
    }
}
