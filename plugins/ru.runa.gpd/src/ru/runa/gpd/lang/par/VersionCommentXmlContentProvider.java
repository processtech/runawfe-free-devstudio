package ru.runa.gpd.lang.par;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VersionInfo;
import ru.runa.gpd.util.XmlUtil;

public class VersionCommentXmlContentProvider extends AuxContentProvider {
    private static final String XML_FILE_NAME = "comments.xml";
    private static final String UUID = "UUID";
    private static final String VERSIONS = "versions";
    private static final String VERSION = "version";
    private static final String VERSION_NUMBER = "versionNumber";
    private static final String VERSION_DATE = "versionDate";
    private static final String VERSION_AUTHOR = "versionAuthor";
    private static final String VERSION_COMMENT = "versionComment";

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }

    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        List<Element> versionList = document.getRootElement().elements(VERSION);
        definition.setUUID(document.getRootElement().attributeValue(UUID));
        for (Element versionInfoElement : versionList) {
            VersionInfo versionInfo = new VersionInfo();
            versionInfo.setNumber(fromXMLSafeText(versionInfoElement.attributeValue(VERSION_NUMBER)));
            versionInfo.setDate(fromXMLSafeText(versionInfoElement.attributeValue(VERSION_DATE)));
            versionInfo.setAuthor(fromXMLSafeText(versionInfoElement.attributeValue(VERSION_AUTHOR)));
            versionInfo.setComment(fromXMLSafeText(versionInfoElement.attributeValue(VERSION_COMMENT)));
            versionInfo.setSavedToFile(true);
            definition.addToVersionInfoList(versionInfo);
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(VERSIONS);
        Element root = document.getRootElement();
        root.addAttribute(UUID, definition.getUUID());
        ArrayList<VersionInfo> versionInfoList = definition.getVersionInfoList();
        for (int i = 0; i < versionInfoList.size(); i++) {
            Element versionInfoElement = root.addElement(VERSION);
            versionInfoElement.addAttribute(VERSION_NUMBER, toXMLSafeText(versionInfoList.get(i).getNumber()));
            versionInfoElement.addAttribute(VERSION_DATE, toXMLSafeText(versionInfoList.get(i).getDateAsString()));
            versionInfoElement.addAttribute(VERSION_AUTHOR, toXMLSafeText(versionInfoList.get(i).getAuthor()));
            versionInfoElement.addAttribute(VERSION_COMMENT, toXMLSafeText(versionInfoList.get(i).getComment()));
            versionInfoList.get(i).setSavedToFile(true);
        }

        return document;
    }

    protected String toXMLSafeText(String text) {
        return text.replace("<", "&lt;").replace(">", "&gt;").replace("\'", "&rsquo;").replace("\"", "&quot;").replace("/", "&frasl;")
                .replace("\r\n", "&013;");
    }

    protected String fromXMLSafeText(String text) {
        return text.replace("&lt;", "<").replace("&gt;", ">").replace("&rsquo;", "\'").replace("&quot;", "\"").replace("&frasl;", "/")
                .replace("&013;", "\r\n").replace("&amp;", "&");
    }

}