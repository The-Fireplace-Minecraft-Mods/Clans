package the_fireplace.clans.client.mapinterceptor;

import org.intellij.lang.annotations.Language;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.logic.ClaimMapToChat;

public class MapRegex {
    @Language("RegExp")
    static final String NUMBER_REGEX = "(-)?[\\d]+";
    static final String CACHE_SEGMENT_SEPARATOR = regexEscaped(ClaimMapToChat.CACHE_SEGMENT_SEPARATOR);
    static final String MAP_SYMBOL_REGEX = buildMapSymbolRegex();
    static final String CONTENT_REGEX = buildContentRegexString();

    public static String regexEscaped(String input) {
        return "\\Q"+input+"\\E";
    }

    private static String buildContentRegexString() {
        return MAP_SYMBOL_REGEX +
            "{" +
            ClaimData.CACHE_SECTION_SIZE +
            "}";
    }

    private static String buildMapSymbolRegex() {
        StringBuilder mapSymbolsBuilder = new StringBuilder();
        mapSymbolsBuilder.append(ClaimMapToChat.WILDERNESS_SYMBOL);
        for (char mapSymbol: ClaimMapToChat.MAP_CHARS)
            mapSymbolsBuilder.append(mapSymbol);
        return "[" +
            regexEscaped(mapSymbolsBuilder.toString()) +
            "]";
    }
}
