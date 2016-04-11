package ru.runa.gpd.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.osgi.framework.Bundle;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

/**
 * Util for HTML with custom tags
 * 
 * @author Dofs
 */
public class XmlUtil {
    public static final String RUNA_NAMESPACE = "http://runa.ru/xml";

    public static boolean isXml(String data) {
        if (Strings.isNullOrEmpty(data)) {
            return false;
        }
        try {
            parseWithoutValidation(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Document parseWithoutValidation(String data) {
        return parseWithoutValidation(data.getBytes(Charsets.UTF_8));
    }

    public static Document parseWithoutValidation(byte[] data) {
        return parse(new ByteArrayInputStream(data), false, null);
    }

    public static Document parseWithoutValidation(InputStream in) {
        return parse(in, false, null);
    }

    public static Document parseWithXSDValidation(InputStream in) {
        return parse(in, true, null);
    }

    public static Document parseWithXSDValidation(byte[] data) {
        return parseWithXSDValidation(new ByteArrayInputStream(data));
    }

    public static Document parseWithXSDValidation(byte[] data, String xsdFileName) {
        return parseWithXSDValidation(new ByteArrayInputStream(data), xsdFileName);
    }

    public static Document parseWithXSDValidation(String data) {
        return parseWithXSDValidation(data.getBytes(Charsets.UTF_8));
    }

    public static Document parseWithXSDValidation(InputStream in, String xsdFileName) {
        return parse(in, true, XmlUtil.class.getResourceAsStream("/schema/" + xsdFileName));
    }

    private static Document parse(InputStream in, boolean xsdValidation, InputStream xsdInputStream) {
        try {
            SAXReader reader;
            if (xsdValidation) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                if (xsdInputStream != null) {
                    SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                    schemaFactory.setResourceResolver(new LSResourceResolver() {
                        @Override
                        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
                            InputStream xsd = getClass().getResourceAsStream("/schema/" + systemId);
                            if (xsd != null) {
                                return new LSInputImpl(publicId, systemId, baseURI, xsd, Charsets.UTF_8.name());
                            }
                            return null;
                        }
                    });
                    factory.setSchema(schemaFactory.newSchema(new Source[] { new StreamSource(xsdInputStream) }));
                } else {
                    factory.setValidating(true);
                }
                SAXParser parser = factory.newSAXParser();
                if (xsdInputStream == null) {
                    parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
                }
                reader = new SAXReader(parser.getXMLReader());
            } else {
                reader = new SAXReader();
            }
            reader.setValidation(xsdValidation && xsdInputStream == null);
            reader.setErrorHandler(SimpleErrorHandler.getInstance());
            return reader.read(new InputStreamReader(in, Charsets.UTF_8));
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    public static byte[] writeXml(Node node) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeXml(node, baos);
        return baos.toByteArray();
    }

    public static byte[] writeXml(Node node, OutputFormat outputFormat) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeXml(node, baos, outputFormat);
        return baos.toByteArray();
    }

    public static void writeXml(Node node, OutputStream outputStream) {
        OutputFormat format = new OutputFormat("  ", true);
        format.setPadText(true);
        writeXml(node, outputStream, format);
    }

    public static void writeXml(Node node, OutputStream outputStream, OutputFormat outputFormat) {
        try {
            XMLWriter writer = new XMLWriter(outputStream, outputFormat);
            writer.write(node);
            writer.flush();
        } catch (IOException e) {
            throw new InternalApplicationException(e);
        }
    }

    public static String toString(Node node) {
        return new String(writeXml(node), Charsets.UTF_8);
    }

    public static String toString(Node node, OutputFormat outputFormat) {
        return new String(writeXml(node, outputFormat), Charsets.UTF_8);
    }

    public static Document createDocument(String rootElementName) {
        Document document = DocumentHelper.createDocument();
        document.addElement(rootElementName);
        return document;
    }

    public static Document createDocument(String rootElementName, String defaultNamespaceUri) {
        Document document = createDocument(rootElementName);
        document.getRootElement().addNamespace("", defaultNamespaceUri);
        return document;
    }

    public static Document createDocument(String rootElementName, String defaultNamespaceUri, String xsdLocation) {
        Document document = createDocument(rootElementName, defaultNamespaceUri);
        document.getRootElement().addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        document.getRootElement().addAttribute("xsi:schemaLocation", defaultNamespaceUri + " " + xsdLocation);
        return document;
    }

    public static String getParamDefConfig(Bundle bundle, String className) {
        int dotIndex = className.lastIndexOf(".");
        String simpleClassName = className.substring(dotIndex + 1);
        String path = "/conf/" + simpleClassName + ".xml";
        try {
            InputStream is = bundle.getEntry(path).openStream();
            return IOUtils.readStream(is);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read config at " + path, e);
        }
    }
    
}
