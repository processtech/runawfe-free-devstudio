package ru.runa.gpd.extension.regulations;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.util.IOUtils;

public class RegulationsRegistry {
    private static String template;
    private static String cssStyles;

    static {
        try {
            init();
        } catch (Exception e) {
            PluginLogger.logError("Unable to load regulations", e);
        }
    }

    public static String getTemplate() {
        return template;
    }

    public static String getCssStyles() {
        return cssStyles;
    }

    private static void init() throws IOException {
        Bundle bundle;
        Path templatePath;
        Path cssPath;
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.regulations").getExtensions();
        if (extensions.length > 0) {
            bundle = Platform.getBundle(extensions[0].getNamespaceIdentifier());
            PluginLogger.logInfo("Using " + extensions[0].getExtensionPointUniqueIdentifier() + " for regulations");
            IConfigurationElement[] configElements = extensions[0].getConfigurationElements();
            templatePath = new Path(configElements[0].getAttribute("template"));
            cssPath = new Path(configElements[0].getAttribute("css"));
        } else {
            bundle = Activator.getDefault().getBundle();
            templatePath = new Path("template/regulations.ftl");
            cssPath = new Path("template/regulations.css");
        }
        template = getContent(bundle, templatePath);
        cssStyles = getContent(bundle, cssPath);
    }

    private static String getContent(Bundle bundle, Path path) throws IOException {
        if (path == null) {
            return null;
        }
        URL url = FileLocator.find(bundle, path, Collections.EMPTY_MAP);
        URL fileUrl = FileLocator.toFileURL(url);
        InputStream input = fileUrl.openConnection().getInputStream();
        return IOUtils.readStream(input);
    }
}
