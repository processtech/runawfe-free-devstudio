package ru.runa.gpd.extension;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class ArtifactContentProvider<T extends Artifact> {
    private static final String ROOT_ELEMENT = "artifacts";
    private static final String ARTIFACT_NODE = "artifact";
    private static final String ENABLED_ATTR = "enabled";
    private static final String NAME_ATTR = "name";
    private static final String DISPLAY_NAME_ATTR = "label";

    protected T createArtifact() {
        return ((T) new Artifact());
    }

    protected void loadArtifact(T artifact, Element element) {
        artifact.setEnabled(Boolean.valueOf(element.attributeValue(ENABLED_ATTR, "true")));
        artifact.setName(element.attributeValue(NAME_ATTR));
        artifact.setLabel(element.attributeValue(DISPLAY_NAME_ATTR));
    }

    public List<T> load(InputStream is) {
        Preconditions.checkNotNull(is, "stream is null");
        List<T> list = Lists.newArrayList();
        Document document = XmlUtil.parseWithoutValidation(is);
        List<Element> elements = document.getRootElement().elements(ARTIFACT_NODE);
        for (Element element : elements) {
            T artifact = createArtifact();
            loadArtifact(artifact, element);
            list.add(artifact);
        }
        return list;
    }

    protected void saveArtifact(T artifact, Element element) {
        element.addAttribute(ENABLED_ATTR, String.valueOf(artifact.isEnabled()));
        element.addAttribute(NAME_ATTR, artifact.getName());
        element.addAttribute(DISPLAY_NAME_ATTR, artifact.getLabel());
    }

    public void save(List<T> list, OutputStream os) {
        Document document = XmlUtil.createDocument(ROOT_ELEMENT);
        Element root = document.getRootElement();
        for (T artifact : list) {
            Element element = root.addElement(ARTIFACT_NODE);
            saveArtifact(artifact, element);
        }
        XmlUtil.writeXml(document, os);
    }
}
