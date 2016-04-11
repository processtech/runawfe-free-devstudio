package ru.runa.gpd.jseditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.HTMLProjectParams;
import ru.runa.gpd.htmleditor.HTMLUtil;

/**
 * The validator for JavaScriptEditor.
 * 
 * @see ru.runa.gpd.jseditor.JavaScriptEditor
 * @author Naoki Takezoe
 */
public class JavaScriptValidator {
	
	private IFile file;
	
	public JavaScriptValidator(IFile file){
		this.file = file;
	}
	
	public void doValidate(){
		Context context = Context.enter();
		try {
			file.deleteMarkers(IMarker.PROBLEM,false,0);
			
			HTMLProjectParams params = new HTMLProjectParams(file.getProject());
			if(!params.getValidateJavaScript()){
				return;
			}
			
			context.setErrorReporter(new ErrorReporterImpl());
			context.initStandardObjects();
			
			context.compileString( 
					new String(HTMLUtil.readStream(file.getContents()), file.getCharset()), 
					file.getName(), 1, null);
			
		} catch(EvaluatorException ex){
			// ignore
		} catch(Exception ex){
			HTMLPlugin.logException(ex);
		} finally {
			Context.exit();
		}
	}
	
	private class ErrorReporterImpl implements ErrorReporter {

		public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
			HTMLUtil.addMarker(file, IMarker.SEVERITY_ERROR, line, message);
		}

		public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
			//addMarker(IMarker.SEVERITY_ERROR, line, message);
			return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
		}

		public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
			HTMLUtil.addMarker(file, IMarker.SEVERITY_WARNING, line, message);
		}
		
	}
}
