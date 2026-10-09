package ru.runa.gpd.aichat.bpmnelements;

public class FormItem<T, V> {

    private final T name;
    private final V value;

    public FormItem(T name, V value) {
        this.name = name;
        this.value = value;
    }

    public T getName() {
        return name;
    }

    public V getValue() {
        return value;
    }
}