package ru.runa.gpd.htmleditor.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * An implementaion of IEditorInput for the Web browser.
 * 
 * @author Naoki Takezoe
 * @see ru.runa.gpd.htmleditor.editors.WebBrowser
 * @since 1.4.1
 */
public class URLEditorInput implements IEditorInput {
	
	private String url;
	
	public URLEditorInput(String url) {
		super();
		this.url = url;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		if(url == null){
			return "";
		}
		return url;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "";
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public String getUrl(){
		return this.url;
	}

}
