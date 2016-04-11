package ru.runa.gpd.htmleditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

/**
 * 
 * @author Naoki Takezoe
 */
public abstract class AbstractHTMLContentDescriber implements ITextContentDescriber {
	
	private String tagName;
	
	private static final QualifiedName[] NO_OPTIONS = new QualifiedName[0];
	
	public AbstractHTMLContentDescriber(String tagName){
		this.tagName = tagName;
	}
	
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		return describe(new InputStreamReader(contents, "ISO-8859-1"), description);
	}

	public QualifiedName[] getSupportedOptions() {
		return NO_OPTIONS;
	}

	public int describe(Reader contents, IContentDescription description) throws IOException {
		BufferedReader reader = new BufferedReader(contents);
		String line = null;
		while((line = reader.readLine()) != null){
			if(line.indexOf("<" + this.tagName) >= 0){
				return VALID;
			}
		}
		return INDETERMINATE;
	}

}
