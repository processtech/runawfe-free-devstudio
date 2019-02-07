package ru.runa.gpd.formeditor.ftl.conv;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.Variable;

public class EditorHashModel extends SimpleHash {
    private static final long serialVersionUID = 1L;
    private final Map<String, Variable> variables;
    private final FormEditor formEditor;
    private boolean stageRenderingParams = false;

    public EditorHashModel(FormEditor formEditor) {
        this.variables = formEditor.getVariables();
        this.formEditor = formEditor;
    }

    private TemplateModel wrapParameter(String prefix, Variable variable) {
        if (prefix != null) {
            prefix += "." + variable.getName();
        } else {
            prefix = variable.getName();
        }
        if (variable.getUserType() != null) {
            Map<String, TemplateModel> properties = new HashMap<String, TemplateModel>();
            for (Variable attribute : variable.getUserType().getAttributes()) {
                properties.put(attribute.getName(), wrapParameter(prefix, attribute));
            }
            return new SimpleHash(properties);
        }
        return new SimpleScalar(prefix);
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        if (stageRenderingParams) {
            Variable variable = variables.get(key);
            if (variable != null) {
                return wrapParameter(null, variable);
            }
            return new SimpleScalar(key);
        }
        if (ComponentTypeRegistry.has(key)) {
            stageRenderingParams = true;
            return new ComponentModel(ComponentTypeRegistry.getNotNull(key));
        }
        stageRenderingParams = true;
        return new UndefinedComponentModel(key);
    }

    private String getRawComponentText(String componentName, List args) {
        Object params = args.stream().map(o -> ("\"" + o + "\"")).collect(Collectors.joining(", "));
        return "${" + componentName + "(" + params + ")}";
    }

    public class ComponentModel implements TemplateMethodModel {
        private final ComponentType componentType;

        public ComponentModel(ComponentType componentType) {
            this.componentType = componentType;
        }

        @Override
        public Object exec(List args) throws TemplateModelException {
            stageRenderingParams = false;
            if (args.size() > componentType.getParameters().size()
                    && !componentType.getParameters().get(componentType.getParameters().size() - 1).getType().isMultiple()) {
                return getRawComponentText(componentType.getId(), args);
            }
            Component component = formEditor.createComponent(componentType.getId());
            component.setRawParameters(args);
            StringBuffer buffer = new StringBuffer();
            buffer.append("<").append(DesignUtils.getComponentHtmlElementName()).append(" ");
            buffer.append(DesignUtils.ATTR_COMPONENT_TYPE).append("=\"").append(componentType.getId()).append("\" ");
            buffer.append("id=\"").append(component.getId()).append("\" ");
            String params = Joiner.on(DesignUtils.PARAMETERS_DELIM).join(component.getRawParameters());
            buffer.append(DesignUtils.ATTR_COMPONENT_PARAMETERS).append("=\"").append(params).append("\"");
            if (WebServerUtils.useCKEditor()) {
                buffer.append("></").append(DesignUtils.getComponentHtmlElementName()).append(">");
            } else {
                String url = "/editor/FtlComponentServlet?command=GetImage&type=" + componentType.getId() + "&parameters=";
                try {
                    url += URLEncoder.encode(params.toString(), Charsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    PluginLogger.logError(e);
                }
                buffer.append(" src=\"").append(url).append("\" ");
                buffer.append(DesignUtils.ATTR_STYLE).append("=\"margin: 3px; border: 2px solid black;\" ");
                buffer.append("/>");
            }
            return buffer.toString();
        }
    }

    public class UndefinedComponentModel implements TemplateMethodModel {
        private final String componentName;

        public UndefinedComponentModel(String componentName) {
            this.componentName = componentName;
        }

        @Override
        public Object exec(List args) throws TemplateModelException {
            stageRenderingParams = false;
            return getRawComponentText(componentName, args);
        }
    }
}
