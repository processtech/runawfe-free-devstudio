package ru.runa.gpd.xmleditor;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IFileEditorInput;
import org.xml.sax.InputSource;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.HTMLProjectParams;
import ru.runa.gpd.htmleditor.HTMLUtil;
import ru.runa.gpd.htmleditor.editors.HTMLConfiguration;
import ru.runa.gpd.htmleditor.editors.HTMLSourceEditor;
import ru.runa.gpd.htmleditor.editors.IHTMLOutlinePage;

/**
 * XML editor.
 * 
 * @author Naoki Takezoe
 */
public class XMLEditor extends HTMLSourceEditor {
	
	private ArrayList resolvers = new ArrayList();
	
	public static final String GROUP_XML = "_xml";
	public static final String ACTION_GEN_DTD = "_generate_dtd";
	public static final String ACTION_GEN_XSD = "_generate_xsd";
	public static final String ACTION_ESCAPE_XML = "_escape_xml";
	
	public XMLEditor() {
		this(new XMLConfiguration(EditorsPlugin.getDefault().getColorProvider()));
	}
	
	public XMLEditor(XMLConfiguration config){
		super(config);
		
		setAction(ACTION_GEN_DTD,new GenerateDTDAction());
		setAction(ACTION_GEN_XSD,new GenerateXSDAction());
		setAction(ACTION_ESCAPE_XML, new EscapeXMLAction());
	}
	
	protected IHTMLOutlinePage createOutlinePage(){
		return new XMLOutlinePage(this);
	}
	
    public void reloadXML() {
        IFileEditorInput input = (IFileEditorInput) getEditorInput();
        try {
            input.getFile().refreshLocal(2, null);
        } catch (CoreException e) {
        }
    }
    
	/**
	 * Adds IDTDResolver.
	 * 
	 * @param resolver IDTDResolver
	 */
	public void addDTDResolver(IDTDResolver resolver){
		resolvers.add(resolver);
	}
	
	/**
	 * Returns an array of IDTDResolver that was added by addEntityResolver().
	 * 
	 * @return an array of IDTDResolver
	 */
	public IDTDResolver[] getDTDResolvers(){
		return (IDTDResolver[])resolvers.toArray(new IDTDResolver[resolvers.size()]);
	}
	
	/**
	 * Validates the XML document.
	 * If getValidation() returns false, this method do nothing.
	 */
	protected void doValidate(){
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						IFileEditorInput input = (IFileEditorInput)getEditorInput();
						String xml = getDocumentProvider().getDocument(input).get();
						IFile resource = input.getFile();
						//String charset = resource.getCharset();
						//charset = "Shift_JIS";
						resource.deleteMarkers(IMarker.PROBLEM,false,0);
						
						HTMLProjectParams params = new HTMLProjectParams(resource.getProject());
						if(!params.getValidateXML()){
							return;
						}
						
						if(params.getUseDTD()==false){
							// remove DOCTYPE decl
							Matcher matcher = patternDoctypePublic.matcher(xml);
							if(matcher.find()){
								xml = removeMatched(xml,matcher.start(),matcher.end());
							}
							matcher = patternDoctypeSystem.matcher(xml);
							if(matcher.find()){
								xml = removeMatched(xml,matcher.start(),matcher.end());
							}
						}
						
						SAXParser parser = new SAXParser();
						
						String   dtd = getDTD(xml);
						String[] xsd = getXSD(xml);
						
						// Validation configuration
						if((dtd==null && xsd==null) || !params.getUseDTD()){
							parser.setFeature("http://xml.org/sax/features/validation", false);
						} else {
							parser.setFeature("http://xml.org/sax/features/validation", true);
							parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
						}
						if(xsd!=null && params.getUseDTD()){
//							parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
//							parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", xsd);
							parser.setFeature("http://apache.org/xml/features/validation/schema", true);
							parser.setFeature("http://xml.org/sax/features/namespaces", true);
						}
						
						parser.setFeature("http://xml.org/sax/features/use-entity-resolver2", true);
						parser.setEntityResolver(new DTDResolver(getDTDResolvers(),
								input.getFile().getLocation().makeAbsolute().toFile().getParentFile()));
						parser.setErrorHandler(new XMLValidationHandler(resource));
						
						parser.parse(new InputSource(new StringReader(xml))); //new ByteArrayInputStream(xml.getBytes(charset))));
						
					} catch(Exception ex){
						// ignore
						//HTMLPlugin.logException(ex);
					}
				}
			},null);
		} catch(Exception ex){
			HTMLPlugin.logException(ex);
		}
	}
	
	/** replace to whitespaces */
	private String removeMatched(String source,int start,int end){
		StringBuffer sb = new StringBuffer();
		sb.append(source.substring(0,start));
		for(int i=start;i<end + 1;i++){
			char c = source.charAt(i);
			if(c=='\r' || c=='\n'){
				sb.append(c);
			} else {
				sb.append(" ");
			}
		}
		sb.append(source.substring(end+1,source.length()));
		return sb.toString();
	}
	
	/**
	 * Returns URI of DTD (SystemID) which is used in the document.
	 * If any DTD isn't used, this method returns null.
	 * 
	 * @param xml XML
	 * @return URL of DTD
	 */
	public String getDTD(String xml){
		// PUBLIC Identifier
		Matcher matcher = patternDoctypePublic.matcher(xml);
		if(matcher.find()){
			return matcher.group(2);
		}
		// SYSTEM Identifier
		matcher = patternDoctypeSystem.matcher(xml);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	/**
	 * Returns URI (schema location) of XML schema which is used in the document.
	 * If any XML schema isn't used, this method returns null.
	 * 
	 * @param xml XML
	 * @return URL of XML schema
	 */
	public String[] getXSD(String xml){
		// PUBLIC Identifier
		Matcher matcher = patternNsXSD.matcher(xml);
		if(matcher.find()){
			String matched = matcher.group(1).trim();
			matched.replaceAll("\r\n","\n");
			matched.replaceAll("\r","\n");
			String[] xsd = matched.split("\n| |\t");
			for(int i=0;i<xsd.length;i++){
				xsd[i] = xsd[i].trim();
			}
			return xsd;
		}
		matcher = patternNoNsXSD.matcher(xml);
		if(matcher.find()){
			return new String[]{matcher.group(3).trim()};
		}
		
		return null;
	}
	
	/** Reular expressions to get DOCTYPE declaration */
	private Pattern patternDoctypePublic
		= Pattern.compile("<!DOCTYPE[\\s\r\n]+?[^<]+?[\\s\r\n]+?PUBLIC[\\s\r\n]*?\"(.+?)\"[\\s\r\n]*?\"(.+?)\".*?>",Pattern.DOTALL);
	private Pattern patternDoctypeSystem
		= Pattern.compile("<!DOCTYPE[\\s\r\n]+?[^<]+?[\\s\r\n]+?SYSTEM[\\s\r\n]*?\"(.+?)\".*?>",Pattern.DOTALL);
	
	/** Reular expressions to get schema location of XMLschema */
	private Pattern patternNsXSD 
		= Pattern.compile("schemaLocation[\\s\r\n]*?=[\\s\r\n]*?\"(.+?)\"",Pattern.DOTALL);
	private Pattern patternNoNsXSD 
		= Pattern.compile("noNamespaceSchemaLocation[\\s\r\n]*?=[\\s\r\n]*?\"(.+?)\"",Pattern.DOTALL);
	
	/**
	 * Update informations about code-completion.
	 */
	protected void updateAssist(){
		try {
			if(!isFileEditorInput()){
				return;
			}
			super.updateAssist();
			
			IFileEditorInput input = (IFileEditorInput)getEditorInput();
			HTMLProjectParams params = new HTMLProjectParams(input.getFile().getProject());
			if(params.getUseDTD()==false){
				return;
			}
			
			String xml = getDocumentProvider().getDocument(input).get();
			
			// Update DTD based completion information.
			String dtd = getDTD(xml);
			if(dtd!=null){
				DTDResolver resolver = new DTDResolver(getDTDResolvers(), 
						input.getFile().getLocation().makeAbsolute().toFile().getParentFile());
				InputStream in = resolver.getInputStream(dtd);
				if(in!=null){
					Reader reader = new InputStreamReader(in);
					// update AssistProcessor
					XMLAssistProcessor assistProcessor = 
						(XMLAssistProcessor)((HTMLConfiguration)getSourceViewerConfiguration()).getAssistProcessor();
					assistProcessor.updateDTDInfo(reader);
					reader.close();
				}
			}
			
			// Update XML Schema based completion information.
			String[] xsd = getXSD(xml);
			if(xsd!=null){
				DTDResolver resolver = new DTDResolver(getDTDResolvers(),
						input.getFile().getLocation().makeAbsolute().toFile().getParentFile());
				for(int i=0;i<xsd.length;i++){
					InputStream in = resolver.getInputStream(xsd[i]);
					if(in!=null){
						Reader reader = new InputStreamReader(in);
						// update AssistProcessor
						XMLAssistProcessor assistProcessor = 
							(XMLAssistProcessor)((HTMLConfiguration)getSourceViewerConfiguration()).getAssistProcessor();
						assistProcessor.updateXSDInfo(xsd[i], reader);
						reader.close();
					}
				}
			}
		} catch(Exception ex){
			HTMLPlugin.logException(ex);
		}
	}
	
	protected void addContextMenuActions(IMenuManager menu){
		menu.add(new Separator(GROUP_HTML));
		//addAction(menu,GROUP_HTML,ACTION_OPEN_PALETTE);
		addAction(menu,GROUP_HTML,ACTION_ESCAPE_XML);
		addAction(menu,GROUP_HTML,ACTION_COMMENT);
		menu.add(new Separator(GROUP_XML));
		addAction(menu,GROUP_XML,ACTION_GEN_DTD);
		addAction(menu,GROUP_XML,ACTION_GEN_XSD);
	}
	
	protected void updateSelectionDependentActions() {
		super.updateSelectionDependentActions();
		ITextSelection sel = (ITextSelection)getSelectionProvider().getSelection();
		if(sel.getText().equals("")){
			getAction(ACTION_ESCAPE_XML).setEnabled(false);
		} else {
			getAction(ACTION_ESCAPE_XML).setEnabled(true);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// actions
	////////////////////////////////////////////////////////////////////////////	
	/** The action to escape XML special chars in the selection. */
	private class EscapeXMLAction extends Action {
		
		public EscapeXMLAction(){
			super(HTMLPlugin.getResourceString("HTMLEditor.EscapeAction"));
			setEnabled(false);
			setAccelerator(SWT.CTRL | '\\');
		}
		
		public void run() {
			ITextSelection sel = (ITextSelection)getSelectionProvider().getSelection();
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			try {
				doc.replace(sel.getOffset(),sel.getLength(),HTMLUtil.escapeXML(sel.getText()));
			} catch (BadLocationException e) {
				HTMLPlugin.logException(e);
			}
		}
	}
	
	/** The action to generate DTD from XML. */
	private class GenerateDTDAction extends Action {
		public GenerateDTDAction(){
			super(HTMLPlugin.getResourceString("XMLEditor.GenerateDTD"));
		}
		public void run() {
			FileDialog dialog = new FileDialog(getViewer().getTextWidget().getShell(),SWT.SAVE);
			dialog.setFilterExtensions(new String[]{"*.dtd"});
			String file = dialog.open();
			if(file!=null){
				try {
					SchemaGenerator.generateDTDFromXML(getFile(),new File(file));
				} catch(Exception ex){
					HTMLPlugin.logException(ex);
				}
			}
		}
	}
	
	/** The action to generate XML schema from XML. */
	private class GenerateXSDAction extends Action {
		public GenerateXSDAction(){
			super(HTMLPlugin.getResourceString("XMLEditor.GenerateXSD"));
		}
		public void run() {
			FileDialog dialog = new FileDialog(getViewer().getTextWidget().getShell(),SWT.SAVE);
			dialog.setFilterExtensions(new String[]{"*.xsd"});
			String file = dialog.open();
			if(file!=null){
				try {
					SchemaGenerator.generateXSDFromXML(getFile(),new File(file));
				} catch(Exception ex){
					HTMLPlugin.logException(ex);
				}
			}
		}
	}
}
