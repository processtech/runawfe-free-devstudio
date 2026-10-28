package ru.runa.gpd.ui.enhancement;

import com.google.common.base.Strings;
import org.dom4j.Element;
import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.lang.Delegation;

/**
 * Adapter for conditional event to expose storage configuration
 * as {@link Delegable}.
 *
 * <p>Works with storage part of delegation configuration stored in
 * {@link Delegation#getConfiguration()} instead of full configuration.
 *
 * <p>Used for: {@link CatchEventNode}, {@link StartState}
 *
 * <p><b>Warning:</b> The {@link ConditionalEventModel} is cached and
 * not synchronized if changes were made concurrently
 */
public class ConditionalEventStorageDelegableAdapter<N extends Node & Delegable & StorageAware> extends AbstractConditionalDelegableAdapter<N> implements StorageAware {

    public ConditionalEventStorageDelegableAdapter(N node) {
        super(node);
    }

    public ConditionalEventStorageDelegableAdapter(N node, ConditionalEventModel model) {
        super(node, model);
    }

    @Override
    public String getDelegationConfiguration() {
        Element storage = model.getStorage();
        return storage == null ? "" : XmlUtil.toString(XmlUtil.createDocument(storage));
    }

    @Override
    public void setDelegationConfiguration(String storageXml) {
        Element storageElement = null;
        if (!Strings.isNullOrEmpty(storageXml)) {
            try {
                storageElement = XmlUtil.parseWithoutValidation(storageXml).getRootElement();
            } catch (Exception e) {
                // storageElement remains null
            }
        }

        model.setStorage(storageElement);
        node.setDelegationConfiguration(model.toXml());
    }

    @Override
    public String getDelegationType() {
        return node.getDelegationType();
    }

    @Override
    public boolean isUseExternalStorageIn() {
        return node.isUseExternalStorageIn();
    }

    @Override
    public boolean isUseExternalStorageOut() {
        return node.isUseExternalStorageOut();
    }
}
