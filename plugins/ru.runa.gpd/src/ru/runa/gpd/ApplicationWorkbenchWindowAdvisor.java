package ru.runa.gpd;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import ru.runa.gpd.editor.ConfigurableTitleEditorPart;
import ru.runa.gpd.util.UiUtil;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setShowStatusLine(true);
        configurer.setTitle(Localization.getString("ApplicationWorkbenchWindowAdvisor.window.title"));
        configurer.getWorkbenchConfigurer().setSaveAndRestore(true);
        configurer.setShowCoolBar(true);
    }

    @Override
    public void postWindowOpen() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(new IPartListener() {

            @Override
            public void partActivated(IWorkbenchPart part) {
            }

            @Override
            public void partBroughtToTop(IWorkbenchPart part) {
            }

            @Override
            public void partClosed(IWorkbenchPart part) {
                if (part instanceof ConfigurableTitleEditorPart) {
                    UiUtil.updateEditorPartNames(true);
                }
            }

            @Override
            public void partDeactivated(IWorkbenchPart part) {
            }

            @Override
            public void partOpened(IWorkbenchPart part) {
                if (part instanceof ConfigurableTitleEditorPart) {
                    UiUtil.updateEditorPartNames(true);
                }
            }
        });
    }

    @Override
    public void openIntro() {
        UiUtil.hideQuickAccess();
        super.openIntro();
    }

}
