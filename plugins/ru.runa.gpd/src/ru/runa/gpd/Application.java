package ru.runa.gpd;

import java.io.File;
import java.net.URL;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.internal.location.LocationHelper;
import org.eclipse.osgi.internal.location.Locker;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import ru.runa.gpd.ui.dialog.InfoWithDetailsDialog;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

    private static final String DEFAULT_LOCK_FILENAME = ".metadata/.lock";

    private Locker workspaceLocker = null;

    @Override
    public Object start(IApplicationContext context) throws Exception {
        Display display = PlatformUI.createDisplay();
        try {
            URL url = Platform.getInstanceLocation().getURL();
            Locker locker = LocationHelper.createLocker(new File(url.getPath() + DEFAULT_LOCK_FILENAME), LocationHelper.LOCKING_NIO, false);
            if (locker.isLocked()) {
                (new InfoWithDetailsDialog(MessageDialog.INFORMATION, Localization.getString("message.information"),
                        Localization.getString("RunaWfeDsAlreadyActivated", url.getPath()), null) {
                    @Override
                    protected void createButtonsForButtonBar(Composite parent) {
                        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
                    }
                }).open();
            } else {
                locker.lock();
                workspaceLocker = locker;
                int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
                if (returnCode == PlatformUI.RETURN_RESTART) {
                    return EXIT_RESTART;
                }
            }
            return EXIT_OK;
        } finally {
            display.dispose();
        }
    }

    @Override
    public void stop() {
        if (workspaceLocker != null) {
            workspaceLocker.release();
        }
    }
    
    public static Version getVersion() {
        Bundle bundle = Activator.getDefault().getBundle();
        return bundle.getVersion();
    }
}
