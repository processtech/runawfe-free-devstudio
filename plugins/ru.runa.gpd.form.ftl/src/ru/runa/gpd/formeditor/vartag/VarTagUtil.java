package ru.runa.gpd.formeditor.vartag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VarTagUtil {
    private static Pattern patternCustomTag = Pattern.compile("<customtag(.*?)/>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static Pattern patternCustomTag2 = Pattern.compile("<customtag(.*?)></customtag>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    public static String fromHtml(String html) {
        html = html.replaceAll("&quot;", "\"");
        html = html.replaceAll("&lt;", "<");
        html = html.replaceAll("&gt;", ">");
        return html;
    }

    public static String toHtml(String xml) {
        Matcher matcher = patternCustomTag.matcher(xml);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String customtagData = matcher.group(1);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement("&lt;customtag" + customtagData + "/&gt;"));
        }
        if (buffer.length() > 0) {
            matcher.appendTail(buffer);
            xml = buffer.toString();
        }
        matcher = patternCustomTag2.matcher(xml);
        buffer = new StringBuffer();
        while (matcher.find()) {
            String customtagData = matcher.group(1);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement("&lt;customtag" + customtagData + "/&gt;"));
        }
        if (buffer.length() > 0) {
            matcher.appendTail(buffer);
            xml = buffer.toString();
        }
        return xml;
    }
}
