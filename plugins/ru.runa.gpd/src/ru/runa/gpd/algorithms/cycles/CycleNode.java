package ru.runa.gpd.algorithms.cycles;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.wfe.lang.bpmn2.BusinessRule;
import ru.runa.wfe.lang.bpmn2.CatchEventNode;
import ru.runa.wfe.lang.bpmn2.ExclusiveGateway;
import ru.runa.wfe.lang.bpmn2.ParallelGateway;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.model.bpmn.ThrowEventNode;

class CycleNode implements Comparable<CycleNode> {

    private int id;
    private Node source;
    private List<CycleNode> targets = new ArrayList<>();

    private final List<Class<?>> troughNodes = Arrays.asList(new Class<?>[] { StartState.class, BusinessRule.class, ScriptTask.class,
            ExclusiveGateway.class, ParallelGateway.class, Timer.class, ThrowEventNode.class, EndTokenState.class, EndState.class, });

    private final List<Class<?>> stopNodes = Arrays.asList(new Class<?>[] { TaskState.class, MultiTaskState.class, CatchEventNode.class, });

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
        if (troughNodes.contains(source.getClass())) {
            return NodeClassification.THROUGH;
        } else if (stopNodes.contains(source.getClass())) {
            return NodeClassification.STOP;
        }
        return NodeClassification.NOT_DEFINED;
    }

    @Override
    public int compareTo(CycleNode o) {
        return id - o.id;
    }
}