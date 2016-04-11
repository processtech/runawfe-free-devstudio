package ru.runa.gpd;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class SharedImages {
    private static final Map<ImageDescriptor, Image> imageMap = new HashMap<ImageDescriptor, Image>();

    public static Image getImage(ImageDescriptor imageDescriptor) {
        Image image = imageMap.get(imageDescriptor);
        if (image == null && imageDescriptor != null) {
            image = imageDescriptor.createImage();
            imageMap.put(imageDescriptor, image);
        }
        return image;
    }

    public static Image getImage(String fileName) {
        ImageDescriptor imageDescriptor = getImageDescriptor(fileName);
        return getImage(imageDescriptor);
    }

    public static Image getImage(Bundle bundle, String fileName) {
        ImageDescriptor imageDescriptor = getImageDescriptor(bundle, fileName, true);
        return getImage(imageDescriptor);
    }

    public static ImageDescriptor getImageDescriptor(String fileName, boolean test) {
        return getImageDescriptor(Activator.getDefault().getBundle(), fileName, test);
    }

    public static ImageDescriptor getImageDescriptor(Bundle bundle, String fileName, boolean test) {
        if (fileName == null) {
            return null;
        }
        URL installURL = bundle.getEntry("/");
        try {
            URL url = new URL(installURL, fileName);
            if (test) {
                try {
                    url.openConnection();
                } catch (IOException e) {
                    return null;
                }
            }
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            PluginLogger.logError(e);
            return null;
        }
    }

    public static ImageDescriptor getImageDescriptor(String fileName) {
        return getImageDescriptor(fileName, false);
    }

}
