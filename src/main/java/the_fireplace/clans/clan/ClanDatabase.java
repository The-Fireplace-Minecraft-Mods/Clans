package the_fireplace.clans.clan;

import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.ClansModContainer;
import the_fireplace.clans.legacy.data.ClaimData;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ClanDatabase {
    private static ClanDatabase instance = null;
    public static final File clanDataLocation = new File(ClansModContainer.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/clan");

    public static ClanDatabase getInstance() {
        if(instance == null)
            load();
        return instance;
    }

    private final ConcurrentMap<UUID, Clan> clans;

    private ClanDatabase(){
        clans = new ConcurrentHashMap<>();
    }

    @Nullable
    public static Clan getClanById(@Nullable UUID clanId){
        if(clanId == null)
            return null;
        return getInstance().clans.get(clanId);
    }

    public static Collection<Clan> getClans(){
        return Collections.unmodifiableCollection(getInstance().clans.values());
    }

    static boolean addClan(UUID clanId, Clan clan){
        if(!getInstance().clans.containsKey(clanId)) {
            getInstance().clans.put(clanId, clan);
            ClanNameCache.addName(clan);
            if(clan.getBanner() != null)
                ClanBanners.cacheBanner(clan.getBanner());
            clan.markChanged();
            return true;
        }
        return false;
    }

    /**
     * For internal use only. Anyone wishing to do this, use {@link Clan#disband(MinecraftServer, ICommandSender, String, Object...)}
     */
    static boolean removeClan(UUID clanId){
        if(getInstance().clans.containsKey(clanId)) {
            Clan removed = getInstance().clans.remove(clanId);
            ClanHomes.clearClanHome(removed);
            ClanMemberCache.uncacheClan(removed);
            ClanNameCache.removeName(removed.getName());
            ClanBanners.uncacheBanner(removed.getBanner());
            ClaimData.delClan(clanId);
            removed.getClanDataFile().delete();
            return true;
        }
        return false;
    }

    /**
     * For internal use only. Anyone wishing to do this, use {@link ClanMemberCache#getClansPlayerIsIn(UUID)}
     * @param player
     * The player to get the clan of
     * @return
     * The player's clans, or an empty list if the player isn't in any
     */
    static List<Clan> lookupPlayerClans(UUID player){
        ArrayList<Clan> clans = Lists.newArrayList();
        for(Clan clan : getInstance().clans.values())
            if(clan.getMembers().containsKey(player))
                clans.add(clan);
        return Collections.unmodifiableList(clans);
    }

    private static void load() {
        instance = new ClanDatabase();
        if(!clanDataLocation.exists())
            clanDataLocation.mkdirs();
        for(File file: clanDataLocation.listFiles()) {
            try {
                Clan loadedClan = Clan.load(file);
                if(loadedClan != null)
                    instance.clans.put(loadedClan.getId(), loadedClan);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        for(Clan clan: getClans())
            clan.save();
    }
}
