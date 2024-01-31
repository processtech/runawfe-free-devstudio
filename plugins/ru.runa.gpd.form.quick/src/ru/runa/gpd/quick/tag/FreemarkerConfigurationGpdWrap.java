package ru.runa.gpd.quick.tag;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import ru.runa.wfe.commons.xml.XmlUtils;

@SuppressWarnings("unchecked")
public class FreemarkerConfigurationGpdWrap {
    private static final String CONFIG = "ftl.form.tags.xml";
    private static final String TAG_ELEMENT = "ftltag";
    private static final String NAME_ATTR = "name";
    private static final String CLASS_ATTR = "class";
    private static final String DEFAULT_ATTR = "default";
    private static final String MAIN_VARIABLE_INDEX_ATTR = "mainVariableIndex";
    private static final String IMAGE_ATTR = "image";
    private final Map<String, Class<? extends FreemarkerTagGpdWrap>> tags = Maps.newHashMap();
    private final Map<String, Integer> tagMainVariableIndexes = new HashMap<>();
    private final Map<String, String> imageTagImageName = new HashMap<>();
    private String defaultTagName;

    private static FreemarkerConfigurationGpdWrap instance;

    public static FreemarkerConfigurationGpdWrap getInstance() {
        if (instance == null) {
            instance = new FreemarkerConfigurationGpdWrap();
        }
        return instance;
    }

    public String getRegistrationInfo() {
        return Joiner.on(", ").join(tags.values());
    }

    private FreemarkerConfigurationGpdWrap() {
        parseTags(CONFIG);
        // parseTags(SystemProperties.RESOURCE_EXTENSION_PREFIX + CONFIG, false);
    }

    private void parseTags(String fileName) {
        InputStream is = null;
        Bundle bundle = Platform.getBundle("ru.runa.gpd.form.quick");
        // if (required) {
        // is = ClassLoaderUtil.getAsStreamNotNull(fileName, getClass());
        try {
            is = bundle.getEntry(fileName).openStream();
        } catch (IOException e) {

        }
        /*
         * } else { is = ClassLoaderUtil.getAsStream(fileName, getClass()); }
         */
        if (is != null) {
            Document document = XmlUtils.parseWithoutValidation(is);
            Element root = document.getRootElement();
            List<Element> tagElements = root.elements(TAG_ELEMENT);
            for (Element tagElement : tagElements) {
                String name = tagElement.attributeValue(NAME_ATTR);
                String mainVariableIndex = tagElement.attributeValue(MAIN_VARIABLE_INDEX_ATTR);
                if (mainVariableIndex != null) { // если атрибут есть
                    this.tagMainVariableIndexes.put(name, Integer.parseInt(mainVariableIndex));
                }
                try {
                    String className = tagElement.attributeValue(CLASS_ATTR);
                    if (className == null) {
                        addTag(name, NotSupportedMessageTag.class);
                    } else {
                        Class<? extends FreemarkerTagGpdWrap> tagClass = (Class<? extends FreemarkerTagGpdWrap>) bundle.loadClass(className);
                        addTag(name, tagClass);
                        if (tagClass.equals(ImageTag.class)) {
                            String imageName = tagElement.attributeValue(IMAGE_ATTR);
                            this.imageTagImageName.put(name, imageName);
                        }
                    }
                    if (Boolean.parseBoolean(tagElement.attributeValue(DEFAULT_ATTR))) {
                        defaultTagName = name;
                    }
                } catch (Throwable e) {
                }
            }
        }
    }

    private void addTag(String name, Class<? extends FreemarkerTagGpdWrap> tagClass) {
        tags.put(name, tagClass);
    }

    public FreemarkerTagGpdWrap getTag(String name) throws InstantiationException, IllegalAccessException {
        /*
         * if (!tags.containsKey(name)) { String possibleTagClassName = "ru.runa.wf.web.ftl.method." + name + "Tag"; try { Class<? extends
         * FreemarkerTag> tagClass = (Class<? extends FreemarkerTag>) ClassLoaderUtil.loadClass(possibleTagClassName); addTag(name, tagClass); } catch
         * (Exception e) { log.warn("Unable to load tag " + name + " as " + possibleTagClassName + ". Check your " + CONFIG); addTag(name, null); } }
         */
        Class<? extends FreemarkerTagGpdWrap> tagClass = tags.get(name);
        if (tagClass != null) {
            FreemarkerTagGpdWrap tag = tagClass.newInstance();
            if (tag instanceof ImageTag) {
                ImageTag imageTag = (ImageTag) tag;
                imageTag.setImageName(this.imageTagImageName.get(name));
            }
            return tag;
        }
        return null;
    }

    public Set<String> getTagsName() {
        return tags.keySet();
    }

    public String getDefaultTagName() {
        return defaultTagName;
    }

    public int getTagMainVariableIndex(String tagName) {
        Integer index = this.tagMainVariableIndexes.get(tagName);
        if (index == null) {
            return 0;
        }
        return index;
    }
}
