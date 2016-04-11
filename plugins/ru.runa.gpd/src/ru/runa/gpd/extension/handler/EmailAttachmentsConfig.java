package ru.runa.gpd.extension.handler;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

// TODO expand ParamDefConfig capabilities to cover this
public class EmailAttachmentsConfig {

    @SuppressWarnings("unchecked")
    public static List<String> parseAttachments(String configuration) {
        List<String> list = new ArrayList<String>();
        try {
            Document doc = DocumentHelper.parseText(configuration);
            Element groupElement = doc.getRootElement().element("attachments");
            if (groupElement != null) {
                List<Element> pElements = groupElement.elements("file");
                for (Element element : pElements) {
                    list.add(element.attributeValue("name"));
                }
            }
        } catch (Exception e) {
        }
        return list;
    }

    public static void addAttachments(Document document, List<String> attachments) {
        Element groupElement = document.getRootElement().addElement("attachments");
        for (String attachment : attachments) {
            Element fElement = groupElement.addElement("file");
            fElement.addAttribute("name", attachment);
        }
    }

}
