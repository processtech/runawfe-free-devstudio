package ru.runa.gpd.settings;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.gef.GefEntry;
import ru.runa.gpd.editor.graphiti.GraphitiEntry;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.TaskState;

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

    private void init(GraphElement element) {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        if (element instanceof NamedGraphElement) {
            store.setDefault(getKey(P_LANGUAGE_NODE_NAME_PATTERN), definition.getLabel());
        }
        if (NodeTypeDefinition.TYPE_NODE.equals(definition.getType())) {
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
        }
        if (element instanceof TaskState) {
            store.setDefault(getKey(P_LANGUAGE_SWIMLANE_INITIALIZER), false);
            store.setDefault(getKey(P_LANGUAGE_SWIMLANE_PERFORMER), true);
        }
    }

    @Override
    public void createFieldEditors() {
        GraphElement element;
        try {
            element = definition.getModelClass().newInstance();
        } catch (Exception e) {
            return;
        }
        init(element);

        if (element instanceof NamedGraphElement) {
            addField(new StringFieldEditor(getKey(P_LANGUAGE_NODE_NAME_PATTERN), Localization.getString("pref.language.node.name.pattern"),
                    getFieldEditorParent()));
        }
        if (NodeTypeDefinition.TYPE_NODE.equals(definition.getType())) {
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
        }
    }

    private String getKey(String property) {
        return id + '.' + property;
    }
}