package ru.runa.gpd.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.gpd.util.XmlUtil;

public class WfeServerRelationImporter extends WfeServerConnectorDataImporter<List<String>> {
    private static WfeServerRelationImporter instance = new WfeServerRelationImporter();

    public static WfeServerRelationImporter getInstance() {
        return instance;
    }

    @Override
    protected void saveCachedData(List<String> data) throws Exception {
        Document document = XmlUtil.createDocument("data");
        for (String name : data) {
            Element element = document.getRootElement().addElement("relation");
            element.addAttribute("name", name);
        }
        try (OutputStream os = new FileOutputStream(getCacheFile())) {
            XmlUtil.writeXml(document, os);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> loadCachedData() throws Exception {
        List<String> result = new ArrayList<String>();
        File cacheFile = getCacheFile();
        if (cacheFile.exists()) {
            Document document = XmlUtil.parseWithoutValidation(new FileInputStream(cacheFile));
            List<Element> nodeList = document.getRootElement().elements("relation");
            for (Element element : nodeList) {
                String name = element.attributeValue("name");
                result.add(name);
            }
        }
        return result;
    }

    @Override
    protected List<String> loadRemoteData() throws Exception {
        return getConnector().getRelationNames();
    }
}
