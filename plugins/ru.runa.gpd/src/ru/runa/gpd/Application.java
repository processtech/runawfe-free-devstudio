package ru.runa.gpd;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
    @Override
    public Object start(IApplicationContext context) throws Exception {
        //org.eclipse.ui.internal.misc.Policy.DEBUG_SWT_GRAPHICS = true;
        Display display = PlatformUI.createDisplay();
        try {
            int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return EXIT_RESTART;
            }
            return EXIT_OK;
        } finally {
            display.dispose();
        }
    }

    @Override
    public void stop() {
    }
    
    public static Version getVersion() {
        Bundle bundle = Activator.getDefault().getBundle();
        return bundle.getVersion();
    }
}
