package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Strings;

public class RegulationsXmlContentProvider extends AuxContentProvider {
    private static final String NODES_SETTINGS = "settings";
    private static final String NODE_SETTINGS = "node";
    private static final String NODE_ID = "id";
    private static final String PREVIOUS_NODE_ID = "previous";
    private static final String NEXT_NODE_ID = "next";
    private static final String IS_ENABLED = "enabled";
    private static final String DESCRIPTION = "description";

    @Override
    public String getFileName() {
        return ParContentProvider.REGULATIONS_XML_FILE_NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Document document, ProcessDefinition definition) throws Exception {
        List<Element> elements = document.getRootElement().elements(NODE_SETTINGS);
        for (Element element : elements) {
            String id = element.elementText(NODE_ID);
            try {
                Node node = definition.getGraphElementById(id);
                if (node == null) {
                    continue;
                }
                node.getRegulationsProperties().setEnabled("true".equals(element.elementText(IS_ENABLED)));
                String previousNodeId = element.elementText(PREVIOUS_NODE_ID);
                if (!Strings.isNullOrEmpty(previousNodeId)) {
                    node.getRegulationsProperties().setPreviousNode((Node) definition.getGraphElementById(previousNodeId));
                }
                String nextNodeId = element.elementText(NEXT_NODE_ID);
                if (!Strings.isNullOrEmpty(nextNodeId)) {
                    node.getRegulationsProperties().setNextNode((Node) definition.getGraphElementById(nextNodeId));
                }
                node.getRegulationsProperties().setDescription(element.elementText(DESCRIPTION));
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("regulation load failed near node " + id, e);
            }
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(NODES_SETTINGS);
        Element root = document.getRootElement();
        for (Node node : definition.getNodes()) {
            if (node.getRegulationsProperties().isEmpty()) {
                continue;
            }
            Element element = root.addElement(NODE_SETTINGS);
            element.addElement(NODE_ID).addText(node.getId());
            element.addElement(IS_ENABLED).addText(String.valueOf(node.getRegulationsProperties().isEnabled()));
            GraphElement previousNodeInRegulation = node.getRegulationsProperties().getPreviousNode();
            if (previousNodeInRegulation != null) {
                element.addElement(PREVIOUS_NODE_ID).addText(previousNodeInRegulation.getId());
            }
            GraphElement nextNodeInRegulation = node.getRegulationsProperties().getNextNode();
            if (nextNodeInRegulation != null) {
                element.addElement(NEXT_NODE_ID).addText(nextNodeInRegulation.getId());
            }
            element.addElement(DESCRIPTION).addCDATA(node.getRegulationsProperties().getDescription());
        }
        if (root.elements().size() > 0) {
            return document;
        }
        return null;
    }
}
