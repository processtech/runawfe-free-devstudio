package ru.runa.gpd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import ru.runa.gpd.editor.ProcessEditorBase;

public class DiagramPdfExporter {

    public static void go(ProcessEditorBase editor, String filePath) throws Exception {
        File imgFile = File.createTempFile("wfe", null);
        imgFile.deleteOnExit();
        DiagramPngExporter.go(editor, imgFile.getPath());
        try (InputStream is = new FileInputStream(imgFile)) {
            PDDocument document = new PDDocument();
            PDImageXObject img = PDImageXObject.createFromFileByContent(imgFile, document);
            PDPage page = null;
            if (img.getWidth() < img.getHeight()) {
                page = new PDPage(PDRectangle.A3);
            } else {
                page = new PDPage(new PDRectangle(PDRectangle.A3.getHeight(), PDRectangle.A3.getWidth()));
            }
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            PDRectangle pdr = page.getMediaBox();
            contentStream.drawImage(img, pdr.getLowerLeftX(), pdr.getLowerLeftY(), pdr.getWidth(), pdr.getHeight());
            contentStream.close();
            document.save(filePath);
            document.close();
        }
    }
    
}
