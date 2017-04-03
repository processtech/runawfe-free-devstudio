package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.jpdl.Action;

/**
 * Action description were moved to processdescription.xml
 * @author dofs
 */
public class ActionDescriptionContentProvider extends AuxContentProvider {
    private static final String DELIM = "/";
    private static final String ACTION_INDEX = "actionIndex";
    private static final String XML_FILE_NAME = "actionDescription.xml";
    private static final String PATH_ATTRIBUTE_NAME = "path";
    private static final String DESC_ATTRIBUTE_NAME = "description";
    private static final String ELEMENT_NAME = "action";

    @Override
    public boolean isSupportedForEmbeddedSubprocess() {
        return false;
    }
    
    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }
    
    @Override
    public void read(Document document, ProcessDefinition definition) {
        try {
            List<Element> elementsList = document.getRootElement().elements(ELEMENT_NAME);
            for (Element element : elementsList) {
                String path = element.attributeValue(PATH_ATTRIBUTE_NAME);
                String description = element.attributeValue(DESC_ATTRIBUTE_NAME);
                Action action = findByPath(definition, path);
                if (action != null) {
                    action.setDescription(description);
                }
            }
        } catch (Exception e) {
            PluginLogger.logError("Unable to apply " + XML_FILE_NAME, e);
        }
    }

    @Override
    public Document save(ProcessDefinition definition) {
        return null;
    }

    private Action findByPath(ProcessDefinition definition, String path) {
        try {
            String[] components = path.split(DELIM, -1);
            GraphElement element = definition;
            if (components.length > 2) {
                for (int i = 1; i < components.length - 1; i++) {
                    String name = components[i];
                    for (NamedGraphElement e : element.getChildren(NamedGraphElement.class)) {
                        if (name.equals(e.getName())) {
                            element = e;
                            break;
                        }
                    }
                }
            }
            String actionName = components[components.length - 1];
            int actionIndex = Integer.parseInt(actionName.substring(ACTION_INDEX.length()));
            if (element.getActions().size() > actionIndex) {
                return element.getActions().get(actionIndex);
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("findByPath " + path + " in " + definition, e);
        }
        return null;
    }
}
