package ru.runa.gpd.algorithms.reachability;

import java.util.Arrays;

public final class TokenState {
    private final int[] counts;
    private final int hashCode;

    TokenState(int[] counts) {
        this.counts = counts;
        this.hashCode = Arrays.hashCode(counts);
    }

    public int size() {
        return counts.length;
    }

    public int getCount(int component) {
        return counts[component];
    }

    int[] getCounts() {
        return counts.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TokenState)) {
            return false;
        }
        return Arrays.equals(counts, ((TokenState) obj).counts);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < counts.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(counts[i]);
        }
        return builder.append(")").toString();
    }
}
