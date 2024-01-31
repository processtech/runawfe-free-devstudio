package ru.runa.gpd.quick.tag;

import freemarker.template.TemplateModelException;
import ru.runa.gpd.quick.Messages;

public class NotSupportedMessageTag extends FreemarkerTagGpdWrap {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        return Messages.getString("NotSupportedMessageTag.message");
    }
}