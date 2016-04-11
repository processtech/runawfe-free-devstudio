package ru.runa.gpd.htmleditor.assist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLParser;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ContextInformationValidator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IFileEditorInput;

import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.HTMLUtil;
import ru.runa.gpd.htmleditor.template.HTMLTemplateAssistProcessor;

/**
 * An implementation of IContentAssistProcessor. This processor provides
 * code-completion for the HTML editor.
 */
public class HTMLAssistProcessor extends HTMLTemplateAssistProcessor { /*
                                                                        * implements
                                                                        * IContentAssistProcessor
                                                                        * {
                                                                        */

    private boolean xhtmlMode = false;
    private char[] chars = {};
    private final Image tagImage;
    private final Image attrImage;
    private final Image valueImage;
    private boolean assistCloseTag = true;
    private List customAttrs = CustomAttribute.loadFromPreference(false);
    private List customElems = CustomElement.loadFromPreference(false);
    private final Set customElemNames = new HashSet();
    protected CSSAssistProcessor cssAssist = new CSSAssistProcessor();

    private int offset;
    private FuzzyXMLDocument doc;

    public HTMLAssistProcessor() {
        tagImage = HTMLPlugin.getImage(HTMLPlugin.ICON_TAG);
        attrImage = HTMLPlugin.getImage(HTMLPlugin.ICON_ATTR);
        valueImage = HTMLPlugin.getImage(HTMLPlugin.ICON_VALUE);

        for (int i = 0; i < customElems.size(); i++) {
            customElemNames.add(((CustomElement) customElems.get(i)).getDisplayName());
        }
    }

    public boolean enableTemplate() {
        return true;
    }

    public void setXHTMLMode(boolean xhtmlMode) {
        this.xhtmlMode = xhtmlMode;
    }

    public void setAutoAssistChars(char[] chars) {
        if (chars != null) {
            this.chars = chars;
        }
    }

    public void setAssistCloseTag(boolean assistCloseTag) {
        this.assistCloseTag = assistCloseTag;
    }

    /**
     * Returns an array of attribute value proposals.
     * 
     * @param tagName
     *            a tag name
     * @param value
     *            an attribute value
     * @param info
     *            an attribute information
     * @return an array of attribute value proposals
     */
    protected AssistInfo[] getAttributeValues(String tagName, String value, AttributeInfo info) {
        // CSS
        if (info.getAttributeType() == AttributeInfo.CSS) {
            return cssAssist.getAssistInfo(tagName, value);
        }
        // FILE
        if (info.getAttributeType() == AttributeInfo.FILE) {
            return new AssistInfo[0];
        }
        // ETC
        String[] values = AttributeValueDefinition.getAttributeValues(info.getAttributeType());
        AssistInfo[] infos = new AssistInfo[values.length];
        for (int i = 0; i < infos.length; i++) {
            infos[i] = new AssistInfo(values[i]);
        }
        return infos;
    }

    /**
     * Returns List of TagInfo.
     * 
     * @return List of TagInfo
     */
    protected List getTagList() {
        return TagDefinition.getTagInfoAsList();
    }

    /**
     * Returns TagInfo.
     * 
     * @param name
     *            a tag name
     * @return TagInfo
     */
    protected TagInfo getTagInfo(String name) {
        List tagList = TagDefinition.getTagInfoAsList();
        for (int i = 0; i < tagList.size(); i++) {
            TagInfo info = (TagInfo) tagList.get(i);
            if (info.getTagName().equals(name)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Returns FuzzyXMLElement by offset.
     * 
     * @return FuzzyXMLElement
     */
    protected FuzzyXMLElement getOffsetElement() {
        return doc.getElementByOffset(offset);
    }

    /**
     * Returns FuzzyXMLDocument.
     * 
     * @return FuzzyXMLDocument
     */
    protected FuzzyXMLDocument getDocument() {
        return doc;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {

        String text = viewer.getDocument().get().substring(0, documentOffset);
        String[] dim = getLastWord(text);
        String word = dim[0].toLowerCase();
        String prev = dim[1].toLowerCase();
        String last = dim[2];
        String attr = dim[3];

        this.offset = documentOffset;
        this.doc = new FuzzyXMLParser().parse(viewer.getDocument().get());

        List list = new ArrayList();
        List tagList = getTagList();

        // attribute value
        if ((word.startsWith("\"") && (word.length() == 1 || !word.endsWith("\""))) || (word.startsWith("'") && (word.length() == 1 || !word.endsWith("\'")))) {
            String value = dim[0].substring(1);
            TagInfo tagInfo = getTagInfo(last.toLowerCase());
            if (tagInfo != null) {
                AttributeInfo attrInfo = tagInfo.getAttributeInfo(attr);
                if (attrInfo != null) {
                    AssistInfo[] keywords = getAttributeValues(last, dim[0].substring(1), attrInfo);
                    for (int i = 0; i < keywords.length; i++) {
                        if (keywords[i].getReplaceString().toLowerCase().startsWith(value.toLowerCase())) {
                            list.add(new CompletionProposal(keywords[i].getReplaceString(), documentOffset - value.length(), value.length(), keywords[i].getReplaceString()
                                    .length(), keywords[i].getImage() == null ? valueImage : keywords[i].getImage(), keywords[i].getDisplayString(), null, null));
                        }
                    }
                }
            }
            // tag
        } else if (word.startsWith("<") && !word.equals("</")) {
            if (supportTagRelation()) {
                TagInfo parent = getTagInfo(last);
                tagList = new ArrayList();
                if (parent != null) {
                    String[] childNames = parent.getChildTagNames();
                    for (int i = 0; i < childNames.length; i++) {
                        tagList.add(getTagInfo(childNames[i]));
                    }
                }
            }
            for (int i = 0; i < tagList.size(); i++) {
                TagInfo tagInfo = (TagInfo) tagList.get(i);
                if (tagInfo instanceof TextInfo) {
                    TextInfo textInfo = (TextInfo) tagInfo;
                    if ((textInfo.getText().toLowerCase()).indexOf(word) == 0) {
                        list.add(new CompletionProposal(textInfo.getText(), documentOffset - word.length(), word.length(), textInfo.getPosition(), tagImage, textInfo
                                .getDisplayString(), null, tagInfo.getDescription()));
                    }
                    continue;
                }
                String tagName = tagInfo.getTagName();
                if (("<" + tagInfo.getTagName().toLowerCase()).indexOf(word) == 0) {
                    String assistKeyword = tagName;
                    int position = 0;
                    // required attributes
                    AttributeInfo[] requierAttrs = tagInfo.getRequiredAttributeInfo();
                    for (int j = 0; j < requierAttrs.length; j++) {
                        assistKeyword = assistKeyword + " " + requierAttrs[j].getAttributeName();
                        if (requierAttrs[j].hasValue()) {
                            assistKeyword = assistKeyword + "=\"\"";
                            if (j == 0) {
                                position = tagName.length() + requierAttrs[j].getAttributeName().length() + 3;
                            }
                        }
                    }
                    if (tagInfo.hasBody()) {
                        assistKeyword = assistKeyword + ">";
                        if (assistCloseTag) {
                            if (position == 0) {
                                position = assistKeyword.length();
                            }
                            assistKeyword = assistKeyword + "</" + tagName + ">";
                        }
                    } else {
                        if (tagInfo.isEmptyTag() && xhtmlMode == false) {
                            assistKeyword = assistKeyword + ">";
                        } else {
                            assistKeyword = assistKeyword + "/>";
                        }
                    }
                    if (position == 0) {
                        position = assistKeyword.length();
                    }
                    try {
                        list.add(new CompletionProposal(assistKeyword, documentOffset - word.length() + 1, word.length() - 1, position, tagImage, tagName, null, tagInfo
                                .getDescription()));
                    } catch (Exception ex) {
                    }
                }
            }
            // custom elements
            for (int i = 0; i < customElems.size(); i++) {
                CustomElement element = (CustomElement) customElems.get(i);
                if ((element.getAssistString().toLowerCase()).indexOf(word) == 0) {
                    int position = element.getAssistString().indexOf('"');
                    if (position == -1) {
                        position = element.getAssistString().indexOf("><");
                    }
                    if (position == -1) {
                        position = element.getAssistString().length();
                    }
                    list.add(new CompletionProposal(element.getAssistString(), documentOffset - word.length(), word.length(), position + 1, tagImage, element.getDisplayName(),
                            null, null));
                }
            }
            // attribute
        } else if (!prev.equals("")) {
            String tagName = prev;
            TagInfo tagInfo = getTagInfo(tagName);
            if (tagInfo != null) {
                AttributeInfo[] attrList = tagInfo.getAttributeInfo();
                for (int j = 0; j < attrList.length; j++) {
                    if (attrList[j].getAttributeName().toLowerCase().indexOf(word) == 0) {
                        String assistKeyword = null;
                        int position = 0;
                        if (attrList[j].hasValue()) {
                            assistKeyword = attrList[j].getAttributeName() + "=\"\"";
                            position = 2;
                        } else {
                            assistKeyword = attrList[j].getAttributeName();
                            position = 0;
                        }
                        list.add(new CompletionProposal(assistKeyword, documentOffset - word.length(), word.length(), attrList[j].getAttributeName().length() + position,
                                attrImage, attrList[j].getAttributeName(), null, attrList[j].getDescription()));
                    }
                }
            }
            // custom attributes
            for (int i = 0; i < customAttrs.size(); i++) {
                CustomAttribute attrInfo = (CustomAttribute) customAttrs.get(i);
                if (attrInfo.getTargetTag().equals("*") || attrInfo.getTargetTag().equals(tagName)) {
                    if (tagName.indexOf(":") < 0 || customElemNames.contains(tagName)) {
                        list.add(new CompletionProposal(attrInfo.getAttributeName() + "=\"\"", documentOffset - word.length(), word.length(),
                                attrInfo.getAttributeName().length() + 2, attrImage, attrInfo.getAttributeName(), null, null));
                    }
                }
            }
            // close tag
        } else if (!last.equals("")) {
            TagInfo info = getTagInfo(last);
            if (info == null || xhtmlMode == true || info.hasBody() || !info.isEmptyTag()) {
                String assistKeyword = "</" + last + ">";
                int length = 0;
                if (word.equals("</")) {
                    length = 2;
                }
                list.add(new CompletionProposal(assistKeyword, documentOffset - length, length, assistKeyword.length(), tagImage, assistKeyword, null, null));
            }
        }

        HTMLUtil.sortCompilationProposal(list);

        if (enableTemplate()) {
            ICompletionProposal[] templates = super.computeCompletionProposals(viewer, documentOffset);
            for (int i = 0; i < templates.length; i++) {
                list.add(templates[i]);
            }
        }

        ICompletionProposal[] prop = (ICompletionProposal[]) list.toArray(new ICompletionProposal[list.size()]);
        return prop;
    }

    /**
     * Returns true if this processor support parent and child relation. In the
     * default, this method returns false.
     */
    protected boolean supportTagRelation() {
        return false;
    }

    /**
     * Returns same informations for code completion from calet position.
     * 
     * @return <ul>
     *         <li>0 - last word from calet position (if it's tag, it contains
     *         &lt;)</li>
     *         <li>1 - target of attribute completion (only tag name, not
     *         contains &lt;)</li>
     *         <li>2 - target of close tag completion (only tag name, not
     *         contains &lt;)</li>
     *         <li>3 - previous attribute name</li>
     *         </ul>
     */
    protected String[] getLastWord(String text) {
        StringBuffer sb = new StringBuffer();
        Stack stack = new Stack();
        String word = "";
        String prevTag = "";
        String lastTag = "";
        String attr = "";
        String temp1 = ""; // temporary
        String temp2 = ""; // temporary
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // skip scriptlet
            if (c == '<' && text.length() > i + 1 && text.charAt(i + 1) == '%') {
                i = text.indexOf("%>", i + 2);
                if (i == -1) {
                    i = text.length();
                }
                continue;
            }
            // skip XML declaration
            if (c == '<' && text.length() > i + 1 && text.charAt(i + 1) == '?') {
                i = text.indexOf("?>", i + 2);
                if (i == -1) {
                    i = text.length();
                }
                continue;
            }
            if (isDelimiter(c)) {
                temp1 = sb.toString();
                // skip whitespaces in the attribute value
                if (temp1.length() > 1 && ((temp1.startsWith("\"") && !temp1.endsWith("\"") && c != '"') || (temp1.startsWith("'") && !temp1.endsWith("'") && c != '\''))) {
                    sb.append(c);
                    continue;
                }

                if (!temp1.equals("")) {
                    temp2 = temp1;
                    if (temp2.endsWith("=") && !prevTag.equals("") && !temp2.equals("=")) {
                        attr = temp2.substring(0, temp2.length() - 1);
                    }
                }
                if (temp1.startsWith("<") && !temp1.startsWith("</") && !temp1.startsWith("<!")) {
                    prevTag = temp1.substring(1);
                    if (!temp1.endsWith("/")) {
                        stack.push(prevTag);
                    }
                } else if (temp1.startsWith("</") && stack.size() != 0) {
                    stack.pop();
                } else if (temp1.endsWith("/") && stack.size() != 0) {
                    stack.pop();
                }
                sb.setLength(0);

                if (c == '<') {
                    sb.append(c);
                } else if (c == '"' || c == '\'') {
                    if (temp1.startsWith("\"") || temp1.startsWith("'")) {
                        sb.append(temp1);
                    }
                    sb.append(c);
                } else if (c == '>') {
                    prevTag = "";
                    attr = "";
                }
            } else {
                if (c == '=' && !prevTag.equals("")) {
                    attr = temp2.trim();
                }
                temp1 = sb.toString();
                if (temp1.length() > 1 && (temp1.startsWith("\"") && temp1.endsWith("\"")) || (temp1.startsWith("'") && temp1.endsWith("'"))) {
                    sb.setLength(0);
                }
                sb.append(c);
            }
        }

        if (stack.size() != 0) {
            lastTag = (String) stack.pop();
        }
        // Hmm... it's not perfect...
        if (attr.endsWith("=")) {
            attr = attr.substring(0, attr.length() - 1);
        }
        word = sb.toString();

        return new String[] { word, prevTag, lastTag, attr };
    }

    /**
     * Tests a character is delimiter or not delimiter.
     */
    protected boolean isDelimiter(char c) {
        if (c == ' ' || c == '(' || c == ')' || c == ',' // || c == '.'
                || c == ';' || c == '\n' || c == '\r' || c == '\t' || c == '+' || c == '>' || c == '<' || c == '*' || c == '^' // ||
                                                                                                                               // c
                                                                                                                               // ==
                                                                                                                               // '{'
                // || c == '}'
                || c == '[' || c == ']' || c == '"' || c == '\'') {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
        ContextInformation[] info = new ContextInformation[0];
        return info;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return chars;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return chars;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return new ContextInformationValidator(this);
    }

    @Override
    public String getErrorMessage() {
        return "Error";
    }

    public void update(IFileEditorInput input, String source) {
        cssAssist.reload(input.getFile(), source);
        customAttrs = CustomAttribute.loadFromPreference(false);
        customElems = CustomElement.loadFromPreference(false);

        customElemNames.clear();
        for (int i = 0; i < customElems.size(); i++) {
            customElemNames.add(((CustomElement) customElems.get(i)).getDisplayName());
        }

    }

}