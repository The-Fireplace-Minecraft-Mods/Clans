package the_fireplace.clans.client.mapprocessing;

import org.intellij.lang.annotations.Language;
import the_fireplace.clans.legacy.logic.ClaimMapToChat;

public enum MapFragmentType {
    CACHE_SEGMENT(MapRegex.CACHE_SEGMENT_SEPARATOR +
        MapRegex.NUMBER_REGEX +
        MapRegex.CACHE_SEGMENT_SEPARATOR +
        MapRegex.NUMBER_REGEX +
        MapRegex.CACHE_SEGMENT_SEPARATOR),
    START_BORDER(ClaimMapToChat.BORDER_STRING),
    CONTENT(MapRegex.CONTENT_REGEX),
    END_BORDER(ClaimMapToChat.BORDER_STRING),
    SYMBOL_GUIDE(MapRegex.MAP_SYMBOL_REGEX+": \\S+"),
    END(MapRegex.regexEscaped(ClaimMapToChat.END_KEY_SYMBOL));

    private final String regex;

    MapFragmentType(@Language("RegExp") String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }
}
