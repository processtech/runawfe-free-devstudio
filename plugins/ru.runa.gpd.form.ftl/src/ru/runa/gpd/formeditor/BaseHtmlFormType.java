package ru.runa.gpd.formeditor;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.cyberneko.html.parsers.DOMParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.jointformeditor.JointFormEditor;
import ru.runa.gpd.lang.model.FormNode;

public abstract class BaseHtmlFormType extends FormType {
    private static final String READONLY_ATTR = "readonly";
    private static final String DISABLED_ATTR = "disabled";
    private static final String NAME_ATTR = "name";
    private static final String FORMAT_OTHER = "\"%s\"";
    protected FormEditor editor;

    @Override
    public IEditorPart openForm(final IFile formFile, final FormNode formNode) throws CoreException {
        return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), formFile, JointFormEditor.ID, true);
    }

    protected Map<String, FormVariableAccess> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        return Maps.newHashMap();
    }

    @Override
    public Map<String, FormVariableAccess> getFormVariableNames(FormNode formNode, byte[] formData) throws Exception {
        Map<String, FormVariableAccess> variableNames = Maps.newHashMap();
        Document document = getDocument(new ByteArrayInputStream(formData));
        NodeList inputElements = document.getElementsByTagName("input");
        addHtmlFields(inputElements, variableNames);
        NodeList textareaElements = document.getElementsByTagName("textarea");
        addHtmlFields(textareaElements, variableNames);
        NodeList selectElements = document.getElementsByTagName("select");
        addHtmlFields(selectElements, variableNames);
        Map<String, FormVariableAccess> typeSpecificVariableNames = getTypeSpecificVariableNames(formNode, formData);
        for (Map.Entry<String, FormVariableAccess> entry : typeSpecificVariableNames.entrySet()) {
            FormVariableAccess access = entry.getValue();
            if (variableNames.containsKey(entry.getKey())) {
                FormVariableAccess oldAccess = variableNames.remove(entry.getKey());
                if (oldAccess == FormVariableAccess.WRITE || access == FormVariableAccess.WRITE) {
                    access = FormVariableAccess.WRITE;
                } else if (access == FormVariableAccess.DOUBTFUL) {
                    access = FormVariableAccess.DOUBTFUL;
                } else {
                    access = oldAccess;
                }
                variableNames.put(entry.getKey(), entry.getValue());
            }
            variableNames.put(entry.getKey(), access);
        }
        return variableNames;
    }

    private static void addHtmlFields(NodeList inputElements, Map<String, FormVariableAccess> variableNames) {
        for (int i = 0; i < inputElements.getLength(); i++) {
            Node nameNode = inputElements.item(i).getAttributes().getNamedItem(NAME_ATTR);
            Node disabledNode = inputElements.item(i).getAttributes().getNamedItem(DISABLED_ATTR);
            Node readonlyNode = inputElements.item(i).getAttributes().getNamedItem(READONLY_ATTR);
            if (nameNode != null) {
                boolean required = (disabledNode == null) && (readonlyNode == null);
                variableNames.put(nameNode.getNodeValue(), required ? FormVariableAccess.WRITE : FormVariableAccess.READ);
            }
        }
    }

    public static Document getDocument(InputStream is) throws IOException, SAXException {
        DOMParser parser = new DOMParser();
        InputSource inputSource = new InputSource(is);
        inputSource.setEncoding(Charsets.UTF_8.name());
        parser.parse(inputSource);
        return parser.getDocument();
    }

    /**
     * Variable name is used double quotes around in file
     */
    @Override
    public MultiTextEdit searchVariableReplacements(IFile file, String variableName, String replacement) throws Exception {
        return super.searchVariableReplacements(file, String.format(FORMAT_OTHER, variableName), String.format(FORMAT_OTHER, replacement));
    }
}
