package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.model.BotTaskLink;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Strings;

public class BotsXmlContentProvider extends AuxContentProvider {
    private static final String XML_FILE_NAME = "bots.xml";
    private static final String TASK = "task";
    private static final String BOT_TASKS = "bottasks";
    private static final String CLASS = "class";
    private static final String BOT_TASK_NAME = "botTaskName";
    private static final String CONFIG = "config";

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }
    
    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        List<Element> elements = document.getRootElement().elements(TASK);
        for (Element element : elements) {
            String taskId = element.attributeValue(ID, element.attributeValue(NAME));
            String className = element.attributeValue(CLASS);
            String botTaskName = element.attributeValue(BOT_TASK_NAME);
            Element configElement = element.element(CONFIG);
            String configuration = configElement != null ? XmlUtil.toString(configElement) : null;
            TaskState taskState = definition.getGraphElementByIdNotNull(taskId);
            BotTaskLink botTaskLink = new BotTaskLink();
            botTaskLink.setBotTaskName(botTaskName);
            botTaskLink.setDelegationClassName(className);
            botTaskLink.setDelegationConfiguration(configuration);
            taskState.setBotTaskLink(botTaskLink);
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(BOT_TASKS);
        int botTasksCount = 0;
        for (TaskState taskState : definition.getChildren(TaskState.class)) {
            BotTaskLink botTaskLink = taskState.getBotTaskLink();
            if (botTaskLink != null) {
                botTasksCount++;
                Element element = document.getRootElement().addElement(TASK);
                element.addAttribute(ID, taskState.getId());
                element.addAttribute(CLASS, botTaskLink.getDelegationClassName());
                element.addAttribute(BOT_TASK_NAME, botTaskLink.getBotTaskName());
                if (!Strings.isNullOrEmpty(botTaskLink.getDelegationConfiguration())) {
                    Document confDocument = XmlUtil.parseWithoutValidation(botTaskLink.getDelegationConfiguration());
                    element.add(confDocument.getRootElement().detach());
                }
            }
        }
        if (botTasksCount == 0) {
            return null;
        } else {
            return document;
        }
    }
}
