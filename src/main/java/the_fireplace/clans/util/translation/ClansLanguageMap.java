package the_fireplace.clans.util.translation;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.IOUtils;
import the_fireplace.clans.Clans;

public class ClansLanguageMap {
    /** Pattern that matches numeric variable placeholders in a resource string, such as "%d", "%3$d", "%.2f" */
    private static final Pattern NUMERIC_VARIABLE_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    /** A Splitter that splits a string on the first "=".  For example, "a=b=c" would split into ["a", "b=c"]. */
    private static final Splitter EQUAL_SIGN_SPLITTER = Splitter.on('=').limit(2);
    private static final ClansLanguageMap instance = new ClansLanguageMap(Clans.cfg.locale);
    private final Map<String, String> languageList = Maps.newHashMap();

    ClansLanguageMap(String locale) {
        InputStream inputstream = ClansLanguageMap.class.getResourceAsStream("/assets/clans/lang/" + locale + ".lang");
        inject(this, inputstream);
    }

    public static void inject(InputStream inputstream) {
        inject(instance, inputstream);
    }

    private static void inject(ClansLanguageMap inst, InputStream inputstream) {
        Map<String, String> map = parseLangFile(inputstream);
        inst.languageList.putAll(map);
    }

    private static Map<String, String> parseLangFile(InputStream inputstream) {
        Map<String, String> table = Maps.newHashMap();
        try {
            inputstream = FMLCommonHandler.instance().loadLanguage(table, inputstream);
            if (inputstream == null) return table;

            for (String s : IOUtils.readLines(inputstream, StandardCharsets.UTF_8)) {
                if (!s.isEmpty() && s.charAt(0) != '#') {
                    String[] astring = Iterables.toArray(EQUAL_SIGN_SPLITTER.split(s), String.class);

                    if (astring != null && astring.length == 2) {
                        String s1 = astring[0];
                        String s2 = NUMERIC_VARIABLE_PATTERN.matcher(astring[1]).replaceAll("%$1s");
                        table.put(s1, s2);
                    }
                }
            }

        } catch (Exception ignored) {}
        return table;
    }

    static ClansLanguageMap getInstance() {
        return instance;
    }

    synchronized String translateKeyFormat(String key, Object... format) {
        String s = this.tryTranslateKey(key);

        try {
            return String.format(s, format);
        } catch (IllegalFormatException e) {
            return "Format error: " + s;
        }
    }

    private String tryTranslateKey(String key) {
        String s = this.languageList.get(key);
        return s == null ? key : s;
    }

    synchronized boolean isKeyTranslated(String key) {
        return this.languageList.containsKey(key);
    }
}