package ru.runa.gpd.wfe;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.PrefConstants;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WFEServerConnectorRegistry implements PrefConstants {
    private static final Map<String, IConfigurationElement> connectors = Maps.newHashMap();
    private static final List<Entry> entries = Lists.newArrayList();
    static {
        try {
            entries.add(new Entry("stub", Localization.getString("none"), ""));
            IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.wfeConnectors").getExtensions();
            for (IExtension extension : extensions) {
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                for (IConfigurationElement configElement : configElements) {
                    Entry entry = new Entry(configElement);
                    connectors.put(entry.id, configElement);
                    entries.add(entry);
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Error processing extension 'ru.runa.gpd.wfeConnectors'", e);
        }
    }

    public static Entry getEntryNotNull(String id) {
        for (Entry entry : entries) {
            if (Objects.equal(id, entry.id)) {
                return entry;
            }
        }
        throw new RuntimeException("No entry found by id " + id);
    }

    public static String[][] getEntriesArray() {
        String[][] strings = new String[entries.size()][];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = new String[2];
            strings[i][0] = entries.get(i).name;
            strings[i][1] = entries.get(i).id;
        }
        return strings;
    }

    public static WFEServerConnector createConnector() {
        try {
            String type = Activator.getPrefString(P_WFE_CONNECTION_TYPE);
            if (Strings.isNullOrEmpty(type)) {
                type = entries.get(entries.size() - 1).id;
            }
            if ("stub".equals(type) || !connectors.containsKey(type)) {
                return new WFEServerConnectorStub();
            }
            return (WFEServerConnector) connectors.get(type).createExecutableExtension("class");
        } catch (Exception e) {
            throw new RuntimeException("Error processing extension 'ru.runa.gpd.wfeConnectors'", e);
        }
    }

    public static class Entry {
        public final String id;
        public final String name;
        public final String description;

        public Entry(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public Entry(IConfigurationElement element) {
            id = element.getAttribute("id");
            name = element.getAttribute("name");
            description = element.getAttribute("description");
        }
    }
}
