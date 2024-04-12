package ru.runa.gpd.ui.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import ru.runa.gpd.extension.SearchTextDialog;

public class FindTextListener implements KeyListener, ExtendedModifyListener {
    private final StyledText editor;

    /**
     * Creates a new instance of this class. Automatically starts listening to corresponding key and modify events coming from the given
     * <var>editor</var>.
     * 
     * @param editor
     *            the text field to which the Find functionality should be added
     */
    public FindTextListener(StyledText editor) {
        editor.addExtendedModifyListener(this);
        editor.addKeyListener(this);
        this.editor = editor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.
     * KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // Listen to CTRL+F for Find
        boolean isCtrl = (e.stateMask & SWT.CTRL) > 0;
        boolean isAlt = (e.stateMask & SWT.ALT) > 0;
        if (isCtrl && !isAlt) {
            boolean isShift = (e.stateMask & SWT.SHIFT) > 0;
            if (!isShift && e.keyCode == 'f') {
                SearchTextDialog dialog = new SearchTextDialog(editor.getShell(), editor);
                dialog.open();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events
     * .KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
        // ignore
    }

    /**
     * 
     * @param event
     * @see org.eclipse.swt.custom.ExtendedModifyListener#modifyText(org.eclipse. swt.custom.ExtendedModifyEvent)
     */
    @Override
    public void modifyText(ExtendedModifyEvent event) {
        // do nothing
    }

}
