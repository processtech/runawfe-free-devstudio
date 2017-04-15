package ru.runa.gpd.lang;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.editor.gef.GefEntry;
import ru.runa.gpd.editor.graphiti.GraphitiEntry;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.ltk.VariableRenameProvider;
import ru.runa.gpd.settings.LanguageElementPreferenceNode;
import ru.runa.gpd.settings.PrefConstants;

@SuppressWarnings("unchecked")
public class NodeTypeDefinition {
    public static final String TYPE_CONNECTION = "connection";
    public static final String TYPE_NODE = "node";
    public static final String TYPE_ARTIFACT = "artifact";
    public static final String TYPE_DEFINITION = "definition";
    private final String jpdlElementName;
    private final String bpmnElementName;
    private final String icon;
    private final String type;
    private final String label;
    private final IConfigurationElement element;
    private final GefEntry gefEntry;
    private final GraphitiEntry graphitiEntry;
    private final Bundle bundle;
    private final boolean enabledInRegulationsByDefault;

    public NodeTypeDefinition(IConfigurationElement configElement) throws CoreException {
        bundle = Platform.getBundle(configElement.getDeclaringExtension().getNamespaceIdentifier());
        this.element = configElement;
        this.type = configElement.getAttribute("type");
        this.bpmnElementName = configElement.getAttribute("bpmn");
        this.jpdlElementName = configElement.getAttribute("jpdl");
        this.icon = configElement.getAttribute("icon");
        this.label = configElement.getAttribute("label");
        IConfigurationElement[] entries = configElement.getChildren("gef");
        if (entries.length > 0) {
            gefEntry = new GefEntry(this, entries[0]);
        } else {
            gefEntry = null;
        }
        entries = configElement.getChildren("graphiti");
        if (entries.length > 0) {
            graphitiEntry = new GraphitiEntry(this, entries[0]);
        } else {
            graphitiEntry = null;
        }
        if (configElement.getAttribute("isEnabledInRegulationsByDefault") != null) {
            this.enabledInRegulationsByDefault = Boolean.valueOf(configElement.getAttribute("isEnabledInRegulationsByDefault"));
        } else {
            this.enabledInRegulationsByDefault = true;
        }
    }

    public boolean hasVariableRenameProvider() {
        return getVariableRenameConfigurationElement() != null;
    }

    private IConfigurationElement getVariableRenameConfigurationElement() {
        IConfigurationElement[] renameProviders = element.getChildren("onVariableRenamed");
        if (renameProviders.length > 0) {
            return renameProviders[0];
        }
        return null;
    }

    public VariableRenameProvider<?> createVariableRenameProvider() {
        try {
            return (VariableRenameProvider<?>) getVariableRenameConfigurationElement().createExecutableExtension("provider");
        } catch (CoreException e) {
            PluginLogger.logError(e);
            return null;
        }
    }

    public ImageDescriptor getImageDescriptor(String notation) {
        return SharedImages.getImageDescriptor(bundle, "icons/" + notation + "/" + getPaletteIcon(), true);
    }

    public Image getImage(String notation) {
        return SharedImages.getImage(bundle, "icons/" + notation + "/" + getPaletteIcon());
    }

    public String getLabel() {
        return label;
    }

    public String getBpmnElementName() {
        return bpmnElementName;
    }

    public String getIcon() {
        return icon;
    }

    public String getPaletteIcon() {
        return "palette/" + icon;
    }

    public String getJpdlElementName() {
        return jpdlElementName;
    }

    public String getType() {
        return type;
    }

    public GefEntry getGefEntry() {
        return gefEntry;
    }

    public GraphitiEntry getGraphitiEntry() {
        return graphitiEntry;
    }

    public String getNamePattern(Language language) {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String key = LanguageElementPreferenceNode.getId(this, language) + '.' + PrefConstants.P_LANGUAGE_NODE_NAME_PATTERN;
        if (store.contains(key)) {
            return store.getString(key);
        } else {
            return this.label;
        }
    }

    public boolean isEnabledInRegulationsByDefault() {
        return this.enabledInRegulationsByDefault;
    }

    private <T> T createExecutableExtension(String propertyName) {
        try {
            if (element == null || element.getAttribute(propertyName) == null) {
                return null;
            }
            return (T) element.createExecutableExtension(propertyName);
        } catch (CoreException e) {
            PluginLogger.logError("Unable to create element '" + this + "'(unable to load property='" + propertyName + "')", e);
            return null;
        }
    }

    public <T extends GraphElement> T createElement(GraphElement parent, boolean setName) {
        GraphElement element = createExecutableExtension("model");
        element.setParent(parent);
        if (setName) {
            String name;
            if (element instanceof Swimlane) {
                name = element.getProcessDefinition().getNextSwimlaneName();
            } else {
                name = getNamePattern(parent.getProcessDefinition().getLanguage());
                if (!(element instanceof EndState) && !(element instanceof StartState)) {
                    name += " " + (parent.getChildren(element.getClass()).size() + 1);
                }
            }
            if (element instanceof NamedGraphElement) {
                ((NamedGraphElement) element).setName(name);
            }
            if (element instanceof TaskState) {
                IPreferenceStore store = Activator.getDefault().getPreferenceStore();
                String key = LanguageElementPreferenceNode.getId(this, parent.getProcessDefinition().getLanguage()) + '.'
                        + PrefConstants.P_LANGUAGE_SWIMLANE_INITIALIZER;
                if (store.contains(key)) {
                    ((TaskState) element).setReassignSwimlaneToInitializerValue(store.getBoolean(key));
                }
                key = LanguageElementPreferenceNode.getId(this, parent.getProcessDefinition().getLanguage()) + '.'
                        + PrefConstants.P_LANGUAGE_SWIMLANE_PERFORMER;
                if (store.contains(key)) {
                    ((TaskState) element).setReassignSwimlaneToTaskPerformer(store.getBoolean(key));
                }
            }
        }
        return (T) element;
    }

    public Class<? extends GraphElement> getModelClass() {
        try {
            Bundle bundle = Platform.getBundle(element.getDeclaringExtension().getNamespaceIdentifier());
            return (Class<? extends GraphElement>) bundle.loadClass(element.getAttribute("model"));
        } catch (Exception e) {
            PluginLogger.logError(e);
            return null;
        }
    }
}
