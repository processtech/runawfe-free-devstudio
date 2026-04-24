package ru.runa.gpd.lang.model;

import com.google.common.base.Strings;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * Configuration model for conditional {@link CatchEventNode}.
 *
 * <p>Represents configuration stored as XML string
 * in {@link DelegationConfiguration#getDelegationConfiguration()}.
 *
 * <p>Provides parsing from XML and serialization back to XML.
 */
public class ConditionalEventModel {

    private static final String ROOT = "config";
    private static final String EXPRESSION = "expression";
    private static final String STORAGE = "storage";
    private static final String INTERVAL = "interval";


    private String expression = "";
    private Element storage;
    private Duration interval = new Duration();

    // --- parse ---
    public static ConditionalEventModel fromXml(String xml) {
        ConditionalEventModel model = new ConditionalEventModel();

        if (Strings.isNullOrEmpty(xml)) {
            return model;
        }

        Document doc = XmlUtils.parseWithoutValidation(xml);
        Element root = doc.getRootElement();

        model.expression = Strings.nullToEmpty(root.elementText(EXPRESSION));

        Element storage = root.element(STORAGE);
        if (storage != null && !storage.elements().isEmpty()) {
            model.storage = storage;
        }

        String intervalStr = root.elementText(INTERVAL);
        if (!Strings.isNullOrEmpty(intervalStr)) {
            model.interval = new Duration(intervalStr);
        }

        return model;
    }

    // --- serialize ---
    public String toXml() {
        Document doc = XmlUtil.createDocument(ROOT);
        Element root = doc.getRootElement();

        boolean hasData = false;

        if (!Strings.isNullOrEmpty(expression)) {
            root.addElement(EXPRESSION).setText(expression);
            hasData = true;
        }

        if (storage != null && !storage.elements().isEmpty()) {
            root.add(storage.createCopy());
            hasData = true;
        }

        if (interval != null && interval.hasDuration()) {
            root.addElement(INTERVAL).setText(interval.getDuration());
            hasData = true;
        }

        return hasData ? XmlUtil.toString(doc) : "";
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Element getStorage() {
        if (storage == null) {
            return null;
        }
        return storage.createCopy();
    }

    public Element getStorageUnsafe() {
        return storage;
    }

    public void setStorage(Element storage) {
        if (storage == null) {
            this.storage = null;
            return;
        }

        Element copy = storage.createCopy();
        copy.setName(STORAGE);
        this.storage = copy;
    }

    public Duration getInterval() {
        return interval;
    }

    public void setInterval(Duration interval) {
        this.interval = interval;
    }
}
