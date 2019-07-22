package ru.runa.gpd.ui.custom;

import java.beans.PropertyChangeListener;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import ru.runa.gpd.Activator;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.settings.WFEListConnectionsModel;
import ru.runa.gpd.settings.WFEListConnectionsPreferenceNode;
import ru.runa.gpd.settings.WFEListConnectionsModel.ConItem;

public class WFEConnectionsControl extends WorkbenchWindowControlContribution implements PropertyChangeListener, PrefConstants {
    private ComboViewer combo;

    public WFEConnectionsControl() {
    }

    public WFEConnectionsControl(String id) {
        super(id);
    }

    @Override
    protected Control createControl(Composite parent) {

        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode integrationNode = preferenceManager.find("gpd.pref.connection");
        IPreferenceNode connectionsPreferenceNode = integrationNode.findSubNode(WFEListConnectionsPreferenceNode.ID);
        String sCon = Activator.getDefault().getPreferenceStore().getString(P_WFE_LIST_CONNECTIONS);
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));

        combo = new ComboViewer(container, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        combo.getCombo().addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
            }
        });

        combo.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof ConItem) {
                    ConItem current = (ConItem) element;
                    return current.getLabel();
                }
                return "";
            }
        });

        combo.setContentProvider(new ObservableListContentProvider());
        combo.setInput(WFEListConnectionsModel.getInstance().getWFEConnections());

        combo.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent e) {
                IStructuredSelection selection = (IStructuredSelection) e.getSelection();
                ConItem selectItem = (ConItem) selection.getFirstElement();
                if (selectItem != null)
                    Activator.getDefault().getPreferenceStore().setValue("wfeListConnector", selectItem.getValue().toString());
            }
        });

        for (ConItem item : WFEListConnectionsModel.getInstance().getWFEConnections()) {
            if (sCon.equals(item.getValue())) {
                ISelection selection = new StructuredSelection(item);
                combo.setSelection(selection);
                break;
            }
        }

        ((WFEListConnectionsPreferenceNode) connectionsPreferenceNode).addConListener(this);

        // @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=471313
        parent.getParent().setRedraw(true);
        return container;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent event) {
        for (ConItem item : WFEListConnectionsModel.getInstance().getWFEConnections()) {
            if (event.getNewValue().equals(item.getValue())) {
                ISelection selection = new StructuredSelection(item);
                combo.setSelection(selection);
                break;
            }
        }
    }
}
