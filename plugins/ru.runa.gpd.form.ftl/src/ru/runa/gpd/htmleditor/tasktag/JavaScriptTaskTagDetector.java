package ru.runa.gpd.htmleditor.tasktag;

import ru.runa.gpd.jseditor.JavaScriptComment;
import ru.runa.gpd.jseditor.JavaScriptModel;

/**
 * {@link ITaskTagDetector} implementation for JavaScript.
 * This detector supports following extensions:
 * 
 * <ul>
 *   <li>.js</li>
 * </ul>
 * 
 * @author Naoki Takezoe
 */
public class JavaScriptTaskTagDetector extends AbstractTaskTagDetector {
	
	public JavaScriptTaskTagDetector(){
		addSupportedExtension("js");
	}
	
	public void doDetect() throws Exception {
		JavaScriptModel model = new JavaScriptModel(this.contents);
		JavaScriptComment[] comments = model.getComments();
		for(int i=0;i<comments.length;i++){
			detectTaskTag(comments[i].getText(), comments[i].getOffset());
		}
	}
}
