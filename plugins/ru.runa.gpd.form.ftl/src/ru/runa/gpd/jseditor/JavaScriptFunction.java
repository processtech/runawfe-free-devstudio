package ru.runa.gpd.jseditor;

import java.util.ArrayList;
import java.util.List;

/**
 * The model for the JavaScript function.
 * 
 * @author Naoki Takezoe
 */
public class JavaScriptFunction implements JavaScriptElement, JavaScriptContext {
	
	private String name;
	private String arguments;
	private int offset;
	private int end;
	private List<JavaScriptElement> children = new ArrayList<>();
	private JavaScriptContext parent;
	
	public JavaScriptFunction(String name, String arguments, int offset){
		this.name = name;
		this.arguments = arguments;
		this.offset = offset;
	}
	
	public String getArguments() {
		return arguments;
	}
	
	public String getName() {
		return name;
	}
	
	public int getOffset(){
		return offset;
	}
	
	public int getStartOffset(){
		return getOffset();
	}
	
	public void setEndOffset(int end){
		this.end = end;
	}
	
	public int getEndOffset(){
		return end;
	}
	
	public void add(JavaScriptFunction func){
		this.children.add(func);
	}
	
	public void add(JavaScriptVariable var){
		this.children.add(var);
	}
	
	public JavaScriptElement[] getChildren(){
		return this.children.toArray(new JavaScriptElement[this.children.size()]);
	}
	
	public JavaScriptElement[] getVisibleElements(){
		List<JavaScriptElement> list = new ArrayList<>();
		JavaScriptContext context = this;
		while (true) {
			JavaScriptElement[] children = context.getChildren();
			for (JavaScriptElement c : children) {
				list.add(c);
			}
			
			if(context.getParent()==null){
				break;
			} else {
				context = context.getParent();
			}
		}
		return list.toArray(new JavaScriptElement[list.size()]);
	}
	
	public void setParent(JavaScriptContext context){
		this.parent = context;
	}
	
	public JavaScriptContext getParent(){
		return parent;
	}

	
	public String toString(){
		return name + "(" + arguments + ")";
	}
}
