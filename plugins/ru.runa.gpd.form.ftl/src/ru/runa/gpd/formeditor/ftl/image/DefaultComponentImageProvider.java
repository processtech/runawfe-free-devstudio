package ru.runa.gpd.formeditor.ftl.image;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.util.IOUtils;

public class DefaultComponentImageProvider extends DynaComponentImageProvider {

    @Override
    public byte[] getImage(ComponentType type, String[] parameters) throws IOException {
        byte[] data = loadComponentImage(type.getBundle(), "metadata/icons/" + type.getId() + ".png");
        if (data == null) {
            data = super.getImage(type, parameters);
        }
        return data;
    }

    /**
     * @return byte array or <code>null</code>
     */
    public static byte[] loadComponentImage(Bundle bundle, String imagePath) throws IOException {
        String lang = Locale.getDefault().getLanguage();
        int ldi = imagePath.lastIndexOf(".");
        if (ldi > 0) {
            String nlImagePath = imagePath.substring(0, ldi) + "." + lang + imagePath.substring(ldi);
            if (FileLocator.find(bundle, new Path(nlImagePath), new HashMap<String, String>()) != null) {
                return IOUtils.readStreamAsBytes(FileLocator.openStream(bundle, new Path(nlImagePath), false));
            }
        }
        if (FileLocator.find(bundle, new Path(imagePath), new HashMap<String, String>()) != null) {
            return IOUtils.readStreamAsBytes(FileLocator.openStream(bundle, new Path(imagePath), false));
        }
        return null;
    }

}
