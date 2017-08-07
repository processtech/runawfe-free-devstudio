package ru.runa.gpd.wfe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.gpd.util.XmlUtil;

public class WFEServerRelationsImporter extends DataImporter {
    private final List<String> relations = new ArrayList<String>();
    private static WFEServerRelationsImporter instance;

    @Override
    protected WFEServerConnector getConnector() {
        return WFEServerConnector.getInstance();
    }

    public static synchronized WFEServerRelationsImporter getInstance() {
        if (instance == null) {
            instance = new WFEServerRelationsImporter();
        }
        return instance;
    }

    @Override
    protected void clearInMemoryCache() {
        relations.clear();
    }

    @Override
    protected void saveCachedData() throws Exception {
        Document document = XmlUtil.createDocument("data");
        for (String name : relations) {
            Element element = document.getRootElement().addElement("relation");
            element.addAttribute("name", name);
        }
        try (OutputStream os = new FileOutputStream(getCacheFile())) {
            XmlUtil.writeXml(document, os);
        }
    }

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
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        relations.addAll(getConnector().getRelationNames());
        monitor.worked(100);
    }
}
