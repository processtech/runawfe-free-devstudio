package ru.runa.gpd.htmleditor;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import ru.runa.gpd.EditorsPlugin;


/**
 * This is a class to access and modify project preferences.
 * 
 * @author Naoki Takezoe
 */
public class HTMLProjectParams {

	private String root = "/";
	private boolean useDTD = true;
	private boolean validateXML = true;
	private boolean validateHTML = true;
	private boolean validateJSP = true;
	private boolean validateDTD = true;
	private boolean validateJS = true;
	private boolean removeMarkers = false;
	private boolean detectTaskTag = false;
	private String[] javaScripts = new String[0];
	
	public static final String P_ROOT = "root";
	public static final String P_USE_DTD = "useDTD";
	public static final String P_VALIDATE_XML = "validateXML";
	public static final String P_VALIDATE_HTML = "validateHTML";
	public static final String P_VALIDATE_JSP = "validateJSP";
	public static final String P_VALIDATE_DTD = "validateDTD";
	public static final String P_VALIDATE_JS = "validateJS";
	public static final String P_REMOVE_MARKERS = "removeMarkers";
	public static final String P_JAVA_SCRIPTS = "javaScripts";
	
	/**
	 * Create empty WebProjectParams.
	 */
	public HTMLProjectParams() {
	}
	
	/**
	 * Create WebProjectParams loading specified project configuration.
	 * 
	 * @param javaProject Java project
	 * @throws Exception
	 */
	public HTMLProjectParams(IProject project) throws Exception {
		load(project);
	}
	
	/**
	 * Returns root of the web application.
	 * 
	 * @return Root of the web application
	 */
	public String getRoot() {
		return root;
	}
	
	/**
	 * Sets root of the web application.
	 * 
	 * @param webAppRoot Root of the web application
	 */
	public void setRoot(String webAppRoot) {
		this.root = webAppRoot;
	}
	
	/**
	 * @param useDTD enable DTD based validation and code completion or not
	 * <ul>
	 *   <li>true - enable</li>
	 *   <li>false - disable</li>
	 * </ul>
	 */
	public void setUseDTD(boolean useDTD){
		this.useDTD = useDTD;
	}
	
	/**
	 * @return enable DTD based validation and code completion or not
	 * <ul>
	 *   <li>true - enable</li>
	 *   <li>false - disable</li>
	 * </ul>
	 */
	public boolean getUseDTD(){
		return this.useDTD;
	}
	
	public void setValidateHTML(boolean validateHTML){
		this.validateHTML = validateHTML;
	}
	
	public boolean getValidateHTML(){
		return this.validateHTML;
	}
	
	public void setValidateJSP(boolean validateJSP){
		this.validateJSP = validateJSP;
	}
	
	public boolean getValidateJSP(){
		return this.validateJSP;
	}
	
	public void setValidateDTD(boolean validateDTD){
		this.validateDTD = validateDTD;
	}
	
	public boolean getValidateDTD(){
		return this.validateDTD;
	}
	
	public void setValidateJavaScript(boolean validateJS){
		this.validateJS = validateJS;
	}
	
	public boolean getValidateJavaScript(){
		return this.validateJS;
	}
	
	public void setValidateXML(boolean validateXML){
		this.validateXML = validateXML;
	}
	
	public boolean getValidateXML(){
		return this.validateXML;
	}
	
	public void setRemoveMarkers(boolean removeMarkers){
		this.removeMarkers = removeMarkers;
	}
	
	public boolean getRemoveMarkers(){
		return this.removeMarkers;
	}
	
	public void setDetectTaskTag(boolean detectTaskTag){
		this.detectTaskTag = detectTaskTag;
	}
	
	public boolean getDetectTaskTag(){
		return this.detectTaskTag;
	}
	
	public void setJavaScripts(String[] javaScripts){
		this.javaScripts = javaScripts;
	}
	
	public String[] getJavaScripts(){
		return this.javaScripts;
	}
		
	/**
	 * Load configuration.
	 * 
	 * @param javaProject Java project
	 * @throws Exception
	 */
	public void load(IProject project) throws Exception {
		IFile configFile = project.getFile(".amateras");
		
		String useDTD = null;
		String validateXML = null;
		String validateHTML = null;
		String validateJSP = null;
		String validateDTD = null;
		String validateJS = null;
		String removeMarkers = null;
		String javaScripts = "";
		
		if(configFile.exists()){
			File file = configFile.getLocation().makeAbsolute().toFile();
			Properties props = new Properties();
			props.load(new FileInputStream(file));
			
			root = props.getProperty(P_ROOT);
			useDTD = props.getProperty(P_USE_DTD);
			validateXML = props.getProperty(P_VALIDATE_XML);
			validateHTML = props.getProperty(P_VALIDATE_HTML);
			validateJSP = props.getProperty(P_VALIDATE_JSP);
			validateDTD = props.getProperty(P_VALIDATE_DTD);
			validateJS = props.getProperty(P_VALIDATE_JS);
			removeMarkers = props.getProperty(P_REMOVE_MARKERS);
			
			javaScripts = props.getProperty(P_JAVA_SCRIPTS);
			if(javaScripts==null){
				javaScripts = "";
			}
			
		} else {
			// for old versions
			this.root = project.getPersistentProperty(
					new QualifiedName(EditorsPlugin.getDefault().getBundle().getSymbolicName(), P_ROOT));
			useDTD = project.getPersistentProperty(
					new QualifiedName(EditorsPlugin.getDefault().getBundle().getSymbolicName(), P_USE_DTD));
			validateHTML = project.getPersistentProperty(new QualifiedName(
			        EditorsPlugin.getDefault().getBundle().getSymbolicName(), P_VALIDATE_HTML));
		}
		
		if(this.root==null){
			this.root = "/";
		}
		
		this.useDTD = getBooleanValue(useDTD, true);
		this.validateXML = getBooleanValue(validateXML, true);
		this.validateHTML = getBooleanValue(validateHTML, true);
		this.validateJSP = getBooleanValue(validateJSP, true);
		this.validateDTD = getBooleanValue(validateDTD, true);
		this.validateJS = getBooleanValue(validateJS, true);
		this.removeMarkers = getBooleanValue(removeMarkers, false);
		this.detectTaskTag = false;
		
		String[] dim = javaScripts.split("\n");
		List list = new ArrayList();
		for(int i=0;i<dim.length;i++){
			if(dim[i].trim().length()!=0){
				list.add(dim[i]);
			}
		}
		this.javaScripts = (String[])list.toArray(new String[list.size()]);
	}
	
	private boolean getBooleanValue(String value, boolean defaultValue){
		if(value!=null){
			if(value.equals("true")){
				return true;
			} else if(value.equals("false")){
				return false;
			}
		}
		return defaultValue;
	}

}
