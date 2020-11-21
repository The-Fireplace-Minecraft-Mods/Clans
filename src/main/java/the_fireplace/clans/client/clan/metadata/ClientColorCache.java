package the_fireplace.clans.client.clan.metadata;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ClientColorCache {
    public static final ConcurrentMap<String, Integer> clanColors = new ConcurrentHashMap<>();

    public static void setColor(String clanName, Integer color) {
        clanColors.put(clanName, color);
    }

    public static int getColor(String clanName) {
        return clanColors.getOrDefault(clanName, 0xFF0000);
    }
}
