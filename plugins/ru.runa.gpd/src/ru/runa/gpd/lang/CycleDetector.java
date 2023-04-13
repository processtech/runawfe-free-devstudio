package ru.runa.gpd.lang;

import java.util.Set;
import java.util.HashSet;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.Decision;


public class CycleDetector {

  private Set<GraphElement> visited;
  private Set<GraphElement> onStack;

  private boolean goThroughDecision = false;

  public CycleDetector() {
    visited = new HashSet<>();
    onStack = new HashSet<>();
  }

  public boolean hasCycle(GraphElement child, AbstractTransition transition) {
    visited.add(child);
    onStack.add(child);

    if (child instanceof Decision) {
      goThroughDecision = true;
    }

    if (transition.getTarget()==null) {
    	return false;
    }
    
    for (AbstractTransition leavingTransitions : transition.getTarget().getLeavingTransitions()) {
      Node targetNode = leavingTransitions.getTarget();
      if (targetNode != null && !visited.contains(targetNode)) {
        if (hasCycle(leavingTransitions.getTarget(), leavingTransitions)) {
          return true && goThroughDecision;
        }
      } else if (onStack.contains(targetNode)) {
        return true && goThroughDecision;
      }
    }

    onStack.remove(transition);
    return false;
  }
}