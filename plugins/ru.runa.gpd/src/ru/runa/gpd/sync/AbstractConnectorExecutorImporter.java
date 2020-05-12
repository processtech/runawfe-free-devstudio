package ru.runa.gpd.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.gpd.util.XmlUtil;

public abstract class AbstractConnectorExecutorImporter extends WfeServerConnectorDataImporter<Map<String, Boolean>> {

    @Override
    protected void saveCachedData(Map<String, Boolean> data) throws Exception {
        Document document = XmlUtil.createDocument("executors");
        for (String name : data.keySet()) {
            Element element = document.getRootElement().addElement("executor");
            element.addAttribute("name", name);
            element.addAttribute("group", String.valueOf(data.get(name)));
        }
        try (OutputStream os = new FileOutputStream(getCacheFile())) {
            XmlUtil.writeXml(document, os);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, Boolean> loadCachedData() throws Exception {
        Map<String, Boolean> data = new TreeMap<>();
        File cacheFile = getCacheFile();
        if (cacheFile.exists()) {
            Document document = XmlUtil.parseWithoutValidation(new FileInputStream(cacheFile));
            List<Element> nodeList = document.getRootElement().elements("executor");
            for (Element element : nodeList) {
                String name = element.attributeValue("name");
                Boolean isGroup = Boolean.parseBoolean(element.attributeValue("group"));
                data.put(name, isGroup);
            }
        }
        return data;
    }
}
