package ru.runa.gpd.ui.enhancement;

import com.google.common.base.Strings;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.lang.Delegation;

/**
 * Adapter for {@link CatchEventNode} to expose storage configuration
 * as {@link Delegable}.
 *
 * <p>Works with storage part of delegation configuration stored in
 * {@link Delegation#getConfiguration()} instead of full configuration.
 */
public class ConditionalEventStorageDelegableAdapter implements Delegable, StorageAware, ProcessDefinitionAware, VariableContainer {

    private final CatchEventNode node;

    public ConditionalEventStorageDelegableAdapter(CatchEventNode node) {
        this.node = node;
    }

    @Override
    public String getDelegationConfiguration() {
        Element storage = ConditionalEventModel.fromXml(node.getDelegationConfiguration()).getStorage();

        if (storage == null || storage.elements().isEmpty()) {
            return "";
        }

        Document document = XmlUtil.createDocument(storage);
        return XmlUtil.toString(document);
    }

    @Override
    public void setDelegationConfiguration(String storageXml) {
        ConditionalEventModel model = ConditionalEventModel.fromXml(node.getDelegationConfiguration());
        Element storageElement = null;

        if (!Strings.isNullOrEmpty(storageXml) && XmlUtil.isXml(storageXml)) {
            storageElement = XmlUtil.parseWithoutValidation(storageXml).getRootElement();
            model.setStorage(storageElement);
        }

        model.setStorage(storageElement);
        node.setDelegationConfiguration(model.toXml());
    }

    @Override
    public String getDelegationClassName() {
        return node.getDelegationClassName();
    }

    @Override
    public void setDelegationClassName(String className) {
        node.setDelegationClassName(className);
    }

    @Override
    public String getDelegationType() {
        return node.getDelegationType();
    }

    @Override
    public List<String> getVariableNames(boolean includeSwimlanes, String... filters) {
        return node.getVariableNames(includeSwimlanes, filters);
    }

    @Override
    public boolean isUseExternalStorageIn() {
        return node.isUseExternalStorageIn();
    }

    @Override
    public boolean isUseExternalStorageOut() {
        return node.isUseExternalStorageOut();
    }

    @Override
    public ProcessDefinition getProcessDefinition() {
        return node.getProcessDefinition();
    }

    @Override
    public List<Variable> getVariables(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters) {
        return node.getVariables(expandComplexTypes, includeSwimlanes, typeClassNameFilters);
    }

    public CatchEventNode getNode() {
        return node;
    }
}
