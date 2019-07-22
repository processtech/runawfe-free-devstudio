package ru.runa.gpd.settings;

import java.beans.PropertyChangeListener;

import org.eclipse.jface.preference.PreferenceNode;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;

public class WFEListConnectionsPreferenceNode extends PreferenceNode implements PrefConstants {
    public static final String ID = "gpd.pref.connection.wfe";
    private String label;

    private PropertyChangeListener conListener;

    public WFEListConnectionsPreferenceNode() {
        super(ID);
        this.label = Localization.getString("pref.connection.wfe.label");
    }

    @Override
    public String getLabelText() {
        return this.label;
    }

    public void addConListener(PropertyChangeListener l) {
        conListener = l;
    }

    public void setHead(int inst) {
        Activator.getDefault().getPreferenceStore().setValue(P_WFE_LIST_CONNECTIONS_HEAD, inst);
    }

    public int getHead() {
        return Activator.getDefault().getPreferenceStore().getInt(P_WFE_LIST_CONNECTIONS_HEAD);
    }

    public void setTale(int inst) {
        Activator.getDefault().getPreferenceStore().setValue(P_WFE_LIST_CONNECTIONS_TAIL, inst);
    }

    public int getTale() {
        return Activator.getDefault().getPreferenceStore().getInt(P_WFE_LIST_CONNECTIONS_TAIL);
    }

    @Override
    public void createPage() {
        WFEListConnectionsPreferencePage page = new WFEListConnectionsPreferencePage();
        setPage(page);
        page.addPropertyChangeListener(conListener);
    }
}