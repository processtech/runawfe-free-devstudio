package ru.runa.gpd.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


public class TextEditorInput implements IEditorInput {
	
	private String url;
	private String content;
	
	public TextEditorInput(String url, String content) {
		super();
		this.url = url;
		this.content = content;
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
		if ( String.class.equals(adapter)) {
			return content;
		} else {
			return null;
		}
	}

}
