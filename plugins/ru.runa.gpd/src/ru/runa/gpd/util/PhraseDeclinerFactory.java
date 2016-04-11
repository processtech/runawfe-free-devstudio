package ru.runa.gpd.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ru.runa.gpd.PluginLogger;

public class PhraseDeclinerFactory {
    protected static Map<String, Class<? extends PhraseDecliner>> decliners = new HashMap<String, Class<? extends PhraseDecliner>>();
    static {
        decliners.put("ru", PhraseDecliner_ru.class);
    }

    public static PhraseDecliner getDecliner() {
        String lang = Locale.getDefault().getLanguage();
        try {
            Class<? extends PhraseDecliner> declainerClass = decliners.get(lang);
            if (declainerClass == null) {
                return new PhraseDecliner();
            }
            return declainerClass.newInstance();
        } catch (Throwable e) {
            PluginLogger.logErrorWithoutDialog("Unable to create decliner " + lang, e);
            return new PhraseDecliner();
        }
    }
}
