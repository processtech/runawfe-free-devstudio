package ru.runa.gpd.app;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.splash.EclipseSplashHandler;

import ru.runa.gpd.Application;

@SuppressWarnings("restriction")
public class VersionSplashHandler extends EclipseSplashHandler {

    @Override
    public void init(final Shell splash) {
        super.init(splash);
        // TODO temporary workaround for the flipped splash image on macOS 14 (Sonoma):
        // https://github.com/eclipse-platform/eclipse.platform.swt/issues/772
        // Eclipse 4.30+ already contains this fix in Workbench:
        // https://github.com/eclipse-platform/eclipse.platform.ui/pull/1355
        // After migrating to Eclipse 4.30+ remove this call and flipBackgroundImageForMacSonomaBug(),
        // otherwise the image will be flipped twice.
        flipBackgroundImageForMacSonomaBug(splash);
        getContent().addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                e.gc.setTextAntialias(SWT.ON);
                e.gc.drawText(Application.getVersion().toString(), 45, splash.getSize().y - 25);
            }
        });
    }

    private void flipBackgroundImageForMacSonomaBug(Shell splash) {
        if (!Platform.OS_MACOSX.equals(Platform.getOS())) {
            return;
        }
        int majorVersion;
        try {
            majorVersion = Integer.parseInt(System.getProperty("os.version").split("\\.")[0]);
        } catch (RuntimeException e) {
            return;
        }
        // fixed by Apple in macOS 15, so Eclipse limited its check to 14 as well:
        // https://github.com/eclipse-platform/eclipse.platform.ui/pull/2170
        if (majorVersion != 14) {
            return;
        }
        Image background = splash.getBackgroundImage();
        if (background == null) {
            return;
        }
        Transform transform = new Transform(splash.getDisplay());
        GC gc = new GC(background);
        try {
            transform.setElements(1, 0, 0, -1, 0, 0);
            gc.setTransform(transform);
            gc.drawImage(background, 0, -background.getBounds().height);
        } finally {
            gc.dispose();
            transform.dispose();
        }
        splash.setBackgroundImage(background);
    }

}
