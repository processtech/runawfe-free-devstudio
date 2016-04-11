package ru.runa.gpd.bot;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.wfe.WFEServerBotElementImporter;

public class BotTaskDeployCommand extends BotTaskExportCommand {
    public BotTaskDeployCommand(IResource exportResource, OutputStream outputStream) {
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
                        WFEServerBotElementImporter.getInstance().deployBot(exportResource.getProject().getName(), baos.toByteArray());
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
