package ru.runa.gpd.util;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchWindow;

public abstract class UiUtil {

    //
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=362420#c91
    //
    public static void hideQuickAccess() {
        MWindow model = ((WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow()).getModel();
        EModelService modelService = model.getContext().get(EModelService.class);
        MUIElement element = modelService.find("SearchField", model);
        if (element != null) {
            element.setToBeRendered(false);
        }
    }

    public static void hideToolBar(IViewSite viewSite) {
        if (viewSite instanceof PartSite) {
            ((PartSite) viewSite).getModel().getToolbar().setToBeRendered(false);
        }
    }

}
