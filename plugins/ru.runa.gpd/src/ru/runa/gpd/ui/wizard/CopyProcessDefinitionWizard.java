package ru.runa.gpd.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.gpd.util.WorkspaceOperations;

public class CopyProcessDefinitionWizard extends Wizard implements INewWizard {
    private IStructuredSelection selection;
    private CopyProcessDefinitionWizardPage page;

    public CopyProcessDefinitionWizard() {
        setWindowTitle(Localization.getString("CopyProcessDefinitionWizard.wizard.title"));
    }

    @Override
    public void init(IWorkbench w, IStructuredSelection currentSelection) {
        this.selection = currentSelection;
    }

    @Override
    public void addPages() {
        IFolder sourceProcessFolder = (IFolder) selection.getFirstElement();
        page = new CopyProcessDefinitionWizardPage(sourceProcessFolder);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Localization.getString("CopyProcessDefinitionWizard.monitor.title"), 3);
                        monitor.worked(1);
                        IFolder targetFolder = page.getTargetProcessFolder();
                        page.getSourceProcessFolder().copy(targetFolder.getFullPath(), true, monitor);
                        IFile definitionFile = IOUtils.getProcessDefinitionFile(targetFolder);
                        monitor.worked(1);
                        ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
                        boolean hasProcessNameBeenChanged = !Objects.equals(definition.getName(), page.getProcessName());
                        if (hasProcessNameBeenChanged) {
                            definition.getVersionInfoList().clear();
                            definition.setName(page.getProcessName());
                        }
                        definition.setLanguage(page.getLanguage());
                        arrangeGraphElements(definition);
                        WorkspaceOperations.saveProcessDefinition(definition);
                        ProcessCache.newProcessDefinitionWasCreated(definitionFile);
                        WorkspaceOperations.refreshResource(targetFolder);
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (Exception e) {
            PluginLogger.logError(e);
            return false;
        }
        return true;
    }

    private void arrangeGraphElements(ProcessDefinition definition) {
        final SwimlaneDisplayMode oldMode = definition.getSwimlaneDisplayMode();
        final SwimlaneDisplayMode newMode = page.getSwimlaneDisplayMode();
        definition.setSwimlaneDisplayMode(newMode);
        if (oldMode == SwimlaneDisplayMode.none && newMode != SwimlaneDisplayMode.none) {
            for (int i = 0; i < definition.getSwimlanes().size(); i++) {
                Swimlane swimlane = definition.getSwimlanes().get(i);
                if (newMode == SwimlaneDisplayMode.horizontal) {
                    swimlane.setConstraint(new Rectangle(0, 200 * i, 1000, 200));
                } else {
                    swimlane.setConstraint(new Rectangle(200 * i, 0, 200, 1000));
                }
            }
            for (GraphElement element : definition.getElements()) {
                Rectangle rectangle = element.getConstraint();
                if (element instanceof SwimlanedNode) {
                    element.setUiParentContainer(((SwimlanedNode) element).getSwimlane());
                    if (newMode == SwimlaneDisplayMode.horizontal) {
                        rectangle.y = 50;
                    } else {
                        rectangle.x = 50;
                    }
                } else if (!(element instanceof Swimlane) && rectangle != null) {
                    if (newMode == SwimlaneDisplayMode.horizontal) {
                        rectangle.y = 100;
                    } else {
                        rectangle.x = 100;
                    }
                }
                element.setConstraint(rectangle);
            }

        } else if (oldMode != SwimlaneDisplayMode.none && newMode == SwimlaneDisplayMode.none) {
            for (Swimlane swimlane : definition.getSwimlanes()) {
                for (GraphElement element : definition.getElements()) {
                    if (swimlane.equals(element.getUiParentContainer())) {
                        element.setUiParentContainer(definition);
                        element.getConstraint().y += swimlane.getConstraint().y;
                        element.getConstraint().x += swimlane.getConstraint().x;
                    }
                }
                swimlane.setConstraint(null);
            }
        } else if (oldMode != SwimlaneDisplayMode.none && newMode != SwimlaneDisplayMode.none && newMode != oldMode) {
            for (GraphElement element : definition.getElements()) {
                if (element.getConstraint() != null) {
                    element.setConstraint(element.getConstraint().getTransposed());
                }
            }
        }

        for (GraphElement element : definition.getElements()) {
            if (element instanceof HasTextDecorator) {
                Point point = new Point(element.getConstraint().x, element.getConstraint().y - 10);
                ((HasTextDecorator) element).getTextDecoratorEmulation().setDefinitionLocation(point);
            }
        }

    }
}
