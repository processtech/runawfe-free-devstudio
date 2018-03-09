package ru.runa.gpd.settings;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.GefEntry;
import ru.runa.gpd.editor.graphiti.GraphitiEntry;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;

public class LanguageElementPreferencePage extends FieldEditorPreferencePage implements PrefConstants {
    private final String id;
    private final NodeTypeDefinition definition;
    private final Language language;

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
                    Localization.getString("Swimlane.reassignSwimlaneToInitializerValue"), getFieldEditorParent()));
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
            case "lane":
                addColorField(P_BPMN_BACKGROUND_COLOR);
                addColorField(P_BPMN_FOREGROUND_COLOR);
                break;
            case "textAnnotation":
                addColorField(P_BPMN_FOREGROUND_COLOR);
                break;
            case "sequenceFlow":
                addColorField(P_BPMN_TRANSITION_COLOR);
                break;
            default:
                break;
            }
        }
    }

    private void init(GraphElement element) {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
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
            case "scriptTask":
            case "userTask":
            case "multiTask":
            case "multiProcess":
            case "subProcess":
            case "lane":
                store.setDefault(getKey(P_BPMN_BACKGROUND_COLOR), "250, 251, 252");
                store.setDefault(getKey(P_BPMN_FOREGROUND_COLOR), "3, 104, 154");
                break;
            case "textAnnotation":
                store.setDefault(getKey(P_BPMN_FOREGROUND_COLOR), "0, 0, 0");
                break;
            case "sequenceFlow":
                store.setDefault(getKey(P_BPMN_TRANSITION_COLOR), "0, 0, 0");
                break;
            default:
                break;
            }
        }
    }

    private void addColorField(String propertyName) {
        String name = LanguageElementPreferenceNode.getBpmnPropertyName(definition.getBpmnElementName(), propertyName);
        addField(new ColorFieldEditor(name, Localization.getString(BPMNPreferencePage.LOCALIZATION_PREFIX + propertyName), getFieldEditorParent()));
    }

    private String getKey(String property) {
        return id + '.' + property;
    }

    @Override
    public boolean performOk() {
        super.performOk();
        LanguageElementPreferenceNode.refreshAllGraphitiEditors();
        return true;
    }

    @Override
    protected void performApply() {
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }

}
