package ru.runa.gpd.lang.par;

import com.google.common.base.Strings;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.util.XmlUtil;

public class FormsXmlContentProvider extends AuxContentProvider {
    public static final String XML_FILE_NAME = "forms.xml";
    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String FILE_ATTRIBUTE_NAME = "file";
    private static final String VALIDATION_FILE_ATTRIBUTE_NAME = "validationFile";
    private static final String JS_VALIDATION_ATTRIBUTE_NAME = "jsValidation";
    private static final String SCRIPT_FILE_ATTRIBUTE_NAME = "scriptFile";
    private static final String STATE_ATTRIBUTE_NAME = "state";
    public static final String FORM_ELEMENT_NAME = "form";
    private static final String FORMS_ELEMENT_NAME = "forms";
    public static final String TEMPLATE_FILE_NAME = "templateFileName";

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }
    
    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> formElementsList = document.getRootElement().elements(FORM_ELEMENT_NAME);
        for (Element formElement : formElementsList) {
            String stateId = formElement.attributeValue(STATE_ATTRIBUTE_NAME);
            FormNode formNode = definition.getGraphElementByIdNotNull(stateId);
            String typeName = formElement.attributeValue(TYPE_ATTRIBUTE_NAME);
            if (!Strings.isNullOrEmpty(typeName)) {
                formNode.setFormType(typeName);
            }
            String fileName = formElement.attributeValue(FILE_ATTRIBUTE_NAME);
            if (!Strings.isNullOrEmpty(fileName)) {
                formNode.setFormFileName(fileName);
            }
            String useJsAttr = formElement.attributeValue(JS_VALIDATION_ATTRIBUTE_NAME);
            if ((useJsAttr != null) && (useJsAttr.length() > 0)) {
                formNode.setUseJSValidation(Boolean.valueOf(useJsAttr));
            }
            String templateFileName = formElement.attributeValue(TEMPLATE_FILE_NAME);
            if (!Strings.isNullOrEmpty(templateFileName)) {
                formNode.setTemplateFileName(templateFileName);
            }
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(FORMS_ELEMENT_NAME);
        Element root = document.getRootElement();
        for (Node node : definition.getNodes()) {
            if (node instanceof FormNode) {
                FormNode formNode = (FormNode) node;
                if (formNode.hasForm() || formNode.hasFormValidation()) {
                    Element formElement = root.addElement(FORM_ELEMENT_NAME);
                    formElement.addAttribute(STATE_ATTRIBUTE_NAME, formNode.getId());
                    if (formNode.hasForm()) {
                        formElement.addAttribute(FILE_ATTRIBUTE_NAME, formNode.getFormFileName());
                        formElement.addAttribute(TYPE_ATTRIBUTE_NAME, formNode.getFormType());
                    }
                    if (formNode.hasFormValidation()) {
                        formElement.addAttribute(VALIDATION_FILE_ATTRIBUTE_NAME, formNode.getValidationFileName());
                        formElement.addAttribute(JS_VALIDATION_ATTRIBUTE_NAME, String.valueOf(formNode.isUseJSValidation()));
                    }
                    if (formNode.hasFormScript()) {
                        formElement.addAttribute(SCRIPT_FILE_ATTRIBUTE_NAME, formNode.getScriptFileName());
                    }
                    if (formNode.hasFormTemplate()) {
                        formElement.addAttribute(TEMPLATE_FILE_NAME, formNode.getTemplateFileName());
                    }
                }
            }
        }
        return document;
    }

    public static Set<String> getFormFiles(ProcessDefinition definition) throws CoreException {
        Set<String> files = new HashSet<>();
        IFile file = ((IFolder) definition.getFile().getParent())
                .getFile((definition instanceof SubprocessDefinition ? definition.getId() + "." : "") + FormsXmlContentProvider.XML_FILE_NAME);
        if (file.exists()) {
            Document document = XmlUtil.parseWithoutValidation(file.getContents(true));
            List<Element> formElementsList = document.getRootElement().elements(FORM_ELEMENT_NAME);
            for (Element formElement : formElementsList) {
                String fileName = formElement.attributeValue(FILE_ATTRIBUTE_NAME);
                if (!Strings.isNullOrEmpty(fileName)) {
                    files.add(fileName);
                }
                String validationFileName = formElement.attributeValue(VALIDATION_FILE_ATTRIBUTE_NAME);
                if (!Strings.isNullOrEmpty(validationFileName)) {
                    files.add(validationFileName);
                }
                String scriptFileName = formElement.attributeValue(SCRIPT_FILE_ATTRIBUTE_NAME);
                if (!Strings.isNullOrEmpty(scriptFileName)) {
                    files.add(scriptFileName);
                }
                String templateFileName = formElement.attributeValue(TEMPLATE_FILE_NAME);
                if (!Strings.isNullOrEmpty(templateFileName)) {
                    files.add(templateFileName);
                }
            }
        }
        return files;
    }
}
