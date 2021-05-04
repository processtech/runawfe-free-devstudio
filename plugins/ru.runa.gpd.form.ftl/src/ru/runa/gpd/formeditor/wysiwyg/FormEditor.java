package ru.runa.gpd.formeditor.wysiwyg;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.net.BindException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.mortbay.util.MultiException;
import ru.runa.gpd.Activator;
import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentIdGenerator;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.formeditor.ftl.TemplateProcessor;
import ru.runa.gpd.formeditor.ftl.conv.DesignUtils;
import ru.runa.gpd.formeditor.ftl.conv.EditorHashModel;
import ru.runa.gpd.formeditor.ftl.ui.ComponentParametersDialog;
import ru.runa.gpd.formeditor.ftl.ui.FormComponentsView;
import ru.runa.gpd.formeditor.ftl.ui.IComponentDropTarget;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.formeditor.vartag.VarTagUtil;
import ru.runa.gpd.htmleditor.editors.HTMLConfiguration;
import ru.runa.gpd.htmleditor.editors.HTMLSourceEditor;
import ru.runa.gpd.jointformeditor.JointFormEditor;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.view.SelectionProvider;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.InternalApplicationException;

/**
 * org.eclipse.ui.texteditor.BasicTextEditorActionContributor
 * </p>
 */
public class FormEditor extends MultiPageEditorPart implements IResourceChangeListener, IComponentDropTarget {

    public static final String ID = "ru.runa.gpd.wysiwyg.WYSIWYGHTMLEditor";
    public static final int CLOSED = 197;
    private static final Pattern HTML_CONTENT_PATTERN = Pattern.compile("^(.*?<(body|BODY).*?>)(.*?)(</(body|BODY)>.*?)$", Pattern.DOTALL);
    private HTMLSourceEditor sourceEditor;
    private Browser browser;
    private final ISelectionProvider selectionProvider = new SelectionProvider();

    private boolean ftlFormat = true;
    protected FormNode formNode;
    protected IFile formFile;
    private final Map<Integer, Component> components = Maps.newConcurrentMap();

    private boolean dirty = false;
    private boolean browserLoaded = false;
    private Object dirtySource = null;
    private static FormEditor lastInitializedInstance;
    private static boolean browserCreationErrorWasShownToUser;

    private int cachedForVariablesCount = -1;
    private final Map<String, Map<String, Variable>> cachedVariables = new HashMap<String, Map<String, Variable>>();
    protected int currentPageIndex = 0;

    protected synchronized boolean isBrowserLoaded() {
        return browserLoaded;
    }

    protected synchronized void setBrowserLoaded(boolean browserLoaded) {
        this.browserLoaded = browserLoaded;
    }

    @Override
    protected void setInput(IEditorInput input) {
        try {
            IResource inputFile = ((FileEditorInput) input).getFile();
            if (!inputFile.isSynchronized(IResource.DEPTH_ZERO)) {
                inputFile.refreshLocal(IResource.DEPTH_ZERO, null);
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
        super.setInput(input);
    }

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        formFile = ((FileEditorInput) editorInput).getFile();
        ftlFormat = editorInput.getName().endsWith("ftl");
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        lastInitializedInstance = this;
        IFile definitionFile = IOUtils.getProcessDefinitionFile((IFolder) formFile.getParent());
        ProcessDefinition processDefinition = ProcessCache.getProcessDefinition(definitionFile);
        if (formFile.getName().startsWith(ParContentProvider.SUBPROCESS_DEFINITION_PREFIX)) {
            String subprocessId = formFile.getName().substring(0, formFile.getName().indexOf("."));
            processDefinition = processDefinition.getEmbeddedSubprocessById(subprocessId);
            Preconditions.checkNotNull(processDefinition, "embedded subpocess");
        }
        for (FormNode formNode : processDefinition.getChildren(FormNode.class)) {
            if (editorInput.getName().equals(formNode.getFormFileName())) {
                this.formNode = formNode;
                setPartName(formNode.getName());
                break;
            }
        }

        if (formNode == null) {
            // user edit description
            return;
        }

        getSite().setSelectionProvider(selectionProvider);

        addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(Object source, int propId) {
                if (propId == FormEditor.CLOSED) {
                    IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if (workbenchPage == null) {
                        return;
                    }
                    boolean formEditorsAvailable = false;
                    for (IEditorReference editorReference : workbenchPage.getEditorReferences()) {
                        IEditorPart editor = editorReference.getEditor(true);
                        if (editor instanceof JointFormEditor) {
                            formEditorsAvailable = true;
                            break;
                        }
                    }
                    if (!formEditorsAvailable) {
                        IViewReference reference = workbenchPage.findViewReference(FormComponentsView.ID);
                        if (reference != null) {
                            Display.getDefault().asyncExec(() -> {
                                workbenchPage.hideView(reference);
                            });
                        }
                    }
                }
            }
        });
    }

    public Map<String, Variable> getVariables() {
        return getVariables(null);
    }

    public List<String> getVariableNames(String typeClassNameFilter) {
        List<String> list = Lists.newArrayList(getVariables(typeClassNameFilter).keySet());
        Collections.sort(list);
        return list;
    }

    public VariableUserType getVariableUserType(String name) {
        return formNode.getProcessDefinition().getVariableUserType(name);
    }

    public ProcessDefinition getProcessDefinition() {
        if (formNode == null) {
            return null;
        }
        return formNode.getProcessDefinition();
    }

    public synchronized Map<String, Variable> getVariables(String typeClassNameFilter) {
        if (formNode == null) {
            // This is because earlier access from web page (not user request)
            return new HashMap<String, Variable>();
        }
        List<Variable> variables = formNode.getVariables(true, true);
        if (cachedForVariablesCount != variables.size()) {
            cachedForVariablesCount = variables.size();
            cachedVariables.clear();
        }
        if (!cachedVariables.containsKey(typeClassNameFilter)) {
            // get variables without strong-typing. (all hierarchy)
            if (!Strings.isNullOrEmpty(typeClassNameFilter) && !Object.class.getName().equals(typeClassNameFilter)) {
                variables = formNode.getVariables(true, true, typeClassNameFilter);
                cachedVariables.put(typeClassNameFilter, VariableUtils.toMap(variables));
            } else {
                cachedVariables.put(typeClassNameFilter, VariableUtils.toMap(variables));
            }
        }
        return cachedVariables.get(typeClassNameFilter);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == ITextEditor.class) {
            return sourceEditor;
        }
        return super.getAdapter(adapter);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (sourceEditor != null && sourceEditor.getEditorInput() != null) {
            EditorUtils.closeEditorIfRequired(event, ((IFileEditorInput) sourceEditor.getEditorInput()).getFile(), this);
        }
    }

    private ITextListener sourceEditorListener = new ITextListener() {

        @Override
        public void textChanged(TextEvent event) {
            dirtySource = sourceEditor;
        }
    };

    @Override
    protected void createPages() {
        int pageNumber = 0;
        try {
            browser = new Browser(getContainer(), SWT.NULL);
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("Browser type = " + browser.getBrowserType());
            }
            browser.addOpenWindowListener(new BrowserWindowHelper(getContainer().getDisplay()));
            new GetHTMLCallbackFunction(browser);
            new OnLoadCallbackFunction(browser);
            new MarkEditorDirtyCallbackFunction(browser);
            new RefreshViewCallbackFunction(browser);
            new LogErrorCallbackFunction(browser);
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
            if (!browserCreationErrorWasShownToUser) {
                PluginLogger.logError(Messages.getString("wysiwyg.design.create_error"), th);
                browserCreationErrorWasShownToUser = true;
            }
        }
        try {
            sourceEditor = new HTMLSourceEditor(new HTMLConfiguration(EditorsPlugin.getDefault().getColorProvider()));
            addPage(sourceEditor, getEditorInput());
            setPageText(pageNumber++, Messages.getString("wysiwyg.source.tab_name"));
            sourceEditor.getViewer().addTextListener(sourceEditorListener);
        } catch (Exception ex) {
            PluginLogger.logError(Messages.getString("wysiwyg.source.create_error"), ex);
        }
    }

    protected void startWebServerIfNotStartedYet(BrowserLoadCallback browserLoadCallback) {
        if (browser == null || browserLoaded) {
            return;
        }
        ConnectorServletHelper.setBaseDir(sourceEditor.getFile().getParent());
        ConnectorServletHelper.sync();
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
                                    browserLoadCallback.onLoad();
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
                        Exception ex = (Exception) e.getTargetException();
                        if (ex instanceof MultiException) {
                            for (Object multExc : ((MultiException) ex).getExceptions()) {
                                if (multExc instanceof BindException) {
                                    String port = Activator.getPrefString(PrefConstants.P_FORM_WEB_SERVER_PORT);
                                    PluginLogger.logError(Messages.getString("wysiwyg.design.create_error_port", port), ex);
                                    break;
                                }
                            }
                        } else {
                            PluginLogger.logError(Messages.getString("wysiwyg.design.create_error"), e.getTargetException());
                        }

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

    public static FormEditor getCurrent() {
        IEditorPart editor = EditorsPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getActivePage().getActiveEditor();
        if (editor instanceof FormEditor) {
            return (FormEditor) editor;
        }
        if (lastInitializedInstance != null) {
            return lastInitializedInstance;
        }
        throw new RuntimeException("No editor instance initialized");
    }

    private void setDirty(boolean dirty, boolean fireEvent) {
        boolean changedDirtyState = this.dirty != dirty;
        this.dirty = dirty;
        if (changedDirtyState && fireEvent) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public boolean isDirty() {
        return dirty || sourceEditor.isDirty();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (isDirty()) {
            if (dirtySource == browser) {
                if (isBrowserLoaded()) {
                    if (!syncBrowser2Editor()) {
                        throw new InternalApplicationException(Messages.getString("wysiwyg.design.save_error"));
                    }
                }
            } else if (dirtySource == sourceEditor) {
                syncEditor2Browser();
            }
            dirtySource = null;
            sourceEditor.doSave(monitor);
            if (isBrowserLoaded()) {
                browser.execute("setHTMLSaved()");
            }
            setDirty(false, !(this instanceof JointFormEditor));
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
        sourceEditor.getViewer().removeTextListener(sourceEditorListener);
        firePropertyChange(CLOSED);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    @Override
    protected void pageChange(int newPageIndex) {
        if (isBrowserLoaded()) {
            if (currentPageIndex == 1 && newPageIndex == 0) {
                ConnectorServletHelper.sync();
                syncEditor2Browser();
            } else if (currentPageIndex == 0 && newPageIndex == 1) {
                syncBrowser2Editor();
            }
        } else if (newPageIndex == 0) {
            startWebServerIfNotStartedYet(() -> {
                ConnectorServletHelper.sync();
                syncEditor2Browser();
            });
        }
        currentPageIndex = newPageIndex;
        super.pageChange(newPageIndex);
    }

    public void insertText(String text) {
        String escapedText = text.replaceAll("\'", "\\\'");
        try {
            if (browser != null) {
                browser.execute("CKEDITOR.instances.editor.insertText('" + escapedText + "');");
                syncBrowser2Editor();
                syncEditor2Browser();
            } else {
                IDocument document = sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput());
                ITextSelection selection = (ITextSelection) sourceEditor.getSelectionProvider().getSelection();
                document.replace(selection.getOffset(), selection.getLength(), escapedText);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private boolean syncBrowser2Editor() {
        if (browser != null) {
            boolean result = browser.execute("getHTML()");
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("syncBrowser2Editor: " + result);
            }
            return result;
        }
        return false;
    }

    private void syncEditor2Browser() {
        String html = getSourceDocumentHTML();
        Matcher matcher = HTML_CONTENT_PATTERN.matcher(html);
        if (matcher.find()) {
            html = matcher.group(3);
        }
        if (ftlFormat) {
            components.clear();
            try {
                html = TemplateProcessor.process(formFile.getFullPath().toString(), html, new EditorHashModel(this));
            } catch (Exception e) {
                EditorsPlugin.logError("ftl WYSIWYGHTMLEditor.syncEditor2Browser()", e);
            }
        } else {
            html = VarTagUtil.toHtml(html);
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
        if (selectedComponentNumber >= 0) {
            selectComponent(selectedComponentNumber);
        }
    }

    protected String getSourceDocumentHTML() {
        return sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).get();
    }

    public Component createComponent(String type) {
        Component component = new Component(ComponentTypeRegistry.getNotNull(type), ComponentIdGenerator.generate());
        component.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                FormEditor editor = FormEditor.this;
                if (editor != null && !editor.isDirty()) {
                    editor.setDirty(true, true);
                }
                refreshView();
            }
        });
        components.put(component.getId(), component);
        return component;
    }

    public Component getSelectedComponent() {
        if (selectionProvider.getSelection().isEmpty()) {
            return null;
        }
        Object result = ((StructuredSelection) selectionProvider.getSelection()).getFirstElement();
        if (!(result instanceof Component)) {
            return null;
        }
        return (Component) result;
    }

    public Component getComponentNotNull(int componentId) {
        Component component = components.get(componentId);
        if (component == null) {
            throw new RuntimeException("Component not found by id " + componentId + ", all: " + components);
        }
        return component;
    }

    public void componentSelected(int componentId) {
        Component component = components.get(componentId);
        if (component != null) {
            final ISelection selection = new StructuredSelection(component);
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    selectionProvider.setSelection(selection);
                }
            });
        }
    }

    public void componentDeselected() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                selectionProvider.setSelection(StructuredSelection.EMPTY);
            }
        });
    }

    public void openParametersDialog(final int componentId) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                ComponentParametersDialog dialog = new ComponentParametersDialog(getComponentNotNull(componentId));
                final Component component = dialog.openDialog();
                if (component != null) {
                    components.put(componentId, component);
                    refreshView();
                }
            }
        });
    }

    @Override
    public void doDrop() {
        refreshView();
    }

    public void refreshView() {
        try {
            syncBrowser2Editor();
            syncEditor2Browser();
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    protected int selectedComponentNumber = -1;

    public void selectComponent(int componentNumber) {
        if (browser != null && browserLoaded) {
            browser.execute("selectFormComponent(" + componentNumber + ");");
            selectedComponentNumber = -1;
        } else {
            selectedComponentNumber = componentNumber;
        }
    }

    private class GetHTMLCallbackFunction extends BrowserFunction {
        public GetHTMLCallbackFunction(Browser browser) {
            super(browser, "getHTMLCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            String html = (String) arguments[0];
            if (ftlFormat) {
                try {
                    html = DesignUtils.transformFromHtml(html, getVariables(), components);
                    Matcher matcher = HTML_CONTENT_PATTERN.matcher(html);
                    if (matcher.find()) {
                        html = matcher.group(3);
                    }
                    if (EditorsPlugin.DEBUG) {
                        PluginLogger.logInfo("Designer html = " + html);
                    }
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("freemarker html transform", e);
                    throw new InternalApplicationException(Messages.getString("wysiwyg.design.transform_to_source_error"));
                }
            } else {
                // bug in closing customtag tag
                html = VarTagUtil.fromHtml(html);
            }
            if (!html.equals(sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).get())) {
                sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).set(html);
            }
            return null;
        }
    }

    private interface BrowserLoadCallback {

        void onLoad();

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
            browser.execute("setIgnoreErrors(" + Activator.getPrefBoolean(PrefConstants.P_FORM_IGNORE_ERRORS_FROM_WEBPAGE) + ")");
            return null;
        }
    }

    private class MarkEditorDirtyCallbackFunction extends BrowserFunction {

        public MarkEditorDirtyCallbackFunction(Browser browser) {
            super(browser, "markEditorDirtyCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            dirtySource = browser;
            setDirty(true, true);
            return null;
        }
    }

    private class RefreshViewCallbackFunction extends BrowserFunction {

        public RefreshViewCallbackFunction(Browser browser) {
            super(browser, "refreshViewCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            refreshView();
            return null;
        }
    }

    private class LogErrorCallbackFunction extends BrowserFunction {
        public LogErrorCallbackFunction(Browser browser) {
            super(browser, "logErrorCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            PluginLogger.logErrorWithoutDialog((String) arguments[0]);
            return null;
        }
    }

}
