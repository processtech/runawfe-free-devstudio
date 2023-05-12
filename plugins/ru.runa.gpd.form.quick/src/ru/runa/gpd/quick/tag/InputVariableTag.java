package ru.runa.gpd.quick.tag;

import freemarker.template.TemplateModelException;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;

public class InputVariableTag extends FreemarkerTagGpdWrap {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String formatClassName = variable.getDefinition().getFormatClassName();
        Object value = variableProvider.getValue(variableName);
        String html;
        if (ListFormat.class.getName().equals(formatClassName)) {
            EditListTag tag = new EditListTag();
            //tag.initChained(this);
            html = tag.renderRequest();
        } else {
            html = ViewUtil.getComponentInput(user, variableName, formatClassName, value);
        }
        return html;
    }

}
