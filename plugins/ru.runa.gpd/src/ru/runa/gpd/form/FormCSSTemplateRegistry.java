package ru.runa.gpd.form;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.util.IOUtils;
import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class FormCSSTemplateRegistry {
    private static final List<FormCSSTemplate> TEMPLATES = Lists.newArrayList();
    
    static {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.cssTemplates").getExtensions();
        for (IExtension extension : extensions) {
            Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                String label = configElement.getAttribute("label");
                try {
                    String path = configElement.getAttribute("path");
                    String content = IOUtils.readStream(bundle.getEntry(path).openStream());
                    FormCSSTemplate template = new FormCSSTemplate(label, content);
                    TEMPLATES.add(template);
                } catch (Exception e) {
                    PluginLogger.logError("Error processing 'cssTemplates' extension for: " + label, e);
                }
            }
        }
    }
    
    public static List<FormCSSTemplate> getTemplates() {
        return TEMPLATES;
    }

    public static FormCSSTemplate getTemplateNotNull(String name) {
        for (FormCSSTemplate template : TEMPLATES) {
            if (Objects.equal(name, template.getName())) {
                return template;
            }
        }
        throw new InternalApplicationException("No template found by name '" + name + "'");
    }

}
