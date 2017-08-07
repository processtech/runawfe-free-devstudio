package ru.runa.gpd.wfe;

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

public abstract class ExecutorsImporter extends DataImporter {
    protected Map<String, Boolean> executors = new TreeMap<String, Boolean>();

    @Override
    protected void clearInMemoryCache() {
        executors.clear();
    }

    @Override
    protected void saveCachedData() throws Exception {
        Document document = XmlUtil.createDocument("executors");
        for (String name : executors.keySet()) {
            Element element = document.getRootElement().addElement("executor");
            element.addAttribute("name", name);
            element.addAttribute("group", String.valueOf(executors.get(name)));
        }
        try (OutputStream os = new FileOutputStream(getCacheFile())) {
            XmlUtil.writeXml(document, os);
        }
    }

    @Override
    public Map<String, Boolean> loadCachedData() throws Exception {
        Map<String, Boolean> result = new TreeMap<String, Boolean>();
        File cacheFile = getCacheFile();
        if (cacheFile.exists()) {
            Document document = XmlUtil.parseWithoutValidation(new FileInputStream(cacheFile));
            List<Element> nodeList = document.getRootElement().elements("executor");
            for (Element element : nodeList) {
                String name = element.attributeValue("name");
                Boolean isGroup = Boolean.parseBoolean(element.attributeValue("group"));
                result.put(name, isGroup);
            }
        }
        return result;
    }
}
