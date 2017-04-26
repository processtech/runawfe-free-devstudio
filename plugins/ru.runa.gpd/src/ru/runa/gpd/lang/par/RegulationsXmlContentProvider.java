package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.XmlUtil;

public class RegulationsXmlContentProvider extends AuxContentProvider {
    private static final String XML_FILE_NAME = "regulations.xml";
    private static final String NODES_SETTINGS = "settings";
    private static final String NODE_SETTINGS = "node";
    private static final String NODE_ID = "id";
    private static final String PREVIOUS_NODE_ID = "previous";
    private static final String NEXT_NODE_ID = "next";
    private static final String IS_ENABLED = "enabled";
    private static final String DESCRIPTION = "description";

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Document document, ProcessDefinition definition) throws Exception {
        List<Element> listOfNodeSettings = document.getRootElement().elements(NODE_SETTINGS);
        for (Element nodeSetting : listOfNodeSettings) {
            String id = nodeSetting.elementText(NODE_ID);
            GraphElement graphElement = definition.getGraphElementById(id);
            if (graphElement == null) {
                continue;
            }
            String previousNodeId = nodeSetting.elementText(PREVIOUS_NODE_ID);
            String nextNodeId = nodeSetting.elementText(NEXT_NODE_ID);
            GraphElement previousNode = null;
            GraphElement nextNode = null;
            if (previousNodeId.isEmpty() != true) {
                previousNode = definition.getGraphElementById(previousNodeId);
            }
            if (previousNode != null) {
                graphElement.getNodeRegulationsProperties().setPreviousNode(previousNode);
            }
            if (nextNodeId.isEmpty() != true) {
                nextNode = definition.getGraphElementById(nextNodeId);
            }
            if (nextNode != null) {
                graphElement.getNodeRegulationsProperties().setNextNode(nextNode);
            }
            String isEnabledInRegulation = nodeSetting.elementText(IS_ENABLED);
            if (isEnabledInRegulation.equals("true")) {
                graphElement.getNodeRegulationsProperties().setIsEnabled(true);
            } else {
                graphElement.getNodeRegulationsProperties().setIsEnabled(false);
            }
            graphElement.getNodeRegulationsProperties().setDescriptionForUser(nodeSetting.elementText(DESCRIPTION));
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(NODES_SETTINGS);
        Element root = document.getRootElement();
        List<GraphElement> listOfChildren = definition.getElements();
        for (GraphElement graphElement : listOfChildren) {
            if (graphElement instanceof Node) {
                Element nodeSettingElement = root.addElement(NODE_SETTINGS);
                nodeSettingElement.addElement(NODE_ID).addText(graphElement.getId());
                GraphElement previousNodeInRegulation = graphElement.getNodeRegulationsProperties().getPreviousNode();
                GraphElement nextNodeInRegulation = graphElement.getNodeRegulationsProperties().getNextNode();
                if (previousNodeInRegulation != null) {
                    nodeSettingElement.addElement(PREVIOUS_NODE_ID).addText(previousNodeInRegulation.getId());
                } else {
                    nodeSettingElement.addElement(PREVIOUS_NODE_ID).addText("");
                }
                if (nextNodeInRegulation != null) {
                    nodeSettingElement.addElement(NEXT_NODE_ID).addText(nextNodeInRegulation.getId());
                } else {
                    nodeSettingElement.addElement(NEXT_NODE_ID).addText("");
                }
                if (graphElement.getNodeRegulationsProperties().getIsEnabled()) {
                    nodeSettingElement.addElement(IS_ENABLED).addText("true");
                } else {
                    nodeSettingElement.addElement(IS_ENABLED).addText("false");
                }
                nodeSettingElement.addElement(DESCRIPTION).addCDATA(graphElement.getNodeRegulationsProperties().getDescriptionForUser());
            }
        }
        return document;
    }
}
