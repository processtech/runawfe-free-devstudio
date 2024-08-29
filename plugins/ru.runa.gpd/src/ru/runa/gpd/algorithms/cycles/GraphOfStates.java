package ru.runa.gpd.algorithms.cycles;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.bpmn.ParallelGateway;

public class GraphOfStates {

    private List<CycleNode> nodes;
    private GraphState root;
    private List<GraphState> initilizedStates = new ArrayList<>();

    public GraphOfStates(List<CycleNode> nodes) {
        this.nodes = nodes;
        List<CycleNode> startState = new ArrayList<>();
        for (CycleNode node : nodes)
            if (node.getSource() instanceof StartState) {
                startState.add(node);
                break;
            }
        root = new GraphState(startState, nodes.size());
        root.setStops(0);
        initilizedStates.add(root);
        createTree(root);
    }

    private List<CycleNode> getBreakpointNodes(GraphState currentState) {
        List<CycleNode> breakpointNodes = new ArrayList<>();
        for (int i = 0; i < nodes.size(); ++i) {
            int breackPointCount = currentState.getStates().get(i);
            while (breackPointCount > 0) {
                breakpointNodes.add(nodes.get(i));
                breackPointCount--;
            }
        }
        return breakpointNodes;
    }

    private void createTree(GraphState currentState) {

        // Get all nodes with breakpoint
        List<CycleNode> breakpointNodes = getBreakpointNodes(currentState);

        // Find all combination of next states
        List<List<CycleNode>> nodesReceivers = new ArrayList<>();
        List<List<CycleNode>> nodesToRemove = new ArrayList<>();
        List<List<CycleNode>> nodesToAdd = new ArrayList<>();
        List<CycleNode> ignoredSources = new ArrayList<>();
        nodesReceivers.add(new ArrayList<>());

        for (CycleNode breakpointNode : breakpointNodes) {
            if (ignoredSources.contains(breakpointNode))
                continue;
            // When source is parallel all states will be in one branch
            if (breakpointNode.getSource() instanceof ParallelGateway) {
                for (List<CycleNode> nodeReceiver : nodesReceivers)
                    nodeReceiver.addAll(breakpointNode.getTargets());
            } else {
                for (CycleNode target : breakpointNode.getTargets()) {
                    for (List<CycleNode> nodeReceiver : nodesReceivers) {
                        List<CycleNode> updated = cloneNodes(nodeReceiver);
                        nodesToAdd.add(updated);
                        // When target is parallel gate check whether it can be reached from current state and update by rules
                        if (target instanceof ParallelCycleNode) {
                            if (((ParallelCycleNode) target).canBeReached(breakpointNodes)) {
                                updated.add(target);
                                ignoredSources.addAll(((ParallelCycleNode) target).getParents());
                            } else {
                                updated.add(breakpointNode);
                            }
                        } else {
                            // When nothing is parallel make all possible combinations of next iteration
                            updated.add(target);
                        }
                        nodesToRemove.add(nodeReceiver);
                    }
                }
                nodesReceivers.removeAll(nodesToRemove);
                nodesReceivers.addAll(nodesToAdd);
                nodesToAdd.clear();
            }
        }

        for (List<CycleNode> nodeReceiver : nodesReceivers) {
            if (nodeReceiver.isEmpty()) {
                nodesToRemove.add(nodeReceiver);
            }
        }
        nodesReceivers.removeAll(nodesToRemove);

        List<GraphState> states = new ArrayList<>();
        for (List<CycleNode> nodeReceiver : nodesReceivers) {
            List<GraphState> nextStates = createNextStates(nodeReceiver, currentState);
            states.addAll(nextStates);
        }

        for (GraphState state : states) {
            int stateIndex = initilizedStates.indexOf(state);
            if (stateIndex != -1) {
                if (isDeadend(state, currentState)) {
                    return;
                }
                state = initilizedStates.get(stateIndex);
            } else {
                initilizedStates.add(state);
            }
            currentState.addNextState(state);
            if (stateIndex == -1) {
                createTree(state);
            }
        }
    }

    private boolean isDeadend(GraphState nextState, GraphState previousState) {
        if (!nextState.equals(previousState)) {
            return false;
        }
        boolean deadend = true;
        for (int i = 0; i < nodes.size(); ++i) {
            if (nextState.getStates().get(i) > 0) {
                for (CycleNode target : nodes.get(i).getTargets()) {
                    deadend &= target instanceof ParallelCycleNode;
                }
            }
        }
        return deadend;
    }

    private List<GraphState> createNextStates(List<CycleNode> recievers, GraphState previousState) {
        List<CycleNode> through = new ArrayList<>();
        List<CycleNode> stop = new ArrayList<>();
        for (CycleNode reciever : recievers) {
            if (reciever.getClassification() == NodeClassification.THROUGH) {
                through.add(reciever);
            } else {
                stop.add(reciever);
            }
        }
        List<GraphState> states = new ArrayList<>();
        if (through.isEmpty()) {
            states.add(new GraphState(stop, nodes.size()));
        } else if (stop.isEmpty()) {
            states.add(new GraphState(through, nodes.size()));
        } else {
            List<CycleNode> allRecievers = cloneNodes(through);
            allRecievers.addAll(stop);
            states.add(new GraphState(through, nodes.size()));
            states.add(new GraphState(allRecievers, nodes.size()));
        }
        return states;
    }

    private List<CycleNode> cloneNodes(List<CycleNode> toClone) {
        List<CycleNode> clone = new ArrayList<>();
        for (CycleNode node : toClone)
            clone.add(node);
        return clone;
    }

    private List<GraphState> cloneStates(List<GraphState> toClone) {
        List<GraphState> clone = new ArrayList<>();
        for (GraphState node : toClone) {
            clone.add(node);
        }
        return clone;
    }

    public List<CycleNode> getShortPeriodCycle() {
        List<CycleNode> cycle = initShortPeriodCycle(new ArrayList<>(), root, 0);
        if (cycle != null) {
            removeDeadends(cycle);
        }
        return cycle;
    }

    private List<CycleNode> initShortPeriodCycle(List<GraphState> visitedStates, GraphState current, int stops) {
        if (visitedStates.contains(current)) {
            if (current.getStops() >= stops) {
                return findShortPeriodCycle(visitedStates, current);
            } else {
                return null;
            }
        }
        visitedStates.add(current);
        Set<GraphState> nextStates = current.getNextStates();
        Deque<GraphState> invocations = new ArrayDeque<>();
        for (GraphState next : nextStates) {
            next.setStops(stops);
            if (next.getStops() > stops) {
                invocations.addLast(next);
            } else {
                invocations.addFirst(next);
            }
        }
        List<CycleNode> loop = null;
        while (!invocations.isEmpty() && loop == null) {
            GraphState next = invocations.pollFirst();
            List<GraphState> updatedVisited = cloneStates(visitedStates);
            loop = initShortPeriodCycle(updatedVisited, next, stops + next.getWeight());
        }
        return loop;
    }

    private List<CycleNode> findShortPeriodCycle(List<GraphState> visitedStates, GraphState lastRepeat) {
        List<CycleNode> loop = new ArrayList<>();
        for (int i = visitedStates.size() - 1; i >= 0; --i) {
            for (int j = 0; j < nodes.size(); ++j) {
                if (visitedStates.get(i).getStates().get(j) > 0) {
                    loop.add(nodes.get(j));
                }
            }
            if (visitedStates.get(i).equals(lastRepeat)) {
                break;
            }
        }
        return loop;
    }

    private void removeDeadends(List<CycleNode> cycleToTest) {
        List<CycleNode> toRemove = new ArrayList<>();
        for (CycleNode node : cycleToTest) {
            if (!canReachEveryone(cycleToTest, node)) {
                toRemove.add(node);
            }
        }
        cycleToTest.removeAll(toRemove);
    }

    private boolean canReachEveryone(List<CycleNode> cycle, CycleNode currentNode) {
        Map<CycleNode, Boolean> isReached = new HashMap<>();
        for (CycleNode node : cycle) {
            isReached.put(node, false);
        }
        Deque<CycleNode> invocationList = new ArrayDeque<>();
        invocationList.add(currentNode);
        while (!invocationList.isEmpty()) {
            CycleNode source = invocationList.pollFirst();
            isReached.put(source, true);
            for (CycleNode target : source.getTargets()) {
                if (isReached.keySet().contains(target) && !isReached.get(target)) {
                    invocationList.add(target);
                }
            }
        }
        boolean isInCycle = true;
        for (CycleNode node : isReached.keySet()) {
            isInCycle &= isReached.get(node);
        }
        return isInCycle;
    }
}