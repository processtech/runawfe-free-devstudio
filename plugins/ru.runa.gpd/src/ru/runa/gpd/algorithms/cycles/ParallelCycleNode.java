package ru.runa.gpd.algorithms.cycles;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.lang.model.Node;

public class ParallelCycleNode extends CycleNode {

    private List<CycleNode> sources = new ArrayList<>();

    public ParallelCycleNode(Node source, int index) {
        super(source, index);
    }

    public void addParent(CycleNode parent) {
        sources.add(parent);
    }

    public List<CycleNode> getParents() {
        return sources;
    }

    public boolean canBeReached(List<CycleNode> sources) {
        return sources.containsAll(this.sources);
    }

}