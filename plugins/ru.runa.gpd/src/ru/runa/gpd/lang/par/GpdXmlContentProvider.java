package ru.runa.gpd.lang.par;

import static java.lang.Math.min;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.Action;
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

    private static int MAGIC_NUMBER_X = 5;
    private static int MAGIC_NUMBER_Y = 47;

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
        IFeatureProvider bpmnFeatureProvider = null;
        if (definition.getLanguage() == Language.BPMN) {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            GraphitiProcessEditor graphitiProcessEditor = (GraphitiProcessEditor) page.findEditor(new FileEditorInput(definition.getFile()));
            if (graphitiProcessEditor != null) { // while copying/renaming
                bpmnFeatureProvider = graphitiProcessEditor.getDiagramEditorPage().getDiagramTypeProvider().getFeatureProvider();
            }
        }
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
        int diagramX = MAGIC_NUMBER_X;
        int diagramY = MAGIC_NUMBER_Y;
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
            xOffset = min(xOffset, constraint.x - canvasShift);
            yOffset = min(yOffset, constraint.y - canvasShift);
            if (graphElement instanceof Node) {
                Node node = (Node) graphElement;
                for (Transition transition : node.getLeavingTransitions()) {
                    Point lableLocation = transition.getLabelLocation();
                    if (bpmnFeatureProvider != null && lableLocation != null) {
                        Connection connection = (Connection) bpmnFeatureProvider.getPictogramElementForBusinessObject(transition);
                        ILocation midpoint = Graphiti.getPeService().getConnectionMidpoint(connection, 0.5d);
                        diagramX = min(diagramX, lableLocation.x + midpoint.getX());
                        diagramY = min(diagramY, lableLocation.y + midpoint.getY());
                    }
                    for (Point bendpoint : transition.getBendpoints()) {
                        // canvasShift for BPMN connections = 0;
                        xOffset = min(xOffset, bendpoint.x);
                        yOffset = min(yOffset, bendpoint.y);
                    }
                }
            }
            if (graphElement instanceof HasTextDecorator) {
                TextDecorationNode decorationNode = ((HasTextDecorator) graphElement).getTextDecoratorEmulation().getDefinition();
                if (decorationNode != null && decorationNode.getConstraint() != null) {
                    xOffset = min(xOffset, decorationNode.getConstraint().x - canvasShift);
                    yOffset = min(yOffset, decorationNode.getConstraint().y - canvasShift);
                }
            }
        }
        if (diagramX < 0) {
            addAttribute(root, X, String.valueOf(-diagramX + MAGIC_NUMBER_X));
            setX(definition, -diagramX + MAGIC_NUMBER_X);
        } else if (diagramX < MAGIC_NUMBER_X) {
            addAttribute(root, X, String.valueOf(MAGIC_NUMBER_X - diagramX));
            setX(definition, MAGIC_NUMBER_X - diagramX);
        } else if (bpmnFeatureProvider == null) {
            Rectangle r = definition.getConstraint();
            if (r != null && r.x != 0) {
                addAttribute(root, X, String.valueOf(r.x));
            }
        }
        if (diagramY < 0) {
            addAttribute(root, Y, String.valueOf(-diagramY + MAGIC_NUMBER_Y));
            setY(definition, -diagramY + MAGIC_NUMBER_Y);
        } else if (diagramY < MAGIC_NUMBER_Y) {
            addAttribute(root, Y, String.valueOf(MAGIC_NUMBER_Y - diagramY));
            setY(definition, MAGIC_NUMBER_Y - diagramY);
        } else if (bpmnFeatureProvider == null) {
            Rectangle r = definition.getConstraint();
            if (r != null && r.y != 0) {
                addAttribute(root, Y, String.valueOf(r.y));
            }
        }
        for (GraphElement graphElement : definition.getElementsRecursive()) {
            if (graphElement instanceof Action || graphElement.getConstraint() == null) {
                continue;
            }
            Element element = root.addElement(NODE);
            addAttribute(element, NAME, graphElement.getId());
            Rectangle constraint = graphElement.getConstraint().getCopy();
            GraphElement parentGraphElement = graphElement.getParentContainer();
            Rectangle parentConstraint = null;
            while (parentGraphElement != null && !parentGraphElement.equals(definition)) {
                parentConstraint = parentGraphElement.getConstraint();
                constraint.x += parentConstraint.x;
                constraint.y += parentConstraint.y;
                parentGraphElement = parentGraphElement.getParentContainer();
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

    private void setX(ProcessDefinition pd, int x) {
        if (pd.getConstraint() == null) {
            pd.setConstraint(new Rectangle());
        }
        pd.getConstraint().x = x;
    }

    private void setY(ProcessDefinition pd, int y) {
        if (pd.getConstraint() == null) {
            pd.setConstraint(new Rectangle());
        }
        pd.getConstraint().y = y;
    }

    private void addProcessDiagramInfo(ProcessDefinition definition, Element processDiagramInfo) {
        int x = getIntAttribute(processDiagramInfo, X, 0);
        int y = getIntAttribute(processDiagramInfo, Y, 0);
        if (x + y > 0) {
            definition.setConstraint(new Rectangle(x, y, 0, 0));
        }
        int width = getIntAttribute(processDiagramInfo, WIDTH, 0);
        int height = getIntAttribute(processDiagramInfo, HEIGHT, 0);
        definition.setDimension(new Dimension(width, height));
        if (!(definition instanceof SubprocessDefinition)) {
            definition.setShowActions(getBooleanAttribute(processDiagramInfo, SHOW_ACTIONS, false));
            definition.setShowGrid(getBooleanAttribute(processDiagramInfo, SHOW_GRID, false));
        }
    }
}
