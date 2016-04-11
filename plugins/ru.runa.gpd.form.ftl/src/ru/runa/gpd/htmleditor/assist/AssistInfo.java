package ru.runa.gpd.htmleditor.assist;

import org.eclipse.swt.graphics.Image;

public class AssistInfo {
	private String displayString;
	private String replaceString;
	private Image image;
	
	public AssistInfo(String displayString){
		this.displayString = displayString;
		this.replaceString = displayString;
	}

	public AssistInfo(String displayString,Image image){
		this.displayString = displayString;
		this.replaceString = displayString;
		this.image = image;
	}
	
	public AssistInfo(String replaceString,String displayString){
		this.displayString = displayString;
		this.replaceString = replaceString;
	}
	
	public AssistInfo(String replaceString,String displayString,Image image){
		this.displayString = displayString;
		this.replaceString = replaceString;
		this.image = image;
	}
	
	public String getDisplayString() {
		return displayString;
	}
	
	public String getReplaceString() {
		return replaceString;
	}
	
	public Image getImage(){
		return this.image;
	}
}
