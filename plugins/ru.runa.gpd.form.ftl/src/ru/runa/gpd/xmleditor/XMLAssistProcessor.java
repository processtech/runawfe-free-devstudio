package ru.runa.gpd.xmleditor;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.ui.IFileEditorInput;

import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.assist.AssistInfo;
import ru.runa.gpd.htmleditor.assist.AttributeInfo;
import ru.runa.gpd.htmleditor.assist.HTMLAssistProcessor;
import ru.runa.gpd.htmleditor.assist.TagInfo;

import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDChoice;
import com.wutka.dtd.DTDDecl;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDEmpty;
import com.wutka.dtd.DTDEnumeration;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDMixed;
import com.wutka.dtd.DTDName;
import com.wutka.dtd.DTDParseException;
import com.wutka.dtd.DTDParser;
import com.wutka.dtd.DTDSequence;

/**
 * AssistProcessor for the XML editor.
 */
public class XMLAssistProcessor extends HTMLAssistProcessor {

    private final List tagList = new ArrayList();
    private final Map nsTagListMap = new HashMap();

    // private TagInfo root = null;

    /**
     * The constructor without DTD / XSD. XMLAssistProcessor that's created by
     * this constructor completes only close tags.
     */
    public XMLAssistProcessor() {
        super();
    }

    /**
     * Update informations about code-completion.
     * 
     * @param input
     *            IFileEditorInput
     * @param source
     *            JSP
     */
    @Override
    public void update(IFileEditorInput input, String source) {
        // nothing to do.
    }

    /**
     * Refresh DTD informations.
     * 
     * @param in
     *            InputStream of DTD
     */
    public void updateDTDInfo(Reader in) {
        // clear at fisrt
        tagList.clear();
        // root = null;
        try {
            DTDParser parser = new DTDParser(in);
            DTD dtd = parser.parse();
            Object[] obj = dtd.getItems();
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] instanceof DTDElement) {
                    DTDElement element = (DTDElement) obj[i];
                    String name = element.getName();
                    DTDItem item = element.getContent();
                    boolean hasBody = true;
                    if (item instanceof DTDEmpty) {
                        hasBody = false;
                    }
                    TagInfo tagInfo = new TagInfo(name, hasBody);
                    Iterator ite = element.attributes.keySet().iterator();

                    // set child tags
                    if (item instanceof DTDSequence) {
                        DTDSequence seq = (DTDSequence) item;
                        setChildTagName(tagInfo, seq.getItem());
                    } else if (item instanceof DTDMixed) {
                        // #PCDATA
                    }

                    while (ite.hasNext()) {
                        String attrName = (String) ite.next();
                        DTDAttribute attr = element.getAttribute(attrName);

                        DTDDecl decl = attr.getDecl();
                        boolean required = false;
                        if (decl == DTDDecl.REQUIRED) {
                            required = true;
                        }

                        AttributeInfo attrInfo = new AttributeInfo(attrName, true, AttributeInfo.NONE, required);
                        tagInfo.addAttributeInfo(attrInfo);

                        Object attrType = attr.getType();
                        if (attrType instanceof DTDEnumeration) {
                            DTDEnumeration dtdEnum = (DTDEnumeration) attrType;
                            String[] items = dtdEnum.getItems();
                            for (int j = 0; j < items.length; j++) {
                                attrInfo.addValue(items[j]);
                            }
                        }
                    }
                    tagList.add(tagInfo);
                }
            }
        } catch (DTDParseException ex) {
            // ignore
        } catch (Exception ex) {
            HTMLPlugin.logException(ex);
        }
    }

    /**
     * Refresh XML schema informations
     * 
     * @param uri
     *            URI of XML schema
     * @param in
     *            InputStream of XML schema
     */
    public void updateXSDInfo(String uri, Reader in) {
        try {
            SchemaGrammar grammer = (SchemaGrammar) new XMLSchemaLoader().loadGrammar(new XMLInputSource(null, null, null, in, null));

            // clear at first
            String targetNS = grammer.getTargetNamespace();
            nsTagListMap.put(targetNS, new ArrayList());
            List tagList = (List) nsTagListMap.get(targetNS);
            // root = null;

            XSNamedMap map = grammer.getComponents(XSConstants.ELEMENT_DECLARATION);
            for (int i = 0; i < map.getLength(); i++) {
                XSElementDeclaration element = (XSElementDeclaration) map.item(i);
                parseXSDElement(tagList, element);
            }
        } catch (Exception ex) {

        }
    }

    private void parseXSDElement(List tagList, XSElementDeclaration element) {
        TagInfo tagInfo = new TagInfo(element.getName(), true);
        if (tagList.contains(tagInfo)) {
            return;
        }
        tagList.add(tagInfo);

        XSTypeDefinition type = element.getTypeDefinition();
        if (type instanceof XSComplexTypeDefinition) {
            XSParticle particle = ((XSComplexTypeDefinition) type).getParticle();
            if (particle != null) {
                XSTerm term = particle.getTerm();
                if (term instanceof XSElementDeclaration) {
                    parseXSDElement(tagList, (XSElementDeclaration) term);
                    tagInfo.addChildTagName(((XSElementDeclaration) term).getName());
                }
                if (term instanceof XSModelGroup) {
                    parseXSModelGroup(tagInfo, tagList, (XSModelGroup) term);
                }
            }
            XSObjectList attrs = ((XSComplexTypeDefinition) type).getAttributeUses();
            for (int i = 0; i < attrs.getLength(); i++) {
                XSAttributeUse attrUse = (XSAttributeUse) attrs.item(i);
                XSAttributeDeclaration attr = attrUse.getAttrDeclaration();
                AttributeInfo attrInfo = new AttributeInfo(attr.getName(), true, AttributeInfo.NONE, attrUse.getRequired());
                tagInfo.addAttributeInfo(attrInfo);
            }

        }
    }

    private void parseXSModelGroup(TagInfo tagInfo, List tagList, XSModelGroup term) {
        XSObjectList list = term.getParticles();
        for (int i = 0; i < list.getLength(); i++) {
            XSObject obj = list.item(i);

            if (obj instanceof XSParticle) {
                XSTerm term2 = ((XSParticle) obj).getTerm();

                if (term2 instanceof XSElementDeclaration) {
                    parseXSDElement(tagList, (XSElementDeclaration) term2);
                    tagInfo.addChildTagName(((XSElementDeclaration) term2).getName());
                }
                if (term2 instanceof XSModelGroup) {
                    parseXSModelGroup(tagInfo, tagList, (XSModelGroup) term2);
                }
            }
        }
    }

    /**
     * Sets a child tag name to TagInfo.
     * 
     * @param tagInfo
     *            TagInfo
     * @param items
     *            an array of DTDItem
     */
    private void setChildTagName(TagInfo tagInfo, DTDItem[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof DTDName) {
                DTDName dtdName = (DTDName) items[i];
                tagInfo.addChildTagName(dtdName.getValue());
            } else if (items[i] instanceof DTDChoice) {
                DTDChoice dtdChoise = (DTDChoice) items[i];
                setChildTagName(tagInfo, dtdChoise.getItem());
            }
        }
    }

    @Override
    protected boolean supportTagRelation() {
        return true;
    }

    /**
     * Returns an array of an attribute value proposal to complete an attribute
     * value.
     * 
     * @param tagName
     *            tag name
     * @param value
     *            inputed value
     * @param info
     *            attribute information
     * @return an array of an attribute value proposal
     */
    @Override
    protected AssistInfo[] getAttributeValues(String tagName, String value, AttributeInfo info) {
        String[] values = info.getValues();
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
    @Override
    protected List getTagList() {
        ArrayList list = new ArrayList();
        list.addAll(this.tagList);
        // get namespace
        FuzzyXMLElement element = getOffsetElement();
        HashMap nsPrefixMap = new HashMap();
        getNamespace(nsPrefixMap, element);
        // add prefix to tag names
        Iterator ite = this.nsTagListMap.keySet().iterator();
        while (ite.hasNext()) {
            String uri = (String) ite.next();
            String prefix = (String) nsPrefixMap.get(uri);
            if (prefix == null || prefix.equals("")) {
                list.addAll((List) this.nsTagListMap.get(uri));
            } else {
                List nsTagList = (List) this.nsTagListMap.get(uri);
                for (int i = 0; i < nsTagList.size(); i++) {
                    TagInfo tagInfo = (TagInfo) nsTagList.get(i);
                    list.add(createPrefixTagInfo(tagInfo, prefix));
                }
            }
        }
        return list;
    }

    /**
     * Adds prefix to TagInfo.
     */
    private TagInfo createPrefixTagInfo(TagInfo tagInfo, String prefix) {
        TagInfo newTagInfo = new TagInfo(prefix + ":" + tagInfo.getTagName(), tagInfo.hasBody());
        AttributeInfo[] attrInfos = tagInfo.getAttributeInfo();
        for (int i = 0; i < attrInfos.length; i++) {
            AttributeInfo newAttrInfo = new AttributeInfo(prefix + ":" + attrInfos[i].getAttributeName(), true, AttributeInfo.NONE, attrInfos[i].isRequired());
            newTagInfo.addAttributeInfo(newAttrInfo);
        }
        String[] children = tagInfo.getChildTagNames();
        for (int i = 0; i < children.length; i++) {
            newTagInfo.addChildTagName(prefix + ":" + children[i]);
        }
        return newTagInfo;
    }

    /**
     * Returns mapping of namespace and prefix at the calet position.
     * 
     * @param map
     *            <ul>
     *            <li>key - namespace</li>
     *            <li>value - prefix</li>
     *            </ul>
     * @param element
     *            FuzzyXMLElement at the calet position
     */
    private void getNamespace(Map map, FuzzyXMLElement element) {
        FuzzyXMLAttribute[] attrs = element.getAttributes();
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].getName().startsWith("xmlns")) {
                String name = attrs[i].getName();
                String prefix = "";
                if (name.indexOf(":") >= 0) {
                    prefix = name.substring(name.indexOf(":") + 1);
                }
                if (map.get(attrs[i].getValue()) == null) {
                    map.put(attrs[i].getValue(), prefix);
                }
            }
        }
        if (element.getParentNode() != null) {
            getNamespace(map, (FuzzyXMLElement) element.getParentNode());
        }
    }

    /**
     * Returns TagInfo.
     * 
     * @param name
     *            tag name
     * @return TagInfo
     */
    @Override
    protected TagInfo getTagInfo(String name) {
        List tagList = getTagList();
        for (int i = 0; i < tagList.size(); i++) {
            TagInfo info = (TagInfo) tagList.get(i);
            if (info.getTagName().equals(name)) {
                return info;
            }
        }
        return null;
    }
}
