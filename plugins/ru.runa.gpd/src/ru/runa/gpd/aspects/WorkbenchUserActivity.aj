package ru.runa.gpd.aspects;

import org.eclipse.core.resources.IProject;
import ru.runa.gpd.Activator;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.action.SaveAll;

public aspect WorkbenchUserActivity extends UserActivity {

    before() : execution(public void ru.runa.gpd.ApplicationWorkbenchAdvisor.postStartup()) {
        if (Activator.getPrefBoolean(PrefConstants.P_ENABLE_USER_ACTIVITY_LOGGING)) {
            startLogging();
        }
    }

    before() : execution(public void ru.runa.gpd.ApplicationWorkbenchAdvisor.postShutdown()) {
        stopLogging();
    }
    
    after(SaveAll action) returning : execution(public void run(..)) && this(action) {
        logWorkbench(UserAction.MM_SaveAll.asString());
    }
    
    after(SaveAll action) throwing(Exception e) : execution(public void run(..)) && this(action) {
        logWorkbench(UserAction.MM_SaveAll.asString(e));
    }
    
    after() returning(IProject project) : execution(private IProject ru.runa.gpd.ui.wizard.NewProcessProjectWizard.createNewProject()) {
        logWorkbench(UserAction.MM_NewProject.asString(project.getFullPath().toString()));
    }
    
    after() throwing(Exception e) : execution(private IProject ru.runa.gpd.ui.wizard.NewProcessProjectWizard.createNewProject()) {
        logWorkbench(UserAction.MM_NewProject.asString(e));
    }
    
}
