package the_fireplace.clans.clan.metadata;

import com.google.gson.JsonObject;
import io.netty.util.internal.ConcurrentSet;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.clan.ClanIdRegistry;
import the_fireplace.clans.io.JsonReader;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanBanners extends ClanData {
    private static final Map<UUID, ClanBanners> BANNERS = new ConcurrentHashMap<>();
    private static final Set<String> BANNER_CACHE = new ConcurrentSet<>();
    private static boolean cacheLoaded = false;

    public static boolean hasBanner(UUID clan) {
        loadIfAbsent(clan);
        return BANNERS.containsKey(clan);
    }

    public static ClanBanners get(UUID clan) {
        return BANNERS.get(clan);
    }

    public static void set(UUID clan, String banner) {
        if(!hasBanner(clan))
            BANNERS.put(clan, new ClanBanners(clan));
        get(clan).setClanBanner(banner);
    }

    public static void delete(UUID clan) {
        ClanBanners banner = BANNERS.remove(clan);
        if(banner != null)
            banner.delete();
    }

    public static boolean isClanBannerAvailable(String clanBanner) {
        ensureBannerCacheLoaded();
        return !BANNER_CACHE.contains(clanBanner.toLowerCase());
    }

    private static void ensureBannerCacheLoaded() {
        if(!cacheLoaded)
            for(UUID clan: ClanIdRegistry.getIds())
                loadIfAbsent(clan);
        cacheLoaded = true;
    }

    private static void loadIfAbsent(UUID clan) {
        if(!cacheLoaded) {
            ClanBanners loadBanner = new ClanBanners(clan);
            if(loadBanner.hasSaveFile())
                BANNERS.putIfAbsent(clan, loadBanner);
            else
                loadBanner.delete();
        }
    }

    private static void cacheBanner(String banner) {
        BANNER_CACHE.add(banner.toLowerCase());
    }

    private static void uncacheBanner(@Nullable String banner) {
        if(banner != null)
            BANNER_CACHE.remove(banner.toLowerCase());
    }

    private String clanBanner = null;

    private ClanBanners(UUID clan) {
        super(clan, "banner");
        loadSavedData();
    }

    public String getClanBanner() {
        return clanBanner;
    }

    private void setClanBanner(String clanBanner) {
        if(this.clanBanner != null)
            uncacheBanner(this.clanBanner);
        this.clanBanner = clanBanner;
        cacheBanner(clanBanner);
        markChanged();
    }

    @Override
    public void readFromJson(JsonReader reader) {
        //noinspection ConstantConditions
        String banner = reader.readString("clanBanner", null);
        if(banner != null && !banner.isEmpty())
            setClanBanner(banner);
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("clanBanner", clanBanner);

        return obj;
    }

    @Override
    protected boolean isDefaultData() {
        return clanBanner == null || clanBanner.isEmpty();
    }
}
