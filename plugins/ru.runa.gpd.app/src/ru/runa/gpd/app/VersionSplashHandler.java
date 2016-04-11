package ru.runa.gpd.app;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.splash.EclipseSplashHandler;

import ru.runa.gpd.Application;

@SuppressWarnings("restriction")
public class VersionSplashHandler extends EclipseSplashHandler {

    @Override
    public void init(final Shell splash) {
        super.init(splash);
        getContent().addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                e.gc.setTextAntialias(SWT.ON);
                e.gc.drawText(Application.getVersion().toString(), 45, splash.getSize().y - 25);
            }
        });
    }

}
