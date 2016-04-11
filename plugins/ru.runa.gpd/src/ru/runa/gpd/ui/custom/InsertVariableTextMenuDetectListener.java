package ru.runa.gpd.ui.custom;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.util.VariableUtils;

@SuppressWarnings({ "restriction", "unused" })
public class InsertVariableTextMenuDetectListener implements MenuDetectListener {
    //    private DeleteActionHandler textDeleteAction = new DeleteActionHandler();
    //    private CutActionHandler textCutAction = new CutActionHandler();
    //    private CopyActionHandler textCopyAction = new CopyActionHandler();
    //    private PasteActionHandler textPasteAction = new PasteActionHandler();
    //    private SelectAllActionHandler textSelectAllAction = new SelectAllActionHandler();
    private final Text text;
    private final List<String> variableNames;

    // TODO add separators as in org.eclipse.ui.dialogs.FilteredTree.createFilterText(Composite)
    public InsertVariableTextMenuDetectListener(Text text, List<String> variableNames) {
        this.text = text;
        this.variableNames = variableNames;
        text.addMenuDetectListener(this);
    }

    @Override
    public void menuDetected(MenuDetectEvent e) {
        if (text.getMenu() == null) {
            MenuManager menuManager = new MenuManager();
            Menu menu = menuManager.createContextMenu(text.getShell());
            menuManager.add(new LoggingAction(Localization.getString("button.insert_variable")) {
                @Override
                protected void execute() throws Exception {
                    ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(variableNames);
                    String variableName = dialog.openDialog();
                    if (variableName != null) {
                        text.setText(VariableUtils.wrapVariableName(variableName));
                    }
                }
            });
            //            menuManager.add(textCutAction);
            //            menuManager.add(textCopyAction);
            //            menuManager.add(textPasteAction);
            //            menuManager.add(textDeleteAction);
            //            menuManager.add(textSelectAllAction);
            menuManager.addMenuListener(new IMenuListener() {
                @Override
                public void menuAboutToShow(IMenuManager manager) {
                    updateActionsEnableState();
                }
            });
            text.setMenu(menu);
        }
    }

    private class DeleteActionHandler extends Action {
        protected DeleteActionHandler() {
            super(IDEWorkbenchMessages.Delete);
            setId("TextDeleteActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_DELETE_ACTION);
        }

        @Override
        public void runWithEvent(Event event) {
            if (!text.isDisposed()) {
                Point selection = text.getSelection();
                if (selection.y == selection.x && selection.x < text.getCharCount()) {
                    text.setSelection(selection.x, selection.x + 1);
                }
                text.insert("");
                updateActionsEnableState();
                return;
            }
        }

        /**
         * Update state.
         */
        public void updateEnabledState() {
            if (!text.isDisposed()) {
                setEnabled(text.getEditable() && (text.getSelectionCount() > 0 || text.getCaretPosition() < text.getCharCount()));
                return;
            }
            setEnabled(false);
        }
    }

    private class CutActionHandler extends Action {
        protected CutActionHandler() {
            super(IDEWorkbenchMessages.Cut);
            setId("TextCutActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_CUT_ACTION);
        }

        @Override
        public void runWithEvent(Event event) {
            if (!text.isDisposed()) {
                text.cut();
                updateActionsEnableState();
                return;
            }
        }

        /**
         * Update state.
         */
        public void updateEnabledState() {
            if (!text.isDisposed()) {
                setEnabled(text.getEditable() && text.getSelectionCount() > 0);
                return;
            }
            setEnabled(false);
        }
    }

    private class CopyActionHandler extends Action {
        protected CopyActionHandler() {
            super(IDEWorkbenchMessages.Copy);
            setId("TextCopyActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_COPY_ACTION);
        }

        @Override
        public void runWithEvent(Event event) {
            if (!text.isDisposed()) {
                text.copy();
                updateActionsEnableState();
                return;
            }
        }

        /**
         * Update the state.
         */
        public void updateEnabledState() {
            if (!text.isDisposed()) {
                setEnabled(text.getSelectionCount() > 0);
                return;
            }
            setEnabled(false);
        }
    }

    private class PasteActionHandler extends Action {
        protected PasteActionHandler() {
            super(IDEWorkbenchMessages.Paste);
            setId("TextPasteActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_PASTE_ACTION);
        }

        @Override
        public void runWithEvent(Event event) {
            if (!text.isDisposed()) {
                text.paste();
                updateActionsEnableState();
                return;
            }
        }

        /**
         * Update the state
         */
        public void updateEnabledState() {
            if (!text.isDisposed()) {
                boolean canPaste = false;
                if (text.getEditable()) {
                    Clipboard clipboard = new Clipboard(text.getDisplay());
                    TransferData[] td = clipboard.getAvailableTypes();
                    for (int i = 0; i < td.length; ++i) {
                        if (TextTransfer.getInstance().isSupportedType(td[i])) {
                            canPaste = true;
                            break;
                        }
                    }
                    clipboard.dispose();
                }
                setEnabled(canPaste);
                return;
            }
            setEnabled(false);
        }
    }

    private class SelectAllActionHandler extends Action {
        protected SelectAllActionHandler() {
            super(IDEWorkbenchMessages.TextAction_selectAll);
            setId("TextSelectAllActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_SELECT_ALL_ACTION);
        }

        @Override
        public void runWithEvent(Event event) {
            if (!text.isDisposed()) {
                text.selectAll();
                updateActionsEnableState();
                return;
            }
        }

        /**
         * Update the state.
         */
        public void updateEnabledState() {
            if (!text.isDisposed()) {
                setEnabled(text.getCharCount() > 0);
                return;
            }
            setEnabled(false);
        }
    }

    private void updateActionsEnableState() {
        //        textCutAction.updateEnabledState();
        //        textCopyAction.updateEnabledState();
        //        textPasteAction.updateEnabledState();
        //        textSelectAllAction.updateEnabledState();
        //        textDeleteAction.updateEnabledState();
    }
}
