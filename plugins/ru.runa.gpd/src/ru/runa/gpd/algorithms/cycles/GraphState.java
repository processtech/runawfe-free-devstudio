package ru.runa.gpd.algorithms.cycles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphState implements Comparable<Object> {
    private final List<Integer> states;
    private final Set<GraphState> nextStates = new HashSet<>();
    private int weight = 0;
    private int stops = 0;

    public GraphState(List<CycleNode> containBreakpoint, int size) {
        states = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            states.add(0);
        }
        for (CycleNode node : containBreakpoint) {
            if (weight == 0 && node.getClassification() != NodeClassification.THROUGH) {
                this.weight = 1;
            }
            states.set(node.getId(), states.get(node.getId()) + 1);
        }
    }

    public void setStops(int previousStops) {
        stops = previousStops + weight;
    }

    public int getWeight() {
        return weight;
    }

    public int getStops() {
        return stops;
    }

    public List<Integer> getStates() {
        return states;
    }

    public void addNextState(GraphState next) {
        nextStates.add(next);
    }

    public Set<GraphState> getNextStates() {
        return nextStates;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GraphState)) {
            return false;
        }
        GraphState obj = (GraphState) o;
        if (this == o) {
            return true;
        }
        if (states.size() != obj.states.size()) {
            return false;
        }
        boolean result = true;
        for (int i = 0; i < states.size(); ++i) {
            result &= states.get(i).equals(obj.states.get(i));
        }
        return result;
    }

    @Override
    public int compareTo(Object o) {
        return stops - ((GraphState) o).stops;
    }
}