package ru.runa.gpd.bot;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.Localization;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.wfe.bot.BotStationDoesNotExistException;

public class BotDeployCommand extends BotExportCommand {
    public BotDeployCommand(IResource exportResource, OutputStream outputStream) {
        super(exportResource, outputStream);
    }

    @Override
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
        try {
            execute(progressMonitor);
            final ByteArrayOutputStream baos = (ByteArrayOutputStream) outputStream;
            try {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            WfeServerConnector.getInstance().deployBot(exportResource.getProject().getName(), baos.toByteArray());
                        } catch (BotStationDoesNotExistException e) {
                            Dialogs.error(Localization.getString("ExportBotWizardPage.page.notExistWarning"));
                        }
                    }
                });
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        } finally {
            progressMonitor.done();
        }
    }
}
