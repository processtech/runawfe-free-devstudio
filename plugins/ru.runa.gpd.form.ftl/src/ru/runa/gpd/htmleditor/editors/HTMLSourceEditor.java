package ru.runa.gpd.htmleditor.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jrcs.diff.AddDelta;
import org.apache.commons.jrcs.diff.ChangeDelta;
import org.apache.commons.jrcs.diff.DeleteDelta;
import org.apache.commons.jrcs.diff.Delta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.Revision;
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
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.MatchingCharacterPainter;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.csseditor.ChooseColorAction;
import ru.runa.gpd.htmleditor.ColorProvider;
import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.HTMLProjectParams;
import ru.runa.gpd.htmleditor.HTMLUtil;
import ru.runa.gpd.htmleditor.assist.HTMLAssistProcessor;
import ru.runa.gpd.htmleditor.assist.InnerJavaScriptAssistProcessor;

/**
 * HTML source editor.
 */
public class HTMLSourceEditor extends TextEditor {

    private final ColorProvider colorProvider;
    private final IHTMLOutlinePage outlinePage;
    private HTMLHyperlinkSupport hyperlink;
    private HTMLCharacterPairMatcher pairMatcher;
    private final SoftTabVerifyListener softTabListener;

    public static final String GROUP_HTML = "_html";
    public static final String ACTION_ESCAPE_HTML = "_escape_html";
    public static final String ACTION_COMMENT = "_comment";
    public static final String ACTION_CHOOSE_COLOR = "_choose_color";

    // private boolean useSoftTab;
    // private String softTab;
    private boolean validation = true;

    public HTMLSourceEditor(HTMLConfiguration config) {
        super();
        config.setEditorPart(this);
        colorProvider = EditorsPlugin.getDefault().getColorProvider();
        setSourceViewerConfiguration(config);
        setPreferenceStore(new ChainedPreferenceStore(
                new IPreferenceStore[] { getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() }));

        setAction(ACTION_ESCAPE_HTML, new EscapeHTMLAction());
        setAction(ACTION_COMMENT, new CommentAction());
        setAction(ACTION_CHOOSE_COLOR, new ChooseColorAction(this));

        IPreferenceStore store = EditorsPlugin.getDefault().getPreferenceStore();
        softTabListener = new SoftTabVerifyListener();
        softTabListener.setUseSoftTab(store.getBoolean(HTMLPlugin.PREF_USE_SOFTTAB));
        softTabListener.setSoftTabWidth(store.getInt(HTMLPlugin.PREF_SOFTTAB_WIDTH));

        outlinePage = createOutlinePage();
    }

    protected HTMLCharacterPairMatcher getPairMatcher() {
        return this.pairMatcher;
    }

    public void setValidation(boolean validation) {
        this.validation = validation;
    }

    public boolean getValidation() {
        return this.validation;
    }

    protected IHTMLOutlinePage createOutlinePage() {
        return new HTMLOutlinePage(this);
    }

    protected HTMLHyperlinkSupport createHyperlinkSupport() {
        return new HTMLHyperlinkSupport(this);
    }

    private ProjectionSupport fProjectionSupport;

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        ISourceViewer viewer = new ProjectionViewer(parent, ruler, fOverviewRuler, true, styles);
        getSourceViewerDecorationSupport(viewer);
        viewer.getTextWidget().addVerifyListener(softTabListener);
        return viewer;
    }

    public ISourceViewer getViewer() {
        return this.getSourceViewer();
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        getSourceViewer().getTextWidget().setKeyBinding(SWT.DEL, ST.DELETE_NEXT);

        hyperlink = createHyperlinkSupport();
        hyperlink.install();

        ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
        fProjectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
        fProjectionSupport.install();
        projectionViewer.doOperation(ProjectionViewer.TOGGLE);
        projectionViewer.getTextWidget().setTabs(getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));

        ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
        pairMatcher = new HTMLCharacterPairMatcher();
        pairMatcher.setEnable(getPreferenceStore().getBoolean(HTMLPlugin.PREF_PAIR_CHAR));
        MatchingCharacterPainter painter = new MatchingCharacterPainter(getSourceViewer(), pairMatcher);
        painter.setColor(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
        extension.addPainter(painter);

        update();
    }

    // public void addTextListener(ITextListener l){
    // getSourceViewer().addTextListener(l);
    // }
    //
    // public void removeTextListener(ITextListener l){
    // getSourceViewer().removeTextListener(l);
    // }

    /** This method is called when configuration is changed. */
    @Override
    protected boolean affectsTextPresentation(PropertyChangeEvent event) {
        return super.affectsTextPresentation(event) || colorProvider.affectsTextPresentation(event);
    }

    /** This method is called when configuration is changed. */
    @Override
    protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
        colorProvider.handlePreferenceStoreChanged(event);
        updateAssistProperties(event);
        softTabListener.preferenceChanged(event);

        String key = event.getProperty();
        if (key.equals(HTMLPlugin.PREF_PAIR_CHAR)) {
            boolean enable = ((Boolean) event.getNewValue()).booleanValue();
            pairMatcher.setEnable(enable);
        }

        super.handlePreferenceStoreChanged(event);
    }

    private void updateAssistProperties(PropertyChangeEvent event) {
        String key = event.getProperty();
        try {
            // auto activation delay
            if (key.equals(HTMLPlugin.PREF_ASSIST_TIMES)) {
                ContentAssistant assistant = (ContentAssistant) getSourceViewerConfiguration().getContentAssistant(null);
                assistant.setAutoActivationDelay(Integer.parseInt((String) event.getNewValue()));

                // auto activation trigger
            } else if (key.equals(HTMLPlugin.PREF_ASSIST_CHARS)) {
                ContentAssistant assistant = (ContentAssistant) getSourceViewerConfiguration().getContentAssistant(null);
                HTMLAssistProcessor processor = (HTMLAssistProcessor) assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
                processor.setAutoAssistChars(((String) event.getNewValue()).toCharArray());

                // completion close tag
            } else if (key.equals(HTMLPlugin.PREF_ASSIST_CLOSE)) {
                ContentAssistant assistant = (ContentAssistant) getSourceViewerConfiguration().getContentAssistant(null);
                HTMLAssistProcessor processor = (HTMLAssistProcessor) assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
                processor.setAssistCloseTag(((Boolean) event.getNewValue()).booleanValue());

                // enable auto activation or not
            } else if (key.equals(HTMLPlugin.PREF_ASSIST_AUTO)) {
                ContentAssistant assistant = (ContentAssistant) getSourceViewerConfiguration().getContentAssistant(null);
                assistant.enableAutoActivation(((Boolean) event.getNewValue()).booleanValue());
            }
        } catch (Exception ex) {
            HTMLPlugin.logException(ex);
        }
    }

    /**
     * Adds actions to the context menu.
     * <p>
     * If you want to customize the context menu, you can override this method in the sub-class instead of editorContextMenuAboutToShow(),
     * 
     * @param menu
     *            IMenuManager
     */
    protected void addContextMenuActions(IMenuManager menu) {
        menu.add(new Separator(GROUP_HTML));
        addAction(menu, GROUP_HTML, ACTION_CHOOSE_COLOR);
        addAction(menu, GROUP_HTML, ACTION_ESCAPE_HTML);
        addAction(menu, GROUP_HTML, ACTION_COMMENT);
    }

    @Override
    protected final void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);
        addContextMenuActions(menu);
    }

    @Override
    protected void updateSelectionDependentActions() {
        super.updateSelectionDependentActions();
        ITextSelection sel = (ITextSelection) getSelectionProvider().getSelection();
        if (sel.getText().equals("")) {
            getAction(ACTION_COMMENT).setEnabled(false);
            getAction(ACTION_ESCAPE_HTML).setEnabled(false);
        } else {
            getAction(ACTION_COMMENT).setEnabled(true);
            getAction(ACTION_ESCAPE_HTML).setEnabled(true);
        }
    }

    /**
     * Validates HTML document. If the editor provides other validation, do override at the subclass.
     */
    protected void doValidate() {
        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        IFileEditorInput input = (IFileEditorInput) getEditorInput();
                        IFile file = input.getFile();
                        file.deleteMarkers(IMarker.PROBLEM, false, 0);

                        HTMLProjectParams params = new HTMLProjectParams(file.getProject());
                        if (params.getValidateHTML()) {
                            new HTMLValidator(input.getFile()).doValidate();
                        }
                    } catch (Exception ex) {
                        // HTMLPlugin.logException(ex);
                    }
                }
            }, null);
        } catch (Exception ex) {
            HTMLPlugin.logException(ex);
        }
    }

    /**
     * Returns HTML source which can be parse by FuzzyXML.
     * 
     * @return HTML source
     */
    public String getHTMLSource() {
        return getDocumentProvider().getDocument(getEditorInput()).get();
    }

    /**
     * This method is called in the following timing:
     * <ul>
     * <li>Initialize editor</li>
     * <li>Save document</li>
     * </ul>
     * And do the following sequence.
     * <ul>
     * <li>invoke {@link HTMLSourceEditor#updateFolding()}</li>
     * <li>invoke {@link HTMLSourceEditor#updateAssist()}</li>
     * <li>invoke {@link HTMLOutlinePage#update()}</li>
     * <li>update {@link HTMLHyperlinkSupport}
     * <li>invoke {@link HTMLSourceEditor#doValidate()()} (only getValidation() returns true)</li>
     * </ul>
     * If it's required to update some information about the editor, do override at the subclass.
     */
    protected void update() {
        updateFolding();
        updateAssist();
        try {
            outlinePage.update();
        } catch (Exception ex) {
            // SWT error goes here sometimes ...
        }
        if (hyperlink != null && isFileEditorInput()) {
            hyperlink.setProject(((IFileEditorInput) getEditorInput()).getFile().getProject());
        }
        // Commented out, see https://rm.processtech.ru/issues/544#note-26
//        if (validation && isFileEditorInput()) {
//            doValidate();
//        }
    }

    /**
     * Update informations about code-completion.
     */
    protected void updateAssist() {
        if (!isFileEditorInput()) {
            return;
        }
        String html = getDocumentProvider().getDocument(getEditorInput()).get();

        // Update AssistProcessors
        HTMLAssistProcessor processor = ((HTMLConfiguration) getSourceViewerConfiguration()).getAssistProcessor();
        processor.update((IFileEditorInput) getEditorInput(), html);

        InnerJavaScriptAssistProcessor jsProcessor = ((HTMLConfiguration) getSourceViewerConfiguration()).getJavaScriptAssistProcessor();
        jsProcessor.update(((IFileEditorInput) getEditorInput()).getFile());
    }

    /*
     * protected void initializeDragAndDrop(ISourceViewer viewer) { super.initializeDragAndDrop(viewer);
     * 
     * DropTarget target = new DropTarget(viewer.getTextWidget(),DND.DROP_DEFAULT|DND.DROP_COPY); LocalSelectionTransfer selTransfer =
     * LocalSelectionTransfer.getInstance(); // FileTransfer fileTransfer = FileTransfer.getInstance(); // TextTransfer textTransfer =
     * TextTransfer.getInstance(); // Transfer[] types = new Transfer[]{fileTransfer,textTransfer}; Transfer[] types = new Transfer[]{selTransfer};
     * target.setTransfer(types); target.addDropListener(new HTMLDropListener()); // getSourceViewer().getTextWidget().addMouseMoveListener(new
     * HTMLDropMouseMoveListener()); }
     */

    @Override
    public void dispose() {
        if (isFileEditorInput() && validation) {
            try {
                ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                    @Override
                    public void run(IProgressMonitor monitor) throws CoreException {
                        try {
                            IFileEditorInput input = (IFileEditorInput) getEditorInput();
                            HTMLProjectParams params = new HTMLProjectParams(input.getFile().getProject());
                            if (params.getRemoveMarkers()) {
                                input.getFile().deleteMarkers(IMarker.PROBLEM, false, 0);
                            }
                        } catch (Exception ex) {
                        }
                    }
                }, null);
            } catch (Exception ex) {
            }
        }

        hyperlink.uninstall();
        fProjectionSupport.dispose();
        pairMatcher.dispose();
        super.dispose();
    }

    /**
     * Returns a java.io.File object that is editing in this editor.
     */
    public File getFile() {
        IFile file = ((FileEditorInput) this.getEditorInput()).getFile();
        return file.getLocation().makeAbsolute().toFile();
    }

    /**
     * Returns a java.io.File object of a temporary file for preview.
     */
    public File getTempFile() {
        IFile file = ((FileEditorInput) this.getEditorInput()).getFile();
        return new File(file.getLocation().makeAbsolute().toFile().getParentFile(), file.getName() + ".html");
    }

    @Override
    protected void createActions() {
        super.createActions();
        // add content assist action
        IAction action = new ContentAssistAction(HTMLPlugin.getResourceBundle(), "ContentAssistProposal", this);
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action);
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (IContentOutlinePage.class.equals(adapter)) {
            return outlinePage;
        }
        if (ProjectionAnnotationModel.class.equals(adapter) && fProjectionSupport != null) {
            Object obj = fProjectionSupport.getAdapter(getSourceViewer(), adapter);
            if (obj != null) {
                return obj;
            }
        }
        return super.getAdapter(adapter);
    }

    protected IDocumentProvider createDocumentProvider(IEditorInput input) {
        if (input instanceof IFileEditorInput) {
            return new HTMLTextDocumentProvider();
        } else if (input instanceof IStorageEditorInput) {
            return new HTMLFileDocumentProvider();
        } else {
            return new HTMLTextDocumentProvider();
        }
    }

    @Override
    protected final void doSetInput(IEditorInput input) throws CoreException {
        setDocumentProvider(createDocumentProvider(input));
        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) input).getFile();
            if (file.getName().endsWith(".xhtml")) {
                HTMLConfiguration config = (HTMLConfiguration) getSourceViewerConfiguration();
                config.getAssistProcessor().setXHTMLMode(true);
            }
        }
        super.doSetInput(input);
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);
        update();
    }

    @Override
    public void doSaveAs() {
        super.doSaveAs();
        update();
    }

    public void setDiff(String text) {
        // apply the graphical editor contents to the source editor
        IEditorInput input = getEditorInput();
        IDocument doc = getDocumentProvider().getDocument(input);

        getSourceViewer().getTextWidget().setRedraw(false);

        String[] text1 = doc.get().split("\n");
        String[] text2 = text.split("\n");
        try {
            Revision rev = Diff.diff(text1, text2);
            int count1 = 0;
            int count2 = 0;
            int index = 0;
            for (int i = 0; i < rev.size(); i++) {
                Delta delta = rev.getDelta(i);
                Range orgRange = new Range(delta.getOriginal().rangeString());
                Range revRange = new Range(delta.getRevised().rangeString());
                // matched
                while (count1 != orgRange.getFrom() - 1) {
                    index = index + text1[count1].length() + 1;
                    count1++;
                }
                count1 = orgRange.getFrom() - 1;
                count2 = revRange.getFrom() - 1;
                // added
                if (delta instanceof AddDelta) {
                    while (count2 != revRange.getTo()) {
                        doc.replace(index, 0, text2[count2] + "\n");
                        index = index + text2[count2].length() + 1;
                        count2++;
                    }
                    // removed
                } else if (delta instanceof DeleteDelta) {
                    while (count1 != orgRange.getTo()) {
                        doc.replace(index, text1[count1].length() + 1, "");
                        count1++;
                    }
                    // replaced
                } else if (delta instanceof ChangeDelta) {
                    while (count1 != orgRange.getTo()) {
                        doc.replace(index, text1[count1].length() + 1, "");
                        count1++;
                    }
                    while (count2 != revRange.getTo()) {
                        doc.replace(index, 0, text2[count2] + "\n");
                        index = index + text2[count2].length() + 1;
                        count2++;
                    }
                }
                count1 = orgRange.getTo();
                count2 = revRange.getTo();
            }
        } catch (Exception ex) {
            doc.set(text);
        }

        getSourceViewer().getTextWidget().setRedraw(true);
    }

    /** This class is used in setDiff(). */
    private class Range {
        private int from;
        private int to;

        public Range(String rangeString) {
            if (rangeString.indexOf(",") != -1) {
                String[] dim = rangeString.split(",");
                from = Integer.parseInt(dim[0]);
                to = Integer.parseInt(dim[1]);
            } else {
                from = Integer.parseInt(rangeString);
                to = Integer.parseInt(rangeString);
            }
        }

        public int getFrom() {
            return this.from;
        }

        public int getTo() {
            return to;
        }
    }

    /** Regular expression to search tags. */
    private final Pattern tagPattern = Pattern.compile("<([^<]*?)>");
    private final Pattern valuePattern = Pattern.compile("\".*?\"");

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

            ArrayList<FoldingInfo> list = new ArrayList<>();
            Stack<FoldingInfo> stack = new Stack<>();
            IDocument doc = getDocumentProvider().getDocument(getEditorInput());

            String xml = HTMLUtil.scriptlet2space(HTMLUtil.comment2space(doc.get(), true), true);

            // trim attribute values
            Matcher matcher = valuePattern.matcher(xml);
            while (matcher.find()) {
                int start = matcher.start(0);
                int length = matcher.group(0).length();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < length; i++) {
                    sb.append(" ");
                }
                xml = xml.substring(0, start) + sb.toString() + xml.substring(start + length);
            }

            matcher = tagPattern.matcher(xml);
            while (matcher.find()) {
                String text = matcher.group(1).trim();
                // skip XML declaration
                if (text.startsWith("?")) {
                    continue;
                }
                // DOCTYPE declaration
                if (text.startsWith("!DOCTYPE")) {
                    // Don't fold if start offset and end offset are same line
                    if (doc.getLineOfOffset(matcher.start()) != doc.getLineOfOffset(matcher.end())) {
                        FoldingInfo info = new FoldingInfo(matcher.start(), matcher.end(), "!DOCTYPE");
                        info.setEnd(info.getEnd() + FoldingInfo.countUpLineDelimiter(xml, matcher.end()));
                        list.add(info);
                    }
                    continue;
                }
                // JSP (I think this shouldn't process in here)
                if (text.startsWith("%")) {
                    // Don't fold if start offset and end offset are same line
                    if (doc.getLineOfOffset(matcher.start()) != doc.getLineOfOffset(matcher.end())) {
                        FoldingInfo info = new FoldingInfo(matcher.start(), matcher.end(), "%");
                        info.setEnd(info.getEnd() + FoldingInfo.countUpLineDelimiter(xml, matcher.end()));
                        list.add(info);
                    }
                    continue;
                }
                // comment
                if (text.startsWith("!--")) {
                    // Don't fold if start offset and end offset are same line
                    if (doc.getLineOfOffset(matcher.start()) != doc.getLineOfOffset(matcher.end())) {
                        FoldingInfo info = new FoldingInfo(matcher.start(), matcher.end(), "!--");
                        info.setEnd(info.getEnd() + FoldingInfo.countUpLineDelimiter(xml, matcher.end()));
                        list.add(info);
                    }
                    continue;
                }
                // close tag
                if (text.startsWith("/")) {
                    text = text.substring(1, text.length());
                    while (stack.size() != 0) {
                        FoldingInfo info = stack.pop();
                        if (info.getType().toLowerCase().equals(text.toLowerCase())) {
                            info.setEnd(matcher.end());
                            // Don't fold if start offset and end offset are same line
                            if (doc.getLineOfOffset(info.getStart()) != doc.getLineOfOffset(info.getEnd())) {
                                info.setEnd(info.getEnd() + FoldingInfo.countUpLineDelimiter(xml, matcher.end()));
                                list.add(info);
                            }
                            break;
                        }
                    }
                    continue;
                }
                // empty tag
                if (text.endsWith("/")) {
                    // Don't fold if start offset and end offset are same line
                    if (doc.getLineOfOffset(matcher.start()) != doc.getLineOfOffset(matcher.end())) {
                        text.substring(0, text.length() - 1);
                        if (text.indexOf(" ") != -1) {
                            text = text.substring(0, text.indexOf(" "));
                        }
                        FoldingInfo info = new FoldingInfo(matcher.start(), matcher.end(), text);
                        info.setEnd(info.getEnd() + FoldingInfo.countUpLineDelimiter(xml, matcher.end()));
                        list.add(info);
                    }
                    continue;
                }
                // start tag (put to the stack)
                if (text.indexOf(" ") != -1) {
                    text = text.substring(0, text.indexOf(" "));
                }
                stack.push(new FoldingInfo(matcher.start(), 0, text));
            }

            FoldingInfo.applyModifiedAnnotations(model, list);

        } catch (Exception ex) {
            HTMLPlugin.logException(ex);
        }
    }

    // //////////////////////////////////////////////////////////////////////////
    // Drag & Drop
    // //////////////////////////////////////////////////////////////////////////
    /*
     * private class HTMLDropListener extends DropTargetAdapter { public void dragEnter(DropTargetEvent evt){ evt.detail = DND.DROP_COPY; setFocus();
     * } // public void dragLeave(DropTargetEvent event) { // }
     * 
     * private Point calcLocation(Composite composite,Point point){ Point compPoint = composite.getLocation(); Point nextPoint = new Point(compPoint.x
     * + point.x ,compPoint.y + point.y); Composite parent = composite.getParent(); if(parent!=null){ Point result = calcLocation(parent,nextPoint);
     * return result; } else { return new Point(nextPoint.x + 30, nextPoint.y + 48); } }
     * 
     * public void dragOver(DropTargetEvent event) { try { Point point = calcLocation(getSourceViewer().getTextWidget().getParent(),new Point(0,0));
     * int x = event.x - point.x; int y = event.y - point.y; int offset = getSourceViewer().getTextWidget().getOffsetAtLocation(new Point(x,y));
     * getSourceViewer().getTextWidget().setCaretOffset(offset); } catch(Exception ex){ } } public void drop(DropTargetEvent evt){ for(int
     * i=0;i<evt.dataTypes.length;i++){ if (TextTransfer.getInstance().isSupportedType(evt.dataTypes[i])){ String text = (String)evt.data; } else if
     * (FileTransfer.getInstance().isSupportedType(evt.dataTypes[i])){ String[] files = (String[])evt.data; for(int j=0;j<files.length;j++){ File file
     * = new File(files[j]); } } else if (LocalSelectionTransfer.getInstance().isSupportedType(evt.dataTypes[i])){ IStructuredSelection sel =
     * (IStructuredSelection)evt.data; Object obj = sel.getFirstElement(); if(obj instanceof IPaletteItem){
     * ((IPaletteItem)obj).execute(HTMLSourceEditor.this); } } } } }
     */

    public boolean isFileEditorInput() {
        if (getEditorInput() instanceof IFileEditorInput) {
            return true;
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////////////////
    // actions
    // //////////////////////////////////////////////////////////////////////////
    /** The action to escape HTML tags in the selection. */
    private class EscapeHTMLAction extends Action {

        public EscapeHTMLAction() {
            super(HTMLPlugin.getResourceString("HTMLEditor.EscapeAction"));
            setEnabled(false);
            setAccelerator(SWT.CTRL | '\\');
        }

        @Override
        public void run() {
            ITextSelection sel = (ITextSelection) getSelectionProvider().getSelection();
            IDocument doc = getDocumentProvider().getDocument(getEditorInput());
            try {
                doc.replace(sel.getOffset(), sel.getLength(), HTMLUtil.escapeHTML(sel.getText()));
            } catch (BadLocationException e) {
                HTMLPlugin.logException(e);
            }
        }
    }

    /** The action to comment out selection range. */
    private class CommentAction extends Action {

        public CommentAction() {
            super(HTMLPlugin.getResourceString("HTMLEditor.CommentAction"));
            setEnabled(false);
            setAccelerator(SWT.CTRL | '/');
        }

        @Override
        public void run() {
            ITextSelection sel = (ITextSelection) getSelectionProvider().getSelection();
            IDocument doc = getDocumentProvider().getDocument(getEditorInput());
            String text = sel.getText().trim();
            try {
                if (text.startsWith("<!--") && text.indexOf("-->") > 3) {
                    text = sel.getText().replaceFirst("<!--", "");
                    text = text.replaceFirst("-->", "");
                    doc.replace(sel.getOffset(), sel.getLength(), text);
                } else {
                    doc.replace(sel.getOffset(), sel.getLength(), "<!--" + sel.getText() + "-->");
                }
            } catch (BadLocationException e) {
                HTMLPlugin.logException(e);
            }
        }
    }

}
