package ru.runa.gpd.csseditor;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
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
import ru.runa.gpd.htmleditor.ColorProvider;
import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.editors.FoldingInfo;
import ru.runa.gpd.htmleditor.editors.SoftTabVerifyListener;

/**
 * CSS Editor
 * 
 * @author Naoki Takezoe
 */
public class CSSEditor extends TextEditor {
	
	private ColorProvider colorProvider;
	private CSSOutlinePage outline;
	private CSSCharacterPairMatcher pairMatcher;
	private SoftTabVerifyListener softTabListener;
	private ProjectionSupport fProjectionSupport; 
	
	public static final String GROUP_CSS = "_css";
	public static final String ACTION_CHOOSE_COLOR = "_choose_color";
	
	public CSSEditor(){
		super();
		colorProvider = EditorsPlugin.getDefault().getColorProvider();
		setSourceViewerConfiguration(new CSSConfiguration(colorProvider));
		setPreferenceStore(new ChainedPreferenceStore(
				new IPreferenceStore[]{
						getPreferenceStore(),
						EditorsPlugin.getDefault().getPreferenceStore()
				}));
		
		outline = new CSSOutlinePage(this);
		
		setAction(ACTION_CHOOSE_COLOR, new ChooseColorAction(this));
		
		IPreferenceStore store = EditorsPlugin.getDefault().getPreferenceStore();
		softTabListener = new SoftTabVerifyListener();
		softTabListener.setUseSoftTab(store.getBoolean(HTMLPlugin.PREF_USE_SOFTTAB));
		softTabListener.setSoftTabWidth(store.getInt(HTMLPlugin.PREF_SOFTTAB_WIDTH));
	}
	
	protected final void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		menu.add(new Separator(GROUP_CSS));
		addAction(menu,GROUP_CSS,ACTION_CHOOSE_COLOR);
	}
	
	protected void doSetInput(IEditorInput input) throws CoreException {
		if(input instanceof IFileEditorInput){
			setDocumentProvider(new CSSTextDocumentProvider());
		} else if(input instanceof IStorageEditorInput){
			setDocumentProvider(new CSSFileDocumentProvider());
		} else {
			setDocumentProvider(new CSSTextDocumentProvider());
		}
		super.doSetInput(input);
	}
	
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		outline.update();
		updateFolding();
	}
	
	protected ISourceViewer createSourceViewer(Composite parent,IVerticalRuler ruler, int styles) {
		ISourceViewer viewer= new ProjectionViewer(parent, ruler, fOverviewRuler, true, styles); 
		getSourceViewerDecorationSupport(viewer);
		viewer.getTextWidget().addVerifyListener(softTabListener);
		return viewer; 
	}
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
        getSourceViewer().getTextWidget().setKeyBinding(SWT.DEL, ST.DELETE_NEXT);

		ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
		fProjectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors()); 
		fProjectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
		updateFolding();
		
		StyledText widget = viewer.getTextWidget();
		widget.setTabs(
				getPreferenceStore().getInt(
						AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));
		widget.addVerifyListener(new SoftTabVerifyListener());
		
		ITextViewerExtension2 extension= (ITextViewerExtension2) getSourceViewer();
		pairMatcher = new CSSCharacterPairMatcher();
		pairMatcher.setEnable(getPreferenceStore().getBoolean(HTMLPlugin.PREF_PAIR_CHAR));
		MatchingCharacterPainter painter = new MatchingCharacterPainter(getSourceViewer(), pairMatcher);
		painter.setColor(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		extension.addPainter(painter);
	}
	
	public void dispose() {
		pairMatcher.dispose();
		fProjectionSupport.dispose();
		super.dispose();
	}
	
	public void doSaveAs() {
		super.doSaveAs();
		outline.update();
		updateFolding();
	}
	
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return super.affectsTextPresentation(event) || colorProvider.affectsTextPresentation(event);
	}
	
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event){
		colorProvider.handlePreferenceStoreChanged(event);
		
		String key = event.getProperty();
		if(key.equals(HTMLPlugin.PREF_PAIR_CHAR)){
			boolean enable = ((Boolean)event.getNewValue()).booleanValue();
			pairMatcher.setEnable(enable);
		}
		
		super.handlePreferenceStoreChanged(event);
		softTabListener.preferenceChanged(event);
	}	
	
	protected void createActions() {
	    super.createActions();
	    // Add a content assist action
	    IAction action = new ContentAssistAction(
	    		HTMLPlugin.getResourceBundle(),"ContentAssistProposal", this);
	    action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
	    setAction("ContentAssistProposal", action);
	}
	
	public Object getAdapter(Class adapter){
		if(IContentOutlinePage.class.equals(adapter)){
			return outline;
		}
		if(ProjectionAnnotationModel.class.equals(adapter) && fProjectionSupport!=null){
			Object obj = fProjectionSupport.getAdapter(getSourceViewer(), adapter); 
			if (obj != null) {
				return obj; 
			}
		}
		return super.getAdapter(adapter);
	}
	
	private static final int FOLDING_NONE = 0;
	private static final int FOLDING_STYLE = 1;
	private static final int FOLDING_COMMENT = 2;
	
	/**
	 * Update folding informations.
	 */
	private void updateFolding(){
		try {
			ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
			if(viewer==null){
				return;
			}
			ProjectionAnnotationModel model = viewer.getProjectionAnnotationModel();
			if(model==null){
				return;
			}
			
			ArrayList<FoldingInfo> list = new ArrayList<>();
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			String source = doc.get();
			
			int type = FOLDING_NONE;
			int start = -1;
			int startBackup = -1;
			
			for(int i=0;i<source.length();i++){
				char c = source.charAt(i);
				// start comment
				if(c=='/' && type!=FOLDING_COMMENT && source.length() > i+1){
					if(source.charAt(i+1)=='*'){
						if(type==FOLDING_STYLE){
							startBackup = start;
						}
						type = FOLDING_COMMENT;
						start = i;
						i++;
					}
				// end comment
				} else if(c=='*' && type==FOLDING_COMMENT && source.length() > i+1){
					if(source.charAt(i+1)=='/'){
						if(doc.getLineOfOffset(start)!=doc.getLineOfOffset(i)){
							list.add(new FoldingInfo(start, i+2 + FoldingInfo.countUpLineDelimiter(source, i+2)));
						}
						if(startBackup!=-1){
							type = FOLDING_STYLE;
							start = startBackup;
						} else {
							type = FOLDING_NONE;
						}
						startBackup = -1;
						i++;
					}
				// start blace
				} else if(c=='{' && type==FOLDING_NONE){
					if(type==FOLDING_COMMENT){
						startBackup = start;
					}
					start = i;
					type = FOLDING_STYLE;
				// end blace
				} else if(type==FOLDING_STYLE && c=='}'){
					if(doc.getLineOfOffset(start)!=doc.getLineOfOffset(i)){
						list.add(new FoldingInfo(start, i+1 + FoldingInfo.countUpLineDelimiter(source, i+1)));
					}
					if(startBackup!=-1){
						type = FOLDING_COMMENT;
						start = startBackup;
					}
					startBackup = -1;
					type = FOLDING_NONE;
				}
			}
			
			FoldingInfo.applyModifiedAnnotations(model, list);
			
		} catch(Exception ex){
			HTMLPlugin.logException(ex);
		}
	}
	
}
