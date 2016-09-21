package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.collect.Lists;

/**
 * Information saved in absolute coordinates for all elements.
 * 
 * @author Dofs
 * @since 4.0
 */
public class GpdXmlContentProvider extends AuxContentProvider {
    public static final String XML_FILE_NAME = "gpd.xml";
    private static final String Y = "y";
    private static final String X = "x";
    private static final String NOTATION = "notation";
    private static final String RENDERED = "rendered";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String MIN_VIEW = "minimizedView";
    private static final String SHOW_ACTIONS = "showActions";
    private static final String SHOW_GRID = "showGrid";
    private static final String PROCESS_DIAGRAM = "process-diagram";
    private static final String NODE = "node";
    private static final String TRANSITION = "transition";
    private static final String BENDPOINT = "bendpoint";
    private static final String LABEL = "label";
    private static final String TEXT_DECORATION = "textDecoration";

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }

    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        Element processDiagramInfo = document.getRootElement();
        addProcessDiagramInfo(definition, processDiagramInfo);
        List<Element> children = processDiagramInfo.elements(NODE);
        for (Element element : children) {
            String nodeId = element.attributeValue(NAME);
            GraphElement graphElement = definition.getGraphElementByIdNotNull(nodeId);
            Rectangle constraint = new Rectangle();
            constraint.x = getIntAttribute(element, X, 0);
            constraint.y = getIntAttribute(element, Y, 0);
            constraint.width = getIntAttribute(element, WIDTH, 0);
            constraint.height = getIntAttribute(element, HEIGHT, 0);
            boolean minimizedView = getBooleanAttribute(element, MIN_VIEW, false);
            graphElement.setConstraint(constraint);
            if (graphElement instanceof Node) {
                ((Node) graphElement).setMinimizedView(minimizedView);
                List<Transition> leavingTransitions = ((Node) graphElement).getLeavingTransitions();
                List<Element> transitionInfoList = element.elements(TRANSITION);
                for (int i = 0; i < leavingTransitions.size(); i++) {
                    Element transitionElement = transitionInfoList.get(i);
                    String transitionName = transitionElement.attributeValue(NAME);
                    for (Transition transition : leavingTransitions) {
                        if (transition.getName().equals(transitionName)) {
                            List<Point> bendpoints = Lists.newArrayList();
                            Element labelElement = transitionElement.element(LABEL);
                            if (labelElement != null) {
                                int x = getIntAttribute(labelElement, X, 0);
                                int y = getIntAttribute(labelElement, Y, 0);
                                transition.setLabelLocation(new Point(x, y));
                            }
                            List<Element> bendpointInfoList = transitionElement.elements(BENDPOINT);
                            for (Element bendpointElement : bendpointInfoList) {
                                int x = getIntAttribute(bendpointElement, X, 0);
                                int y = getIntAttribute(bendpointElement, Y, 0);
                                bendpoints.add(new Point(x, y));
                            }
                            transition.setBendpoints(bendpoints);
                            break;
                        }
                    }
                }
            }
            if (graphElement instanceof HasTextDecorator) {
                HasTextDecorator node = (HasTextDecorator) graphElement;
                Element definitionPoint = element.element(TEXT_DECORATION);
                if (definitionPoint != null) {
                    Point point = new Point(getIntAttribute(definitionPoint, X, 0), getIntAttribute(definitionPoint, Y, 0));
                    node.getTextDecoratorEmulation().setDefinitionLocation(point);
                }
            }
        }
        for (GraphElement graphElement : definition.getElementsRecursive()) {
            GraphElement parentGraphElement = graphElement.getParentContainer();
            if (parentGraphElement != null && !parentGraphElement.equals(definition)) {
                Rectangle parentConstraint = parentGraphElement.getConstraint();
                Rectangle constraint = graphElement.getConstraint();
                constraint.x -= parentConstraint.x;
                constraint.y -= parentConstraint.y;
            }
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(PROCESS_DIAGRAM);
        Element root = document.getRootElement();
        addAttribute(root, NAME, definition.getName());
        addAttribute(root, NOTATION, definition.getLanguage().getNotation());
        if (definition.getLanguage() == Language.BPMN) {
            addAttribute(root, RENDERED, "graphiti");
        }
        Dimension dimension = definition.getDimension();
        addAttribute(root, WIDTH, String.valueOf(dimension.width));
        addAttribute(root, HEIGHT, String.valueOf(dimension.height));
        addAttribute(root, SHOW_ACTIONS, String.valueOf(definition.isShowActions()));
        addAttribute(root, SHOW_GRID, String.valueOf(definition.isShowGrid()));
        int xOffset = 0;
        int yOffset = 0;
        int canvasShift = 0;
        if (definition.getLanguage() == Language.BPMN) {
            canvasShift = 5;
        }
        // calculating negative offsets;
        for (GraphElement graphElement : definition.getElementsRecursive()) {
            if (graphElement.getConstraint() == null) {
                continue;
            }
            if (definition.getLanguage() == Language.BPMN && graphElement.getParentContainer() != null
                    && !(graphElement.getParentContainer() instanceof ProcessDefinition)) {
                continue;
            }
            Rectangle constraint = graphElement.getConstraint();
            if (constraint.x - canvasShift < xOffset) {
                xOffset = constraint.x - canvasShift;
            }
            if (constraint.y - canvasShift < yOffset) {
                yOffset = constraint.y - canvasShift;
            }
            if (graphElement instanceof Node) {
                Node node = (Node) graphElement;
                for (Transition transition : node.getLeavingTransitions()) {
                    for (Point bendpoint : transition.getBendpoints()) {
                        // canvasShift for BPMN connections = 0;
                        if (bendpoint.x < xOffset) {
                            xOffset = bendpoint.x;
                        }
                        if (bendpoint.y < yOffset) {
                            yOffset = bendpoint.y;
                        }
                    }
                }
            }
            if (graphElement instanceof HasTextDecorator) {
                TextDecorationNode decorationNode = ((HasTextDecorator) graphElement).getTextDecoratorEmulation().getDefinition();
                if (decorationNode != null && decorationNode.getConstraint() != null) {
                    if (decorationNode.getConstraint().x - canvasShift < xOffset) {
                        xOffset = decorationNode.getConstraint().x - canvasShift;
                    }
                    if (decorationNode.getConstraint().y - canvasShift < yOffset) {
                        yOffset = decorationNode.getConstraint().y - canvasShift;
                    }
                }
            }
        }
        for (GraphElement graphElement : definition.getElementsRecursive()) {
            if (graphElement.getConstraint() == null) {
                continue;
            }
            Element element = root.addElement(NODE);
            addAttribute(element, NAME, graphElement.getId());
            Rectangle constraint = graphElement.getConstraint().getCopy();
            GraphElement parentGraphElement = graphElement.getParentContainer();
            Rectangle parentConstraint = null;
            if (parentGraphElement != null && !parentGraphElement.equals(definition)) {
                parentConstraint = parentGraphElement.getConstraint();
                constraint.x += parentConstraint.x;
                constraint.y += parentConstraint.y;
            }
            if (constraint.isEmpty()) {
                throw new Exception("Invalid figure size: " + constraint.getSize());
            }
            addAttribute(element, X, String.valueOf(constraint.x - xOffset));
            addAttribute(element, Y, String.valueOf(constraint.y - yOffset));
            addAttribute(element, WIDTH, String.valueOf(constraint.width));
            addAttribute(element, HEIGHT, String.valueOf(constraint.height));
            if (graphElement instanceof Node) {
                Node node = (Node) graphElement;
                if (node.isMinimizedView()) {
                    addAttribute(element, MIN_VIEW, "true");
                }
                for (Transition transition : node.getLeavingTransitions()) {
                    Element transitionElement = element.addElement(TRANSITION);
                    String name = transition.getName();
                    if (name != null) {
                        addAttribute(transitionElement, NAME, name);
                    }
                    if (transition.getLabelLocation() != null) {
                        Element labelElement = transitionElement.addElement(LABEL);
                        addAttribute(labelElement, X, String.valueOf(transition.getLabelLocation().x));
                        addAttribute(labelElement, Y, String.valueOf(transition.getLabelLocation().y));
                    }
                    for (Point bendpoint : transition.getBendpoints()) {
                        Element bendpointElement = transitionElement.addElement(BENDPOINT);
                        int x = bendpoint.x - xOffset;
                        int y = bendpoint.y - yOffset;
                        addAttribute(bendpointElement, X, String.valueOf(x));
                        addAttribute(bendpointElement, Y, String.valueOf(y));
                    }
                }
            }
            if (graphElement instanceof HasTextDecorator) {
                TextDecorationNode decorationNode = ((HasTextDecorator) graphElement).getTextDecoratorEmulation().getDefinition();
                if (decorationNode != null && decorationNode.getConstraint() != null) {
                    Element pointDefinition = element.addElement(TEXT_DECORATION);
                    addAttribute(pointDefinition, X, String.valueOf(decorationNode.getConstraint().x - xOffset));
                    addAttribute(pointDefinition, Y, String.valueOf(decorationNode.getConstraint().y - yOffset));
                }
            }
        }
        return document;
    }

    private void addProcessDiagramInfo(ProcessDefinition definition, Element processDiagramInfo) {
        int width = getIntAttribute(processDiagramInfo, WIDTH, 0);
        int height = getIntAttribute(processDiagramInfo, HEIGHT, 0);
        definition.setDimension(new Dimension(width, height));
        if (!(definition instanceof SubprocessDefinition)) {
            definition.setShowActions(getBooleanAttribute(processDiagramInfo, SHOW_ACTIONS, false));
            definition.setShowGrid(getBooleanAttribute(processDiagramInfo, SHOW_GRID, false));
        }
    }
}
