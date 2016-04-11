package ru.runa.gpd.lang.par;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.swimlane.BotSwimlaneInitializer;

public class SwimlaneGUIContentProvider extends AuxContentProvider {
    public static final String XML_FILE_NAME = "swimlaneGUIconfig.xml";
    private static final String PATH = "guiElementPath";
    private static final String SWIMLANE = "swimlane";
    private static final Pattern PATTERN = Pattern.compile("ru.runa.wfe.extension.orgfunction.ExecutorByNameFunction\\((.*)\\)");

    @Override
    public boolean isSupportedForEmbeddedSubprocess() {
        return false;
    }

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }

    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        List<Element> elementsList = document.getRootElement().elements(SWIMLANE);
        for (Element element : elementsList) {
            String swimlaneName = element.attributeValue(NAME);
            Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
            if (swimlane != null) {
                String path = element.attributeValue(PATH);
                if ("SwimlaneElement.BotLabel".equals(path) && swimlane.getDelegationConfiguration() != null) {
                    // back compatibility
                    Matcher matcher = PATTERN.matcher(swimlane.getDelegationConfiguration());
                    if (matcher.find()) {
                        String botName = matcher.group(1);
                        swimlane.setDelegationConfiguration(BotSwimlaneInitializer.BEGIN + botName);
                    }
                }
                swimlane.setEditorPath(path);
            }
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        return null;
    }
}
