package ru.runa.gpd.formeditor.wysiwyg;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.resources.Messages;

public class RegulationsHTMLEditor extends MultiPageEditorPart implements IResourceChangeListener {
    public static final int CLOSED = 197;
    public static final String ID = "ru.runa.gpd.wysiwyg.RegulationsHTMLEditor";
    private Text sourceEditor;
    private Browser browser;
    private boolean browserLoaded = false;
    private static final Pattern pattern = Pattern.compile("^(.*?<(body|BODY).*?>)(.*?)(</(body|BODY)>.*?)$", Pattern.DOTALL);
    private static RegulationsHTMLEditor lastInitializedInstance;

    private synchronized boolean isBrowserLoaded() {
        return browserLoaded;
    }

    private synchronized void setBrowserLoaded(boolean browserLoaded) {
        this.browserLoaded = browserLoaded;
    }

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        lastInitializedInstance = this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == ITextEditor.class) {
            return sourceEditor;
        }
        return super.getAdapter(adapter);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {}

    @Override
    protected void createPages() {
        sourceEditor = new Text(getContainer() , SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        sourceEditor.setText((String) getEditorInput().getAdapter(String.class));
        int pageNumber = 0;
        try {
            browser = new Browser(getContainer(), SWT.NULL);
            browser.addOpenWindowListener(new BrowserWindowHelper(getContainer().getDisplay()));
            new GetHTMLCallbackFunction(browser);
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
        
        addPage(sourceEditor);
    	setPageText(pageNumber++, Messages.getString("wysiwyg.source.tab_name"));
        
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
                                if (!browser.isDisposed()) {
                                    browser.setUrl(WebServerUtils.getEditorURL());
                                }
                            }
                        });
                        monitorDialog.setCancelable(true);
                        while (!isBrowserLoaded() && !monitor.isCanceled()) {
                            Thread.sleep(1000);
                        }
                        monitor.worked(1);
                        display.asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                if (!browser.isDisposed()) {
                                    setActivePage(0);
                                }
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

    // Used from servlets
    public static RegulationsHTMLEditor getCurrent() {
        IEditorPart editor = EditorsPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getActivePage().getActiveEditor();
        if (editor instanceof FormEditor) {
            return (RegulationsHTMLEditor) editor;
        }
        if (lastInitializedInstance != null) {
            return lastInitializedInstance;
        }
        throw new RuntimeException("No editor instance initialized");
    }

    
    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {}
    
    
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void dispose() {
        firePropertyChange(CLOSED);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    @Override
    protected void pageChange(int newPageIndex) {
        if (isBrowserLoaded()) {
            if (newPageIndex == 1) {
                syncBrowser2Editor();
            } else if (newPageIndex == 0) {
                syncEditor2Browser();
            }
        } else if (EditorsPlugin.DEBUG) {
            PluginLogger.logInfo("pageChange to = " + newPageIndex + " but editor is not loaded yet");
        }
        super.pageChange(newPageIndex);
    }

    private boolean syncBrowser2Editor() {
        if (browser != null) {
            boolean result = browser.execute("getHTML()");
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("syncBrowser2Editor = " + result);
            }
            return result;
        }
        return false;
    }

    private void syncEditor2Browser() {
        String html = sourceEditor.getText();
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            html = matcher.group(3);
        }
        html = html.replaceAll("\r\n", "\n");
        html = html.replaceAll("\r", "\n");
        html = html.replaceAll("\n", "\\\\n");
        html = html.replaceAll("'", "\\\\'");
        if (browser != null) {
            boolean result = browser.execute("setHTML('" + html + "')");
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("syncEditor2Browser = " + result);
            }
        }
    }

    private class GetHTMLCallbackFunction extends BrowserFunction {

        public GetHTMLCallbackFunction(Browser browser) {
            super(browser, "getHTMLCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            String html = (String) arguments[0];
            if (!html.equals(sourceEditor.getText())) {
                sourceEditor.setText(html);
            }
            return null;
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
            setBrowserLoaded(true);
            return null;
        }
    }

}

