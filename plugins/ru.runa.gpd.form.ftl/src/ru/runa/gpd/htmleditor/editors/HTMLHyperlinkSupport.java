package ru.runa.gpd.htmleditor.editors;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLParser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;

import ru.runa.gpd.htmleditor.HTMLPlugin;

/**
 * This class provides the hyperlink feature.
 * 
 * @author Naoki Takezoe
 */
public class HTMLHyperlinkSupport implements KeyListener,MouseListener,MouseMoveListener,
	FocusListener, PaintListener, IDocumentListener, ITextInputListener, ITextPresentationListener {
	
	private boolean active;
	private IRegion activeRegion;
	private Cursor cursor;
	private Color color;
	private Object open;
	private Position fRememberedPosition;
	private IProject project;
	private HTMLSourceEditor editor;
	
	public HTMLHyperlinkSupport(HTMLSourceEditor editor){
		this.editor = editor;
	}
	
	public void install() {
		ISourceViewer sourceViewer= editor.getViewer();
		if (sourceViewer == null)
			return;
			
		StyledText text= sourceViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
		
		color = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
//		updateColor(sourceViewer);

		sourceViewer.addTextInputListener(this);
		
		IDocument document= sourceViewer.getDocument();
		if (document != null)
			document.addDocumentListener(this);

		text.addKeyListener(this);
		text.addMouseListener(this);
		text.addMouseMoveListener(this);
		text.addFocusListener(this);
		text.addPaintListener(this);
		
		((ITextViewerExtension4)sourceViewer).addTextPresentationListener(this);
		
//		updateKeyModifierMask();
		
//		IPreferenceStore preferenceStore= getPreferenceStore();
//		preferenceStore.addPropertyChangeListener(this);
	}
	
	public void uninstall() {

//		if (color != null) {
//			color.dispose();
//			color= null;
//		}
		
		if (cursor != null) {
			cursor.dispose();
			cursor= null;
		}
		
		ISourceViewer sourceViewer= editor.getViewer();
		if (sourceViewer != null)
			sourceViewer.removeTextInputListener(this);
		
		IDocumentProvider documentProvider= editor.getDocumentProvider();
		if (documentProvider != null) {
			IDocument document= documentProvider.getDocument(editor.getEditorInput());
			if (document != null)
				document.removeDocumentListener(this);
		}
			
//		IPreferenceStore preferenceStore= getPreferenceStore();
//		if (preferenceStore != null)
//			preferenceStore.removePropertyChangeListener(this);
		
		if (sourceViewer == null)
			return;
		
		StyledText text= sourceViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
			
		text.removeKeyListener(this);
		text.removeMouseListener(this);
		text.removeMouseMoveListener(this);
		text.removeFocusListener(this);
		text.removePaintListener(this);
		
		((ITextViewerExtension4)sourceViewer).removeTextPresentationListener(this);
	}
	
	public void deactivate() {
		deactivate(false);
	}
	
	public void deactivate(boolean redrawAll) {
		if (!active)
			return;
			repairRepresentation(redrawAll);
		active = false;
	}
	
	private void resetCursor(ISourceViewer viewer) {
		StyledText text= viewer.getTextWidget();
		if (text != null && !text.isDisposed())
			text.setCursor(null);
					
		if (cursor != null) {
			cursor.dispose();
			cursor= null;
		}
	}
	
	private void repairRepresentation() {
		repairRepresentation(false);
	}
	
	private void repairRepresentation(boolean redrawAll) {
		if (activeRegion == null)
			return;
		
		int offset= activeRegion.getOffset();
		int length= activeRegion.getLength();
		activeRegion= null;
			
		ISourceViewer viewer= editor.getViewer();
		if (viewer != null) {
			
			resetCursor(viewer);
			
			// Invalidate ==> remove applied text presentation
			if (!redrawAll && viewer instanceof ITextViewerExtension2)
				((ITextViewerExtension2) viewer).invalidateTextPresentation(offset, length);
			else
				viewer.invalidateTextPresentation();
			
			// Remove underline
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
				offset= extension.modelOffset2WidgetOffset(offset);
			} else {
				offset -= viewer.getVisibleRegion().getOffset();
			}
			try {
				StyledText text= viewer.getTextWidget();

				text.redrawRange(offset, length, false);
			} catch (IllegalArgumentException x) {
//				JavaPlugin.log(x);
			}
		}
	}
	
	public void keyPressed(KeyEvent e) {
		if (active) {
			deactivate();
			return;	
		}
		if (e.keyCode != SWT.CTRL) {
			deactivate();
			return;
		}
		active = true;
	}
	
	public void keyReleased(KeyEvent event) {
		if (!active)
			return;
		deactivate();
	}

	
	public void mouseUp(MouseEvent e) {
		if (!active)
			return;
			
		if (e.button != 1) {
			deactivate();
			return;
		}
		
		boolean wasActive= cursor != null;
		deactivate();
		
		if (wasActive) {
			try {
				if(open instanceof IFile){
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IDE.openEditor(window.getActivePage(),(IFile)open,true);
				} else if(open instanceof IJavaElement){
					JavaUI.revealInEditor(JavaUI.openInEditor((IJavaElement)open), (IJavaElement)open);
				}
			} catch(Exception ex){
				HTMLPlugin.logException(ex);
			}
		}
	}
	
	public void mouseDoubleClick(MouseEvent e) {}
	
	public void mouseDown(MouseEvent event) {
		if (!active)
			return;
			
		if (event.stateMask != SWT.CTRL) {
			deactivate();
			return;	
		}
		if (event.button != 1) {
			deactivate();
			return;	
		}			
	}
	
	public void mouseMove(MouseEvent event) {
		
		if (event.widget instanceof Control && !((Control) event.widget).isFocusControl()) {
			deactivate();
			return;
		}
		
		if (!active) {
			if (event.stateMask != SWT.CTRL)
				return;
			// modifier was already pressed
			active = true;
		}

		ISourceViewer viewer= editor.getViewer();
		if (viewer == null) {
			deactivate();
			return;
		}
			
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			deactivate();
			return;
		}
			
		if ((event.stateMask & SWT.BUTTON1) != 0 && text.getSelectionCount() != 0) {
			deactivate();
			return;
		}
	
		IRegion region= getCurrentTextRegion(viewer);
		if (region == null || region.getLength() == 0) {
			repairRepresentation();
			return;
		}
		
		highlightRegion(viewer, region);
		activateCursor(viewer);
	}
	
	private void activateCursor(ISourceViewer viewer) {
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
		Display display= text.getDisplay();
		if (cursor == null)
			cursor= new Cursor(display, SWT.CURSOR_HAND);
		text.setCursor(cursor);
	}
	
	private IRegion selectWord(IDocument doc,int offset){
		FuzzyXMLDocument document = new FuzzyXMLParser().parse(editor.getHTMLSource());
		FuzzyXMLElement element = document.getElementByOffset(offset);
		if(element==null){
			return null;
		}
		FuzzyXMLAttribute[] attrs = element.getAttributes();
		for(int i=0;i<attrs.length;i++){
			if(attrs[i].getOffset() < offset && offset < attrs[i].getOffset()+attrs[i].getLength()){
				int attrOffset = getAttributeValueOffset(doc.get(),attrs[i]);
				int attrLength = attrs[i].getValue().length();
				if(attrOffset >= 0 && attrLength >= 0 && attrOffset <= offset){
					HTMLHyperlinkInfo info = getOpenFileInfo(document,element,attrs[i].getName(),attrs[i].getValue(),offset-attrOffset);
					if(info==null || info.getObject()==null){
						open = null;
						return null;
					} else {
						open = info.getObject();
						return new Region(attrOffset+info.getOffset(),info.getLength());
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns a target of hyperlink.
	 */
	private HTMLHyperlinkInfo getOpenFileInfo(FuzzyXMLDocument doc,FuzzyXMLElement element,String attrName,String attrValue,int offset){
		try {
			if(project==null){
				return null;
			}
			IFile file = ((IFileEditorInput)editor.getEditorInput()).getFile();
			if(attrName.equalsIgnoreCase("href")){
				String href = attrValue;
				if(href.indexOf("#") > 0){
					href = href.substring(0,href.indexOf("#"));
				}
				IPath path = file.getParent().getProjectRelativePath();
				IResource resource = project.findMember(path.append(href));
				if(resource!=null && resource.exists() && resource instanceof IFile){
					HTMLHyperlinkInfo info = new HTMLHyperlinkInfo();
					info.setObject(resource);
					info.setOffset(0);
					info.setLength(attrValue.length());
					return info;
				}
			}
		} catch(Exception ex){
			HTMLPlugin.logException(ex);
		}
		return null;
	}
	
	/**
	 * Returns an attribute value offset.
	 */
	private int getAttributeValueOffset(String source,FuzzyXMLAttribute attr){
		int offset = source.indexOf('=',attr.getOffset());
		if(offset == -1){
			return -1;
		}
		char c = ' ';
		while(c==' ' || c=='\t' || c=='\r' || c=='\n' || c=='"' || c=='\''){
			offset++;
			if(source.length() == offset+1){
				break;
			}
			c = source.charAt(offset);
		}
		return offset;
	}
	
	private IRegion getCurrentTextRegion(ISourceViewer viewer) {

		int offset= getCurrentTextOffset(viewer);
		if (offset == -1)
			return null;

		return selectWord(viewer.getDocument(), offset);
	}

	private int getCurrentTextOffset(ISourceViewer viewer) {

		try {
			StyledText text= viewer.getTextWidget();
			if (text == null || text.isDisposed())
				return -1;

			Display display= text.getDisplay();
			Point absolutePosition= display.getCursorLocation();
			Point relativePosition= text.toControl(absolutePosition);
			
			int widgetOffset= text.getOffsetAtLocation(relativePosition);
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
				return extension.widgetOffset2ModelOffset(widgetOffset);
			} else {
				return widgetOffset + viewer.getVisibleRegion().getOffset();
			}

		} catch (IllegalArgumentException e) {
			return -1;
		}
	}
	
	private void highlightRegion(ISourceViewer viewer, IRegion region) {

		if (region.equals(activeRegion))
			return;

		repairRepresentation();
		
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
		
		// Underline
		int offset= 0;
		int length= 0;
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(region);
			if (widgetRange == null)
				return;
				
			offset= widgetRange.getOffset();
			length= widgetRange.getLength();
			
		} else {
			offset= region.getOffset() - viewer.getVisibleRegion().getOffset();
			length= region.getLength();
		}
		text.redrawRange(offset, length, false);
		
		// Invalidate region ==> apply text presentation
		activeRegion = region;
		if (viewer instanceof ITextViewerExtension2)
			((ITextViewerExtension2) viewer).invalidateTextPresentation(region.getOffset(), region.getLength());
		else
			viewer.invalidateTextPresentation();
	}
	
	public void focusGained(FocusEvent e) {}

	public void focusLost(FocusEvent event) {
		deactivate();
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		if (active && activeRegion != null) {
			fRememberedPosition= new Position(activeRegion.getOffset(), activeRegion.getLength());
			try {
				event.getDocument().addPosition(fRememberedPosition);
			} catch (BadLocationException x) {
				fRememberedPosition= null;
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		if (fRememberedPosition != null) {
			if (!fRememberedPosition.isDeleted()) {
				
				event.getDocument().removePosition(fRememberedPosition);
				activeRegion= new Region(fRememberedPosition.getOffset(), fRememberedPosition.getLength());
				fRememberedPosition= null;
				
				ISourceViewer viewer= editor.getViewer();
				if (viewer != null) {
					StyledText widget= viewer.getTextWidget();
					if (widget != null && !widget.isDisposed()) {
						widget.getDisplay().asyncExec(new Runnable() {
							public void run() {
								deactivate();
							}
						});
					}
				}
				
			} else {
				activeRegion= null;
				fRememberedPosition= null;
				deactivate();
			}
		}
	}

	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (oldInput == null)
			return;
		deactivate();
		oldInput.removeDocumentListener(this);
	}

	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput == null)
			return;
		newInput.addDocumentListener(this);
	}
	
	public void applyTextPresentation(TextPresentation textPresentation) {
		if (activeRegion == null)
			return;
		IRegion region= textPresentation.getExtent();
		if (activeRegion.getOffset() + activeRegion.getLength() >= region.getOffset() && region.getOffset() + region.getLength() > activeRegion.getOffset())
			textPresentation.mergeStyleRange(new StyleRange(activeRegion.getOffset(), activeRegion.getLength(), color, null));
	}

	public void paintControl(PaintEvent event) {	
		if (activeRegion == null)
			return;

		ISourceViewer viewer= editor.getViewer();
		if (viewer == null)
			return;
			
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
			
			
		int offset= 0;
		int length= 0;

		if (viewer instanceof ITextViewerExtension5) {
			
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(activeRegion);
			if (widgetRange == null)
				return;
				
			offset= widgetRange.getOffset();
			length= widgetRange.getLength();
			
		} else {
			
			IRegion region= viewer.getVisibleRegion();			
			if (!includes(region, activeRegion))
				return;		    
			
			offset= activeRegion.getOffset() - region.getOffset();
			length= activeRegion.getLength();
		}
		
		// support for bidi
		Point minLocation= getMinimumLocation(text, offset, length);
		Point maxLocation= getMaximumLocation(text, offset, length);

		int x1= minLocation.x;
		int x2= minLocation.x + maxLocation.x - minLocation.x - 1;
		int y= minLocation.y + text.getLineHeight() - 1;
		
		GC gc= event.gc;
		if (color != null && !color.isDisposed())
		gc.setForeground(color);
		gc.drawLine(x1, y, x2, y);
	}
	
	private boolean includes(IRegion region, IRegion position) {
		return
			position.getOffset() >= region.getOffset() &&
			position.getOffset() + position.getLength() <= region.getOffset() + region.getLength();
	}
	
	private Point getMinimumLocation(StyledText text, int offset, int length) {
		Point minLocation= new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

		for (int i= 0; i <= length; i++) {
			Point location= text.getLocationAtOffset(offset + i);
			
			if (location.x < minLocation.x)
				minLocation.x= location.x;			
			if (location.y < minLocation.y)
				minLocation.y= location.y;			
		}	
		
		return minLocation;
	}

	private Point getMaximumLocation(StyledText text, int offset, int length) {
		Point maxLocation= new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		for (int i= 0; i <= length; i++) {
			Point location= text.getLocationAtOffset(offset + i);
			
			if (location.x > maxLocation.x)
				maxLocation.x= location.x;			
			if (location.y > maxLocation.y)
				maxLocation.y= location.y;			
		}	
		
		return maxLocation;
	}
	
	public void setProject(IProject project){
		this.project = project;
	}
	
}