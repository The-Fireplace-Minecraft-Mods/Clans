package the_fireplace.clans.clan;

import io.netty.util.internal.ConcurrentSet;

import javax.annotation.Nullable;
import java.util.Set;

public final class ClanBanners {
    private static final Set<String> clanBanners = new ConcurrentSet<>();

    public static boolean isClanBannerAvailable(String clanBanner) {
        ensureBannerCacheLoaded();
        return clanBanners.contains(clanBanner.toLowerCase());
    }

    static void cacheBanner(String banner) {
        ensureBannerCacheLoaded();
        clanBanners.add(banner.toLowerCase());
    }

    private static void ensureBannerCacheLoaded() {
        if (clanBanners.isEmpty())
            for (Clan clan : ClanDatabase.getClans())
                if (clan.getBanner() != null)
                    clanBanners.add(clan.getBanner().toLowerCase());
    }

    static void uncacheBanner(@Nullable String banner) {
        if(banner != null)
            clanBanners.remove(banner.toLowerCase());
    }
}
