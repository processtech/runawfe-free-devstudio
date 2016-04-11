package ru.runa.gpd.util;

public abstract class SafeLoopRunner {
    private final int maxIterations; 
    private int currentIteration = 0;
    
    public SafeLoopRunner(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public SafeLoopRunner() {
        this(100);
    }

    protected abstract boolean condition();
    
    protected abstract void loop();
    
    public final void runWhile() {
        while (condition() && currentIteration < maxIterations) {
            loop();
            currentIteration++;
        }
    }
}
