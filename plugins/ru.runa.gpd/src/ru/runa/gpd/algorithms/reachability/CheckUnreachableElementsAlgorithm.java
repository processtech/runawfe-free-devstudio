package ru.runa.gpd.algorithms.reachability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// State graph of the modified algorithm (A.G. Mikheev, 2024): instant elements fire right after
// the element whose firing enabled them, so only token placements without enabled instant elements become states.
public class CheckUnreachableElementsAlgorithm {
    private final ProcessGraph graph;
    private final List<ProcessElement> componentElements = new ArrayList<>();
    private final Map<ProcessElement, Integer> elementComponents = new HashMap<>();
    private final Map<ProcessFlow, Integer> flowComponents = new HashMap<>();
    private final Map<TokenState, TokenState> parentStates = new LinkedHashMap<>();
    private final List<StateTransition> transitions = new ArrayList<>();
    private final Set<ProcessElement> reachedElements = new HashSet<>();
    private ProcessElement unboundedElement;

    public CheckUnreachableElementsAlgorithm(ProcessGraph graph) {
        this.graph = graph;
        for (ProcessElement element : graph.getElements()) {
            if (element.getType() == ElementType.PARALLEL_GATEWAY) {
                for (ProcessFlow flow : element.getArrivingFlows()) {
                    flowComponents.put(flow, componentElements.size());
                    componentElements.add(element);
                }
            } else {
                elementComponents.put(element, componentElements.size());
                componentElements.add(element);
            }
        }
    }

    public void startAlgorithm() {
        List<ProcessElement> elements = graph.getElements();
        Deque<TokenState> unprocessedStates = new ArrayDeque<>();
        for (ProcessElement element : elements) {
            if (element.getType() == ElementType.START) {
                int[] counts = new int[componentElements.size()];
                enter(counts, element);
                addState(new TokenState(counts), null, unprocessedStates);
            }
        }
        while (!unprocessedStates.isEmpty() && unboundedElement == null) {
            TokenState state = unprocessedStates.poll();
            for (ProcessElement element : elements) {
                if (element.getType().isInstant() || state.getCount(elementComponents.get(element)) == 0) {
                    continue;
                }
                for (ProcessFlow flow : element.getLeavingFlows()) {
                    int[] counts = state.getCounts();
                    counts[elementComponents.get(element)]--;
                    Set<ProcessElement> firedElements = new LinkedHashSet<>();
                    firedElements.add(element);
                    TokenState nextState = new TokenState(deliver(counts, flow, firedElements));
                    transitions.add(new StateTransition(state, nextState, new ArrayList<>(firedElements)));
                    reachedElements.addAll(firedElements);
                    addState(nextState, state, unprocessedStates);
                    if (unboundedElement != null) {
                        return;
                    }
                }
            }
        }
    }

    public boolean isTokenCountBounded() {
        return unboundedElement == null;
    }

    public ProcessElement getUnboundedElement() {
        return unboundedElement;
    }

    public List<ProcessElement> getUnreachableElements() {
        List<ProcessElement> unreachableElements = new ArrayList<>();
        for (ProcessElement element : graph.getElements()) {
            if (!reachedElements.contains(element)) {
                unreachableElements.add(element);
            }
        }
        return unreachableElements;
    }

    public List<ProcessElement> getComponentElements() {
        return Collections.unmodifiableList(componentElements);
    }

    public List<TokenState> getStates() {
        return Collections.unmodifiableList(new ArrayList<>(parentStates.keySet()));
    }

    public List<StateTransition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    private void addState(TokenState state, TokenState parentState, Deque<TokenState> unprocessedStates) {
        if (parentStates.containsKey(state)) {
            return;
        }
        parentStates.put(state, parentState);
        for (int i = 0; i < state.size(); i++) {
            if (state.getCount(i) > 0) {
                reachedElements.add(componentElements.get(i));
            }
        }
        unboundedElement = findGrownElement(state);
        unprocessedStates.add(state);
    }

    private int[] deliver(int[] counts, ProcessFlow flow, Set<ProcessElement> firedElements) {
        Deque<ProcessFlow> flows = new ArrayDeque<>();
        flows.add(flow);
        List<ProcessElement> endElements = new ArrayList<>();
        while (!flows.isEmpty()) {
            ProcessFlow arrivingFlow = flows.poll();
            ProcessElement target = arrivingFlow.getTarget();
            switch (target.getType()) {
            case PARALLEL_GATEWAY:
                counts[flowComponents.get(arrivingFlow)]++;
                while (isEnabled(counts, target)) {
                    for (ProcessFlow gatewayFlow : target.getArrivingFlows()) {
                        counts[flowComponents.get(gatewayFlow)]--;
                    }
                    firedElements.add(target);
                    flows.addAll(target.getLeavingFlows());
                }
                break;
            case END_TOKEN:
                firedElements.add(target);
                break;
            case END:
                endElements.add(target);
                break;
            default:
                enter(counts, target);
            }
        }
        if (endElements.isEmpty()) {
            return counts;
        }
        firedElements.addAll(endElements);
        return new int[counts.length];
    }

    private boolean isEnabled(int[] counts, ProcessElement parallelGateway) {
        for (ProcessFlow flow : parallelGateway.getArrivingFlows()) {
            if (counts[flowComponents.get(flow)] == 0) {
                return false;
            }
        }
        return true;
    }

    private void enter(int[] counts, ProcessElement element) {
        counts[elementComponents.get(element)]++;
    }

    // '?' of the article: the state covers one of its ancestors in the tree of first discovery
    private ProcessElement findGrownElement(TokenState state) {
        for (TokenState ancestor = parentStates.get(state); ancestor != null; ancestor = parentStates.get(ancestor)) {
            int grownComponent = getGrownComponent(ancestor, state);
            if (grownComponent >= 0) {
                return componentElements.get(grownComponent);
            }
        }
        return null;
    }

    private static int getGrownComponent(TokenState ancestor, TokenState state) {
        int grownComponent = -1;
        for (int i = 0; i < state.size(); i++) {
            if (ancestor.getCount(i) > state.getCount(i)) {
                return -1;
            }
            if (grownComponent < 0 && ancestor.getCount(i) < state.getCount(i)) {
                grownComponent = i;
            }
        }
        return grownComponent;
    }
}
