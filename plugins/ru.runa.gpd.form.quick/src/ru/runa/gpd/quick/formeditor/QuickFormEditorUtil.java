package ru.runa.gpd.quick.formeditor;

import java.util.List;

import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;

public final class QuickFormEditorUtil {

	public static final boolean isTemplateUsingInForms(ProcessDefinition processDefinition, FormNode formNode, String prevTemplateFileName) {
        boolean isUsing = false;
        List<FormNode> formNodes = processDefinition.getChildren(FormNode.class);
        for (FormNode localFormNode : formNodes) {
            if (localFormNode.hasForm() && FormTypeProvider.getFormType(localFormNode.getFormType()) instanceof QuickFormType) {
                if (localFormNode == formNode) {
                    continue;
                }

                String templateName = formNode.getTemplateFileName();
                if (prevTemplateFileName.equals(templateName)) {
                    isUsing = true;
                    break;
                }
            }
        }

        return isUsing;
    }
}
