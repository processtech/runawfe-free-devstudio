package ru.runa.gpd.ui.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

public abstract class AbstractUndoRedoListener implements KeyListener {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events. KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // Listen to CTRL+Z for Undo, to CTRL+Y or CTRL+SHIFT+Z for Redo
        boolean isCtrl = (e.stateMask & SWT.CTRL) > 0;
        boolean isAlt = (e.stateMask & SWT.ALT) > 0;
        if (isCtrl && !isAlt) {
            boolean isShift = (e.stateMask & SWT.SHIFT) > 0;
            if (!isShift && e.keyCode == 'z') {
                undo();
            } else if (!isShift && e.keyCode == 'y' || isShift && e.keyCode == 'z') {
                redo();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events .KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
        // ignore
    }

    /**
     * Performs the Undo action. A new corresponding Redo step is automatically pushed to the stack.
     */
    abstract protected void undo();

    /**
     * Performs the Redo action. A new corresponding Undo step is automatically pushed to the stack.
     */
    abstract protected void redo();

}
