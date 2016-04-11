package ru.runa.gpd.quick.formeditor.util;

import java.io.IOException;
import java.util.Properties;

public class PresentationVariableUtils {
	public static String getPresentationValue(String format) {
    	Properties prop = new Properties();
    	
    	try {
    		prop.load(PresentationVariableUtils.class.getResourceAsStream("presentation.properties"));

    		return prop.getProperty(format);

    	} catch (IOException ex) {
    		ex.printStackTrace();
    	}
    	
    	return null;
    }
}
