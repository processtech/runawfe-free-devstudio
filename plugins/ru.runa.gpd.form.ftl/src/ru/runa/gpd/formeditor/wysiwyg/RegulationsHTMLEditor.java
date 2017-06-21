package ru.runa.gpd.formeditor.wysiwyg;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.util.IOUtils;

import com.google.common.base.Charsets;

public class RegulationsHTMLEditor extends MultiPageEditorPart implements IResourceChangeListener {
    public static final int CLOSED = 197;
    public static final String ID = "ru.runa.gpd.wysiwyg.RegulationsHTMLEditor";
    private Text sourceEditor;
    private Browser browser;
    private boolean browserLoaded = false;
    private static final Pattern BODY_PATTERN = Pattern.compile("^(.*?<(body|BODY).*?>)(.*?)(</(body|BODY)>.*?)$", Pattern.DOTALL);
    private boolean dirty;
    private IFile file;
    private final int VIEW_MODE_WYSIWYG = 0;
    private final int VIEW_MODE_SOURCE = 1;
    private String currentHashSum = "";
    private String lastSavedHashSum = "";

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
        file = ((FileEditorInput) editorInput).getFile();
        IFile definitionFile = IOUtils.getProcessDefinitionFile((IFolder) file.getParent());
        ProcessDefinition processDefinition = ProcessCache.getProcessDefinition(definitionFile);
        this.setPartName(Localization.getString("Regulations.tab.title", processDefinition.getMainProcessDefinition().getName()));
    }

    private String getHashSum(String input) {
        String result = "";
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(input.getBytes());
            result = javax.xml.bind.DatatypeConverter.printHexBinary(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            PluginLogger.logError(e);
        }
        return result;
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
    public void resourceChanged(IResourceChangeEvent event) {
    }

    @Override
    protected void createPages() {
        sourceEditor = new Text(getContainer(), SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        try {
            sourceEditor.setText(prepareHtml(IOUtils.readStream(file.getContents())));
        } catch (Exception e1) {
            // TODO 2797 temporary
            e1.printStackTrace();
        }
        int pageNumber = 0;
        try {
            browser = new Browser(getContainer(), SWT.NULL);
            browser.addOpenWindowListener(new BrowserWindowHelper(getContainer().getDisplay()));
            new GetHTMLCallbackFunction(browser);
            new OnLoadCallbackFunction(browser);
            new OnEditorChangeCallbackFunction(browser);
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
        sourceEditor.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                String currentText = sourceEditor.getText();
                currentText = currentText.replaceAll("\r\n", "\n");
                currentHashSum = getHashSum(currentText);
                RegulationsHTMLEditor.this.setDirty(!lastSavedHashSum.equals(currentHashSum));
            }
        });

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
                                    setActivePage(VIEW_MODE_WYSIWYG);
                                    setDirty(true);
                                    syncBrowser2Editor();
                                    doSave(null);
                                    // Workaround for CKEditor's bug - editor
                                    // occasionally becomes dirty
                                    syncEditor2Browser();
                                    syncBrowser2Editor();
                                    doSave(null);
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

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (isDirty()) {
            try {
                String textToSave = this.sourceEditor.getText();
                textToSave = textToSave.replaceAll("\\\\n", "\n");
                IOUtils.createOrUpdateFile(file, new ByteArrayInputStream(textToSave.getBytes(Charsets.UTF_8)));
                setDirty(false);
                lastSavedHashSum = currentHashSum;
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }

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
            if (newPageIndex == VIEW_MODE_SOURCE) {
                syncBrowser2Editor();
            } else if (newPageIndex == VIEW_MODE_WYSIWYG) {
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
        html = prepareHtml(html);
        if (browser != null) {
            boolean result = browser.execute("setHTML('" + html + "')");
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("syncEditor2Browser = " + result);
            }
        }
    }

    private String prepareHtml(String html) {
        Matcher matcher = BODY_PATTERN.matcher(html);
        if (matcher.find()) {
            html = matcher.group(3);
        }
        html = html.replaceAll("\r\n", "\n");
        html = html.replaceAll("\r", "\n");
        html = html.replaceAll("\n", "\\\\n");
        html = html.replaceAll("'", "\\\\'");
        return html;
    }

    private class GetHTMLCallbackFunction extends BrowserFunction {

        public GetHTMLCallbackFunction(Browser browser) {
            super(browser, "getHTMLCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            String html = (String) arguments[0];
            if (!html.equals(prepareHtml(sourceEditor.getText()))) {
                sourceEditor.setText(html);
                currentHashSum = getHashSum(html);
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

    private class OnEditorChangeCallbackFunction extends BrowserFunction {

        public OnEditorChangeCallbackFunction(Browser browser) {
            super(browser, "onEditorChangeCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            String html = (String) arguments[0];
            currentHashSum = getHashSum(html);
            setDirty(!lastSavedHashSum.equals(currentHashSum));
            syncBrowser2Editor();
            return null;
        }
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }
}
