package ru.runa.gpd.office;

public enum FilesSupplierMode {
    IN, OUT, BOTH, MULTI_IN_SINGLE_OUT;

    public boolean isInSupported() {
        return this == IN || this == BOTH;
    }

    public boolean isMultiInSupported() {
        return this == MULTI_IN_SINGLE_OUT;
    }

    
    public boolean isOutSupported() {
        return this == OUT || this == BOTH || this == MULTI_IN_SINGLE_OUT;
    }

}
