package ru.runa.gpd.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import com.google.common.base.Strings;

public enum GroovyStuff {
    
    TYPE("type"),
    METHOD("method"),
    CONSTANT("constant"),
    STATEMENT("statement");
    
    private List<Item> all = new ArrayList<>();
    
    private GroovyStuff(String itemType) {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.groovyStuff").getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                if (configElement.getName().equals(itemType)) {
                    all.add(new Item(configElement.getAttribute("label"), configElement.getValue()));
                }
            }
        }
    }
    
    public List<Item> getAll() {
        return all;
    }

    public static class Item implements Comparable<Item> {
        
        private String label;
        private String body;
        
        public Item(String label, String body) {
            super();
            this.label = label;
            this.body = body;
        }

        public String getLabel() {
            return label;
        }

        public String getBody() {
            return Strings.isNullOrEmpty(body) ? label : body;
        }

        @Override
        public int compareTo(Item o) {
            return this.getLabel().compareTo(o.getLabel());
        }
        
    }

}
