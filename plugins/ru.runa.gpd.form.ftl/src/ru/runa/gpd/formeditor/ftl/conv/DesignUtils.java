package ru.runa.gpd.formeditor.ftl.conv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ru.runa.gpd.formeditor.BaseHtmlFormType;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public class DesignUtils {
    public static final String PARAMETERS_DELIM = "|";
    public static final String ATTR_COMPONENT_TYPE = "type";
    public static final String ATTR_COMPONENT_ID = "id";
    public static final String ATTR_COMPONENT_PARAMETERS = "parameters";
    public static final String ATTR_STYLE = "style";

    public static String getComponentHtmlElementName() {
        return WebServerUtils.useCKEditor() ? "ftl_component" : "img";
    }

    public static String transformFromHtml(String html, Map<String, Variable> variables, Map<Integer, Component> components) throws Exception {
        if (html.length() == 0) {
            return html;
        }
        Document document = BaseHtmlFormType.getDocument(new ByteArrayInputStream(html.getBytes(Charsets.UTF_8)));
        NodeList componentElements = document.getElementsByTagName(getComponentHtmlElementName());
        List<Node> nodes = Lists.newArrayListWithExpectedSize(componentElements.getLength());
        for (int i = 0; i < componentElements.getLength(); i++) {
            Node componentNode = componentElements.item(i);
            nodes.add(componentNode);
        }
        for (Node node : nodes) {
            try {
                int id = Integer.valueOf(node.getAttributes().getNamedItem(ATTR_COMPONENT_ID).getNodeValue());
                Component component = components.get(id);
                if (component == null) {
                    throw new Exception("Component not found by id " + id);
                }
                String ftl = component.toString();
                Text ftlText = document.createTextNode(ftl);
                node.getParentNode().replaceChild(ftlText, node);
            } catch (Exception e) {
                throw new Exception("Unable to convert component from design html: " + toString(node), e);
            }
        }
        return toString(document);
    }

    private static String toString(Node node) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, Charsets.UTF_8.name());
        transformer.transform(new DOMSource(node), new StreamResult(os));
        return new String(os.toByteArray(), Charsets.UTF_8.name());
    }

}
