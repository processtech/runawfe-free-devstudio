package ru.runa.gpd.settings;

import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Activator;
import ru.runa.gpd.ui.view.PropertiesView;

public class WFEConnectionPreferenceNode extends PreferenceNode implements PrefConstants {
    public static final String ROOT_ID = "gpd.pref.connection.wfe";
    public static final String PREF_INST = "srv";
    private String name;
    public boolean cr = true;

    public WFEConnectionPreferenceNode(int inst) {
        super(genId(inst));
        StringBuilder name = new StringBuilder();
        String host = Activator.getPrefString(this.getId() + "." + P_WFE_CONNECTION_HOST);
        String port = Activator.getPrefString(this.getId() + "." + P_WFE_CONNECTION_PORT);
        if (!host.isEmpty())
            name.append(host);
        else
            name.append("[host]");

        name.append(" : ");

        if (!port.isEmpty())
            name.append(port);
        else
            name.append("[port]");

        this.name = name.toString();
    }

    public static String genId(int inst) {
        return ROOT_ID + '.' + PREF_INST + inst;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNext() {
        return Activator.getDefault().getPreferenceStore().getInt(this.getId() + "." + P_WFE_CONNECTION_NODE_NEXT);
    }

    public void setNext(int inst) {
        Activator.getDefault().getPreferenceStore().setValue(this.getId() + "." + P_WFE_CONNECTION_NODE_NEXT, inst);
    }

    public int getPrevious() {
        return Activator.getDefault().getPreferenceStore().getInt(this.getId() + "." + P_WFE_CONNECTION_NODE_PREVIOUS);
    }

    public void setPrevious(int inst) {
        Activator.getDefault().getPreferenceStore().setValue(this.getId() + "." + P_WFE_CONNECTION_NODE_PREVIOUS, inst);
    }

    @Override
    public String getLabelText() {
        return this.name;
    }

    @Override
    public void createPage() {
        setPage(new WFEConnectionPreferencePage(getId()));
    }
}
