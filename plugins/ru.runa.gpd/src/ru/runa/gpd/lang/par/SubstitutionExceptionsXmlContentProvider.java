package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TaskState;

/**
 * Substitution exceptions were moved to processdefinition.xml
 * @author dofs
 */
public class SubstitutionExceptionsXmlContentProvider extends AuxContentProvider {
    private static final String XML_FILE_NAME = "substitutionExceptions.xml";
    private static final String TASK_ELEMENT_NAME = "task";

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }
    
    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        List<Element> elementsList = document.getRootElement().elements(TASK_ELEMENT_NAME);
        for (Element element : elementsList) {
            String taskId = element.attributeValue(NAME);
            try {
                TaskState taskState = definition.getGraphElementByIdNotNull(taskId);
                taskState.setIgnoreSubstitutionRules(true);
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("No swimlane found for " + taskId, e);
            }
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        return null;
    }
}
