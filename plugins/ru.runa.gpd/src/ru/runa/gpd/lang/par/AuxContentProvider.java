package ru.runa.gpd.lang.par;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.model.ProcessDefinition;

import com.google.common.base.Strings;

public abstract class AuxContentProvider {
    protected static final String ID = "id";
    protected static final String NAME = "name";
    
    public boolean isSupportedForEmbeddedSubprocess() {
        return true;
    }
    
    public abstract String getFileName();
    
    public abstract Document save(ProcessDefinition definition) throws Exception;

    public abstract void read(Document document, ProcessDefinition definition) throws Exception;

    protected void addAttribute(Element e, String name, String value) {
        if (value != null) {
            e.addAttribute(name, value);
        }
    }

    protected int getIntAttribute(Element e, String name, int defaultValue) {
        String attrValue = e.attributeValue(name);
        if (Strings.isNullOrEmpty(attrValue)) {
            return defaultValue;
        }
        return Integer.valueOf(attrValue);
    }

    protected boolean getBooleanAttribute(Element e, String name, boolean defaultValue) {
        String attrValue = e.attributeValue(name);
        if (Strings.isNullOrEmpty(attrValue)) {
            return defaultValue;
        }
        return Boolean.valueOf(attrValue);
    }
}
