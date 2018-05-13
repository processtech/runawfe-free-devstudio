package ru.runa.gpd.formeditor.wysiwyg;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.RegulationsRegistry;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.htmleditor.editors.HTMLConfiguration;
import ru.runa.gpd.htmleditor.editors.HTMLSourceEditor;
import ru.runa.gpd.util.EditorUtils;

import com.google.common.base.Throwables;
import com.google.common.io.Files;

public class RegulationsViewer extends MultiPageEditorPart implements IResourceChangeListener {
    private HTMLSourceEditor sourceEditor;
    private Browser browser;

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        this.setPartName(Localization.getString("regulations"));
    }

    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (sourceEditor != null && sourceEditor.getEditorInput() != null) {
            EditorUtils.closeEditorIfRequired(event, ((IFileEditorInput) sourceEditor.getEditorInput()).getFile(), this);
        }
    }

    @Override
    protected void createPages() {
        sourceEditor = new HTMLSourceEditor(new HTMLConfiguration(EditorsPlugin.getDefault().getColorProvider()));
        int pageNumber = 0;
        try {
            browser = new Browser(getContainer(), SWT.NULL);
            browser.addOpenWindowListener(new BrowserWindowHelper(getContainer().getDisplay()));
            new OnLoadCallbackFunction(browser);
            browser.addProgressListener(new ProgressAdapter() {
                @Override
                public void completed(ProgressEvent event) {
                    if (EditorsPlugin.DEBUG) {
                        PluginLogger.logInfo("completed " + event);
                    }
                }
            });
            addPage(browser);
            setPageText(pageNumber++, Messages.getString("wysiwyg.design.tab_name"));
        } catch (Throwable th) {
            PluginLogger.logError(Messages.getString("wysiwyg.design.create_error"), th);
        }
        try {
            addPage(sourceEditor, getEditorInput());
            setPageText(pageNumber++, Messages.getString("wysiwyg.source.tab_name"));
        } catch (PartInitException e) {
            Throwables.propagate(e);
        }
        if (browser == null) {
            return;
        }
        syncCss();
        try {
            final Display display = Display.getCurrent();
            final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(getSite().getShell());
            final IRunnableWithProgress runnable = new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask(Messages.getString("editor.task.init_wysiwyg"), 10);
                        WebServerUtils.startWebServer(monitor, 9);
                        monitor.subTask(Messages.getString("editor.subtask.waiting_init"));
                        display.asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                monitorDialog.setCancelable(true);
                                browser.setUrl(WebServerUtils.getRegulationsViewerUrl());
                            }
                        });
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            };
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        monitorDialog.run(true, false, runnable);
                    } catch (InvocationTargetException e) {
                        PluginLogger.logError(Messages.getString("wysiwyg.design.create_error"), e.getTargetException());
                    } catch (InterruptedException e) {
                        EditorsPlugin.logError("Web editor page", e);
                    }
                }
            });
        } catch (Exception e) {
            MessageDialog.openError(getContainer().getShell(), Messages.getString("wysiwyg.design.create_error"), e.getCause().getMessage());
            EditorsPlugin.logError("Web editor page", e);
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public boolean isDirty() {
        // readonly mode
        return false;
    }

    @Override
    public void doSaveAs() {
    }

    private void syncCss() {
        try {
            if (RegulationsRegistry.getCssStyles() == null) {
                return;
            }
            File formCssFile = new File(WebServerUtils.getEditorDirectory(), "regulations.css");
            if (formCssFile.exists()) {
                formCssFile.delete();
            }
            formCssFile.createNewFile();
            Files.write(RegulationsRegistry.getCssStyles().getBytes(), formCssFile);
        } catch (IOException e) {
            PluginLogger.logError(e);
        }
    }

    private class OnLoadCallbackFunction extends BrowserFunction {
        public OnLoadCallbackFunction(Browser browser) {
            super(browser, "onLoadCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("Invoked OnLoadCallbackFunction");
            }
            String html = sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).get();
            html = html.replaceAll("\r\n", "\n");
            html = html.replaceAll("\r", "\n");
            html = html.replaceAll("\n", "\\\\n");
            html = html.replaceAll("'", "\\\\'");
            boolean result = browser.execute("setHTML('" + html + "')");
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("syncEditor2Browser = " + result);
            }
            return result;
        }
    }

}
