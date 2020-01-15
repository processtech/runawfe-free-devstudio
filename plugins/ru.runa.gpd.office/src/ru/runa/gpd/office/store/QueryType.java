package ru.runa.gpd.office.store;

import java.util.Arrays;
import java.util.List;

public enum QueryType {
    INSERT,
    SELECT,
    UPDATE,
    DELETE;

    public static List<QueryType> byIntent(boolean isUseExternalStorageIn, boolean isUseExternalStorageOut) {
        if (isUseExternalStorageIn) {
            return Arrays.asList(SELECT);
        } else if (isUseExternalStorageOut) {
            return Arrays.asList(INSERT, UPDATE, DELETE);
        } else {
            return Arrays.asList(values());
        }
    }
}
