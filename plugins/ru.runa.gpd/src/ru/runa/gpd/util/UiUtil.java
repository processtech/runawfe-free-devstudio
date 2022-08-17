package ru.runa.gpd.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchWindow;
import ru.runa.gpd.editor.ConfigurableTitleEditorPart;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.settings.PrefConstants;

public abstract class UiUtil {

    //
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=362420#c91
    //
    public static void hideQuickAccess() {
        MWindow model = ((WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow()).getModel();
        EModelService modelService = model.getContext().get(EModelService.class);
        MUIElement element = modelService.find("SearchField", model);
        if (element != null) {
            element.setToBeRendered(false);
        }
    }

    public static void hideToolBar(IViewSite viewSite) {
        if (viewSite instanceof PartSite) {
            ((PartSite) viewSite).getModel().getToolbar().setToBeRendered(false);
        }
    }

    public static void updateEditorPartNames(boolean onlyInNonDuplicatedMode) {
        if (!PrefConstants.P_EDITOR_PART_NAME_MODE_NON_DUPLICATED.equals(CommonPreferencePage.getEditorPartNameMode())) {
            if (onlyInNonDuplicatedMode) {
                return;
            }
            IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
            for (IEditorReference editorReference : editorReferences) {
                IEditorPart editorPart = editorReference.getEditor(true);
                if (editorPart instanceof ConfigurableTitleEditorPart) {
                    ((ConfigurableTitleEditorPart) editorPart).setPartName(getPartName(((ConfigurableTitleEditorPart) editorPart).getPartNameInput()));
                }
            }
            return;
        }
        Map<ConfigurableTitleEditorPart, List<String>> editorPartNames = new HashMap<>();
        Map<String, List<ConfigurableTitleEditorPart>> nonDuplicatedPartNames = new HashMap<>();
        IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        for (IEditorReference editorReference : editorReferences) {
            IEditorPart editorPart = editorReference.getEditor(true);
            if (editorPart instanceof ConfigurableTitleEditorPart) {
                ConfigurableTitleEditorPart configurableTitleEditorPart = (ConfigurableTitleEditorPart) editorPart;
                String longPartName = getLongPartName(configurableTitleEditorPart.getPartNameInput());
                List<String> list = Lists.newArrayList(longPartName.split(String.valueOf(IPath.SEPARATOR)));
                Collections.reverse(list);
                editorPartNames.put(configurableTitleEditorPart, list);
                nonDuplicatedPartNames.compute(list.get(0), (k, v) -> {
                    if (v == null) {
                        return Lists.newArrayList(configurableTitleEditorPart);
                    } else {
                        v.add(configurableTitleEditorPart);
                        return v;
                    }
                });
            }
        }
        boolean needDuplicatesCheck = true;
        while (needDuplicatesCheck) {
            needDuplicatesCheck = false;
            for (Entry<String, List<ConfigurableTitleEditorPart>> entry : Sets.newHashSet(nonDuplicatedPartNames.entrySet())) {
                if (entry.getValue().size() == 1) {
                    continue;
                }
                for (ConfigurableTitleEditorPart editorPart : entry.getValue()) {
                    editorPartNames.computeIfPresent(editorPart, (k, v) -> {
                        if (v.size() < 2) {
                            throw new IllegalArgumentException(editorPartNames.toString());
                        }
                        String newPath = v.get(1) + IPath.SEPARATOR + v.get(0);
                        v.remove(1);
                        v.set(0, newPath);
                        return v;
                    });
                    nonDuplicatedPartNames.compute(editorPartNames.get(editorPart).get(0), (k, v) -> {
                        if (v == null) {
                            return Lists.newArrayList(editorPart);
                        } else {
                            v.add(editorPart);
                            return v;
                        }
                    });
                }
                nonDuplicatedPartNames.remove(entry.getKey());
                needDuplicatesCheck = true;
            }
        }
        for (Entry<ConfigurableTitleEditorPart, List<String>> entry : editorPartNames.entrySet()) {
            entry.getKey().setPartName(entry.getValue().get(0));
        }
    }

    public static String getPartName(Object input) {
        if (PrefConstants.P_EDITOR_PART_NAME_MODE_LONG.equals(CommonPreferencePage.getEditorPartNameMode())) {
            return getLongPartName(input);
        }
        if (input instanceof ProcessDefinition) {
            return ((ProcessDefinition) input).getName();
        } else if (input instanceof FormNode) {
            return ((FormNode) input).getName();
        } else {
            throw new IllegalArgumentException(String.valueOf(input));
        }
    }

    public static String getLongPartName(Object input) {
        ProcessDefinition processDefinition;
        if (input instanceof ProcessDefinition) {
            processDefinition = (ProcessDefinition) input;
        } else if (input instanceof FormNode) {
            processDefinition = ((FormNode) input).getProcessDefinition();
        } else {
            throw new IllegalArgumentException(String.valueOf(input));
        }
        String partName = processDefinition.getFile().getProject().getName() + IPath.SEPARATOR;
        partName += processDefinition.getFile().getProjectRelativePath().removeLastSegments(1).toPortableString();
        if (processDefinition instanceof SubprocessDefinition) {
            partName += IPath.SEPARATOR + processDefinition.getName();
        }
        if (input instanceof FormNode) {
            partName += IPath.SEPARATOR + ((FormNode) input).getName();
        }
        return partName;
    }
}
