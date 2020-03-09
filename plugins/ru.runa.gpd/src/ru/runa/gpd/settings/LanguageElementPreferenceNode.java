package ru.runa.gpd.settings;

import org.eclipse.jface.preference.PreferenceNode;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;

public class LanguageElementPreferenceNode extends PreferenceNode {
    public static final String ROOT_ID = "gpd.pref.language";
    public static final String BPMN_ID = "gpd.pref.language.bpmn";
    public static final String JPDL_ID = "gpd.pref.language.jpdl";

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
        return BPMN_ID + "." + bpmnName + "." + propertyName;
    }

    public static String getBpmnDefaultPropertyName(String propertyName) {
        return getBpmnPropertyName(StyleUtil.DEFAULT_BPMN_NAME, propertyName);
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