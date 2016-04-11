package ru.runa.gpd.extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.runa.gpd.PluginLogger;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public abstract class ArtifactRegistry<T extends Artifact> {
    private final List<T> list = Lists.newArrayList();
    private final ArtifactContentProvider<T> contentProvider;

    public ArtifactRegistry(ArtifactContentProvider<T> contentProvider) {
        this.contentProvider = contentProvider;
        try {
            load();
        } catch (Exception e) {
            PluginLogger.logError("Unable to load artifacts in " + getClass(), e);
        }
    }

    /**
     * 
     * @return <code>null</code> if load/save operations does not supported
     */
    protected abstract File getContentFile();

    public void load() throws Exception {
        list.clear();
        File file = getContentFile();
        if (file != null && file.exists()) {
            load(new FileInputStream(file));
        } else {
            loadDefaults(list);
        }
    }

    public void load(InputStream is) {
        list.clear();
        list.addAll(contentProvider.load(is));
    }

    protected void loadDefaults(List<T> list) {
    }

    public List<T> getAll() {
        List<T> sortedByLabelList = Lists.newArrayList();
        sortedByLabelList.addAll(list);
        Collections.sort(sortedByLabelList, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        return sortedByLabelList;
    }

    public T getArtifact(String name) {
        for (T artifact : list) {
            if (Objects.equal(name, artifact.getName())) {
                return artifact;
            }
        }
        return null;
    }

    public T getArtifactNotNull(String name) {
        T artifact = getArtifact(name);
        if (artifact == null) {
            throw new RuntimeException("Artifact '" + name + "' does not exist");
        }
        return artifact;
    }

    public T getArtifactByLabel(String label) {
        for (T artifact : list) {
            if (Objects.equal(label, artifact.getLabel())) {
                return artifact;
            }
        }
        return null;
    }

    public T getArtifactNotNullByLabel(String label) {
        T artifact = getArtifactByLabel(label);
        if (artifact == null) {
            throw new RuntimeException("Artifact label='" + label + "' does not exist");
        }
        return artifact;
    }

    public boolean isEnabled(String name) {
        Artifact artifact = getArtifact(name);
        return artifact == null || artifact.isEnabled();
    }

    public void add(T artifact) {
        list.add(artifact);
    }

    public void delete(T artifact) {
        list.remove(artifact);
    }

    public void save() throws Exception {
        File file = getContentFile();
        if (file != null) {
            export(new FileOutputStream(file));
        }
    }

    public void export(OutputStream os) throws Exception {
        try {
            contentProvider.save(list, os);
        } finally {
            os.close();
        }
    }
}
