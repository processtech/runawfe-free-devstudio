package ru.runa.gpd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.editor.ProcessEditorBase;

public class DiagramPdfExporter {

    private static final float POINTS_PER_INCH = 72;

    public static void go(ProcessEditorBase editor, String filePath, String paperSize) throws Exception {
        File imgFile = File.createTempFile("wfe", null);
        imgFile.deleteOnExit();
        DiagramPngExporter.go(editor, imgFile.getPath());
        try (InputStream is = new FileInputStream(imgFile)) {
            PDDocument document = new PDDocument();
            PDRectangle pageRectangle = PDRectangle.A3;
            switch (paperSize) {
            case "A4":
                pageRectangle = PDRectangle.A4;
                break;
            case "A5":
                pageRectangle = PDRectangle.A5;
                break;
            case "A6":
                pageRectangle = PDRectangle.A6;
                break;
            }
            PDImageXObject img = PDImageXObject.createFromFileByContent(imgFile, document);
            PDPage page = null;
            float ratio = img.getWidth() * 1.0f / img.getHeight();
            if (ratio < 1) {
                page = new PDPage(pageRectangle);
            } else {
                page = new PDPage(new PDRectangle(pageRectangle.getHeight(), pageRectangle.getWidth()));
            }
            Point dpi = Display.getDefault().getDPI();
            pageRectangle = page.getCropBox();
            PDRectangle imgRectangle = new PDRectangle(img.getWidth() * 1.0f / dpi.x * POINTS_PER_INCH, img.getHeight() * 1.0f / dpi.y * POINTS_PER_INCH);
            if (imgRectangle.getWidth() > pageRectangle.getWidth()) {
                imgRectangle = new PDRectangle(pageRectangle.getWidth(), pageRectangle.getWidth() / ratio);
            }
            if (imgRectangle.getHeight() > pageRectangle.getHeight()) {
                imgRectangle = new PDRectangle(pageRectangle.getHeight() * ratio, pageRectangle.getHeight());
            }
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(img, pageRectangle.getLowerLeftX() + (pageRectangle.getWidth() - imgRectangle.getWidth()) / 2
                    , pageRectangle.getLowerLeftY() + (pageRectangle.getHeight() - imgRectangle.getHeight()) / 2
                    , imgRectangle.getWidth(), imgRectangle.getHeight());
            contentStream.close();
            document.save(filePath);
            document.close();
        }
    }
    
}
