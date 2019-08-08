package ru.runa.gpd.aspects;

import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.action.Save;

public aspect ProcessEditorUserActivity extends UserActivity {

    after(ProcessEditorBase editor) returning : execution(public void init(..)) && this(editor) {
        startEditingSession(editor);
    }
    
    after(ProcessEditorBase editor) throwing(Exception e) : execution(public void init(..)) && this(editor) {
        startEditingSession(editor);
        log(editor.getDefinition(), UserAction.Exception.asString(e));
    }
    
    after(Save action) returning : execution(public void run(..)) && this(action) {
        ProcessEditorBase editor = (ProcessEditorBase) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(); 
        startEditingSession(editor);
        log(editor.getDefinition(), UserAction.MM_Save.asString());
    }
    
    after(Save action) throwing(Exception e) : execution(public void run(..)) && this(action) {
        ProcessEditorBase editor = (ProcessEditorBase) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(); 
        startEditingSession(editor);
        log(editor.getDefinition(), UserAction.MM_Save.asString(e));
    }
    
    after(ProcessEditorBase editor) : execution(public void dispose()) && this(editor) {
        stopEditingSession(editor.getDefinition());
    }
    
    pointcut graphElementProperyChange(String propertyName, Object oldValue, Object newValue) : 
        call(public void firePropertyChange(..)) && target(ru.runa.gpd.lang.model.GraphElement) && args(propertyName, oldValue, newValue) 
        && !cflow(call(public static ProcessDefinition ru.runa.gpd.ProcessCache.getProcessDefinition(..)));

    after(String propertyName, Object oldValue, Object newValue) : graphElementProperyChange(propertyName, oldValue, newValue) {
        if (isStarted()) {
            GraphElement graphElement = (GraphElement) thisJoinPoint.getThis();
            log(graphElement.getProcessDefinition(), UserAction.GE_ChangeProperty.asString(graphElement, graphElement.getId(), propertyName, oldValue, newValue));
        }
    }

}
