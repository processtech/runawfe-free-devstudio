package ru.runa.gpd.formeditor.ftl.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import ru.runa.gpd.formeditor.ftl.ComponentType;

public class DynaComponentImageProvider implements ComponentImageProvider {

    protected String getLabel(ComponentType type, String[] parameters) {
        return type.getLabel().toUpperCase();
    }

    @Override
    public byte[] getImage(ComponentType type, String[] parameters) throws IOException {
        String label = getLabel(type, parameters);
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 17);

        Rectangle2D stringBounds = font.getStringBounds(label, new FontRenderContext(font.getTransform(), true, true));
        int width = (int) Math.round(stringBounds.getWidth()) + (2 * 10);

        BufferedImage image = new BufferedImage(width, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2d = image.createGraphics();
        graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        graphics2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics2d.setFont(font);
        graphics2d.setColor(Color.BLACK);
        graphics2d.drawString(label, 10, image.getHeight() / 2 + 5);
        graphics2d.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

}
