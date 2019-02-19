package ru.runa.gpd.quick.formeditor;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.google.common.collect.Maps;

import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.quick.formeditor.util.QuickFormXMLUtil;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.gpd.validation.FormNodeValidation;

public class QuickFormType extends FormType {
    public static final String TYPE = "quick";
    public static final String READ_TAG = "DisplayVariable";
    public static final String WRITE_TAG = "InputVariable";
    private static final String FORMAT_QUICK = "<param>%s</param>";
    private static final String FORMAT_OTHER = "\"%s\"";

    @Override
    public IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException {
        return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), formFile, QuickFormEditor.ID, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, FormVariableAccess> getFormVariableNames(FormNode formNode, byte[] formData) throws Exception {
        Map<String, FormVariableAccess> variableNames = Maps.newHashMap();
        if (formData.length == 0) {
            return variableNames;
        }
        Document document = XmlUtil.parseWithoutValidation(formData);
        Element tagsElement = document.getRootElement().element(QuickFormXMLUtil.ELEMENT_TAGS);
        List<Element> varElementsList = tagsElement.elements(QuickFormXMLUtil.ELEMENT_TAG);
        for (Element varElement : varElementsList) {
            String tag = varElement.elementText(QuickFormXMLUtil.ATTRIBUTE_NAME);
            List<Element> paramElements = varElement.elements(QuickFormXMLUtil.ELEMENT_PARAM);
            if (paramElements != null && paramElements.size() > 0) {
                for (Element paramElement : paramElements) {
                    variableNames.put(paramElement.getText(), READ_TAG.equals(tag) ? FormVariableAccess.READ : FormVariableAccess.WRITE);
                    break;
                }
            }
        }
        return variableNames;
    }

    @Override
    public void validate(FormNode formNode, byte[] formData, FormNodeValidation validation, List<ValidationError> errors) throws Exception {
        super.validate(formNode, formData, validation, errors);
        if (!formNode.hasFormTemplate()) {
            errors.add(ValidationError.createLocalizedError(formNode, "formNode.templateIsRequired"));
        }
    }

    @Override
    public MultiTextEdit searchVariableReplacements(IFile file, String variableName, String replacement) throws Exception {
        if (TYPE.equals(file.getFileExtension())) {
            return super.searchVariableReplacements(file, String.format(FORMAT_QUICK, variableName), String.format(FORMAT_QUICK, replacement));
        }
        return super.searchVariableReplacements(file, String.format(FORMAT_OTHER, variableName), String.format(FORMAT_OTHER, replacement));
    }

}
