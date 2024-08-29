package ru.runa.gpd.algorithms.cycles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.model.bpmn.ThrowEventNode;
import ru.runa.wfe.lang.bpmn2.BusinessRule;
import ru.runa.wfe.lang.bpmn2.CatchEventNode;
import ru.runa.wfe.lang.bpmn2.ExclusiveGateway;
import ru.runa.wfe.lang.bpmn2.ParallelGateway;

class CycleNode implements Comparable<CycleNode> {
    private static final List<Class<?>> TROUGH_NODES = Arrays.asList(StartState.class, BusinessRule.class, ScriptTask.class,
            ExclusiveGateway.class, ParallelGateway.class, Timer.class, ThrowEventNode.class, EndTokenState.class, EndState.class);
    private static final List<Class<?>> STOP_NODES = Arrays.asList(TaskState.class, MultiTaskState.class, CatchEventNode.class);
    private final int id;
    private final Node source;
    private final List<CycleNode> targets = new ArrayList<>();

    public CycleNode(Node source, int id) {
        this.id = id;
        this.source = source;
    }

    public int getId() {
        return id;
    }

    public Node getSource() {
        return source;
    }

    public void addTarget(CycleNode cycleNode) {
        targets.add(cycleNode);
    }

    public List<CycleNode> getTargets() {
        return targets;
    }

    public NodeClassification getClassification() {
        if (TROUGH_NODES.contains(source.getClass())) {
            return NodeClassification.THROUGH;
        } else if (STOP_NODES.contains(source.getClass())) {
            return NodeClassification.STOP;
        }
        return NodeClassification.NOT_DEFINED;
    }

    @Override
    public int compareTo(CycleNode o) {
        return id - o.id;
    }
}