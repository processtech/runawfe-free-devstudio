package ru.runa.gpd.editor.graphiti;

/* This is a marker interface. 
 * All implementors features are protected in DefaultOperationHistory.RedoList from accidental removing due to flushRedo(context);  
 * UndoContext will be added to command wrapper.
 * see ru.runa.gpd.editor.graphiti.CustomCommandStack
*/
public interface IRedoProtected {

}
