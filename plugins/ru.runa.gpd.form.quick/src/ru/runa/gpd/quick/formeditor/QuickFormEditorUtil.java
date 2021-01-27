package ru.runa.gpd.quick.formeditor;

import java.util.List;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;

public final class QuickFormEditorUtil {

	public static final boolean isTemplateUsingInForms(ProcessDefinition processDefinition, FormNode formNode, String prevTemplateFileName) {
        boolean isUsing = false;
        ProcessDefinition parentDefinition = processDefinition;
        if (processDefinition instanceof SubprocessDefinition) {
            parentDefinition = (ProcessDefinition) processDefinition.getParent();
        }
        List<FormNode> formNodes = parentDefinition.getChildren(FormNode.class);
        for (SubprocessDefinition sd : parentDefinition.getEmbeddedSubprocesses().values()) {
            formNodes.addAll(sd.getChildren(FormNode.class));
        }
        for (FormNode localFormNode : formNodes) {
            if (localFormNode.hasForm() && FormTypeProvider.getFormType(localFormNode.getFormType()) instanceof QuickFormType) {
                if (prevTemplateFileName.equals(localFormNode.getTemplateFileName())) {
                    isUsing = true;
                    break;
                }
            }
        }
        return isUsing;
    }
}
