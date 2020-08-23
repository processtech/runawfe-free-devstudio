package ru.runa.gpd.formeditor.ftl.util;

import java.util.Scanner;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public final class CdataWrapUtils {
    private CdataWrapUtils() {

    }

    public static String wrapCdata(String s) {
        final StringBuilder sb = new StringBuilder().append("<![CDATA[");
        try (Scanner scanner = new Scanner(s)) {
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine().trim());
            }
        }
        return sb.append("]]>").toString();
    }

    public static String unwrapCdata(String s) {
        return StringUtils.isNotBlank(s)
                ? StringEscapeUtils.unescapeXml(s.replace("&amp;", "&").replace("amp;", "")).replace("<![CDATA[", "").replace("]]>", "")
                : "";
    }

}
