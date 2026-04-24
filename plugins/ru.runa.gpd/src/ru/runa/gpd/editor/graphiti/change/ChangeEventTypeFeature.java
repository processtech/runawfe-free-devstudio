package ru.runa.gpd.editor.graphiti.change;

import java.util.List;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.update.DeleteElementFeature;
import ru.runa.gpd.lang.model.bpmn.AbstractEventNode;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.ConnectableViaDottedTransition;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;
import ru.runa.gpd.lang.model.bpmn.EventNodeType;

public class ChangeEventTypeFeature extends ChangePropertyFeature<AbstractEventNode, EventNodeType> {

    private List<DottedTransition> dottedTransitions;

    public ChangeEventTypeFeature(AbstractEventNode target, EventNodeType newValue) {
        super(target, target.getEventNodeType(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        if (target instanceof CatchEventNode) {
            handleArrivingDottedTransitionsDeleted((CatchEventNode) target);
        }
        target.setEventNodeType(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setEventNodeType(oldValue);
        if (target instanceof CatchEventNode) {
            undoArrivingDottedTransitions();
        }
    }

    @Override
    public String getName() {
        return Localization.getString("property.eventType");
    }

    private void handleArrivingDottedTransitionsDeleted(CatchEventNode node) {
        if (node.isUseExternalStorageIn() && newValue != EventNodeType.conditional) {
            dottedTransitions = node.getArrivingDottedTransitions();
            dottedTransitions.forEach(this::deleteDottedConnection);
        }
    }

    private void deleteDottedConnection(DottedTransition transition) {
        PictogramElement pictogramElement =
                getFeatureProvider().getPictogramElementForBusinessObject(transition);

        if (pictogramElement != null) {
            Graphiti.getPeService().deletePictogramElement(pictogramElement);
        }

        DeleteElementFeature.removeDottedTransition(transition);
    }

    private void undoArrivingDottedTransitions() {
        if (dottedTransitions != null) {
            for (DottedTransition transition : dottedTransitions) {

                ConnectableViaDottedTransition source =
                        (ConnectableViaDottedTransition) transition.getSource();

                transition.setTarget(target);

                source.addLeavingDottedTransition(transition);
                ((ConnectableViaDottedTransition) target).addArrivingDottedTransition(transition);
            }
        }
    }

}
