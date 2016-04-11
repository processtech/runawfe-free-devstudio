package ru.runa.gpd.htmleditor.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.htmleditor.ColorProvider;
import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.assist.HTMLAssistProcessor;
import ru.runa.gpd.htmleditor.assist.InnerCSSAssistProcessor;
import ru.runa.gpd.htmleditor.assist.InnerJavaScriptAssistProcessor;

/**
 * <code>SourceViewerConfiguration</code> for <code>HTMLSourceEditor</code>.
 * 
 * @author Naoki Takezoe
 * @see HTMLSourceEditor
 */
public class HTMLConfiguration extends SourceViewerConfiguration {

    private HTMLDoubleClickStrategy doubleClickStrategy;

    private HTMLScanner scanner;
    private HTMLTagScanner tagScanner;
    private RuleBasedScanner commentScanner;
    private RuleBasedScanner scriptScanner;
    private RuleBasedScanner doctypeScanner;
    private RuleBasedScanner directiveScanner;
    private RuleBasedScanner javaScriptScanner;
    private RuleBasedScanner cssScanner;

    private final ColorProvider colorProvider;

    private IEditorPart editor;

    private ContentAssistant assistant;
    private HTMLAssistProcessor processor;
    private InnerJavaScriptAssistProcessor jsProcessor;
    private InnerCSSAssistProcessor cssProcessor;

    public HTMLConfiguration(ColorProvider colorProvider) {
        this.colorProvider = colorProvider;
    }

    public void setEditorPart(IEditorPart editor) {
        this.editor = editor;
    }

    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new HTMLAnnotationHover(editor);
    }

    /**
     * Returns all supportted content types.
     */
    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] { IDocument.DEFAULT_CONTENT_TYPE, HTMLPartitionScanner.HTML_COMMENT, HTMLPartitionScanner.HTML_TAG, HTMLPartitionScanner.HTML_SCRIPT,
                HTMLPartitionScanner.HTML_DOCTYPE, HTMLPartitionScanner.HTML_DIRECTIVE, HTMLPartitionScanner.JAVASCRIPT, HTMLPartitionScanner.HTML_CSS };
    }

    /**
     * Creates or Returns the assist processor for HTML.
     */
    public HTMLAssistProcessor getAssistProcessor() {
        if (processor == null) {
            processor = createAssistProcessor();
        }
        return processor;
    }

    /**
     * Creates the assist processor for HTML.
     */
    protected HTMLAssistProcessor createAssistProcessor() {
        HTMLAssistProcessor processor = new HTMLAssistProcessor();
        return processor;
    }

    /**
     * Creates or Returns the assist processor for inner JavaScript.
     */
    public InnerJavaScriptAssistProcessor getJavaScriptAssistProcessor() {
        if (jsProcessor == null) {
            jsProcessor = new InnerJavaScriptAssistProcessor(getAssistProcessor());
        }
        return jsProcessor;
    }

    /**
     * Creates or Returns the assist processor for inner JavaScript.
     */
    public InnerCSSAssistProcessor getCSSAssistProcessor() {
        if (cssProcessor == null) {
            cssProcessor = new InnerCSSAssistProcessor(getAssistProcessor());
        }
        return cssProcessor;
    }

    /**
     * Creates or Returns the <code>IContentAssistant</code>.
     */
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        if (assistant == null) {
            assistant = new ContentAssistant();
            assistant.setInformationControlCreator(new IInformationControlCreator() {
                @Override
                public IInformationControl createInformationControl(Shell parent) {
                    return new DefaultInformationControl(parent);
                }
            });
            assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
            assistant.enableAutoInsert(true);

            HTMLAssistProcessor processor = getAssistProcessor();
            assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
            assistant.setContentAssistProcessor(processor, HTMLPartitionScanner.HTML_TAG);

            InnerJavaScriptAssistProcessor jsProcessor = getJavaScriptAssistProcessor();
            assistant.setContentAssistProcessor(jsProcessor, HTMLPartitionScanner.JAVASCRIPT);

            InnerCSSAssistProcessor cssProcessor = getCSSAssistProcessor();
            assistant.setContentAssistProcessor(cssProcessor, HTMLPartitionScanner.HTML_CSS);

            assistant.install(sourceViewer);

            IPreferenceStore store = EditorsPlugin.getDefault().getPreferenceStore();
            assistant.enableAutoActivation(store.getBoolean(HTMLPlugin.PREF_ASSIST_AUTO));
            assistant.setAutoActivationDelay(store.getInt(HTMLPlugin.PREF_ASSIST_TIMES));
            processor.setAutoAssistChars(store.getString(HTMLPlugin.PREF_ASSIST_CHARS).toCharArray());
            processor.setAssistCloseTag(store.getBoolean(HTMLPlugin.PREF_ASSIST_CLOSE));
        }
        return assistant;
    }

    /**
     * Returns <code>ITextDoubleClickStrategy</code>.
     */
    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
        if (doubleClickStrategy == null) {
            doubleClickStrategy = new HTMLDoubleClickStrategy();
        }
        return doubleClickStrategy;
    }

    /**
     * Creates or Returns the scanner for HTML.
     */
    protected HTMLScanner getHTMLScanner() {
        if (scanner == null) {
            scanner = new HTMLScanner(colorProvider);
            scanner.setDefaultReturnToken(colorProvider.getToken(HTMLPlugin.PREF_COLOR_FG));
        }
        return scanner;
    }

    /**
     * Creates or Returns the scanner for HTML tags.
     */
    protected HTMLTagScanner getTagScanner() {
        if (tagScanner == null) {
            tagScanner = new HTMLTagScanner(colorProvider);
            tagScanner.setDefaultReturnToken(colorProvider.getToken(HTMLPlugin.PREF_COLOR_TAG));
        }
        return tagScanner;
    }

    /**
     * Creates or Returns the scanner for HTML comments.
     */
    protected RuleBasedScanner getCommentScanner() {
        if (commentScanner == null) {
            commentScanner = new RuleBasedScanner();
            commentScanner.setDefaultReturnToken(colorProvider.getToken(HTMLPlugin.PREF_COLOR_COMMENT));
        }
        return commentScanner;
    }

    /**
     * Creates or Returns the scanner for scriptlets (&lt;% ... %&gt;).
     */
    protected RuleBasedScanner getScriptScanner() {
        if (scriptScanner == null) {
            scriptScanner = new RuleBasedScanner();
            scriptScanner.setDefaultReturnToken(colorProvider.getToken(HTMLPlugin.PREF_COLOR_SCRIPT));
        }
        return scriptScanner;
    }

    /**
     * Creates or Returns the scanner for directives (&lt;%@ ... %&gt;).
     */
    protected RuleBasedScanner getDirectiveScanner() {
        if (directiveScanner == null) {
            directiveScanner = new RuleBasedScanner();
            directiveScanner.setDefaultReturnToken(colorProvider.getToken(HTMLPlugin.PREF_COLOR_SCRIPT));
        }
        return directiveScanner;
    }

    /**
     * Creates or Returns the scanner for DOCTYPE decl.
     */
    protected RuleBasedScanner getDoctypeScanner() {
        if (doctypeScanner == null) {
            doctypeScanner = new RuleBasedScanner();
            doctypeScanner.setDefaultReturnToken(colorProvider.getToken(HTMLPlugin.PREF_COLOR_DOCTYPE));
        }
        return doctypeScanner;
    }

    /**
     * Creates or Returns the scanner for inner JavaScript.
     */
    protected RuleBasedScanner getJavaScriptScanner() {
        if (javaScriptScanner == null) {
            javaScriptScanner = new InnerJavaScriptScanner(colorProvider);
            javaScriptScanner.setDefaultReturnToken(colorProvider.getToken(HTMLPlugin.PREF_COLOR_FG));
        }
        return javaScriptScanner;
    }

    /**
     * Creates or Returns the scanner for inner CSS.
     */
    protected RuleBasedScanner getCSSScanner() {
        if (cssScanner == null) {
            cssScanner = new InnerCSSScanner(colorProvider);
            cssScanner.setDefaultReturnToken(colorProvider.getToken(HTMLPlugin.PREF_COLOR_FG));
        }
        return cssScanner;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

        DefaultDamagerRepairer dr = null;

        dr = new HTMLTagDamagerRepairer(getTagScanner());
        reconciler.setDamager(dr, HTMLPartitionScanner.HTML_TAG);
        reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_TAG);

        dr = new DefaultDamagerRepairer(getHTMLScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(getCommentScanner());
        reconciler.setDamager(dr, HTMLPartitionScanner.HTML_COMMENT);
        reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_COMMENT);

        dr = new DefaultDamagerRepairer(getScriptScanner());
        reconciler.setDamager(dr, HTMLPartitionScanner.HTML_SCRIPT);
        reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_SCRIPT);

        dr = new DefaultDamagerRepairer(getDoctypeScanner());
        reconciler.setDamager(dr, HTMLPartitionScanner.HTML_DOCTYPE);
        reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_DOCTYPE);

        dr = new DefaultDamagerRepairer(getDirectiveScanner());
        reconciler.setDamager(dr, HTMLPartitionScanner.HTML_DIRECTIVE);
        reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_DIRECTIVE);

        dr = new JavaScriptDamagerRepairer(getJavaScriptScanner());
        reconciler.setDamager(dr, HTMLPartitionScanner.JAVASCRIPT);
        reconciler.setRepairer(dr, HTMLPartitionScanner.JAVASCRIPT);

        dr = new JavaScriptDamagerRepairer(getCSSScanner());
        reconciler.setDamager(dr, HTMLPartitionScanner.HTML_CSS);
        reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_CSS);

        return reconciler;
    }

    /**
     * Returns the <code>ColorProvider</code>.
     */
    protected ColorProvider getColorProvider() {
        return this.colorProvider;
    }

    private class HTMLTagDamagerRepairer extends DefaultDamagerRepairer {

        public HTMLTagDamagerRepairer(ITokenScanner scanner) {
            super(scanner);
        }

        @Override
        public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
            if (!documentPartitioningChanged) {
                String source = fDocument.get();
                int start = source.substring(0, e.getOffset()).lastIndexOf('<');
                if (start == -1) {
                    start = 0;
                }
                int end = source.indexOf('>', e.getOffset());
                int end2 = e.getOffset() + (e.getText() == null ? e.getLength() : e.getText().length());
                if (end == -1) {
                    end = source.length();
                } else if (end2 > end) {
                    end = end2;
                } else {
                    end++;
                }

                return new Region(start, end - start);
            }
            return partition;
        }

    }

    private class JavaScriptDamagerRepairer extends DefaultDamagerRepairer {

        public JavaScriptDamagerRepairer(ITokenScanner scanner) {
            super(scanner);
        }

        @Override
        public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
            if (!documentPartitioningChanged) {
                String source = fDocument.get();
                int start = source.substring(0, e.getOffset()).lastIndexOf("/*");
                if (start == -1) {
                    start = 0;
                }
                int end = source.indexOf("*/", e.getOffset());
                int end2 = e.getOffset() + (e.getText() == null ? e.getLength() : e.getText().length());
                if (end == -1) {
                    end = source.length();
                } else if (end2 > end) {
                    end = end2;
                } else {
                    end++;
                }

                return new Region(start, end - start);
            }
            return partition;
        }

    }

}