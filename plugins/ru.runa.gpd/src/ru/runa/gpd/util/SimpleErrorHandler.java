package ru.runa.gpd.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler {
    private static final SimpleErrorHandler INSTANCE = new SimpleErrorHandler();

    private SimpleErrorHandler() {
    }

    public static SimpleErrorHandler getInstance() {
        return INSTANCE;
    }

    public void warning(SAXParseException e) throws SAXException {
    }

    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }

}
