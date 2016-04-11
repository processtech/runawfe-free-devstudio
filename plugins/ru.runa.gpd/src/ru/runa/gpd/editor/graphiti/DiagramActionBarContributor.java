package ru.runa.gpd.editor.graphiti;

import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.graphiti.platform.IPlatformImageConstants;
import org.eclipse.graphiti.ui.internal.action.ToggleContextButtonPadAction;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.RetargetAction;

import ru.runa.gpd.editor.ProcessEditorContributor;

// see org.eclipse.graphiti.ui.editor.DiagramEditorActionBarContributor
public class DiagramActionBarContributor extends ProcessEditorContributor {
    /**
     * Creates and initialises all Actions. See the corresponding method in the
     * super class.
     * 
     * @see org.eclipse.gef.ui.actions.ActionBarContributor
     */
    @Override
    protected void buildActions() {
        super.buildActions();

        // addRetargetAction(new
        // AlignmentRetargetAction(PositionConstants.LEFT));
        // addRetargetAction(new
        // AlignmentRetargetAction(PositionConstants.CENTER));
        // addRetargetAction(new
        // AlignmentRetargetAction(PositionConstants.RIGHT));
        // addRetargetAction(new
        // AlignmentRetargetAction(PositionConstants.TOP));
        // addRetargetAction(new
        // AlignmentRetargetAction(PositionConstants.MIDDLE));
        // addRetargetAction(new
        // AlignmentRetargetAction(PositionConstants.BOTTOM));
        // addRetargetAction(new MatchWidthRetargetAction());
        // addRetargetAction(new MatchHeightRetargetAction());

        addRetargetAction(new ZoomInRetargetAction());
        addRetargetAction(new ZoomOutRetargetAction());
        // addRetargetAction(new
        // RetargetAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY,
        // Messages.DiagramEditorActionBarContributor_Grid,
        // IAction.AS_CHECK_BOX));
        // addRetargetAction(new
        // RetargetAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY,
        // Messages.DiagramEditorActionBarContributor_SnapGeometry,
        // IAction.AS_CHECK_BOX));

        // Bug 323351: Add button to toggle a flag if the context pad buttons
        // shall be shown or not
        RetargetAction toggleContextPadAction = new RetargetAction(ToggleContextButtonPadAction.ACTION_ID, ToggleContextButtonPadAction.TEXT,
                IAction.AS_CHECK_BOX);
        toggleContextPadAction.setImageDescriptor(GraphitiUi.getImageService().getImageDescriptorForId(IPlatformImageConstants.IMG_TOGGLE_PAD));
        addRetargetAction(toggleContextPadAction);
        // End bug 323351
    }

    /**
     * Adds Actions to the given IToolBarManager, which is displayed above the
     * editor. See the corresponding method in the super class.
     * 
     * @param tbm
     *            the {@link IToolBarManager}
     * 
     * @see org.eclipse.ui.part.EditorActionBarContributor
     */
    @Override
    public void contributeToToolBar(IToolBarManager tbm) {
        // tbm.add(new Separator());
        // tbm.add(getAction(GEFActionConstants.ALIGN_LEFT));
        // tbm.add(getAction(GEFActionConstants.ALIGN_CENTER));
        // tbm.add(getAction(GEFActionConstants.ALIGN_RIGHT));
        // tbm.add(new Separator());
        // tbm.add(getAction(GEFActionConstants.ALIGN_TOP));
        // tbm.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
        // tbm.add(getAction(GEFActionConstants.ALIGN_BOTTOM));
        // tbm.add(new Separator());
        // tbm.add(getAction(GEFActionConstants.MATCH_WIDTH));
        // tbm.add(getAction(GEFActionConstants.MATCH_HEIGHT));

        // Bug 323351: Add button to toggle a flag if the context pad buttons
        // shall be shown or not
        tbm.add(new Separator());
        tbm.add(getAction(ToggleContextButtonPadAction.ACTION_ID));
        // End bug 323351

        tbm.add(new Separator());
        tbm.add(getAction(GEFActionConstants.ZOOM_OUT));
        tbm.add(getAction(GEFActionConstants.ZOOM_IN));
        ZoomComboContributionItem zoomCombo = new ZoomComboContributionItem(getPage());
        tbm.add(zoomCombo);
        super.contributeToToolBar(tbm);
    }
}
