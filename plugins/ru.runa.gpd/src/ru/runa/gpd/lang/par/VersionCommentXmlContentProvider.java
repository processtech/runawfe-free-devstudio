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
    private static final String VERSIONS = "versions";
    private static final String VERSION = "version";
    private static final String VERSION_DATE = "date";
    private static final String VERSION_AUTHOR = "author";
    private static final String VERSION_COMMENT = "comment";

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }

    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        List<Element> versionList = document.getRootElement().elements(VERSION);
        for (Element versionInfoElement : versionList) {
            VersionInfo versionInfo = new VersionInfo();
            versionInfo.setDateTime(versionInfoElement.elementText(VERSION_DATE));
            versionInfo.setAuthor(versionInfoElement.elementText(VERSION_AUTHOR));
            versionInfo.setComment(versionInfoElement.elementText(VERSION_COMMENT));
            versionInfo.setSavedToFile(true);
            definition.addToVersionInfoList(versionInfo);
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(VERSIONS);
        Element root = document.getRootElement();
        ArrayList<VersionInfo> versionInfoList = definition.getVersionInfoList();
        for (int i = 0; i < versionInfoList.size(); i++) {
            Element versionInfoElement = root.addElement(VERSION);
            versionInfoElement.addElement(VERSION_DATE).addText(versionInfoList.get(i).getDateTimeAsString());
            versionInfoElement.addElement(VERSION_AUTHOR).addText(versionInfoList.get(i).getAuthor());
            versionInfoElement.addElement(VERSION_COMMENT).addCDATA(versionInfoList.get(i).getComment());
            versionInfoList.get(i).setSavedToFile(true);
        }

        return document;
    }

}