package ru.runa.gpd.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.eclipse.core.runtime.IPath;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;

public class SubprocessFinder {

    public static ProcessDefinition findSubProcessDefinition(Subprocess subprocess) {
        return findSubProcessDefinition(subprocess.getProcessDefinition(), subprocess.getSubProcessName());
    }

    public static ProcessDefinition findSubProcessDefinition(ProcessDefinition processDefinition, String subProcessName) {
        List<ProcessDefinition> candidates = new ArrayList<>(ProcessCache.getProcessDefinitions(subProcessName, false));
        candidates.sort(new SubProcessComparator(processDefinition));
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private static class SubProcessComparator implements Comparator<ProcessDefinition> {
        private final IPath parentProcessPath;

        public SubProcessComparator(ProcessDefinition processDefinition) {
            this.parentProcessPath = getPath(processDefinition);
        }

        @Override
        public int compare(ProcessDefinition o1, ProcessDefinition o2) {
            IPath path1 = getPath(o1);
            IPath path2 = getPath(o2);
            int distance1 = getDistance(path1);
            int distance2 = getDistance(path2);
            if (distance1 == distance2) {
                // alphabet sorting
                return path1.toString().compareTo(path2.toString());
            }
            return distance1 - distance2;
        }

        private IPath getPath(ProcessDefinition processDefinition) {
            // folder
            return processDefinition.getFile().getParent().getParent().getFullPath();
        }

        private int getDistance(IPath subprocessPath) {
            if (parentProcessPath.equals(subprocessPath)) {
                return 0;
            }
            if (subprocessPath.isPrefixOf(parentProcessPath)) {
                return parentProcessPath.segmentCount() - subprocessPath.segmentCount();
            } else if (parentProcessPath.isPrefixOf(subprocessPath)) {
                return subprocessPath.segmentCount() - parentProcessPath.segmentCount();
            } else {
                return subprocessPath.segmentCount() + parentProcessPath.segmentCount() - 2 * parentProcessPath.matchingFirstSegments(subprocessPath);
            }
        }
    }

}
