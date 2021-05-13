package ru.runa.gpd.settings;

import com.google.common.collect.Maps;
import java.util.Map;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.GefEntry;
import ru.runa.gpd.editor.graphiti.GraphitiEntry;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;

public class LanguageElementPreferencePage extends FieldEditorPreferencePage implements PrefConstants {
    private static final String OVERRIDE_LOCALIZATION_SUFFIX = "override";
    private static final String OVERRIDE_PROPERTY_SUFFIX = ".override";
    private final String id;
    private final NodeTypeDefinition definition;
    private final Language language;
    private final Map<BooleanFieldEditor, FieldEditor> overrideFieldEditors = Maps.newHashMap();

    public LanguageElementPreferencePage(String id, NodeTypeDefinition definition, Language language) {
        super(definition.getLabel(), GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        this.id = id;
        this.definition = definition;
        this.language = language;
    }

    @Override
    public void createFieldEditors() {
        GraphElement element;
        try {
            element = definition.getModelClass().newInstance();
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog(e.toString());
            return;
        }
        init(element);
        if (element instanceof NamedGraphElement) {
            addField(new StringFieldEditor(getKey(P_LANGUAGE_NODE_NAME_PATTERN), Localization.getString("pref.language.node.name.pattern"),
                    getFieldEditorParent()));
        }
        if (!(element instanceof Transition)) {
            IntegerFieldEditor widthEditor = new IntegerFieldEditor(getKey(P_LANGUAGE_NODE_WIDTH),
                    Localization.getString("pref.language.node.width"), getFieldEditorParent());
            IntegerFieldEditor heightEditor = new IntegerFieldEditor(getKey(P_LANGUAGE_NODE_HEIGHT),
                    Localization.getString("pref.language.node.height"), getFieldEditorParent());
            boolean fixedSize = language == Language.JPDL ? (definition.getGefEntry() != null && definition.getGefEntry().isFixedSize())
                    : language == Language.BPMN ? (definition.getGraphitiEntry() != null && definition.getGraphitiEntry().isFixedSize()) : true;
            widthEditor.setEnabled(!fixedSize, getFieldEditorParent());
            heightEditor.setEnabled(!fixedSize, getFieldEditorParent());
            addField(widthEditor);
            addField(heightEditor);
        }
        if (element instanceof TaskState) {
            addField(new BooleanFieldEditor(getKey(P_LANGUAGE_SWIMLANE_INITIALIZER),
                    Localization.getString("Swimlane.reassignSwimlaneToInitializer"), getFieldEditorParent()));
            addField(new BooleanFieldEditor(getKey(P_LANGUAGE_SWIMLANE_PERFORMER),
                    Localization.getString("Swimlane.reassignSwimlaneToTaskPerformer"), getFieldEditorParent()));
            addField(new BooleanFieldEditor(getKey(P_LANGUAGE_TASK_STATE_ASYNC_INPUT_DATA),
                    Localization.getString("TaskNode.inputDataAllowedInAsyncMode"), getFieldEditorParent()));
        }
        if (element instanceof Subprocess) {
            addField(new BooleanFieldEditor(getKey(P_LANGUAGE_SUB_PROCESS_ASYNC_INPUT_DATA),
                    Localization.getString("Subprocess.inputDataAllowedInAsyncSubprocess"), getFieldEditorParent()));
        }
        if (language == Language.BPMN) {
            switch (definition.getBpmnElementName()) {
            case "scriptTask":
            case "userTask":
            case "multiTask":
            case "multiProcess":
            case "subProcess":
                addOverrideFieldEditor(P_BPMN_BACKGROUND_COLOR);
                addOverrideFieldEditor(P_BPMN_FOREGROUND_COLOR);
                break;
            case "lane":
                addOverrideFieldEditor(P_BPMN_FONT);
                addOverrideFieldEditor(P_BPMN_FONT_COLOR);
                addOverrideFieldEditor(P_BPMN_BACKGROUND_COLOR);
                addOverrideFieldEditor(P_BPMN_FOREGROUND_COLOR);
                addOverrideFieldEditor(P_BPMN_LINE_WIDTH);
                break;
            case "exclusiveGateway":
                addField(new BooleanFieldEditor(getKey(P_BPMN_MARK_DEFAULT_TRANSITION),
                        Localization.getString("ExclusiveGateway.markDefaultTransition"), getFieldEditorParent()));
                addField(new StringFieldEditor(getKey(P_BPMN_DEFAULT_TRANSITION_NAMES),
                        Localization.getString("ExclusiveGateway.defaultTransitionNames"), getFieldEditorParent()));
                break;
            case StyleUtil.TEXT_ANNOTATION_BPMN_NAME:
            case StyleUtil.TRANSITION_BPMN_NAME:
                addOverrideFieldEditor(P_BPMN_FONT);
                addOverrideFieldEditor(P_BPMN_FONT_COLOR);
                addOverrideFieldEditor(P_BPMN_FOREGROUND_COLOR);
                addOverrideFieldEditor(P_BPMN_LINE_WIDTH);
                break;
            default:
                break;
            }
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        updateOverridesFieldEditors();
    }

    private void init(GraphElement element) {
        // TODO this section does not works until settings dialog opened
        // it is better to move them to ru.runa.gpd.settings.PreferenceInitializer
        IPreferenceStore store = getPreferenceStore();
        if (element instanceof NamedGraphElement) {
            store.setDefault(getKey(P_LANGUAGE_NODE_NAME_PATTERN), definition.getLabel());
        }
        if (language == Language.JPDL && definition.getGefEntry() != null) {
            GefEntry entry = definition.getGefEntry();
            Dimension size = entry.getDefaultSystemSize();
            store.setDefault(getKey(P_LANGUAGE_NODE_WIDTH), size.width);
            store.setDefault(getKey(P_LANGUAGE_NODE_HEIGHT), size.height);
        }
        if (language == Language.BPMN && definition.getGraphitiEntry() != null) {
            GraphitiEntry entry = definition.getGraphitiEntry();
            Dimension size = entry.getDefaultSystemSize();
            store.setDefault(getKey(P_LANGUAGE_NODE_WIDTH), size.width);
            store.setDefault(getKey(P_LANGUAGE_NODE_HEIGHT), size.height);
        }
        if (element instanceof TaskState) {
            store.setDefault(getKey(P_LANGUAGE_SWIMLANE_INITIALIZER), false);
            store.setDefault(getKey(P_LANGUAGE_SWIMLANE_PERFORMER), true);
            store.setDefault(getKey(P_LANGUAGE_TASK_STATE_ASYNC_INPUT_DATA), false);
        }
        if (language == Language.BPMN) {
            switch (definition.getBpmnElementName()) {
            case "exclusiveGateway":
                store.setDefault(getKey(P_BPMN_DEFAULT_TRANSITION_NAMES), "");
                break;
            case StyleUtil.TEXT_ANNOTATION_BPMN_NAME:
                store.setDefault(getKey(P_BPMN_FOREGROUND_COLOR) + OVERRIDE_PROPERTY_SUFFIX, true);
                break;
            case StyleUtil.TRANSITION_BPMN_NAME:
                store.setDefault(getKey(P_BPMN_FOREGROUND_COLOR) + OVERRIDE_PROPERTY_SUFFIX, true);
                store.setDefault(getKey(P_BPMN_LINE_WIDTH) + OVERRIDE_PROPERTY_SUFFIX, true);
                break;
            default:
                break;
            }
        }
    }

    private void addOverrideFieldEditor(String propertyNameSuffix) {
        String propertyName = LanguageElementPreferenceNode.getBpmnPropertyName(definition.getBpmnElementName(), propertyNameSuffix);
        String enabledPropertyName = propertyName + OVERRIDE_PROPERTY_SUFFIX;
        BooleanFieldEditor overrideFieldEditor = new BooleanFieldEditor(enabledPropertyName,
                Localization.getString(BpmnPreferencePage.LOCALIZATION_PREFIX + OVERRIDE_LOCALIZATION_SUFFIX), getFieldEditorParent());
        addField(overrideFieldEditor);
        FieldEditor fieldEditor;
        if (P_BPMN_FONT.equals(propertyNameSuffix)) {
            fieldEditor = new FontFieldEditor(propertyName, Localization.getString(BpmnPreferencePage.LOCALIZATION_PREFIX + propertyNameSuffix),
                    getFieldEditorParent());
        } else if (P_BPMN_LINE_WIDTH.equals(propertyNameSuffix)) {
            fieldEditor = new IntegerFieldEditor(propertyName, Localization.getString(BpmnPreferencePage.LOCALIZATION_PREFIX + propertyNameSuffix),
                    getFieldEditorParent());
        } else {
            fieldEditor = new ColorFieldEditor(propertyName, Localization.getString(BpmnPreferencePage.LOCALIZATION_PREFIX + propertyNameSuffix),
                    getFieldEditorParent());
        }
        addField(fieldEditor);
        overrideFieldEditors.put(overrideFieldEditor, fieldEditor);
    }

    private void updateOverridesFieldEditors() {
        for (Map.Entry<BooleanFieldEditor, FieldEditor> entry : overrideFieldEditors.entrySet()) {
            entry.getValue().setEnabled(entry.getKey().getBooleanValue(), getFieldEditorParent());
        }
    }

    private String getKey(String property) {
        return id + '.' + property;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (FieldEditor.VALUE.equals(event.getProperty()) && overrideFieldEditors.containsKey(event.getSource())) {
            FieldEditor fieldEditor = overrideFieldEditors.get(event.getSource());
            if (Boolean.TRUE.equals(event.getNewValue())) {
                // force applying value
                IPreferenceStore store = getPreferenceStore();
                if (fieldEditor instanceof ColorFieldEditor) {
                    store.setValue(fieldEditor.getPreferenceName(), "128, 128, 128");
                }
                if (fieldEditor instanceof FontFieldEditor) {
                    store.setValue(fieldEditor.getPreferenceName(), new FontData("Arial", 8, SWT.NORMAL).toString());
                }
                if (fieldEditor instanceof IntegerFieldEditor) {
                    store.setValue(fieldEditor.getPreferenceName(), 1);
                }
                fieldEditor.load();
            } else {
                fieldEditor.loadDefault();
            }
            updateOverridesFieldEditors();
        }
    }

    @Override
    public boolean performOk() {
        super.performOk();
        GraphitiProcessEditor.refreshAllActiveEditors();
        return true;
    }

    @Override
    protected void performApply() {
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        for (Map.Entry<BooleanFieldEditor, FieldEditor> entry : overrideFieldEditors.entrySet()) {
            if (!entry.getKey().getBooleanValue()) {
                entry.getValue().loadDefault();
            }
        }
        updateOverridesFieldEditors();
    }

}
