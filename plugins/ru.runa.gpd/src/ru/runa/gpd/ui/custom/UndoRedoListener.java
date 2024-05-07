package ru.runa.gpd.ui.custom;

import java.util.Stack;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;

public class UndoRedoListener extends AbstractUndoRedoListener implements ExtendedModifyListener {
    private final StyledText editor;
    private final UndoRedoStack<ExtendedModifyEvent> stack;
    private boolean isUndo;
    private boolean isRedo;

    /**
     * Creates a new instance of this class. Automatically starts listening to
     * corresponding key and modify events coming from the given
     * <var>editor</var>.
     * 
     * @param editor
     *            the text field to which the Undo-Redo functionality should be
     *            added
     */
    public UndoRedoListener(StyledText editor) {
        editor.addExtendedModifyListener(this);
        editor.addKeyListener(this);
        this.editor = editor;
        stack = new UndoRedoStack<ExtendedModifyEvent>();
    }


    /**
     * Creates a corresponding Undo or Redo step from the given event and pushes
     * it to the stack. The Redo stack is, logically, emptied if the event comes
     * from a normal user action.
     * 
     * @param event
     * @see org.eclipse.swt.custom.ExtendedModifyListener#modifyText(org.eclipse.
     *      swt.custom.ExtendedModifyEvent)
     */
    @Override
    public void modifyText(ExtendedModifyEvent event) {
        if (isUndo) {
            stack.pushRedo(event);
        } else { // is Redo or a normal user action
            stack.pushUndo(event);
            if (!isRedo) {
                stack.clearRedo();
                // TODO Switch to treat consecutive characters as one event?
            }
        }
    }

    @Override
    protected void undo() {
        if (stack.hasUndo()) {
            isUndo = true;
            revertEvent(stack.popUndo());
            isUndo = false;
        }
    }

    @Override
    protected void redo() {
        if (stack.hasRedo()) {
            isRedo = true;
            revertEvent(stack.popRedo());
            isRedo = false;
        }
    }

    /**
     * Reverts the given modify event, in the way as the Eclipse text editor
     * does it.
     * 
     * @param event
     */
    private void revertEvent(ExtendedModifyEvent event) {
        editor.replaceTextRange(event.start, event.length, event.replacedText);
        // (causes the modifyText() listener method to be called)
        editor.setSelectionRange(event.start, event.replacedText.length());
    }

    /**
     * Encapsulation of the Undo and Redo stack(s).
     */
    private static class UndoRedoStack<T> {
        private final Stack<T> undo;
        private final Stack<T> redo;

        public UndoRedoStack() {
            undo = new Stack<T>();
            redo = new Stack<T>();
        }

        public void pushUndo(T delta) {
            undo.add(delta);
        }

        public void pushRedo(T delta) {
            redo.add(delta);
        }

        public T popUndo() {
            T res = undo.pop();
            return res;
        }

        public T popRedo() {
            T res = redo.pop();
            return res;
        }

        public void clearRedo() {
            redo.clear();
        }

        public boolean hasUndo() {
            return !undo.isEmpty();
        }

        public boolean hasRedo() {
            return !redo.isEmpty();
        }

    }
}
