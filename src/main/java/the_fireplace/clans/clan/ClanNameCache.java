package the_fireplace.clans.clan;

import com.google.common.collect.Sets;
import the_fireplace.clans.legacy.commands.CommandClan;
import the_fireplace.clans.legacy.commands.CommandOpClan;
import the_fireplace.clans.legacy.commands.CommandRaid;
import the_fireplace.clans.legacy.util.TextStyles;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ClanNameCache {
    private static final Map<String, Clan> clanNames = new ConcurrentHashMap<>();
    private static final Set<String> forbiddenClanNames = Sets.newHashSet("wilderness", "underground", "opclan", "clan", "raid", "null");

    static {
        forbiddenClanNames.addAll(CommandClan.commands.keySet());
        forbiddenClanNames.addAll(CommandClan.aliases.keySet());
        forbiddenClanNames.addAll(CommandOpClan.commands.keySet());
        forbiddenClanNames.addAll(CommandOpClan.aliases.keySet());
        forbiddenClanNames.addAll(CommandRaid.commands.keySet());
        forbiddenClanNames.addAll(CommandRaid.aliases.keySet());
    }

    @Nullable
    public static Clan getClanByName(String name){
        ensureNameCacheLoaded();
        return clanNames.get(cleanName(name));
    }

    public static boolean isClanNameAvailable(String clanName) {
        ensureNameCacheLoaded();
        return !isForbiddenClanName(clanName) && !isClanNameUsed(clanName);
    }

    public static boolean isForbiddenClanName(String name) {
        return forbiddenClanNames.contains(cleanName(name));
    }

    public static boolean isClanNameUsed(String name) {
        return clanNames.containsKey(cleanName(name));
    }

    public static void addName(Clan nameClan){
        ensureNameCacheLoaded();
        clanNames.put(cleanName(nameClan.getName()), nameClan);
    }

    public static Collection<String> getClanNames() {
        ensureNameCacheLoaded();
        return Collections.unmodifiableCollection(clanNames.keySet());
    }

    private static void ensureNameCacheLoaded() {
        if (clanNames.isEmpty())
            for (Clan clan : ClanDatabase.getClans())
                clanNames.put(cleanName(clan.getName()), clan);
    }

    public static void removeName(String name){
        clanNames.remove(cleanName(name));
    }

    private static String cleanName(String name) {
        return TextStyles.stripFormatting(name.toLowerCase());
    }
}
