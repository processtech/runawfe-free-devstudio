package ru.runa.gpd.aichat.utils;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DocxCreator {

    public static byte[] create(String template) throws IOException {
        try (
                XWPFDocument doc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {

            String[] lines = template.split("\\R", -1);

            for (String line : lines) {
                XWPFParagraph paragraph = doc.createParagraph();

                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }

            doc.write(out);

            return out.toByteArray();
        }
    }
}