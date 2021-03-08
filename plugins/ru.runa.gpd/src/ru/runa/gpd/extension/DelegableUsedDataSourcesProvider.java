package ru.runa.gpd.extension;

import java.util.Set;

public interface DelegableUsedDataSourcesProvider {
    Set<String> usedDataSourceNames(String configuration);
}
