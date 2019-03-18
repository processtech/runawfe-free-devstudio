package ru.runa.gpd.jseditor;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.MatchingCharacterPainter;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.htmleditor.ColorProvider;
import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.HTMLProjectParams;
import ru.runa.gpd.htmleditor.editors.FoldingInfo;
import ru.runa.gpd.htmleditor.editors.SoftTabVerifyListener;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.TemplateUtils;
import ru.runa.gpd.util.WorkspaceOperations;

public class JavaScriptEditor extends TextEditor {
    private FormNode formNode;

    private ColorProvider colorProvider;
    private SoftTabVerifyListener softTabListener;
    private JavaScriptOutlinePage outline;
    private JavaScriptCharacterPairMatcher pairMatcher;
    private ProjectionSupport fProjectionSupport;

    public static final String GROUP_JAVASCRIPT = "_javascript";
    public static final String ACTION_COMMENT = "_comment";

    public JavaScriptEditor() {
        super();
        colorProvider = EditorsPlugin.getDefault().getColorProvider();
        setSourceViewerConfiguration(new JavaScriptConfiguration(colorProvider));
        setPreferenceStore(
                new ChainedPreferenceStore(new IPreferenceStore[] { getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() }));

        outline = new JavaScriptOutlinePage(this);

        IPreferenceStore store = EditorsPlugin.getDefault().getPreferenceStore();
        softTabListener = new SoftTabVerifyListener();
        softTabListener.setUseSoftTab(store.getBoolean(HTMLPlugin.PREF_USE_SOFTTAB));
        softTabListener.setSoftTabWidth(store.getInt(HTMLPlugin.PREF_SOFTTAB_WIDTH));

        setAction(ACTION_COMMENT, new CommentAction());
    }

    public JavaScriptEditor(FormNode formNode, IFile jsFile) {
        this();
        this.formNode = formNode;
        if (!jsFile.exists()) {
            try {
                IOUtils.createFile(jsFile, TemplateUtils.getFormTemplateAsStream());
            } catch (CoreException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void updateSelectionDependentActions() {
        super.updateSelectionDependentActions();
        ITextSelection sel = (ITextSelection) getSelectionProvider().getSelection();
        if (sel.getText().equals("")) {
            getAction(ACTION_COMMENT).setEnabled(false);
        } else {
            getAction(ACTION_COMMENT).setEnabled(true);
        }
    }

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        ISourceViewer viewer = new ProjectionViewer(parent, ruler, fOverviewRuler, true, styles);
        getSourceViewerDecorationSupport(viewer);
        viewer.getTextWidget().addVerifyListener(softTabListener);
        return viewer;
    }

    @Override
    protected final void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);
        menu.add(new Separator(GROUP_JAVASCRIPT));
        addAction(menu, GROUP_JAVASCRIPT, ACTION_COMMENT);
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        if (input instanceof IFileEditorInput) {
            setDocumentProvider(new JavaScriptTextDocumentProvider());
        } else if (input instanceof IStorageEditorInput) {
            setDocumentProvider(new JavaScriptFileDocumentProvider());
        } else {
            setDocumentProvider(new JavaScriptTextDocumentProvider());
        }
        super.doSetInput(input);
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);
        update();
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        getSourceViewer().getTextWidget().setKeyBinding(SWT.DEL, ST.DELETE_NEXT);

        ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
        fProjectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        fProjectionSupport.install();
        viewer.doOperation(ProjectionViewer.TOGGLE);
        updateFolding();

        StyledText widget = viewer.getTextWidget();
        widget.setTabs(getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));
        widget.addVerifyListener(softTabListener);
        /*
         * widget.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { firePropertyChange(PROP_DIRTY); } });
         */

        ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
        pairMatcher = new JavaScriptCharacterPairMatcher();
        pairMatcher.setEnable(getPreferenceStore().getBoolean(HTMLPlugin.PREF_PAIR_CHAR));
        MatchingCharacterPainter painter = new MatchingCharacterPainter(getSourceViewer(), pairMatcher);
        painter.setColor(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
        extension.addPainter(painter);

        update();
    }

    /**
     * Updates internal status.
     *
     * <ol>
     * <li>updates the outline view (invoke {@link JavaScriptOutlinePage#update()})</li>
     * <li>validates contents (invoke {@link JavaScriptEditor#doValidate()})</li>
     * </ol>
     */
    protected void update() {
        outline.update();
        doValidate();
        updateFolding();

        IFileEditorInput input = (IFileEditorInput) getEditorInput();
        JavaScriptConfiguration config = (JavaScriptConfiguration) getSourceViewerConfiguration();
        config.getAssistProcessor().update(input.getFile());
    }

    protected void doValidate() {
        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        IFileEditorInput input = (IFileEditorInput) getEditorInput();
                        new JavaScriptValidator(input.getFile()).doValidate();
                    } catch (Exception ex) {
                    }
                }
            }, null);
        } catch (Exception ex) {
            HTMLPlugin.logException(ex);
        }
    }

    @Override
    public void dispose() {
        if (getEditorInput() instanceof IFileEditorInput) {
            try {
                IFile script = ((IFileEditorInput) getEditorInput()).getFile();
                if (script.exists()) {
                    try (InputStream is = script.getContents()) {
                        if (TemplateUtils.getFormTemplateAsString().equals(CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8)))) {
                            is.close();
                            WorkspaceOperations.job("Java script editor disposing", (p) -> {
                                try {
                                    if (new HTMLProjectParams(script.getProject()).getRemoveMarkers()) {
                                        script.deleteMarkers(IMarker.PROBLEM, false, 0);
                                    }
                                    script.delete(true, null);
                                    if (formNode != null && !formNode.getProcessDefinition().isDirty()) {
                                        // perform forms.xml synchronization now (script requires that)
                                        ParContentProvider.saveFormsXml(formNode.getProcessDefinition());
                                    }
                                } catch (Exception e) {
                                    PluginLogger.logError(e);
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }

        pairMatcher.dispose();
        super.dispose();
    }

    @Override
    public void doSaveAs() {
        super.doSaveAs();
        update();
    }

    @Override
    protected boolean affectsTextPresentation(PropertyChangeEvent event) {
        return super.affectsTextPresentation(event) || colorProvider.affectsTextPresentation(event);
    }

    @Override
    protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
        colorProvider.handlePreferenceStoreChanged(event);
        // updateAssistProperties(event);

        String key = event.getProperty();
        if (key.equals(HTMLPlugin.PREF_PAIR_CHAR)) {
            boolean enable = ((Boolean) event.getNewValue()).booleanValue();
            pairMatcher.setEnable(enable);
        }

        super.handlePreferenceStoreChanged(event);
        softTabListener.preferenceChanged(event);
    }

    @Override
    protected void createActions() {
        super.createActions();
        // Add a content assist action
        IAction action = new ContentAssistAction(HTMLPlugin.getResourceBundle(), "ContentAssistProposal", this);
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action);
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (IContentOutlinePage.class.equals(adapter)) {
            return outline;
        }
        return super.getAdapter(adapter);
    }

    /**
     * Update folding informations.
     */
    private void updateFolding() {
        try {
            ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
            if (viewer == null) {
                return;
            }
            ProjectionAnnotationModel model = viewer.getProjectionAnnotationModel();
            if (model == null) {
                return;
            }

            IDocument doc = getDocumentProvider().getDocument(getEditorInput());
            String source = doc.get();

            ArrayList list = new ArrayList();
            Stack stack = new Stack();
            FoldingInfo prev = null;
            char quote = 0;
            boolean escape = false;

            for (int i = 0; i < source.length(); i++) {
                char c = source.charAt(i);
                // skip string
                if (quote != 0 && escape == true) {
                    escape = false;
                } else if ((prev == null || !prev.getType().equals("comment")) && (c == '"' || c == '\'')) {
                    if (quote == 0) {
                        quote = c;
                    } else if (quote == c) {
                        quote = 0;
                    }
                } else if (quote != 0 && (c == '\\')) {
                    escape = true;
                } else if (quote != 0 && (c == '\n' || c == '\r')) {
                    quote = 0;
                // start comment
                } else if (c == '/' && source.length() > i + 1 && quote == 0) {
                    if (source.charAt(i + 1) == '*') {
                        prev = new FoldingInfo(i, -1, "comment");
                        stack.push(prev);
                        i++;
                    }
                // end comment
                } else if (c == '*' && source.length() > i + 1 && !stack.isEmpty() && quote == 0) {
                    if (source.charAt(i + 1) == '/' && prev.getType().equals("comment")) {
                        FoldingInfo info = (FoldingInfo) stack.pop();
                        if (doc.getLineOfOffset(info.getStart()) != doc.getLineOfOffset(i)) {
                            list.add(new FoldingInfo(info.getStart(), i + 2 + FoldingInfo.countUpLineDelimiter(source, i + 2), "comment"));
                        }
                        prev = stack.isEmpty() ? null : (FoldingInfo) stack.get(stack.size() - 1);
                        i++;
                    }
                // open blace
                } else if (c == '{' && quote == 0) {
                    if (prev == null || !prev.getType().equals("comment")) {
                        if (findFunction(source, i)) {
                            prev = new FoldingInfo(i, -1, "function");
                        } else {
                            prev = new FoldingInfo(i, -1, "blace");
                        }
                        stack.push(prev);
                    }
                // close blace
                } else if (c == '}' && prev != null && !prev.getType().equals("comment") && quote == 0) {
                    FoldingInfo info = (FoldingInfo) stack.pop();
                    if (info.getType().equals("function") && doc.getLineOfOffset(info.getStart()) != doc.getLineOfOffset(i)) {
                        list.add(new FoldingInfo(info.getStart(), i + 2 + FoldingInfo.countUpLineDelimiter(source, i + 2), "function"));
                    }
                    prev = stack.isEmpty() ? null : (FoldingInfo) stack.get(stack.size() - 1);
                }
            }

            FoldingInfo.applyModifiedAnnotations(model, list);

        } catch (Exception ex) {
            HTMLPlugin.logException(ex);
        }
    }

    private boolean findFunction(String text, int pos) {
        text = text.substring(0, pos);
        int index1 = text.lastIndexOf("function");
        int index2 = text.lastIndexOf("{");
        if (index1 == -1) {
            return false;
        } else if (index1 > index2) {
            return true;
        } else {
            return false;
        }
    }

    /** The action to comment out selection range. */
    private class CommentAction extends Action {

        public CommentAction() {
            super(HTMLPlugin.getResourceString("JavaScriptEditor.CommentAction"));
            setEnabled(false);
            setAccelerator(SWT.CTRL | '/');
        }

        @Override
        public void run() {
            ITextSelection sel = (ITextSelection) getSelectionProvider().getSelection();
            IDocument doc = getDocumentProvider().getDocument(getEditorInput());
            String text = sel.getText();
            try {
                if (text.startsWith("//")) {
                    text = text.replaceAll("(^|\r\n|\r|\n)//", "$1");
                } else {
                    text = text.replaceAll("(^|\r\n|\r|\n)(.+?)", "$1//$2");
                }
                doc.replace(sel.getOffset(), sel.getLength(), text);
            } catch (BadLocationException e) {
                HTMLPlugin.logException(e);
            }
        }
    }

    @Override
    protected void performSaveAs(IProgressMonitor progressMonitor) {
        // Do nothing
    }

}
