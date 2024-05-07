package ru.runa.gpd.editor.graphiti;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.gef.commands.Command;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;
import ru.runa.gpd.util.SwimlaneDisplayMode;

/**
 * This is second part of Paste method. But EMF need to do it on other transaction/command
 */
public class DrawAfterPasteCommand extends Command {
	
	private List<NamedGraphElement> elementList;
	private ProcessDefinition processDefinition;
	private DiagramEditorPage diagramPage;
	
	public DrawAfterPasteCommand( List<NamedGraphElement> elementList, ProcessDefinition processDefinition, DiagramEditorPage diagramPage) {
		this.elementList = elementList;
		this.processDefinition = processDefinition;
		this.diagramPage = diagramPage;
	}
	
	@Override
	public boolean canExecute() {
		return  Language.BPMN.equals(processDefinition.getLanguage()) && diagramPage != null;
	}
	
	@Override
	public void execute() {
		if ( processDefinition.getSwimlaneDisplayMode() == SwimlaneDisplayMode.none) {
			removeSwimlanes(elementList);
		}
        diagramPage.drawElements(diagramPage.getDiagramTypeProvider().getDiagram(), elementList);
		List<AbstractTransition> transitions = new ArrayList<AbstractTransition>();
		for (NamedGraphElement element : elementList) {
			transitions.addAll(element.getChildrenRecursive(Transition.class));
			transitions.addAll(element.getChildrenRecursive(DottedTransition.class));
		}
        diagramPage.drawTransitions(transitions);
	}
	
	private void removeSwimlanes(List<NamedGraphElement> elementList ) {
		List<NamedGraphElement> toRemove = new ArrayList<NamedGraphElement>();
		for ( NamedGraphElement element : elementList) {
			if ( element instanceof Swimlane ) {
				toRemove.add(element);
				// this crazy line created because element.getConstraint() == null is checking of visibility for swimlane in many places
				element.setConstraint(null);
			}
		}
		elementList.removeAll(toRemove);
	}
	
	@Override
	public void undo() {
		super.undo();
		// TODO How to remove graphic presentation of element(nodes)?
	}

    @Override
    public String getLabel() {
        return Localization.getString(this.getClass().getSimpleName());
    }

}
