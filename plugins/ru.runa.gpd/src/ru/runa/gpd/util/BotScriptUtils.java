package ru.runa.gpd.util;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.bot.IBotFileSupportProvider;
import ru.runa.gpd.lang.model.BotTask;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class BotScriptUtils {
    private final static String NAME_ATTRIBUTE_NAME = "name";
    private final static String EMBEDDED_FILE_ATTRIBUTE_NAME = "embeddedFile";
    private final static String PASSWORD_ATTRIBUTE_NAME = "password";
    private final static String STARTTIMEOUT_ATTRIBUTE_NAME = "startTimeout";
    private final static String HANDLER_ATTRIBUTE_NAME = "handler";
    private final static String CONFIGURATION_STRING_ATTRIBUTE_NAME = "configuration";
    private final static String ADD_BOT_CONFIGURATION_ELEMENT_NAME = "addConfigurationsToBot";
    private final static String BOT_CONFIGURATION_ELEMENT_NAME = "botConfiguration";

    public static Document createScriptForBotLoading(String botName, List<BotTask> tasks) {
        Document script = XmlUtil.createDocument("workflowScript", XmlUtil.RUNA_NAMESPACE, "workflowScript.xsd");
        Element rootElement = script.getRootElement();
        Element createBotElement = rootElement.addElement("createBot", XmlUtil.RUNA_NAMESPACE);
        createBotElement.addAttribute(NAME_ATTRIBUTE_NAME, botName);
        createBotElement.addAttribute(PASSWORD_ATTRIBUTE_NAME, "");
        createBotElement.addAttribute(STARTTIMEOUT_ATTRIBUTE_NAME, "");
        if (tasks.size() > 0) {
            Element removeTasks = rootElement.addElement("removeConfigurationsFromBot", XmlUtil.RUNA_NAMESPACE);
            removeTasks.addAttribute(NAME_ATTRIBUTE_NAME, botName);
            for (BotTask task : tasks) {
                Element taskElement = removeTasks.addElement(BOT_CONFIGURATION_ELEMENT_NAME, XmlUtil.RUNA_NAMESPACE);
                taskElement.addAttribute(NAME_ATTRIBUTE_NAME, task.getName());
            }
            Element addTasks = rootElement.addElement("addConfigurationsToBot", XmlUtil.RUNA_NAMESPACE);
            addTasks.addAttribute(NAME_ATTRIBUTE_NAME, botName);
            for (BotTask task : tasks) {
                Element taskElement = addTasks.addElement(BOT_CONFIGURATION_ELEMENT_NAME);
                taskElement.addAttribute(NAME_ATTRIBUTE_NAME, task.getName());
                taskElement.addAttribute(HANDLER_ATTRIBUTE_NAME, task.getDelegationClassName());

                if (!Strings.isNullOrEmpty(task.getDelegationClassName())) {
                    DelegableProvider provider = HandlerRegistry.getProvider(task.getDelegationClassName());
                    if (provider instanceof IBotFileSupportProvider) {
                        IBotFileSupportProvider botFileProvider = (IBotFileSupportProvider) provider;
                        String embeddedFileName = botFileProvider.getEmbeddedFileName(task);
                        if (!Strings.isNullOrEmpty(embeddedFileName)) {
                            taskElement.addAttribute(EMBEDDED_FILE_ATTRIBUTE_NAME, embeddedFileName);
                        }
                    }
                    taskElement.addAttribute(CONFIGURATION_STRING_ATTRIBUTE_NAME, task.getName() + "." + BotCache.CONFIGURATION_FILE_EXTENSION);
                }
            }
        }
        return script;
    }

    /**
     * 
     * @param inputStream
     *            xml script stream
     * @return map of bot task without configuration set -> configuration file
     *         name
     */
    public static List<BotTask> getBotTasksFromScript(String botStationName, String botName, byte[] scriptXml, Map<String, byte[]> files) {
        List<BotTask> botTasks = Lists.newArrayList();
        Document document = XmlUtil.parseWithXSDValidation(scriptXml, "workflowScript.xsd");
        List<Element> taskElements = document.getRootElement().elements(ADD_BOT_CONFIGURATION_ELEMENT_NAME);
        for (Element taskElement : taskElements) {
            List<Element> botList = taskElement.elements(BOT_CONFIGURATION_ELEMENT_NAME);
            for (Element botElement : botList) {
                String name = botElement.attributeValue(NAME_ATTRIBUTE_NAME, "").trim();
                if (Strings.isNullOrEmpty(name)) {
                    continue;
                }
                String handler = botElement.attributeValue(HANDLER_ATTRIBUTE_NAME, "");
                String embeddedFileName = botElement.attributeValue(EMBEDDED_FILE_ATTRIBUTE_NAME, "");
                String configurationFileName = botElement.attributeValue(CONFIGURATION_STRING_ATTRIBUTE_NAME);
                byte[] configurationFileData = files.remove(configurationFileName);
                String configuration = configurationFileData != null ? new String(configurationFileData) : "";
                BotTask botTask = BotTaskUtils.createBotTask(botStationName, botName, name, handler, configuration);
                if (!Strings.isNullOrEmpty(embeddedFileName)) {
                    botTask.getFilesToSave().add(embeddedFileName);
                }
                botTasks.add(botTask);
            }
        }
        return botTasks;
    }
}
