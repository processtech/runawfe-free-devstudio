package ru.runa.gpd.quick.tag;

import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.osgi.framework.Bundle;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.quick.Activator;

public class ImageTag extends FreemarkerTagGpdWrap {
    private static final long serialVersionUID = 1L;
    private String imageName;
    private static final Path imagesDirectory = Paths.get("ImageTagData");

    @Override
    protected Object executeTag() throws TemplateModelException {
        Path imageName = Paths.get(this.imageName);
        Path imagePath = imagesDirectory.resolve(imageName);
        if (!Files.exists(imagesDirectory)) {
            try {
                Files.createDirectory(imagesDirectory);
            } catch (IOException e) {
                return "Error creating directory for image for this tag";
            }
        }
        if (!Files.exists(imagePath)) { // if, чтобы не выполнять следующий код много раз для одного и того же изображения
            Bundle bundle = Activator.getDefault().getBundle();
            Image image = SharedImages.getImage(bundle, "images/" + this.imageName);
            ImageLoader imageLoader = new ImageLoader();
            imageLoader.data = new ImageData[] { image.getImageData() };
            imageLoader.save(imagePath.toString(), SWT.IMAGE_PNG);
        }
        return "<img src=\"" + imagePath.toAbsolutePath().toString() + "\" />";
    }

    public void setImageName(String path) {
        this.imageName = path;
    }

    public static void freeResources() {
        try (Stream<Path> filesInDirectory = Files.list(imagesDirectory)) {
            List<Path> filesList = filesInDirectory.collect(Collectors.toList());
            for (Path i : filesList) {
                Files.delete(i);
            }
            Files.delete(imagesDirectory);
        } catch (IOException e) {
        }
    }
}
