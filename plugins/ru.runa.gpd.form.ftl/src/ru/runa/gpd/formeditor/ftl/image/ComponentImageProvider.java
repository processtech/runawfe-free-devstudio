package ru.runa.gpd.formeditor.ftl.image;

import java.io.IOException;

import ru.runa.gpd.formeditor.ftl.ComponentType;

public interface ComponentImageProvider {

    public byte[] getImage(ComponentType type, String[] parameters) throws IOException;

}
