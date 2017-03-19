package ru.runa.gpd.ltk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.ui.refactoring.TextStatusContextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.BotTaskLink;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.model.jpdl.Action;
import ru.runa.gpd.util.VariableMapping;

public class RenameVariableRefactoringStatusContextViewer extends TextStatusContextViewer {

    @Override
    public void setInput(RefactoringStatusContext input) {
        if (input instanceof RenameVariableRefactoringStatusContext) {
            Object element = ((VariableRenameProvider<?>) ((RenameVariableRefactoringStatusContext) input).getCorrespondingElement()).getElement();
            StringBuilder content = new StringBuilder();
            if (element instanceof FormNode) {
                if (element instanceof MultiTaskState) {
                    content.append(((MultiTaskState) element).getDiscriminatorValue());
                } else if (element instanceof TaskState) {
                    TaskState taskState = (TaskState) element;
                    if (taskState.getTimer() != null) {
                        content.append(taskState.getTimer().getDelay().getDuration());
                    } else if (taskState.getBotTaskLink() != null) {
                        content.append(((BotTaskLink) element).getDelegationConfiguration());
                    } else if (taskState.getSwimlaneBotName() != null) {
                        content.append(((BotTask) element).getDelegationConfiguration());
                    }
                } else {
                    FormNode formNode = (FormNode) element;
                    if (formNode.hasForm()) {
                        IFile formFile = ((RenameVariableRefactoringStatusContext) input).getDefinitionFolder().getFile(formNode.getFormFileName());
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(formFile.getContents()));
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                content.append(line + "\r\n");
                            }
                        } catch (Exception e) {
                            PluginLogger.logErrorWithoutDialog("Unable to read form file " + formFile.getName(), e);
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    PluginLogger.logErrorWithoutDialog("Unable to close form file " + formFile.getName(), e);
                                }
                            }
                        }
                    }
                }
            } else if (element instanceof Timer) {
                content.append(((Timer) element).getDelay().getDuration());
            } else if (element instanceof Action) {
                content.append(((Action) element).getDelegationConfiguration());
            } else if (element instanceof ScriptTask) {
                content.append(((ScriptTask) element).getDelegationConfiguration());
            } else if (element instanceof Decision) {
                content.append(((Decision) element).getDelegationConfiguration());
            } else if (element instanceof Subprocess) {
                for (VariableMapping mapping : ((Subprocess) element).getVariableMappings()) {
                    content.append(mapping.getName());
                }
            } else if (element instanceof Swimlane) {
                content.append(((Swimlane) element).getDelegationConfiguration());
            } else if (element instanceof GraphElement) {
                content.append(((GraphElement) element).getDelegationConfiguration());
            }
            if (content.length() > 0) {
                setInput(new Document(content.toString()), null);
            }
        }
    }

    @Override
    protected SourceViewer createSourceViewer(Composite parent) {
        return new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
    }
}
