package ru.runa.gpd.quick.tag;

import freemarker.template.TemplateModelException;
import ru.runa.wfe.var.dto.WfVariable;

public class DisplayVariableTag extends FreemarkerTagGpdWrap {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        boolean componentView = getParameterAs(boolean.class, 1);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        if (componentView) {
            return ViewUtil.getComponentOutput(user, variableName, variable.getDefinition().getFormatClassName(), variable.getValue());
        } else {
            String html = "<span class=\"displayVariable\">";
            html += ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variable);
            html += "</span>";
            return html;
        }
    }
}
