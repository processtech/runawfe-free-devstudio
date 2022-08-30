package ru.runa.gpd.lang.model.bpmn;

import java.util.List;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.IReceiveMessageNode;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;

public class CatchEventNode extends AbstractEventNode implements IReceiveMessageNode, IBoundaryEvent, IBoundaryEventContainer {

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    @Override
    protected void validateOnEmptyRules(List<ValidationError> errors) {
        if (getEventNodeType() == EventNodeType.error && getParent() instanceof IBoundaryEventContainer) {
            return;
        }
        super.validateOnEmptyRules(errors);
    }
    
	@Override
	protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
		if (getParent() instanceof IBoundaryEvent) {
			// boundary event
			return false;
		}
		return super.allowArrivingTransition(source, transitions);
	}

	@Override
	public void validate(List<ValidationError> errors, IFile definitionFile) {
		super.validate(errors, definitionFile);
		if (getParent() instanceof IBoundaryEvent && getArrivingTransitions().size() > 0) {
			errors.add(ValidationError.createLocalizedError(this, "unresolved Arriving Transition"));
		}
	}
}
