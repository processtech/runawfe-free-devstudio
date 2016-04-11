package ru.runa.gpd.formeditor.ftl;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.osgi.framework.Bundle;

import ru.runa.gpd.formeditor.ftl.image.ComponentImageProvider;
import ru.runa.gpd.formeditor.ftl.image.DefaultComponentImageProvider;
import ru.runa.gpd.formeditor.ftl.validation.DefaultComponentValidator;
import ru.runa.gpd.formeditor.ftl.validation.IComponentValidator;

import com.google.common.collect.Lists;

public class ComponentType {
    private static int orderCounter = 0;
    private final Bundle bundle;
    private final boolean enabled;
    private final int order;
    private final String id;
    private final String label;
    private final String description;
    private final ComponentImageProvider imageProvider;
    private final IComponentValidator validator;
    private final List<ComponentParameter> parameters = Lists.newArrayList();

    public ComponentType(Bundle bundle, IConfigurationElement element) throws CoreException {
        this.bundle = bundle;
        this.enabled = Boolean.valueOf(element.getAttribute("enabled"));
        int o;
        try {
            o = Integer.valueOf(element.getAttribute("order"));
        } catch (Exception e) {
            o = orderCounter++;
        }
        this.order = o;
        this.id = element.getAttribute("id");
        this.label = element.getAttribute("name");
        this.description = element.getAttribute("description");
        if (element.getAttribute("imageProvider") != null) {
            this.imageProvider = (ComponentImageProvider) element.createExecutableExtension("imageProvider");
        } else {
            this.imageProvider = new DefaultComponentImageProvider();
        }
        if (element.getAttribute("validator") != null) {
            this.validator = (IComponentValidator) element.createExecutableExtension("validator");
        } else {
            this.validator = new DefaultComponentValidator();
        }
    }

    public Bundle getBundle() {
        return bundle;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getOrder() {
        return order;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public ComponentImageProvider getImageProvider() {
        return imageProvider;
    }

    public IComponentValidator getValidator() {
        return validator;
    }

    public List<ComponentParameter> getParameters() {
        return parameters;
    }

    public ComponentParameter getParameterOrLastMultiple(int index) {
        if (parameters.size() > index) {
            return parameters.get(index);
        }
        ComponentParameter parameter = parameters.get(parameters.size() - 1);
        if (parameter.getType().isMultiple()) {
            return parameter;
        }
        return null;
    }

    @Override
    public String toString() {
        return id + " " + label + (enabled ? "" : " (disabled)");
    }

}
