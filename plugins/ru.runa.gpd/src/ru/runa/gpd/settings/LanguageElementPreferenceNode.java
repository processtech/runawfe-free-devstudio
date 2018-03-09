package ru.runa.gpd.settings;

import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;

public class LanguageElementPreferenceNode extends PreferenceNode {
    public static final String ROOT_ID = "gpd.pref.language";
    public static final String BPMN_ID = "gpd.pref.language.bpmn";
    public static final String JPDL_ID = "gpd.pref.language.jpdl";
    public static final String DEFAULT = "default";

    private final NodeTypeDefinition definition;
    private final Language language;

    public LanguageElementPreferenceNode(NodeTypeDefinition definition, Language language) {
        super(getId(definition, language));
        this.definition = definition;
        this.language = language;
    }

    public static String getId(NodeTypeDefinition definition, Language language) {
        return language == Language.BPMN ? (BPMN_ID + '.' + definition.getBpmnElementName()) : (JPDL_ID + '.' + definition.getJpdlElementName());
    }

    public static String getBpmnPropertyName(String bpmnName, String propertyName) {
        return BPMN_ID + "." + propertyName + "." + bpmnName;
    }

    public static String getBpmnDefaultPropertyName(String propertyName) {
        return getBpmnPropertyName(DEFAULT, propertyName);
    }

    public static void refreshAllGraphitiEditors() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (IEditorReference ref : page.getEditorReferences()) {
            IEditorPart editor = ref.getEditor(true);
            if (editor instanceof GraphitiProcessEditor) {
                ((GraphitiProcessEditor) editor).getDiagramEditorPage().applyStyles();
            }
        }
    }

    @Override
    public String getLabelText() {
        return definition.getLabel();
    }

    @Override
    public void createPage() {
        setPage(new LanguageElementPreferencePage(getId(), definition, language));
    }
}