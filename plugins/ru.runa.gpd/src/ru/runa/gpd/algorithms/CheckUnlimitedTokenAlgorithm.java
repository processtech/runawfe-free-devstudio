package ru.runa.gpd.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventCapable;
import ru.runa.gpd.lang.model.bpmn.ParallelGateway;
import ru.runa.gpd.lang.model.jpdl.Fork;
import ru.runa.gpd.lang.model.jpdl.Join;

public class CheckUnlimitedTokenAlgorithm {
    private static final boolean DEBUG = "true".equals(System.getProperty("ru.runa.gpd.algorithms.checkUnlimitedTokens.debug"));
    List<Transition> transitions;
    List<Node> nodes;
    List<Vector> vectorList = new ArrayList<Vector>();
    List<Vector> graphList = new ArrayList<Vector>();
    ListUnprocessedStates listUnprocessedStates = new ListUnprocessedStates();
    List<TransitionVector> transitionVectors = new ArrayList<TransitionVector>();

    public CheckUnlimitedTokenAlgorithm(List<Transition> transitions, List<Node> nodes) {
        this.transitions = transitions;
        this.nodes = nodes;
        init();
    }

    public Transition startAlgorithm() {
        if (DEBUG) {
            StringBuilder str = new StringBuilder();
            str.append("CheckUnlimitedTokenAlgorithm started.");
            str.append("\n");
            str.append("The list of V vectors contains:");
            str.append("\n");
            for (Vector vVector : vectorList) {
                str.append(vVector.toString());
                str.append("\n");
            }
            PluginLogger.logInfo(str.toString());
        }
        List<Vector> startVectorList = new ArrayList<Vector>();
        Vector vector = new Vector(transitions.size() + 1);
        vector.setElementValue(0, 1);
        startVectorList.add(vector);
        listUnprocessedStates.addInList(startVectorList);

        while (listUnprocessedStates.isFirstObjExist()) {
            Vector uVector = listUnprocessedStates.getFirstObj();
            if (DEBUG) {
                StringBuilder str = new StringBuilder();
                str.append("Current U vector is:");
                str.append("\n");
                str.append(uVector.toString());
                str.append("\n");
                PluginLogger.logInfo(str.toString());
            }
            List<Vector> listIntermediateVectors = new ArrayList<Vector>();
            for (Vector vVector : vectorList) {
                Vector tempVector = uVector.getVectorsSum(vVector);
                if (!tempVector.isNegativeNumberExist() && !tempVector.isNullValueVector()) {
                    listIntermediateVectors.add(tempVector);
                }
            }
            if (DEBUG) {
                StringBuilder str = new StringBuilder();
                str.append("Intermediate vectors are:");
                str.append("\n");
                for (Vector intermediateVector : listIntermediateVectors) {
                    str.append(intermediateVector.toString());
                    str.append("\n");
                }
                PluginLogger.logInfo(str.toString());
            }
            StringBuilder str = new StringBuilder();
            List<Vector> equalVectors = new ArrayList<Vector>();
            for (Vector intermediateVector : listIntermediateVectors) {
                for (Vector graphVector : graphList) {
                    if (Arrays.equals(intermediateVector.getElements(), graphVector.getElements())) {
                        equalVectors.add(intermediateVector);
                        transitionVectors.add(new TransitionVector(uVector, graphVector));
                        str.append("Create transition between: " + uVector.toString() + " and " + graphVector.toString());
                        str.append("\n");
                    }
                }
            }

            listIntermediateVectors.removeAll(equalVectors);

            for (Vector intermediateVector : listIntermediateVectors) {
                transitionVectors.add(new TransitionVector(uVector, intermediateVector));
                str.append("Create transition between: " + uVector.toString() + " and " + intermediateVector.toString());
                str.append("\n");
            }
            if (DEBUG) {
                PluginLogger.logInfo(str.toString());
            }
            graphList.addAll(listIntermediateVectors);
            listUnprocessedStates.addInList(listIntermediateVectors);
            listIntermediateVectors.clear();

            listUnprocessedStates.removeFirst();
            if (DEBUG) {
                str = new StringBuilder();
                str.append("List unprocessed vectors:");
                str.append("\n");
                for (Vector unprocessedVector : listUnprocessedStates.getList()) {
                    str.append(unprocessedVector.toString());
                    str.append("\n");
                }
                PluginLogger.logInfo(str.toString());
            }
            for (Vector unprocessedVector : listUnprocessedStates.getList()) {
                List<Vector> attainableVectorList = getAttainableVectorList(unprocessedVector);
                if (DEBUG) {
                    str = new StringBuilder();
                    str.append("Current unprocessed vectors:" + unprocessedVector.toString());
                    str.append("\n");
                    str.append("List attainable vectors");
                    str.append("\n");
                    for (Vector attainableVector : attainableVectorList) {
                        str.append(attainableVector.toString());
                        str.append("\n");
                    }
                    PluginLogger.logInfo(str.toString());
                }
                for (Vector attainableVector : attainableVectorList) {
                    int strongminusindex = 0;
                    int strongminus = 0;
                    int minusequal = 0;
                    for (int i = 0; i < unprocessedVector.getElements().length; i++) {
                        if (attainableVector.getElements()[i] < unprocessedVector.getElements()[i] && strongminusindex == 0) {
                            strongminus++;
                            strongminusindex = i;
                            continue;
                        }
                        if (attainableVector.getElements()[i] <= unprocessedVector.getElements()[i]) {
                            minusequal++;
                        }
                    }
                    if (strongminus == 1 && minusequal == unprocessedVector.getElements().length - 1) {
                        if (true) {
                            str = new StringBuilder();
                            str.append("The required vector has been found:" + attainableVector.toString());
                            PluginLogger.logInfo(str.toString());
                        }
                        return transitions.get(strongminusindex - 1);
                    }
                }
            }
        }

        return null;
    }

    private void init() {
        populateVectorList();
        Vector vector = new Vector(transitions.size() + 1);
        vector.setElementValue(0, 1);
        graphList.add(vector);
    }

    private void populateVectorList() {
        for (Node node : nodes) {
            if (node instanceof StartState) {
                for (Transition transition : transitions) {
                    if (transition.getSource().equals(node)) {
                        Vector v = new Vector(transitions.size() + 1);
                        v.setElementValue(0, -1);
                        v.setElementValue(transitions.indexOf(transition) + 1, 1);
                        vectorList.add(v);
                    }
                }
            }
        }

        for (Node node : nodes) {
            if (node instanceof Join || node instanceof Fork || node instanceof ParallelGateway) {
                Vector v = new Vector(transitions.size() + 1);
                for (Transition transition : transitions) {
                    if (transition.getSource().equals(node) && !(transition.getTarget() instanceof EndState)) {
                        v.setElementValue(transitions.indexOf(transition) + 1, 1);
                    }
                    if (transition.getTarget().equals(node)) {
                        v.setElementValue(transitions.indexOf(transition) + 1, -1);
                    }
                }
                vectorList.add(v);
            }
        }

        for (Node node : nodes) {
            if (!(node instanceof Join || node instanceof Fork || node instanceof ParallelGateway || node instanceof StartState || node instanceof EndState)) {
                for (Transition transition : transitions) {
                    if (transition.getTarget().equals(node)) {
                        List<Transition> addedVectors = new ArrayList<Transition>();
                        for (Transition transition1 : transitions) {
                            Node sourceNode = transition1.getSource();
                            if (sourceNode instanceof IBoundaryEventCapable && sourceNode.getParent() instanceof Node) {
                                sourceNode = (Node) sourceNode.getParent();
                            }
                            if (sourceNode.equals(node) && !addedVectors.contains(transition1)) {
                                Vector v = new Vector(transitions.size() + 1);
                                v.setElementValue(transitions.indexOf(transition) + 1, -1);
                                if(!(transition1.getTarget() instanceof EndState)) {
                                    v.setElementValue(transitions.indexOf(transition1) + 1, 1);
                                }
                                vectorList.add(v);
                                addedVectors.add(transition1);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<Vector> getAttainableVectorList(Vector unprocessedVector) {
        List<Vector> buffer = new ArrayList<Vector>();
        List<Vector> listVectors = new ArrayList<Vector>();

        for (TransitionVector transitionVector : transitionVectors) {
            if (Arrays.equals(unprocessedVector.getElements(), transitionVector.getToVector().getElements())) {
                buffer.add(transitionVector.getFromVector());
            }
        }

        while (buffer.size() > 0) {
            List<Vector> foundVectors = new ArrayList<Vector>();
            for (TransitionVector transitionVector : transitionVectors) {
                for (Vector tempVector : buffer) {
                    if (Arrays.equals(tempVector.getElements(), transitionVector.getToVector().getElements())) {
                        foundVectors.add(transitionVector.getFromVector());
                    }
                }
            }

            Iterator<Vector> foundedIterator = foundVectors.iterator();
            while (foundedIterator.hasNext()) {
                Vector foundVector = foundedIterator.next();
                for (Vector bufferVector : buffer) {
                    if (Arrays.equals(foundVector.getElements(), bufferVector.getElements())) {
                        foundedIterator.remove();
                        break;
                    }
                }
            }

            foundedIterator = foundVectors.iterator();
            while (foundedIterator.hasNext()) {
                Vector foundVector = foundedIterator.next();
                for (Vector listVector : listVectors) {
                    if (Arrays.equals(foundVector.getElements(), listVector.getElements())) {
                        foundedIterator.remove();
                        break;
                    }
                }
            }

            listVectors.addAll(buffer);
            buffer.clear();
            buffer.addAll(foundVectors);
        }

        return listVectors;
    }
}
