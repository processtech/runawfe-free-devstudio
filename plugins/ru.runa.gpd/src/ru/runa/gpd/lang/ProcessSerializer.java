package ru.runa.gpd.lang;

import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import ru.runa.gpd.lang.model.ProcessDefinition;

public abstract class ProcessSerializer {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String TYPE = "type";
    public static final String VERSION = "version";
    public static final String ACCESS_TYPE = "accessType";
    public static final String ASYNC = "async";
    public static final String ASYNC_COMPLETION_MODE = "asyncCompletionMode";
    public static final String VARIABLE = "variable";
    public static final String REASSIGN = "reassign";
    public static final String REASSIGN_SWIMLANE_TO_TASK_PERFORMER = "reassignSwimlaneToTaskPerformer";
    public static final String CLASS = "class";
    public static final String IGNORE_SUBSTITUTION_RULES = "ignoreSubstitutionRules";
    public static final String EMBEDDED = "embedded";
    public static final String TRANSACTIONAL = "transactional";
    public static final String NODE_ASYNC_EXECUTION = "asyncExecution";
    public static final String BEHAVIOR = "behavior";
    protected static final String USE_GLOBALS = "useGlobals";
    protected static final String GLOBAL = "global";
    protected static final String VALIDATE_AT_START = "validateAtStart";

    public abstract boolean isSupported(Document document);

    public abstract Document getInitialProcessDefinitionDocument(String processName, Map<String, String> properties);

    public abstract void parseXML(Document document, ProcessDefinition definition);

    public abstract void saveToXML(ProcessDefinition definition, Document document);

    public abstract void validateProcessDefinitionXML(IFile file);

    protected void setAttribute(Element node, String attributeName, String attributeValue) {
        if (attributeValue != null) {
            node.addAttribute(attributeName, attributeValue);
        }
    }

    protected void setNodeValue(Element node, String nodeValue) {
        if (nodeValue != null) {
            node.addCDATA(nodeValue);
        }
    }

}
