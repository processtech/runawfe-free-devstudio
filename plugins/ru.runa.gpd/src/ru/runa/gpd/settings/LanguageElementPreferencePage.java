package ru.runa.gpd.settings;

import java.awt.Font;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.gef.GefEntry;
import ru.runa.gpd.editor.graphiti.GraphitiEntry;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Subprocess;

public class LanguageElementPreferencePage extends FieldEditorPreferencePage implements PrefConstants {
    private final String id;
    private final NodeTypeDefinition definition;
    private final Language language;
    private static final String PREF_COMMON_BPMN = "pref.common.bpmn.";

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
            store.setDefault(getKey(P_LANGUAGE_TASK_STATE_ASYNC_INPUT_DATA), false);
        }
    }

	@Override
	public void createFieldEditors() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(P_BPMN_FONT, new FontData("Arial", 8, Font.PLAIN).toString());
		GraphElement element;
		try {
			element = definition.getModelClass().newInstance();
		} catch (Exception e) {
			return;
		}
		init(element);
		if (element instanceof NamedGraphElement) {
			addField(new StringFieldEditor(
					getKey(P_LANGUAGE_NODE_NAME_PATTERN),
					Localization.getString("pref.language.node.name.pattern"),
					getFieldEditorParent()));
		}
		if (NodeTypeDefinition.TYPE_NODE.equals(definition.getType())) {
			IntegerFieldEditor widthEditor = new IntegerFieldEditor(
					getKey(P_LANGUAGE_NODE_WIDTH),
					Localization.getString("pref.language.node.width"),
					getFieldEditorParent());
			IntegerFieldEditor heightEditor = new IntegerFieldEditor(
					getKey(P_LANGUAGE_NODE_HEIGHT),
					Localization.getString("pref.language.node.height"),
					getFieldEditorParent());
			boolean fixedSize = language == Language.JPDL ? (definition
					.getGefEntry() != null && definition.getGefEntry()
					.isFixedSize()) : language == Language.BPMN ? (definition
					.getGraphitiEntry() != null && definition
					.getGraphitiEntry().isFixedSize()) : true;
			widthEditor.setEnabled(!fixedSize, getFieldEditorParent());
			heightEditor.setEnabled(!fixedSize, getFieldEditorParent());
			addField(widthEditor);
			addField(heightEditor);
			if (language == Language.BPMN){
				switch (definition.getBpmnElementName()) {
				case "scriptTask":
					addField(new FontFieldEditor(P_BPMN_SCRIPTTASK_FONT,
							Localization.getString(PREF_COMMON_BPMN + P_BPMN_SCRIPTTASK_FONT),
							getFieldEditorParent()));
					addColorField(store, P_BPMN_SCRIPTTASK_FONT_COLOR);
					addColorField(store, P_BPMN_SCRIPTTASK_BACKGROUND_COLOR);
					addColorField(store, P_BPMN_SCRIPTTASK_BASE_COLOR);
					break;
				case "userTask":
					addField(new FontFieldEditor(P_BPMN_STATE_FONT,
							Localization.getString(PREF_COMMON_BPMN + P_BPMN_STATE_FONT), getFieldEditorParent()));
					addColorField(store, P_BPMN_STATE_FONT_COLOR);
					addColorField(store, P_BPMN_STATE_BACKGROUND_COLOR);
					addColorField(store, P_BPMN_STATE_BASE_COLOR);
					break;
				case "endTokenEvent":
					addField(new FontFieldEditor(P_BPMN_ENDTOKEN_FONT,
							Localization.getString(PREF_COMMON_BPMN + P_BPMN_ENDTOKEN_FONT), getFieldEditorParent()));
					addColorField(store, P_BPMN_ENDTOKEN_FONT_COLOR);
					break;
				case "endEvent":
					addField(new FontFieldEditor(P_BPMN_END_FONT,
							Localization.getString(PREF_COMMON_BPMN + P_BPMN_END_FONT), getFieldEditorParent()));
					addColorField(store, P_BPMN_END_FONT_COLOR);
					break;
				case "startEvent":
					addField(new FontFieldEditor(P_BPMN_STARTSTATE_FONT,
							Localization.getString(PREF_COMMON_BPMN + P_BPMN_STARTSTATE_FONT),
							getFieldEditorParent()));
					addColorField(store, P_BPMN_STARTSTATE_FONT_COLOR);
					break;
				case "multiTask":
					addField(new FontFieldEditor(P_BPMN_MULTITASKSTATE_FONT,
							Localization.getString(PREF_COMMON_BPMN + P_BPMN_MULTITASKSTATE_FONT),
							getFieldEditorParent()));
					addColorField(store, P_BPMN_MULTITASKSTATE_FONT_COLOR);
					addColorField(store, P_BPMN_MULTITASKSTATE_BACKGROUND_COLOR);
					addColorField(store, P_BPMN_MULTITASKSTATE_BASE_COLOR);
					break;
				case "multiProcess":
					addField(new FontFieldEditor(P_BPMN_MULTISUBPROCESS_FONT,
							Localization.getString(PREF_COMMON_BPMN + P_BPMN_MULTISUBPROCESS_FONT),
							getFieldEditorParent()));
					addColorField(store, P_BPMN_MULTISUBPROCESS_FONT_COLOR);
					addColorField(store, P_BPMN_MULTISUBPROCESS_BACKGROUND_COLOR);
					addColorField(store, P_BPMN_MULTISUBPROCESS_BASE_COLOR);
					break;
				case "subProcess":
					addField(new FontFieldEditor(P_BPMN_SUBPROCESS_FONT,
							Localization.getString(PREF_COMMON_BPMN + P_BPMN_SUBPROCESS_FONT),
							getFieldEditorParent()));
					addColorField(store, P_BPMN_SUBPROCESS_FONT_COLOR);
					addColorField(store, P_BPMN_SUBPROCESS_BACKGROUND_COLOR);
					addColorField(store, P_BPMN_SUBPROCESS_BASE_COLOR);
					break;
				}
			}
		}
		if (element instanceof TaskState) {
			addField(new BooleanFieldEditor(
					getKey(P_LANGUAGE_SWIMLANE_INITIALIZER),
					Localization.getString("Swimlane.reassignSwimlaneToInitializerValue"),
					getFieldEditorParent()));
			addField(new BooleanFieldEditor(
					getKey(P_LANGUAGE_SWIMLANE_PERFORMER),
					Localization.getString("Swimlane.reassignSwimlaneToTaskPerformer"),
					getFieldEditorParent()));
			addField(new BooleanFieldEditor(
					getKey(P_LANGUAGE_TASK_STATE_ASYNC_INPUT_DATA),
					Localization.getString("TaskNode.inputDataAllowedInAsyncMode"),
					getFieldEditorParent()));
			switch (definition.getBpmnElementName()) {

			}
		}

		if (element instanceof Subprocess) {
			addField(new BooleanFieldEditor(
					getKey(P_LANGUAGE_SUB_PROCESS_ASYNC_INPUT_DATA),
					Localization.getString("Subprocess.inputDataAllowedInAsyncSubprocess"),
					getFieldEditorParent()));
		}
	}

    private void addColorField(IPreferenceStore store, String name) {
    	addField(new ColorFieldEditor(name, Localization.getString(PREF_COMMON_BPMN + name), getFieldEditorParent()));
    }

    private String getKey(String property) {
        return id + '.' + property;
    }
    
    @Override
    public boolean performOk() {
    	boolean performOk = super.performOk();
    	if(performOk){
    		applyStyles();
    	}
    	return performOk;
    }
    
    @Override
    protected void performApply() {
    	super.performApply();
    	applyStyles();
    }
    
	private void applyStyles() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (IEditorReference ref : page.getEditorReferences()) {
			IEditorPart editor = ref.getEditor(true);
			if (editor instanceof GraphitiProcessEditor) {
				((GraphitiProcessEditor) editor).getDiagramEditorPage().applyStyles();
			}
		}
    }
}
