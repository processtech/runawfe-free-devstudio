package ru.runa.gpd.editor;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import com.google.common.collect.Lists;

/**
 * An action which selects all edit parts in the active workbench part.
 * Code copied from org.eclipse.gef.ui.actions.SelectAllAction; transition included
 */
@SuppressWarnings("restriction")
public class SelectAllAction extends Action {
    private IWorkbenchPart part;

    /**
     * Constructs a <code>SelectAllAction</code> and associates it with the
     * given part.
     * 
     * @param part
     *            The workbench part associated with this SelectAllAction
     */
    public SelectAllAction(IWorkbenchPart part) {
        this.part = part;
        setText(GEFMessages.SelectAllAction_Label);
        setToolTipText(GEFMessages.SelectAllAction_Tooltip);
        setId(ActionFactory.SELECT_ALL.getId());
    }

    /**
     * Selects all edit parts in the active workbench part.
     */
    @Override
    public void run() {
        GraphicalViewer viewer = (GraphicalViewer) part.getAdapter(GraphicalViewer.class);
        if (viewer != null) {
            viewer.setSelection(new StructuredSelection(getSelectableEditParts(viewer)));
        }
    }

    /**
     * Retrieves edit parts which can be selected
     * 
     * @param viewer
     *            from which the edit parts are to be retrieved
     * @return list of selectable EditParts
     * @since 3.5
     */
    private List<EditPart> getSelectableEditParts(GraphicalViewer viewer) {
        List<EditPart> selection = Lists.newArrayList();
        addEditPartChildren(viewer.getContents(), selection);
        return selection;
    }

    private void addEditPartChildren(EditPart parent, List<EditPart> selection) {
        for (EditPart childPart : (List<EditPart>) parent.getChildren()) {
            if (childPart.isSelectable() && childPart.getTargetEditPart(new Request(RequestConstants.REQ_SELECTION)) == childPart ) {
                selection.add(childPart);
            }
            if (childPart instanceof GraphicalEditPart) {
                for (EditPart transitionPart : (List<EditPart>) ((GraphicalEditPart) childPart).getTargetConnections()) {
                    if (transitionPart.isSelectable()) {
                        selection.add(transitionPart);
                    }
                }
            }
            addEditPartChildren(childPart, selection);
        }
    }
}
