package ru.runa.gpd.quick.extension;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ru.runa.gpd.Activator;
import ru.runa.gpd.extension.Artifact;
import ru.runa.gpd.extension.ArtifactRegistry;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class QuickTemplateRegister extends ArtifactRegistry<QuickTemplateArtifact> {
	private static Map<String, Bundle> templateBundles = new HashMap<String, Bundle>();
    private static final String XML_FILE_NAME = "quicktemplates.xml";
    private static final QuickTemplateRegister instance = new QuickTemplateRegister(new QuickTemplateContentProvider());
    
    public static QuickTemplateRegister getInstance() {
        return instance;
    }
    
	public QuickTemplateRegister(QuickTemplateContentProvider contentProvider) {
		super(contentProvider);
	}

    @Override
    protected File getContentFile() {
        return new File(Activator.getPreferencesFolder(), XML_FILE_NAME);
    }

    @Override
    protected void loadDefaults(List<QuickTemplateArtifact> list) {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.form.quick.templates").getExtensions();
        for (IExtension extension : extensions) {
            Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                boolean enabled = Boolean.valueOf(configElement.getAttribute("enabled"));
                String name = configElement.getAttribute("name");
                String label = configElement.getAttribute("label");
                String fileName = configElement.getAttribute("filename");
                QuickTemplateArtifact artifact = new QuickTemplateArtifact(enabled, name, label, fileName);
                if(configElement.getChildren() != null) {
                	List<Artifact> parameters = new ArrayList<Artifact>();
                	for(IConfigurationElement parameterConfig : configElement.getChildren()) {
                		Artifact parameterArtifact = new Artifact();
                		parameterArtifact.setName(parameterConfig.getAttribute("name"));
                		parameterArtifact.setLabel(parameterConfig.getAttribute("label"));
                		parameters.add(parameterArtifact);
                	}
                	artifact.setParameters(parameters);
                }
                list.add(artifact);
                templateBundles.put(fileName, bundle);
            }
        }
    }
    
    public static Bundle getBundle(String fileName) {
    	if (templateBundles.containsKey(fileName)) {
            return templateBundles.get(fileName);
        }
        return null;
    }

    public List<QuickTemplateArtifact> getAll(boolean onlyEnabled) {
        List<QuickTemplateArtifact> list = Lists.newArrayList();
        for (QuickTemplateArtifact handlerArtifact : getAll()) {
            if (onlyEnabled && !handlerArtifact.isEnabled()) {
                continue;
            }
            list.add(handlerArtifact);
        }
        return list;
    }

    public boolean isArtifactRegistered(String name) {
    	QuickTemplateArtifact handlerArtifact = getArtifact(name);
        return handlerArtifact != null;
    }

    public QuickTemplateArtifact getArtifactByFileName(String fileName) {
        for (QuickTemplateArtifact artifact : getAll()) {
            if (Objects.equal(fileName, artifact.getFileName())) {
                return artifact;
            }
        }
        
        return null;
    }
}
