package ru.runa.gpd.settings;

import java.util.Iterator;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;

public class WFEListConnectionsModel {
    private static WFEListConnectionsModel _instance = null;
    private IObservableList<ConItem> observableList;

    private WFEListConnectionsModel() {
        observableList = new WritableList<ConItem>();
    }

    public synchronized static WFEListConnectionsModel getInstance() {
        if (_instance == null)
            _instance = new WFEListConnectionsModel();
        return _instance;
    }

    public IObservableList<ConItem> getWFEConnections() {
        return observableList;
    }

    public void addWFEConnection(ConItem conItem) {
        observableList.add(conItem);
    }

    public void removeWFEConnection(ConItem conItem) {
        int i = 0;
        Iterator<ConItem> it = observableList.iterator();
        while (it.hasNext()) {
            ConItem item = it.next();
            if (item.getValue().equals(conItem.getValue())) {
                observableList.remove(i);
                break;
            }
            i++;
        }
    }

    public void updateWFEConnection(ConItem conItem) {
        int i = 0;
        Iterator<ConItem> it = observableList.iterator();
        while (it.hasNext()) {
            ConItem item = it.next();
            if (item.getValue().equals(conItem.getValue())) {
                observableList.set(i, conItem);
            }
            i++;
        }
    }

    public static class ConItem {
        private String label;
        private String value;

        public ConItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
