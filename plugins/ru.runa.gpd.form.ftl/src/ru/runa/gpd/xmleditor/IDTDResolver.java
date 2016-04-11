package ru.runa.gpd.xmleditor;

import java.io.InputStream;

public interface IDTDResolver {
	
	public InputStream getInputStream(String uri);
	
}
