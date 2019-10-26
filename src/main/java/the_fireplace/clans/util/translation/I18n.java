package the_fireplace.clans.util.translation;

class I18n {
    private static final ClansLanguageMap localizedName = ClansLanguageMap.getInstance();
    private static final ClansLanguageMap fallbackTranslator = new ClansLanguageMap("en_us");

    static String translateToLocalFormatted(String key, Object... format) {
        return canTranslate(key) ? localizedName.translateKeyFormat(key, format) : fallbackTranslator.translateKeyFormat(key, format);
    }

    private static boolean canTranslate(String key) {
        return localizedName.isKeyTranslated(key);
    }
}