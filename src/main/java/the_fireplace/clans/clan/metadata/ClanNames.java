package the_fireplace.clans.clan.metadata;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.clan.ClanIdRegistry;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.commands.CommandClan;
import the_fireplace.clans.legacy.commands.CommandOpClan;
import the_fireplace.clans.legacy.commands.CommandRaid;
import the_fireplace.clans.legacy.logic.ClaimMapToChat;
import the_fireplace.clans.legacy.util.TextStyles;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClanNames extends ClanData {
    private static final Map<UUID, ClanNames> NAME_INSTANCES = new ConcurrentHashMap<>();
    private static final Map<String, UUID> NAME_TO_UUID_CACHE = new ConcurrentHashMap<>();
    private static boolean cacheLoaded = false;
    public static final String NULL_CLAN_NAME = ClaimMapToChat.SECTION_SYMBOL+"knull"+ClaimMapToChat.SECTION_SYMBOL+'r';
    private static final Set<String> FORBIDDEN_CLAN_NAMES = Sets.newHashSet("wilderness", "underground", "opclan", "clan", "raid", NULL_CLAN_NAME);
    static {
        FORBIDDEN_CLAN_NAMES.addAll(CommandClan.COMMANDS.keySet());
        FORBIDDEN_CLAN_NAMES.addAll(CommandClan.COMMAND_ALIASES.keySet());
        FORBIDDEN_CLAN_NAMES.addAll(CommandOpClan.COMMANDS.keySet());
        FORBIDDEN_CLAN_NAMES.addAll(CommandOpClan.COMMAND_ALIASES.keySet());
        FORBIDDEN_CLAN_NAMES.addAll(CommandRaid.COMMANDS.keySet());
        FORBIDDEN_CLAN_NAMES.addAll(CommandRaid.aliases.keySet());
    }

    public static ClanNames get(UUID clan) {
        NAME_INSTANCES.computeIfAbsent(clan, ClanNames::new);
        return NAME_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        ClanNames name = NAME_INSTANCES.remove(clan);
        if (name != null) {
            name.delete();
        }
    }

    @Nullable
    public static UUID getClanByName(String name){
        ensureNameCacheLoaded();
        return NAME_TO_UUID_CACHE.get(cleanNameForCache(name));
    }

    public static boolean isClanNameAvailable(String clanName) {
        return !isForbiddenClanName(clanName) && !isClanNameUsed(clanName);
    }

    public static boolean isForbiddenClanName(String name) {
        return FORBIDDEN_CLAN_NAMES.contains(cleanNameForCache(name));
    }

    public static boolean isClanNameUsed(String name) {
        ensureNameCacheLoaded();
        return NAME_TO_UUID_CACHE.containsKey(cleanNameForCache(name));
    }

    private static void cacheName(String name, UUID clan) {
        NAME_TO_UUID_CACHE.put(cleanNameForCache(name), clan);
    }

    public static Collection<String> getClanNames() {
        ensureNameCacheLoaded();
        return Collections.unmodifiableCollection(NAME_TO_UUID_CACHE.keySet());
    }

    private static void ensureNameCacheLoaded() {
        if (!cacheLoaded)
            for (UUID clan : ClanIdRegistry.getIds())
                NAME_TO_UUID_CACHE.put(cleanNameForCache(get(clan).getName()), clan);
        cacheLoaded = true;
    }

    private static void uncacheName(String name) {
        NAME_TO_UUID_CACHE.remove(cleanNameForCache(name));
    }

    private static String cleanNameForCache(String name) {
        return TextStyles.stripFormatting(name.toLowerCase());
    }

    private String clanName = NULL_CLAN_NAME;

    private ClanNames(UUID clan) {
        super(clan, "name");
        loadSavedData();
    }

    public String getName() {
        return clanName;
    }

    public void setName(String clanName) {
        if (this.clanName != null)
            uncacheName(this.clanName);
        this.clanName = TextStyles.stripFormatting(clanName);
        cacheName(this.clanName, clan);
        markChanged();
    }

    @Override
    public void readFromJson(JsonReader reader) {
        clanName = reader.readString("clanName", NULL_CLAN_NAME);
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("clanName", TextStyles.stripFormatting(clanName));

        return obj;
    }
}
